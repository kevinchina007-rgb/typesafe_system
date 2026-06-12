// ManagerBookingTaskPlannerPlainSql 封装operations模块的plain SQL 实现。

package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant

object ManagerBookingTaskPlannerPlainSql:
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
