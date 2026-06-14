package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.attraction.api.BookAttractionItemPlanner
import com.typesafe.travel.attraction.domain.BookAttractionItemPlannerRequest
import com.typesafe.travel.flight.api.BookFlightPlanner
import com.typesafe.travel.flight.objects.BookFlightPlannerRequest
import com.typesafe.travel.hotel.api.BookHotelPlanner
import com.typesafe.travel.hotel.objects.BookHotelPlannerRequest
import com.typesafe.travel.order.domain.CreateOrderPlannerRequest
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.BookTrainItemPlanner
import com.typesafe.travel.train.domain.BookTrainItemPlannerRequest
import io.circe.syntax.*

import java.sql.Connection
import java.time.Instant

import TourGroupPlannerPlainSqlSupport.*

object TourGroupPlannerPlainSqlSelection:
  def createPlanItem(connection: Connection, input: CreateTourGroupPlanItemPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      TourGroupPlannerPlainSqlSupport.ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val planItem = TourGroupPlannerPlainSqlSupport.createGroupPlanItem(
        GroupPlanItemId(TourGroupPlannerPlainSqlSupport.nextId("plan-item")),
        tourGroup.groupId,
        GroupPlanItemType.fromText(input.itemType),
        input.title,
        input.description,
        Instant.parse(input.scheduledAt),
        input.endsAt.map(Instant.parse),
        input.sequenceNo
      ).fold(throw _, identity)
      PlainSqlSupport.withStatement(
        connection,
        "insert into group_plan_items(plan_item_id, group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, planItem.planItemId.value)
        statement.setString(2, planItem.groupId.value)
        statement.setString(3, planItem.itemType.toString)
        statement.setString(4, planItem.title)
        statement.setString(5, planItem.description)
        statement.setTimestamp(6, java.sql.Timestamp.from(planItem.scheduledAt))
        statement.setTimestamp(7, planItem.endsAt.map(java.sql.Timestamp.from).orNull)
        statement.setInt(8, planItem.sequenceNo)
        statement.setString(9, planItem.status.toString)
        statement.executeUpdate()
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def createPlanOption(connection: Connection, input: CreateTourGroupPlanOptionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      TourGroupPlannerPlainSqlSupport.ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val planItem = TourGroupPlannerPlainSqlSupport.planItemById(connection, input.planItemId)
      if planItem.groupId.value != input.groupId then
        throw TourGroupError.PlanItemWasNotFound(planItem.planItemId)
      val resourceType = GroupPlanOptionResourceType.fromText(input.resourceType)
      val compatible = planItem.itemType match
        case GroupPlanItemType.Flight     => resourceType == GroupPlanOptionResourceType.Flight
        case GroupPlanItemType.Hotel      => resourceType == GroupPlanOptionResourceType.HotelRoomType
        case GroupPlanItemType.Train      => resourceType == GroupPlanOptionResourceType.TrainJourneySeat
        case GroupPlanItemType.Attraction => resourceType == GroupPlanOptionResourceType.AttractionTicketType
      if !compatible then
        throw TourGroupError.PlanOptionResourceTypeDidNotMatchPlanItem(planItem.planItemId, planItem.itemType, resourceType)
      val planOption = TourGroupPlannerPlainSqlSupport.createGroupPlanOption(
        GroupPlanOptionId(TourGroupPlannerPlainSqlSupport.nextId("plan-option")),
        planItem.planItemId,
        resourceType,
        input.resourceId,
        input.resourceVariantCode,
        input.resourceContext,
        input.label,
        input.description,
        input.defaultQuantity
      ).fold(throw _, identity)
      PlainSqlSupport.withStatement(
        connection,
        "insert into group_plan_options(option_id, plan_item_id, resource_type, resource_id, resource_variant_code, resource_context, label, description, default_quantity, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, planOption.optionId.value)
        statement.setString(2, planOption.planItemId.value)
        statement.setString(3, planOption.resourceType.toString)
        statement.setString(4, planOption.resourceId)
        statement.setString(5, planOption.resourceVariantCode.orNull)
        statement.setString(6, planOption.resourceContext.orNull)
        statement.setString(7, planOption.label)
        statement.setString(8, planOption.description)
        statement.setInt(9, planOption.defaultQuantity)
        statement.setString(10, planOption.status.toString)
        statement.executeUpdate()
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def createSelection(connection: Connection, input: CreateTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val activeMembershipRow = TourGroupPlannerPlainSqlSupport.activeMembership(connection, input.groupId, input.userId)
      val membershipTravelerIds =
        TourGroupPlannerPlainSqlSupport.membershipTravelers(connection, input.groupId)
          .collect {
            case row if row.membershipId == activeMembershipRow.membershipId.value && row.status == "Active" =>
              TravelerId(row.travelerId)
          }
          .toSet
      val planItem = TourGroupPlannerPlainSqlSupport.planItems(connection, input.groupId).find(_.planItemId.value == input.planItemId).getOrElse {
        throw TourGroupError.PlanItemWasNotFound(GroupPlanItemId(input.planItemId))
      }
      val planOption = TourGroupPlannerPlainSqlSupport.planOptions(connection, input.groupId).find(_.optionId.value == input.optionId).getOrElse {
        throw TourGroupError.PlanOptionWasNotFound(GroupPlanOptionId(input.optionId))
      }
      if planOption.planItemId != planItem.planItemId then
        throw TourGroupError.PlanOptionDidNotBelongToPlanItem(planOption.optionId, planItem.planItemId)
      val requestedTravelerIds = input.travelerIds.map(TravelerId.apply).distinct.toVector
      if requestedTravelerIds.isEmpty then
        throw TourGroupError.SelectionTravelerWasEmpty(planItem.planItemId)
      if requestedTravelerIds.size != input.quantity then
        throw TourGroupError.SelectionQuantityDidNotMatchTravelerCount(GroupPlanSelectionId("pending"), input.quantity, requestedTravelerIds.size)
      requestedTravelerIds.foreach { travelerId =>
        if !membershipTravelerIds.contains(travelerId) then
          throw TourGroupError.SelectionTravelerWasNotInMembership(GroupPlanSelectionId("pending"), travelerId)
      }
      val selection = TourGroupPlannerPlainSqlSupport.createGroupPlanSelection(
        GroupPlanSelectionId(TourGroupPlannerPlainSqlSupport.nextId("selection")),
        TourGroupId(input.groupId),
        planItem.planItemId,
        planOption.optionId,
        activeMembershipRow.membershipId,
        input.quantity,
        requestedTravelerIds,
        now
      ).fold(throw _, identity)
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into group_plan_selections(
            selection_id, group_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, selection.selectionId.value)
        statement.setString(2, selection.groupId.value)
        statement.setString(3, selection.planItemId.value)
        statement.setString(4, selection.optionId.value)
        statement.setString(5, selection.membershipId.value)
        statement.setInt(6, selection.quantity)
        statement.setString(7, selection.status.toString)
        statement.setTimestamp(8, java.sql.Timestamp.from(selection.createdAt))
        statement.setTimestamp(9, null)
        statement.setString(10, null)
        statement.setString(11, null)
        statement.executeUpdate()
      }
      requestedTravelerIds.zipWithIndex.foreach { case (travelerId, index) =>
        PlainSqlSupport.withStatement(
          connection,
          """
            insert into group_plan_selection_travelers(selection_traveler_id, selection_id, traveler_id)
            values (?, ?, ?)
          """
        ) { statement =>
          statement.setString(1, TourGroupPlannerPlainSqlSupport.nextId(s"selection-traveler-${index + 1}"))
          statement.setString(2, selection.selectionId.value)
          statement.setString(3, travelerId.value)
          statement.executeUpdate()
        }
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def submitSelection(connection: Connection, input: SubmitTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val selection = TourGroupPlannerPlainSqlSupport.selectionById(connection, input.selectionId)
      val activeMembershipRow = TourGroupPlannerPlainSqlSupport.activeMembership(connection, selection.groupId.value, input.userId)
      if selection.membershipId != activeMembershipRow.membershipId then
        throw TourGroupError.MembershipScopeDidNotMatch(activeMembershipRow.membershipId, UserId(input.userId))
      val acceptedSelection = submitGroupPlanSelection(selection, now).fold(throw _, identity)
      val planItem = TourGroupPlannerPlainSqlSupport.planItems(connection, selection.groupId.value).find(_.planItemId == selection.planItemId).getOrElse {
        throw TourGroupError.PlanItemWasNotFound(selection.planItemId)
      }
      val planOption = TourGroupPlannerPlainSqlSupport.planOptions(connection, selection.groupId.value).find(_.optionId == selection.optionId).getOrElse {
        throw TourGroupError.PlanOptionWasNotFound(selection.optionId)
      }
      existingSelectionOrderId(connection, acceptedSelection.selectionId.value).flatMap {
        case Some(_) => IO.blocking(TourGroupPlannerPlainSqlSupport.details(connection, acceptedSelection.groupId.value))
        case None =>
          createOrderForSelection(connection, input.userId, acceptedSelection, planItem, planOption, now).flatMap { orderId =>
            IO.blocking {
              insertSelectionOrderLink(connection, acceptedSelection.selectionId.value, orderId, now)
              updateSelectionAsConvertedToOrder(connection, acceptedSelection.selectionId.value, now)
              TourGroupPlannerPlainSqlSupport.details(connection, acceptedSelection.groupId.value)
            }
          }
      }
    }

  private def createOrderForSelection(
      connection: Connection,
      userId: String,
      selection: GroupPlanSelection,
      planItem: GroupPlanItem,
      planOption: GroupPlanOption,
      now: Instant
  ): IO[String] =
    val travelerIds = selection.travelerIds.map(_.value).toList
    planOption.resourceType match
      case GroupPlanOptionResourceType.Flight =>
        val (departureAirport, arrivalAirport, departureDate) = parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Flight selection is missing resource context")))
        val cabinClass = planOption.resourceVariantCode.getOrElse(throw new IllegalArgumentException("Flight selection is missing cabin class"))
        BookFlightPlanner
          .plan(
            BookFlightPlannerRequest(
              userId = userId,
              flightId = planOption.resourceId,
              travelerIds = travelerIds,
              cabinClass = cabinClass
            ),
            connection
          )
          .map(_.orderId)
      case GroupPlanOptionResourceType.HotelRoomType =>
        val (checkInDate, checkOutDate) = parseStayContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Hotel selection is missing stay context")))
        BookHotelPlanner
          .plan(
            BookHotelPlannerRequest(
              userId = userId,
              roomTypeId = planOption.resourceId,
              guestTravelerIds = travelerIds,
              checkInDate = checkInDate,
              checkOutDate = checkOutDate,
              roomCount = selection.quantity
            ),
            connection
          )
          .map(_.orderId)
      case GroupPlanOptionResourceType.TrainJourneySeat =>
        val (fromStationCode, toStationCode, date) = parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Train selection is missing route context")))
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookTrainItemPlanner.plan(
            BookTrainItemPlannerRequest(
              userId = userId,
              orderId = order.orderId,
              trainId = planOption.resourceId,
              travelerIds = travelerIds,
              fromStationCode = fromStationCode,
              toStationCode = toStationCode,
              seatClass = planOption.resourceVariantCode.getOrElse(throw new IllegalArgumentException("Train selection is missing seat class")),
              seatPreference = None
            ),
            connection
          )
        yield order.orderId
      case GroupPlanOptionResourceType.AttractionTicketType =>
        val useDate = planItem.scheduledAt.toString.take(10)
        val attractionId = planOption.resourceContext.map(_.trim).filter(_.nonEmpty).getOrElse {
          throw new IllegalArgumentException("Attraction selection is missing attraction context")
        }
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookAttractionItemPlanner.plan(
            BookAttractionItemPlannerRequest(
              userId = userId,
              orderId = order.orderId,
              attractionId = attractionId,
              ticketTypeId = planOption.resourceId,
              sessionId = planOption.resourceVariantCode,
              travelerIds = travelerIds,
              useDate = useDate
            ),
            connection
          )
        yield order.orderId

  private def parseTripContext(resourceContext: String): (String, String, String) =
    resourceContext.split('|').map(_.trim) match
      case Array(left, right, date) => (left, right, normalizeDateOnly(date))
      case _ => throw new IllegalArgumentException(s"Invalid tour-group resource context: '$resourceContext'")

  private def parseStayContext(resourceContext: String): (String, String) =
    resourceContext.split('|').map(_.trim) match
      case Array(checkInDate, checkOutDate) => (normalizeDateOnly(checkInDate), normalizeDateOnly(checkOutDate))
      case _ => throw new IllegalArgumentException(s"Invalid tour-group hotel context: '$resourceContext'")

  private def normalizeDateOnly(value: String): String =
    value.trim.take(10)
