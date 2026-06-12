// BookAttractionItemPlanner 是景点模块的预订入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.attraction.api

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.order.domain.{OrderIdPlannerRequest, OrderPlannerResponse}
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql
import com.typesafe.travel.shared.kernel.UserId
import com.typesafe.travel.traveler.domain.{TravelerPlannerPlainSql, TravelerProfile, TravelerProfileStatus}
import io.circe.Json

import java.sql.{Connection, Date}
import java.time.{Instant, LocalDate, Period}
import java.util.UUID
import scala.util.Try

object BookAttractionItemPlanner extends ConnectionApiPlan[BookAttractionItemPlannerRequest, BookAttractionItemPlannerResponse]:
  override val name: String = "BookAttractionItemPlanner"

  override def plan(input: BookAttractionItemPlannerRequest, connection: Connection): IO[BookAttractionItemPlannerResponse] =
    for
      useDate <- IO.fromEither(parseUseDate(input.useDate))
      order <- OrderPlannerPlainSql.get(connection, OrderIdPlannerRequest(input.orderId))
      attraction <- AttractionPlannerPlainSql.details(connection, GetAttractionDetailsPlannerRequest(input.attractionId, Some(input.useDate)))
      ticketType <- IO.fromOption(attraction.ticketTypes.find(_.ticketTypeId.value == input.ticketTypeId))(
        new IllegalArgumentException(s"Ticket type '${input.ticketTypeId}' was not found")
      )
      _ <- IO.fromEither(validateBookingContext(order, ticketType, useDate, input.userId))
      _ <- IO.fromEither(validateTicketType(ticketType, useDate))
      _ <- IO.fromEither(validateSession(ticketType, input.sessionId, useDate))
      travelers <- TravelerPlannerPlainSql.listByOwner(connection, UserId(input.userId))
      selectedTravelers <- IO.fromEither(selectTravelers(travelers, input.travelerIds))
      _ <- IO.fromEither(validateTravelers(selectedTravelers, ticketType, useDate))
      response <- IO.blocking {
        val orderItemId = s"order-item-${UUID.randomUUID().toString.take(12)}"
        val selectedTravelerIds = selectedTravelers.map(_.travelerId.value)
        val unitPrice = ticketType.unitPrice
        val totalPrice = unitPrice.amount * BigDecimal(selectedTravelerIds.size.max(1))
        val session = resolveSession(ticketType, input.sessionId)
        val sortIndex = nextOrderItemSortIndex(connection, input.orderId)
        val snapshotJson = buildSnapshotJson(
          attraction = attraction,
          ticketType = ticketType,
          session = session,
          useDate = useDate,
          travelerIds = selectedTravelerIds,
          unitAmount = unitPrice.amount,
          totalAmount = totalPrice,
          currency = unitPrice.currency.toString
        )

        insertOrderItem(
          connection = connection,
          orderItemId = orderItemId,
          orderId = input.orderId,
          attractionId = attraction.attractionId.value,
          ticketTypeId = ticketType.ticketTypeId.value,
          sessionId = session.map(_.sessionId.value),
          useDate = useDate,
          travelerIds = selectedTravelerIds,
          unitAmount = unitPrice.amount,
          unitCurrency = unitPrice.currency.toString,
          bookedAmount = totalPrice,
          bookedCurrency = unitPrice.currency.toString,
          snapshotJson = snapshotJson,
          sortIndex = sortIndex
        )
        refreshOrderSummary(connection, input.orderId)
        BookAttractionItemPlannerResponse(input.orderId, orderItemId)
      }
    yield response

  private def parseUseDate(value: String): Either[Throwable, LocalDate] =
    Try(LocalDate.parse(value.trim)).toEither.left.map(_ => new IllegalArgumentException("Attraction ticket requires a valid visit date"))

  private def validateBookingContext(order: OrderPlannerResponse, ticketType: TicketType, useDate: LocalDate, userId: String): Either[Throwable, Unit] =
    val normalizedStatus = order.status.trim
    if order.buyerUserId.trim.isEmpty then Left(new IllegalArgumentException("Order buyer is missing"))
    else if order.buyerUserId.trim != userId.trim then Left(new IllegalArgumentException(s"Order '${order.orderId}' does not belong to user '$userId'"))
    else if order.orderCurrency.trim.isEmpty then Left(new IllegalArgumentException(s"Order '${order.orderId}' is missing a currency"))
    else if !Set("Draft", "PendingSelection", "PendingPayment").contains(normalizedStatus) then
      Left(new IllegalArgumentException(s"Order '${order.orderId}' cannot accept attraction items while in status ${order.status}"))
    else if !order.orderCurrency.trim.equalsIgnoreCase(ticketType.unitPrice.currency.toString) then
      Left(new IllegalArgumentException(s"Order '${order.orderId}' expects currency ${order.orderCurrency} but ticket type '${ticketType.ticketTypeId.value}' uses ${ticketType.unitPrice.currency}"))
    else if !ticketTypeSupportsUseDate(ticketType, useDate) then
      Left(new IllegalArgumentException(s"Ticket type '${ticketType.ticketTypeId.value}' is not available on '$useDate'"))
    else Right(())

  private def validateTicketType(ticketType: TicketType, useDate: LocalDate): Either[Throwable, Unit] =
    if !ticketTypeIsActive(ticketType) then Left(new IllegalArgumentException(s"Ticket type '${ticketType.ticketTypeId.value}' is inactive"))
    else if ticketType.ticketTypeName.trim.isEmpty then Left(new IllegalArgumentException(s"Ticket type '${ticketType.ticketTypeId.value}' has no display name"))
    else if ticketType.unitPrice.amount <= BigDecimal(0) then Left(new IllegalArgumentException(s"Ticket type '${ticketType.ticketTypeId.value}' must have a positive price"))
    else if !ticketTypeSupportsUseDate(ticketType, useDate) then Left(new IllegalArgumentException(s"Ticket type '${ticketType.ticketTypeId.value}' is not available on '$useDate'"))
    else Right(())

  private def resolveSession(ticketType: TicketType, sessionId: Option[String]): Option[AttractionTicketSession] =
    sessionId.flatMap { rawSessionId =>
      val normalizedSessionId = rawSessionId.trim
      if normalizedSessionId.isEmpty then None
      else ticketType.sessions.find(_.sessionId.value == normalizedSessionId)
    }

  private def validateSession(ticketType: TicketType, sessionId: Option[String], useDate: LocalDate): Either[Throwable, Unit] =
    if ticketType.sessions.nonEmpty && sessionId.forall(_.trim.isEmpty) then
      Left(new IllegalArgumentException(s"Ticket type '${ticketType.ticketTypeId.value}' requires a session"))
    else
      sessionId match
        case None => Right(())
        case Some(rawSessionId) =>
          val normalizedSessionId = rawSessionId.trim
          ticketType.sessions.find(_.sessionId.value == normalizedSessionId) match
            case None => Left(new IllegalArgumentException(s"Session '$rawSessionId' was not found"))
            case Some(resolvedSession) =>
              if !attractionTicketSessionIsActive(resolvedSession) then
                Left(new IllegalArgumentException(s"Session '$rawSessionId' is inactive"))
              else if resolvedSession.useDate != useDate then
                Left(new IllegalArgumentException(s"Session '$rawSessionId' does not match use date '$useDate'"))
              else if resolvedSession.ticketTypeId != ticketType.ticketTypeId then
                Left(new IllegalArgumentException(s"Session '$rawSessionId' does not belong to ticket type '${ticketType.ticketTypeId.value}'"))
              else Right(())

  private def selectTravelers(travelers: List[TravelerProfile], requestedTravelerIds: List[String]): Either[Throwable, Vector[TravelerProfile]] =
    val normalizedRequestedTravelerIds = requestedTravelerIds.map(_.trim).filter(_.nonEmpty).distinct
    val activeTravelers = travelers.filter(_.travelerProfileStatus != TravelerProfileStatus.Archived)
    if normalizedRequestedTravelerIds.nonEmpty then
      val selectedTravelers = activeTravelers.filter(traveler => normalizedRequestedTravelerIds.contains(traveler.travelerId.value))
      if selectedTravelers.size != normalizedRequestedTravelerIds.size then
        Left(new IllegalArgumentException("One or more selected travelers were not found"))
      else Right(selectedTravelers.toVector)
    else
      val fallbackTravelers = {
        val defaultTravelers = activeTravelers.filter(_.isDefaultTravelerProfile)
        if defaultTravelers.nonEmpty then defaultTravelers else activeTravelers
      }
      if fallbackTravelers.isEmpty then Left(new IllegalArgumentException("At least one traveler is required for attraction booking"))
      else Right(fallbackTravelers.toVector)

  private def validateTravelers(travelers: Vector[TravelerProfile], ticketType: TicketType, useDate: LocalDate): Either[Throwable, Unit] =
    val failures = travelers.flatMap { traveler =>
      validateTravelerEligibility(traveler, ticketType, useDate).left.toOption.map(message => s"${traveler.travelerFullName.value}: $message")
    }
    if failures.nonEmpty then Left(new IllegalArgumentException(s"traveler_not_eligible: ${failures.mkString(" / ")}"))
    else Right(())

  private def validateTravelerEligibility(traveler: TravelerProfile, ticketType: TicketType, useDate: LocalDate): Either[String, Unit] =
    val ageInYears = Period.between(traveler.travelerBirthDate.value, useDate).getYears
    val normalizedDocumentType = traveler.travelerDocumentType.toString.trim.toLowerCase
    val normalizedDocumentNumber = traveler.travelerDocumentNumber.value.trim.toLowerCase

    ticketType.eligibilityRules.foldLeft[Either[String, Unit]](Right(())) { (acc, rule) =>
      acc.flatMap { _ =>
        parseTicketEligibilityRuleConfig(rule).left.map(_.message).flatMap {
          case TicketEligibilityRuleConfig.AgeLessThan(maxExclusive) =>
            if ageInYears < maxExclusive then Right(()) else Left(s"age must be below $maxExclusive")
          case TicketEligibilityRuleConfig.AgeBetween(minInclusive, maxInclusive) =>
            if ageInYears >= minInclusive && ageInYears <= maxInclusive then Right(()) else Left(s"age must be between $minInclusive and $maxInclusive")
          case TicketEligibilityRuleConfig.AgeAtLeast(minInclusive) =>
            if ageInYears >= minInclusive then Right(()) else Left(s"age must be at least $minInclusive")
          case TicketEligibilityRuleConfig.DocumentTypeEquals(documentType) =>
            if normalizedDocumentType == documentType.toString.trim.toLowerCase then Right(()) else Left(s"document type must be ${documentType.toString}")
          case TicketEligibilityRuleConfig.DocumentNumberPrefix(prefix) =>
            if normalizedDocumentNumber.startsWith(prefix.trim.toLowerCase) then Right(()) else Left(s"document number must start with $prefix")
        }
      }
    }

  private def nextOrderItemSortIndex(connection: Connection, orderId: String): Int =
    PlainSqlSupport.withStatement(connection, "select coalesce(max(sort_index), 0) + 1 from order_line_items where order_id = ?") { statement =>
      statement.setString(1, orderId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getInt(1) else 1
      finally resultSet.close()
    }

  private def insertOrderItem(
      connection: Connection,
      orderItemId: String,
      orderId: String,
      attractionId: String,
      ticketTypeId: String,
      sessionId: Option[String],
      useDate: LocalDate,
      travelerIds: Vector[String],
      unitAmount: BigDecimal,
      unitCurrency: String,
      bookedAmount: BigDecimal,
      bookedCurrency: String,
      snapshotJson: String,
      sortIndex: Int
  ): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into order_line_items(
          order_item_id, order_id, item_kind, item_status,
          attraction_id, ticket_type_id, use_date,
          traveler_ids_json, unit_amount, unit_currency,
          supplier_review_status, booked_amount, booked_currency, snapshot_json, sort_index
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, orderItemId)
      statement.setString(2, orderId)
      statement.setString(3, "Attraction")
      statement.setString(4, "Active")
      statement.setString(5, attractionId)
      statement.setString(6, ticketTypeId)
      statement.setDate(7, Date.valueOf(useDate))
      statement.setString(8, Json.fromValues(travelerIds.map(Json.fromString)).noSpaces)
      statement.setBigDecimal(9, unitAmount.bigDecimal)
      statement.setString(10, unitCurrency)
      statement.setString(11, "NotSubmitted")
      statement.setBigDecimal(12, bookedAmount.bigDecimal)
      statement.setString(13, bookedCurrency)
      statement.setString(14, snapshotJson)
      statement.setInt(15, sortIndex)
      statement.executeUpdate()
    }

  private def refreshOrderSummary(connection: Connection, orderId: String): Unit =
    val orderType = determineOrderType(connection, orderId)
    val totals = PlainSqlSupport.withStatement(
      connection,
      "select coalesce(sum(booked_amount), 0) as total_amount from order_line_items where order_id = ?"
    ) { statement =>
      statement.setString(1, orderId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then BigDecimal(resultSet.getBigDecimal("total_amount")) else BigDecimal(0)
      finally resultSet.close()
    }
    PlainSqlSupport.withStatement(
      connection,
      "update orders set order_type = ?, total_price_amount = ?, remaining_refundable_amount = ? where order_id = ?"
    ) { statement =>
      statement.setString(1, orderType)
      statement.setBigDecimal(2, totals.bigDecimal)
      statement.setBigDecimal(3, totals.bigDecimal)
      statement.setString(4, orderId)
      statement.executeUpdate()
    }

  private def determineOrderType(connection: Connection, orderId: String): String =
    val itemKinds = PlainSqlSupport.withStatement(connection, "select distinct item_kind from order_line_items where order_id = ? order by item_kind") { statement =>
      statement.setString(1, orderId)
      PlainSqlSupport.queryList(statement)(resultSet => resultSet.getString("item_kind")).toVector
    }
    itemKinds.toList match
      case Nil                 => "PendingSelection"
      case "Attraction" :: Nil => "AttractionBooking"
      case _                   => "MixedBooking"

  private def buildSnapshotJson(
      attraction: Attraction,
      ticketType: TicketType,
      session: Option[AttractionTicketSession],
      useDate: LocalDate,
      travelerIds: Vector[String],
      unitAmount: BigDecimal,
      totalAmount: BigDecimal,
      currency: String
  ): String =
    Json
      .obj(
        "attractionId" -> Json.fromString(attraction.attractionId.value),
        "managerId" -> Json.fromString(attraction.managerId.value),
        "attractionName" -> Json.fromString(attraction.attractionName),
        "ticketTypeId" -> Json.fromString(ticketType.ticketTypeId.value),
        "ticketTypeName" -> Json.fromString(ticketType.ticketTypeName),
        "sessionId" -> session.map(value => Json.fromString(value.sessionId.value)).getOrElse(Json.Null),
        "sessionName" -> session.map(value => Json.fromString(value.sessionName)).getOrElse(Json.Null),
        "sessionStartsAt" -> session.map(value => Json.fromString(value.startsAt.toString)).getOrElse(Json.Null),
        "sessionEndsAt" -> session.map(value => Json.fromString(value.endsAt.toString)).getOrElse(Json.Null),
        "useDate" -> Json.fromString(useDate.toString),
        "travelerIds" -> Json.fromValues(travelerIds.map(Json.fromString)),
        "unitPriceAmount" -> Json.fromBigDecimal(unitAmount),
        "unitPriceCurrency" -> Json.fromString(currency),
        "totalPriceAmount" -> Json.fromBigDecimal(totalAmount),
        "totalPriceCurrency" -> Json.fromString(currency),
        "ruleSummaries" -> Json.fromValues(ticketType.eligibilityRules.map(rule => Json.fromString(ticketEligibilityRuleHumanReadableSummary(rule)))),
        "eligibilityValidatedAt" -> Json.fromString(Instant.now().toString)
      )
      .noSpaces
