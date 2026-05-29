package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.auth.domain.{AuthManagerType, CredentialStatus, hashPasswordForLoginEmail}
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object ManagerPlannerPlainSql:
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

