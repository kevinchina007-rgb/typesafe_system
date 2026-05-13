package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.auth.domain.{AuthManagerType, CredentialStatus, hashPasswordForLoginEmail}
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.{Connection, Date, ResultSet, Timestamp}
import java.time.{Instant, LocalDate, OffsetDateTime}
import java.util.UUID

object ManagerPlannerPlainSql:
  def registerAirline(connection: Connection, input: RegisterAirlineManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val airlineId = s"airline-${UUID.randomUUID().toString.take(12)}"
      val managerId = s"manager-${UUID.randomUUID().toString.take(12)}"
      insertAirline(connection, airlineId, input.airlineName, input.airlineCode, now)
      insertManager(connection, "airline_managers", managerId, Some(airlineId), input.email, input.displayName, now)
      insertManagerCredential(connection, "Airline", managerId, input.email, passwordHash, now)
      ManagerSessionPlannerResponse(managerId, "Airline", input.email, input.displayName, "Active", airlineId, now.toString)
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
      ManagerSessionPlannerResponse(managerId, "Hotel", input.email, input.displayName, "Active", hotelId, now.toString)
    }

  def registerSiteAdmin(connection: Connection, input: RegisterSiteAdminPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      val managerId = s"site-admin-${UUID.randomUUID().toString.take(12)}"
      insertManagerCredential(connection, "SiteAdmin", managerId, input.email, passwordHash, now)
      ManagerSessionPlannerResponse(managerId, "SiteAdmin", input.email, input.displayName, "Active", "site-admin", now.toString)
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

  def batchDecision(connection: Connection, input: ManagerBatchDecisionPlannerRequest, action: String, now: Instant): IO[ManagerBatchDecisionPlannerResponse] =
    IO.blocking {
      input.orderItemIds.foreach(orderItemId => updateDecision(connection, input.managerId, orderItemId, action, input.reason.orElse(input.note), now))
      ManagerBatchDecisionPlannerResponse(input.orderItemIds.size, input.orderItemIds, action)
    }

  def decision(connection: Connection, input: ManagerDecisionPlannerRequest, action: String, now: Instant): IO[ManagerBatchDecisionPlannerResponse] =
    IO.blocking {
      updateDecision(connection, input.managerId, input.orderItemId, action, input.reason.orElse(input.note), now)
      ManagerBatchDecisionPlannerResponse(1, List(input.orderItemId), action)
    }

  def listFlights(connection: Connection, input: ManagerScopedPlannerRequest): IO[ManagerFlightListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select f.flight_id, f.airline_id, a.name as airline_name, a.code as airline_code, f.flight_number,
                 f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.status,
                 f.base_price_amount, f.base_price_currency, f.created_at
          from airline_managers m
          join airlines a on a.airline_id = m.airline_id
          join flights f on f.airline_id = a.airline_id
          where m.manager_id = ?
          order by f.departure_time, f.flight_id
        """
      ) { statement =>
        statement.setString(1, input.managerId)
        ManagerFlightListPlannerResponse(PlainSqlSupport.queryList(statement)(readFlight))
      }
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
        ManagerHotelListPlannerResponse(PlainSqlSupport.queryList(statement)(readHotel))
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

  def createFlight(connection: Connection, input: CreateManagerFlightPlannerRequest, now: Instant): IO[ManagerFlightPlannerResponse] =
    IO.blocking {
      val airlineId = findScopeId(connection, "airline_managers", "airline_id", input.managerId)
      val flightId = s"flight-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into flights(flight_id, airline_id, flight_number, departure_airport, arrival_airport, departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, flightId)
        statement.setString(2, airlineId)
        statement.setString(3, input.flightNumber)
        statement.setString(4, input.departureAirport)
        statement.setString(5, input.arrivalAirport)
        statement.setObject(6, OffsetDateTime.parse(input.departureTime))
        statement.setObject(7, OffsetDateTime.parse(input.arrivalTime))
        statement.setString(8, "Scheduled")
        statement.setBigDecimal(9, BigDecimal(input.economyPrice).bigDecimal)
        statement.setString(10, input.currency)
        statement.setTimestamp(11, Timestamp.from(now))
        statement.executeUpdate()
      }
      insertCabin(connection, flightId, "economy", input.economySeatCount, input.economyPrice, input.currency)
      insertCabin(connection, flightId, "business", input.businessSeatCount, input.businessPrice, input.currency)
      readCreatedFlight(connection, flightId)
    }

  def createRoomType(connection: Connection, input: CreateManagerRoomTypePlannerRequest): IO[ManagerHotelPlannerResponse] =
    IO.blocking {
      val hotelId = findScopeId(connection, "hotel_managers", "hotel_id", input.managerId)
      val roomTypeId = s"room-type-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into hotel_room_types(room_type_id, hotel_id, name, capacity, bed_type, base_price_amount, base_price_currency, status) values (?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, roomTypeId)
        statement.setString(2, hotelId)
        statement.setString(3, input.roomTypeName)
        statement.setInt(4, input.capacity)
        statement.setString(5, input.bedType)
        statement.setBigDecimal(6, BigDecimal(input.nightlyPrice).bigDecimal)
        statement.setString(7, input.currency)
        statement.setString(8, "OpenForBooking")
        statement.executeUpdate()
      }
      val start = LocalDate.parse(input.inventoryStartDate)
      val end = LocalDate.parse(input.inventoryEndDate)
      Iterator.iterate(start)(_.plusDays(1)).takeWhile(!_.isAfter(end)).foreach { date =>
        PlainSqlSupport.withStatement(connection, "insert into hotel_room_inventories(inventory_id, room_type_id, inventory_date, available_rooms, unit_price_amount, unit_price_currency, status) values (?, ?, ?, ?, ?, ?, ?)") { statement =>
          statement.setString(1, s"room-inv-${UUID.randomUUID().toString.take(12)}")
          statement.setString(2, roomTypeId)
          statement.setDate(3, Date.valueOf(date))
          statement.setInt(4, input.availableRooms)
          statement.setBigDecimal(5, BigDecimal(input.nightlyPrice).bigDecimal)
          statement.setString(6, input.currency)
          statement.setString(7, "Available")
          statement.executeUpdate()
        }
      }
      readHotelById(connection, hotelId)
    }

  def refundDecision(connection: Connection, orderId: String, action: String, now: Instant): IO[ManagerBatchDecisionPlannerResponse] =
    IO.blocking {
      val status = if action == "approve" then "Settled" else "Rejected"
      PlainSqlSupport.withStatement(connection, "update order_refunds set refund_status = ?, approved_at = ?, settled_at = ? where order_id = ? and refund_status = ?") { statement =>
        statement.setString(1, status)
        statement.setTimestamp(2, if action == "approve" then Timestamp.from(now) else null)
        statement.setTimestamp(3, if action == "approve" then Timestamp.from(now) else null)
        statement.setString(4, orderId)
        statement.setString(5, "Requested")
        statement.executeUpdate()
      }
      ManagerBatchDecisionPlannerResponse(1, List(orderId), action)
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

  private def updateDecision(connection: Connection, managerId: String, orderItemId: String, action: String, reason: Option[String], now: Instant): Unit =
    val status = if action == "confirm" then "SupplierConfirmed" else "SupplierRejected"
    val decision = if action == "confirm" then "Confirm" else "Reject"
    PlainSqlSupport.withStatement(connection, "update order_line_items set supplier_review_status = ?, review_decision = ?, review_reason = ?, reviewed_at = ?, reviewed_by_manager_id = ? where order_item_id = ?") { statement =>
      statement.setString(1, status)
      statement.setString(2, decision)
      statement.setString(3, reason.orNull)
      statement.setTimestamp(4, Timestamp.from(now))
      statement.setString(5, managerId)
      statement.setString(6, orderItemId)
      statement.executeUpdate()
    }

  private def insertCabin(connection: Connection, flightId: String, cabinClass: String, seats: Int, price: String, currency: String): Unit =
    PlainSqlSupport.withStatement(connection, "insert into flight_cabin_inventories(inventory_id, flight_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status) values (?, ?, ?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, s"cabin-${UUID.randomUUID().toString.take(12)}")
      statement.setString(2, flightId)
      statement.setString(3, cabinClass)
      statement.setInt(4, seats)
      statement.setBigDecimal(5, BigDecimal(price).bigDecimal)
      statement.setString(6, currency)
      statement.setString(7, "Open")
      statement.executeUpdate()
    }

  private def findScopeId(connection: Connection, table: String, column: String, managerId: String): String =
    PlainSqlSupport.withStatement(connection, s"select $column from $table where manager_id = ?") { statement =>
      statement.setString(1, managerId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString(column) else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
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

  private def readFlight(resultSet: ResultSet): ManagerFlightPlannerResponse =
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
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString
    )

  private def readHotel(resultSet: ResultSet): ManagerHotelPlannerResponse =
    ManagerHotelPlannerResponse(resultSet.getString("hotel_id"), resultSet.getString("name"), resultSet.getString("location"), resultSet.getString("status"), resultSet.getTimestamp("created_at").toInstant.toString)

  private def readCreatedFlight(connection: Connection, flightId: String): ManagerFlightPlannerResponse =
    PlainSqlSupport.withStatement(connection, "select f.flight_id, f.airline_id, a.name as airline_name, a.code as airline_code, f.flight_number, f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.status, f.base_price_amount, f.base_price_currency, f.created_at from flights f join airlines a on a.airline_id = f.airline_id where f.flight_id = ?") { statement =>
      statement.setString(1, flightId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readFlight(resultSet) else throw new IllegalStateException("Inserted flight could not be read")
      finally resultSet.close()
    }

  private def readHotelById(connection: Connection, hotelId: String): ManagerHotelPlannerResponse =
    PlainSqlSupport.withStatement(connection, "select hotel_id, name, location, status, created_at from hotels where hotel_id = ?") { statement =>
      statement.setString(1, hotelId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readHotel(resultSet) else throw new IllegalStateException("Hotel could not be read")
      finally resultSet.close()
    }

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
