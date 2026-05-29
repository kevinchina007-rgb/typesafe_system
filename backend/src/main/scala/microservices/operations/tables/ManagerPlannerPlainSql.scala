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

