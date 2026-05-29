package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.auth.domain.{AuthManagerType, CredentialStatus, hashPasswordForLoginEmail}
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.EmailAddress
import io.circe.parser.decode

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Instant, LocalDate, OffsetDateTime}
import java.util.UUID

object ManagerPlannerPlainSql:
  private final case class ManagerFlightOrderRow(
      orderId: String,
      orderItemId: String,
      buyerUserId: String,
      buyerNickname: String,
      cabinClass: String,
      orderStatus: String,
      orderCreatedAt: String,
      travelerIds: List[String]
  )

  def registerAirline(connection: Connection, input: RegisterAirlineManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val airlineId = s"airline-${UUID.randomUUID().toString.take(12)}"
      val managerId = s"manager-${UUID.randomUUID().toString.take(12)}"
      insertAirline(connection, airlineId, input.airlineName, input.airlineCode, now)
      insertManager(connection, "airline_managers", managerId, Some(airlineId), input.email, input.displayName, now)
      insertManagerCredential(connection, "Airline", managerId, input.email, passwordHash, now)
      ManagerSessionPlannerResponse(managerId, "Airline", input.email, input.displayName, "Active", airlineId, None, now.toString)
    }

  def registerHotel(connection: Connection, input: RegisterHotelManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val hotelId = s"hotel-${UUID.randomUUID().toString.take(12)}"
      val managerId = s"manager-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into hotels(hotel_id, name, location, status, created_at) values (?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, hotelId)
        statement.setString(2, input.hotelName)
        statement.setString(3, input.location)
        statement.setString(4, "Open")
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
      insertManager(connection, "hotel_managers", managerId, Some(hotelId), input.email, input.displayName, now)
      insertManagerCredential(connection, "Hotel", managerId, input.email, passwordHash, now)
      ManagerSessionPlannerResponse(managerId, "Hotel", input.email, input.displayName, "Active", hotelId, None, now.toString)
    }

  def updateHotelProfile(connection: Connection, input: UpdateHotelManagerProfilePlannerRequest, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val hotelId = findScopeId(connection, "hotel_managers", "hotel_id", input.managerId)
      PlainSqlSupport.withStatement(connection, "update hotel_managers set email = ?, display_name = ? where manager_id = ?") { statement =>
        statement.setString(1, input.email.trim)
        statement.setString(2, input.displayName.trim)
        statement.setString(3, input.managerId)
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(connection, "update hotels set name = ?, location = ? where hotel_id = ?") { statement =>
        statement.setString(1, input.hotelName.trim)
        statement.setString(2, input.hotelLocation.trim)
        statement.setString(3, hotelId)
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(connection, "update manager_credentials set login_email = ?, updated_at = ? where manager_type = ? and manager_id = ?") { statement =>
        statement.setString(1, input.email.trim)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setString(3, "Hotel")
        statement.setString(4, input.managerId)
        statement.executeUpdate()
      }
      readHotelManagerSession(connection, input.managerId, now)
    }

  def registerAttraction(connection: Connection, input: RegisterAttractionManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val managerId = s"attraction-manager-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into attraction_managers(manager_id, email, display_name, status, created_at) values (?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, managerId)
        statement.setString(2, input.email.trim)
        statement.setString(3, input.displayName.trim)
        statement.setString(4, "Active")
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
      insertManagerCredential(connection, "Attraction", managerId, input.email.trim, passwordHash, now)
      ManagerSessionPlannerResponse(managerId, "Attraction", input.email.trim, input.displayName.trim, "Active", managerId, None, now.toString)
    }

  def registerSiteAdmin(connection: Connection, input: RegisterSiteAdminPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val managerId = s"site-admin-${UUID.randomUUID().toString.take(12)}"
      insertManagerCredential(connection, "SiteAdmin", managerId, input.email, passwordHash, now)
      ManagerSessionPlannerResponse(managerId, "SiteAdmin", input.email, input.displayName, "Active", "site-admin", None, now.toString)
    }

  def listTasks(connection: Connection, input: ManagerTasksPlannerRequest): IO[ManagerBookingTaskListPlannerResponse] =
    IO.blocking {
      val kindFilter = itemKindFor(input.managerType)
      PlainSqlSupport.withStatement(
        connection,
        """
          select o.order_id, o.buyer_user_id, o.created_at, li.order_item_id, li.item_kind, li.item_status,
                 li.supplier_review_status, li.review_decision, li.review_reason, li.reviewed_at, li.reviewed_by_manager_id,
                 li.booked_amount, li.booked_currency, li.snapshot_json
          from order_line_items li
          join orders o on o.order_id = li.order_id
          where lower(li.item_kind) = lower(?)
          order by o.created_at desc, li.order_item_id
        """
      ) { statement =>
        statement.setString(1, kindFilter)
        ManagerBookingTaskListPlannerResponse(PlainSqlSupport.queryList(statement)(readTask))
      }
    }

  def updateSupplierReviewDecision(
      connection: Connection,
      managerId: String,
      orderItemId: String,
      supplierReviewStatus: String,
      reviewDecision: String,
      reason: Option[String],
      now: Instant
  ): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update order_line_items set supplier_review_status = ?, review_decision = ?, review_reason = ?, reviewed_at = ?, reviewed_by_manager_id = ? where order_item_id = ?") { statement =>
        statement.setString(1, supplierReviewStatus)
        statement.setString(2, reviewDecision)
        statement.setString(3, reason.orNull)
        statement.setTimestamp(4, Timestamp.from(now))
        statement.setString(5, managerId)
        statement.setString(6, orderItemId)
        statement.executeUpdate()
      }
    }

  def listFlights(connection: Connection, input: ManagerFlightsPlannerRequest): IO[ManagerFlightListPlannerResponse] =
    IO.blocking {
      val filters = scala.collection.mutable.ListBuffer.empty[String]
      val parameters = scala.collection.mutable.ListBuffer[AnyRef](input.managerId)

      input.departureAirports.map(_.filter(_.trim.nonEmpty).map(_.trim.toUpperCase)).filter(_.nonEmpty).foreach { airports =>
        filters += s"f.departure_airport in (${airports.map(_ => "?").mkString(", ")})"
        parameters ++= airports
      }

      input.arrivalAirports.map(_.filter(_.trim.nonEmpty).map(_.trim.toUpperCase)).filter(_.nonEmpty).foreach { airports =>
        filters += s"f.arrival_airport in (${airports.map(_ => "?").mkString(", ")})"
        parameters ++= airports
      }

      input.departureDate.map(_.trim).filter(_.nonEmpty).foreach { departureDate =>
        filters += "cast(f.departure_time as date) = cast(? as date)"
        parameters += departureDate
      }

      input.timeRange.map(_.trim).filter(value => value.nonEmpty && value != "all").foreach { timeRange =>
        val parts = timeRange.split("-").toList
        if parts.size == 2 then
          filters += "cast(f.departure_time as time) between cast(? as time) and cast(? as time)"
          parameters += parts.head
          parameters += parts(1)
      }

      val extraWhere = if filters.isEmpty then "" else filters.mkString(" and ", " and ", "")
      val sortDirection = input.sortDirection.map(_.trim.toLowerCase).filter(_ == "desc").map(_ => "desc").getOrElse("asc")
      PlainSqlSupport.withStatement(
        connection,
        s"""
          select f.flight_id, f.airline_id, a.name as airline_name, a.code as airline_code, f.flight_number,
                 f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.status,
                 f.base_price_amount, f.base_price_currency, f.created_at,
                 ci.inventory_id, ci.cabin_class, ci.available_seats, ci.unit_price_amount,
                 ci.unit_price_currency, ci.status as cabin_inventory_status
          from airline_managers m
          join airlines a on a.airline_id = m.airline_id
          join flights f on f.airline_id = a.airline_id
          left join flight_cabin_inventories ci on ci.flight_id = f.flight_id
          where m.manager_id = ?
          $extraWhere
          order by
            f.departure_time $sortDirection,
            f.flight_id,
            case upper(ci.cabin_class)
              when 'ECONOMY' then 1
              when 'PREMIUM_ECONOMY' then 2
              when 'BUSINESS' then 3
              when 'FIRST' then 4
              else 5
            end,
            ci.inventory_id
          limit 800
        """
      ) { statement =>
        parameters.zipWithIndex.foreach { case (parameter, index) =>
          statement.setObject(index + 1, parameter)
        }
        val rows = PlainSqlSupport.queryList(statement)(readFlightWithOptionalCabin)
        val orderedFlightIds = rows.map(_.flight.flightId).distinct
        val flightsById = rows.groupMap(_.flight.flightId)(_.flight).view.mapValues(_.head).toMap
        val inventoriesByFlightId = rows.groupMap(_.flight.flightId)(_.cabinInventory).view.mapValues(_.flatten).toMap
        ManagerFlightListPlannerResponse(
          orderedFlightIds.flatMap(flightId => flightsById.get(flightId).map(_.copy(cabinInventories = inventoriesByFlightId.getOrElse(flightId, Nil))))
        )
      }
    }

  def listFlightOrders(connection: Connection, input: ManagerFlightOrdersPlannerRequest): IO[ManagerFlightOrderListPlannerResponse] =
    IO.blocking {
      requireManagedFlight(connection, input.managerId, input.flightId)
      val rows = PlainSqlSupport.withStatement(
        connection,
        """
          select o.order_id, o.buyer_user_id, u.nickname as buyer_nickname, o.status as order_status, o.created_at,
                 li.order_item_id, li.cabin_class, li.traveler_ids_json, li.snapshot_json
          from order_line_items li
          join orders o on o.order_id = li.order_id
          join users u on u.user_id = o.buyer_user_id
          where lower(li.item_kind) = lower(?)
            and (li.flight_id = ? or li.snapshot_json::jsonb ->> 'flightId' = ?)
          order by o.created_at desc, li.order_item_id
        """
      ) { statement =>
        statement.setString(1, "Flight")
        statement.setString(2, input.flightId)
        statement.setString(3, input.flightId)
        PlainSqlSupport.queryList(statement)(readManagerFlightOrderRow)
      }
      val travelerIds = rows.flatMap(_.travelerIds).distinct
      val travelersById = listTravelersByIds(connection, travelerIds).map(traveler => traveler.travelerId -> traveler).toMap
      ManagerFlightOrderListPlannerResponse(
        rows.map(row =>
          ManagerFlightOrderPlannerResponse(
            orderId = row.orderId,
            orderItemId = row.orderItemId,
            buyerUserId = row.buyerUserId,
            buyerNickname = row.buyerNickname,
            cabinClass = row.cabinClass,
            orderStatus = row.orderStatus,
            orderCreatedAt = row.orderCreatedAt,
            travelerIds = row.travelerIds,
            travelers = row.travelerIds.flatMap(travelersById.get)
          )
        )
      )
    }

  def listHotels(connection: Connection, input: ManagerScopedPlannerRequest): IO[ManagerHotelListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select h.hotel_id, h.name, h.location, h.status, h.created_at
          from hotel_managers m
          join hotels h on h.hotel_id = m.hotel_id
          where m.manager_id = ?
        """
      ) { statement =>
        statement.setString(1, input.managerId)
        ManagerHotelListPlannerResponse(
          PlainSqlSupport.queryList(statement) { resultSet =>
            readHotel(connection, resultSet.getString("hotel_id"))
          }
        )
      }
    }

  def listRefundTasks(connection: Connection, input: ManagerScopedPlannerRequest): IO[ManagerRefundTaskListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select o.order_id, o.buyer_user_id, r.refund_id, r.refund_reason, r.refund_amount, r.refund_currency, r.created_at
          from order_refunds r
          join orders o on o.order_id = r.order_id
          where r.refund_status = ?
          order by r.created_at desc, r.refund_id
        """
      ) { statement =>
        statement.setString(1, "Requested")
        ManagerRefundTaskListPlannerResponse(PlainSqlSupport.queryList(statement) { resultSet =>
          ManagerRefundTaskPlannerResponse(
            orderId = resultSet.getString("order_id"),
            buyerUserId = resultSet.getString("buyer_user_id"),
            taskType = normalizeManagerType(input.managerType),
            summaryLabel = s"Refund ${resultSet.getString("refund_id")}",
            refundId = resultSet.getString("refund_id"),
            refundReason = resultSet.getString("refund_reason"),
            refundAmount = resultSet.getBigDecimal("refund_amount").toString,
            refundCurrency = resultSet.getString("refund_currency"),
            requestedAt = resultSet.getTimestamp("created_at").toInstant.toString
          )
        })
      }
    }

  def updateAirlineProfile(connection: Connection, input: UpdateAirlineManagerProfilePlannerRequest, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val airlineId = findScopeId(connection, "airline_managers", "airline_id", input.managerId)
      PlainSqlSupport.withStatement(connection, "update airline_managers set display_name = ? where manager_id = ?") { statement =>
        statement.setString(1, input.displayName.trim)
        statement.setString(2, input.managerId)
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(connection, "update airlines set name = ?, code = ?, logo_asset_path = ? where airline_id = ?") { statement =>
        statement.setString(1, input.airlineName.trim)
        statement.setString(2, input.airlineCode.trim)
        statement.setString(3, input.logoAssetPath.map(_.trim).filter(_.nonEmpty).orNull)
        statement.setString(4, airlineId)
        statement.executeUpdate()
      }
      readAirlineManagerSession(connection, input.managerId, now)
    }

  def updateRequestedRefundDecision(connection: Connection, orderId: String, refundStatus: String, approved: Boolean, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update order_refunds set refund_status = ?, approved_at = ?, settled_at = ? where order_id = ? and refund_status = ?") { statement =>
        statement.setString(1, refundStatus)
        statement.setTimestamp(2, if approved then Timestamp.from(now) else null)
        statement.setTimestamp(3, if approved then Timestamp.from(now) else null)
        statement.setString(4, orderId)
        statement.setString(5, "Requested")
        statement.executeUpdate()
      }
    }

  private def insertAirline(connection: Connection, airlineId: String, name: String, code: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(connection, "insert into airlines(airline_id, name, code, status, created_at) values (?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, airlineId)
      statement.setString(2, name)
      statement.setString(3, code)
      statement.setString(4, "Active")
      statement.setTimestamp(5, Timestamp.from(now))
      statement.executeUpdate()
    }

  private def insertManager(connection: Connection, table: String, managerId: String, scopeId: Option[String], email: String, displayName: String, now: Instant): Unit =
    val scopeColumn = if table == "hotel_managers" then "hotel_id" else "airline_id"
    PlainSqlSupport.withStatement(connection, s"insert into $table(manager_id, $scopeColumn, email, display_name, status, created_at) values (?, ?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, managerId)
      statement.setString(2, scopeId.orNull)
      statement.setString(3, email)
      statement.setString(4, displayName)
      statement.setString(5, "Active")
      statement.setTimestamp(6, Timestamp.from(now))
      statement.executeUpdate()
    }

  private def insertManagerCredential(connection: Connection, managerType: String, managerId: String, email: String, passwordHash: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(connection, "insert into manager_credentials(credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, s"credential-${UUID.randomUUID().toString.take(12)}")
      statement.setString(2, managerType)
      statement.setString(3, managerId)
      statement.setString(4, email)
      statement.setString(5, passwordHash)
      statement.setString(6, CredentialStatus.Active.toString)
      statement.setTimestamp(7, Timestamp.from(now))
      statement.setTimestamp(8, Timestamp.from(now))
      statement.setTimestamp(9, Timestamp.from(now))
      statement.executeUpdate()
    }

  private def findScopeId(connection: Connection, table: String, column: String, managerId: String): String =
    PlainSqlSupport.withStatement(connection, s"select $column from $table where manager_id = ?") { statement =>
      statement.setString(1, managerId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString(column) else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
      finally resultSet.close()
    }

  private def readAirlineManagerSession(connection: Connection, managerId: String, fallbackCreatedAt: Instant): ManagerSessionPlannerResponse =
    PlainSqlSupport.withStatement(
      connection,
      """
        select m.manager_id, m.email, m.display_name, m.status, m.airline_id, a.logo_asset_path, m.created_at
        from airline_managers m
        join airlines a on a.airline_id = m.airline_id
        where m.manager_id = ?
      """
    ) { statement =>
      statement.setString(1, managerId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          ManagerSessionPlannerResponse(
            managerId = resultSet.getString("manager_id"),
            managerType = "Airline",
            email = resultSet.getString("email"),
            displayName = resultSet.getString("display_name"),
            status = resultSet.getString("status"),
            scopeId = resultSet.getString("airline_id"),
            logoAssetPath = Option(resultSet.getString("logo_asset_path")).map(_.trim).filter(_.nonEmpty),
            createdAt = Option(resultSet.getTimestamp("created_at")).map(_.toInstant.toString).getOrElse(fallbackCreatedAt.toString)
          )
        else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
        finally resultSet.close()
    }

  private def readHotelManagerSession(connection: Connection, managerId: String, fallbackCreatedAt: Instant): ManagerSessionPlannerResponse =
    PlainSqlSupport.withStatement(
      connection,
      """
        select m.manager_id, m.email, m.display_name, m.status, m.hotel_id, m.created_at
        from hotel_managers m
        where m.manager_id = ?
      """
    ) { statement =>
      statement.setString(1, managerId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          ManagerSessionPlannerResponse(
            managerId = resultSet.getString("manager_id"),
            managerType = "Hotel",
            email = resultSet.getString("email"),
            displayName = resultSet.getString("display_name"),
            status = resultSet.getString("status"),
            scopeId = resultSet.getString("hotel_id"),
            logoAssetPath = None,
            createdAt = Option(resultSet.getTimestamp("created_at")).map(_.toInstant).getOrElse(fallbackCreatedAt).toString
          )
        else
          throw new IllegalStateException(s"Hotel manager '$managerId' could not be read")
      finally resultSet.close()
    }

  private def readTask(resultSet: ResultSet): ManagerBookingTaskPlannerResponse =
    ManagerBookingTaskPlannerResponse(
      orderId = resultSet.getString("order_id"),
      orderItemId = resultSet.getString("order_item_id"),
      buyerUserId = resultSet.getString("buyer_user_id"),
      taskType = normalizeManagerType(resultSet.getString("item_kind")),
      supplierReviewStatus = resultSet.getString("supplier_review_status"),
      summaryLabel = Option(resultSet.getString("snapshot_json")).getOrElse(resultSet.getString("item_kind")),
      detailLabel = Option(resultSet.getString("snapshot_json")).getOrElse(""),
      requestedAt = resultSet.getTimestamp("created_at").toInstant.toString,
      reviewDecision = Option(resultSet.getString("review_decision")).map(decision =>
        SupplierReviewDecisionPlannerResponse(decision, Option(resultSet.getString("review_reason")), Option(resultSet.getTimestamp("reviewed_at")).map(_.toInstant.toString), Option(resultSet.getString("reviewed_by_manager_id")))
      ),
      reviewedBy = Option(resultSet.getString("reviewed_by_manager_id")),
      reviewedAt = Option(resultSet.getTimestamp("reviewed_at")).map(_.toInstant.toString),
      reviewNote = Option(resultSet.getString("review_reason"))
    )

  private def readFlightWithoutCabins(resultSet: ResultSet): ManagerFlightPlannerResponse =
    ManagerFlightPlannerResponse(
      flightId = resultSet.getString("flight_id"),
      airlineId = resultSet.getString("airline_id"),
      airlineName = resultSet.getString("airline_name"),
      airlineCode = resultSet.getString("airline_code"),
      flightNumber = resultSet.getString("flight_number"),
      departureAirport = resultSet.getString("departure_airport"),
      arrivalAirport = resultSet.getString("arrival_airport"),
      departureTime = resultSet.getObject("departure_time", classOf[OffsetDateTime]).toString,
      arrivalTime = resultSet.getObject("arrival_time", classOf[OffsetDateTime]).toString,
      status = resultSet.getString("status"),
      basePrice = resultSet.getBigDecimal("base_price_amount").toString,
      currency = resultSet.getString("base_price_currency"),
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      cabinInventories = Nil
    )

  private final case class ManagerFlightCabinRow(flight: ManagerFlightPlannerResponse, cabinInventory: Option[ManagerCabinInventoryPlannerResponse])

  private def readFlightWithOptionalCabin(resultSet: ResultSet): ManagerFlightCabinRow =
    val inventoryId = resultSet.getString("inventory_id")
    ManagerFlightCabinRow(
      flight = readFlightWithoutCabins(resultSet),
      cabinInventory =
        Option(inventoryId).map { id =>
          val status = resultSet.getString("cabin_inventory_status")
          val availableSeats = resultSet.getInt("available_seats")
          ManagerCabinInventoryPlannerResponse(
            inventoryId = id,
            cabinClass = resultSet.getString("cabin_class"),
            availableSeats = availableSeats,
            unitPrice = resultSet.getBigDecimal("unit_price_amount").toString,
            currency = resultSet.getString("unit_price_currency"),
            status = status,
            isBookable = status == "Open" && availableSeats > 0
          )
        }
    )

  private def readManagerFlightOrderRow(resultSet: ResultSet): ManagerFlightOrderRow =
    val snapshotJson = Option(resultSet.getString("snapshot_json"))
    val typedTravelerIds = Option(resultSet.getString("traveler_ids_json")).flatMap(parseStringListJson).getOrElse(Nil)
    val snapshotTravelerIds = snapshotJson.flatMap(json => extractStringListField(json, "travelerIds")).getOrElse(Nil)
    ManagerFlightOrderRow(
      orderId = resultSet.getString("order_id"),
      orderItemId = resultSet.getString("order_item_id"),
      buyerUserId = resultSet.getString("buyer_user_id"),
      buyerNickname = resultSet.getString("buyer_nickname"),
      cabinClass = Option(resultSet.getString("cabin_class"))
        .filter(_.trim.nonEmpty)
        .orElse(snapshotJson.flatMap(json => extractStringField(json, "cabinClass")))
        .getOrElse("ECONOMY"),
      orderStatus = resultSet.getString("order_status"),
      orderCreatedAt = resultSet.getTimestamp("created_at").toInstant.toString,
      travelerIds = if typedTravelerIds.nonEmpty then typedTravelerIds else snapshotTravelerIds
    )

  private def listTravelersByIds(connection: Connection, travelerIds: List[String]): List[ManagerFlightOrderTravelerPlannerResponse] =
    if travelerIds.isEmpty then Nil
    else
      PlainSqlSupport.withStatement(
        connection,
        s"""
          select traveler_id, full_name, birth_date, document_type, document_number, phone,
                 gender, nationality, document_expiry_date, email, preferences_json,
                 quiet_seat_preferred, assistance_type, special_requirement_note,
                 has_large_luggage, luggage_note
          from traveler_profiles
          where traveler_id in (${travelerIds.map(_ => "?").mkString(", ")})
          order by full_name, traveler_id
        """
      ) { statement =>
        travelerIds.zipWithIndex.foreach { case (travelerId, index) => statement.setString(index + 1, travelerId) }
        PlainSqlSupport.queryList(statement) { resultSet =>
          val fullName = resultSet.getString("full_name")
          val birthDate = resultSet.getDate("birth_date").toLocalDate
          val documentType = resultSet.getString("document_type")
          val documentNumber = resultSet.getString("document_number")
          val phone = resultSet.getString("phone")
          val preferences = com.typesafe.travel.persistence.codecs.DatabaseCodecs
            .decodeTravelerPreferences(resultSet.getString("preferences_json"))
            .fold(throw _, identity)
          val basicInfo = ManagerTravelerBasicInfo(
            fullName = fullName,
            gender = readOptionalString(resultSet, "gender").getOrElse("unspecified"),
            birthDate = birthDate.toString,
            nationality = readOptionalString(resultSet, "nationality").getOrElse("China")
          )
          val documentInfo = ManagerTravelerDocumentInfo(
            documentType = documentType,
            documentNumber = documentNumber,
            documentExpiryDate = Option(resultSet.getDate("document_expiry_date")).map(_.toLocalDate.toString)
          )
          val contactInfo = ManagerTravelerContactInfo(
            phone = phone,
            email = readOptionalString(resultSet, "email")
          )
          val preferenceInfo = ManagerTravelerPreferenceInfo(
            seatPreference = preferences.travelerSeatPreference.toString,
            mealPreference = preferences.travelerMealPreference.toString,
            quietSeatPreferred = resultSet.getBoolean("quiet_seat_preferred")
          )
          val specialRequirementInfo = ManagerTravelerSpecialRequirementInfo(
            assistanceType = readOptionalString(resultSet, "assistance_type").getOrElse("none"),
            requirementNote = readOptionalString(resultSet, "special_requirement_note").orElse(preferences.accessibilityRequestNotes),
            hasLargeLuggage = resultSet.getBoolean("has_large_luggage"),
            luggageNote = readOptionalString(resultSet, "luggage_note")
          )
          val requirementLabel =
            List(
              Option.when(specialRequirementInfo.assistanceType != "none")(specialRequirementInfo.assistanceType),
              specialRequirementInfo.requirementNote,
              Option.when(specialRequirementInfo.hasLargeLuggage)("largeLuggage"),
              specialRequirementInfo.luggageNote
            ).flatten.mkString(" / ")
          val serviceSummary = ManagerTravelerServiceSummary(
            age = Some(java.time.Period.between(birthDate, LocalDate.now()).getYears),
            documentLabel = s"$documentType $documentNumber",
            contactLabel = List(Some(phone), contactInfo.email).flatten.mkString(" / "),
            preferenceLabel = List(preferenceInfo.seatPreference, preferenceInfo.mealPreference, Option.when(preferenceInfo.quietSeatPreferred)("quietSeat").getOrElse("")).filter(_.nonEmpty).mkString(" / "),
            requirementLabel = if requirementLabel.nonEmpty then requirementLabel else "none",
            warningLevel = if specialRequirementInfo.assistanceType != "none" || specialRequirementInfo.requirementNote.exists(_.trim.nonEmpty) then "attention" else "normal"
          )
          ManagerFlightOrderTravelerPlannerResponse(
            travelerId = resultSet.getString("traveler_id"),
            fullName = fullName,
            documentNumber = documentNumber,
            basicInfo = basicInfo,
            documentInfo = documentInfo,
            contactInfo = contactInfo,
            preferenceInfo = preferenceInfo,
            specialRequirementInfo = specialRequirementInfo,
            serviceSummary = serviceSummary
          )
        }
      }

  private def readOptionalString(resultSet: ResultSet, columnName: String): Option[String] =
    Option(resultSet.getString(columnName)).map(_.trim).filter(_.nonEmpty)

  private def requireManagedFlight(connection: Connection, managerId: String, flightId: String): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        select 1
        from airline_managers m
        join flights f on f.airline_id = m.airline_id
        where m.manager_id = ? and f.flight_id = ?
        limit 1
      """
    ) { statement =>
      statement.setString(1, managerId)
      statement.setString(2, flightId)
      val resultSet = statement.executeQuery()
      try
        if !resultSet.next() then throw new IllegalArgumentException(s"Flight '$flightId' does not belong to manager '$managerId'")
      finally resultSet.close()
    }

  private def readHotel(connection: Connection, hotelId: String): ManagerHotelPlannerResponse =
    PlainSqlSupport.withStatement(
      connection,
      "select hotel_id, name, location, status, created_at from hotels where hotel_id = ?"
    ) { statement =>
      statement.setString(1, hotelId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          ManagerHotelPlannerResponse(
            hotelId = resultSet.getString("hotel_id"),
            hotelName = resultSet.getString("name"),
            location = resultSet.getString("location"),
            status = resultSet.getString("status"),
            createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
            roomTypes = readHotelRoomTypes(connection, hotelId)
          )
        else throw new IllegalStateException(s"Hotel '$hotelId' could not be read")
      finally resultSet.close()
    }

  private def readHotelRoomTypes(connection: Connection, hotelId: String): List[ManagerHotelRoomTypePlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select room_type_id, name, capacity, bed_type, base_price_amount, base_price_currency, status
        from hotel_room_types
        where hotel_id = ?
        order by room_type_id
      """
    ) { statement =>
      statement.setString(1, hotelId)
      PlainSqlSupport.queryList(statement) { resultSet =>
        ManagerHotelRoomTypePlannerResponse(
          roomTypeId = resultSet.getString("room_type_id"),
          roomTypeName = resultSet.getString("name"),
          capacity = resultSet.getInt("capacity"),
          bedType = resultSet.getString("bed_type"),
          basePrice = resultSet.getBigDecimal("base_price_amount").toString,
          currency = resultSet.getString("base_price_currency"),
          status = resultSet.getString("status"),
          isBookableForRequestedStay = resultSet.getString("status") == "OpenForBooking",
          availableRoomsForRequestedStay = None
        )
      }
    }

  private def readCreatedFlight(connection: Connection, flightId: String): ManagerFlightPlannerResponse =
    PlainSqlSupport.withStatement(connection, "select f.flight_id, f.airline_id, a.name as airline_name, a.code as airline_code, f.flight_number, f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.status, f.base_price_amount, f.base_price_currency, f.created_at from flights f join airlines a on a.airline_id = f.airline_id where f.flight_id = ?") { statement =>
      statement.setString(1, flightId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          readFlightWithoutCabins(resultSet).copy(cabinInventories = listCabinInventories(connection, flightId))
        else throw new IllegalStateException("Inserted flight could not be read")
      finally resultSet.close()
    }

  private def listCabinInventories(connection: Connection, flightId: String): List[ManagerCabinInventoryPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select inventory_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status
        from flight_cabin_inventories
        where flight_id = ?
        order by
          case upper(cabin_class)
            when 'ECONOMY' then 1
            when 'PREMIUM_ECONOMY' then 2
            when 'BUSINESS' then 3
            when 'FIRST' then 4
            else 5
          end,
          inventory_id
      """
    ) { statement =>
      statement.setString(1, flightId)
      PlainSqlSupport.queryList(statement)(readCabinInventory)
    }

  private def readCabinInventory(resultSet: ResultSet): ManagerCabinInventoryPlannerResponse =
    val status = resultSet.getString("status")
    val availableSeats = resultSet.getInt("available_seats")
    ManagerCabinInventoryPlannerResponse(
      inventoryId = resultSet.getString("inventory_id"),
      cabinClass = resultSet.getString("cabin_class"),
      availableSeats = availableSeats,
      unitPrice = resultSet.getBigDecimal("unit_price_amount").toString,
      currency = resultSet.getString("unit_price_currency"),
      status = status,
      isBookable = status == "Open" && availableSeats > 0
    )

  private def readHotelById(connection: Connection, hotelId: String): ManagerHotelPlannerResponse =
    readHotel(connection, hotelId)

  private def itemKindFor(managerType: String): String =
    managerType.trim.toLowerCase match
      case "hotel" => "hotel"
      case "attraction" => "attraction"
      case _ => "flight"

  private def normalizeManagerType(value: String): String =
    value.trim.toLowerCase match
      case "hotel" => "Hotel"
      case "attraction" => "Attraction"
      case "train" => "Train"
      case "siteadmin" | "site-admin" => "SiteAdmin"
      case _ => "Airline"

  private def parseStringListJson(jsonText: String): Option[List[String]] =
    decode[List[String]](jsonText).toOption.map(_.filter(_.trim.nonEmpty))

  private def extractStringField(snapshotJson: String, fieldName: String): Option[String] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[String](fieldName).toOption).filter(_.trim.nonEmpty)

  private def extractStringListField(snapshotJson: String, fieldName: String): Option[List[String]] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[List[String]](fieldName).toOption).map(_.filter(_.trim.nonEmpty))
