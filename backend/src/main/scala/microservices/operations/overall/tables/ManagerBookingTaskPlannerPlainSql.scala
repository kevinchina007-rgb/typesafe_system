// 本文件封装 `ListManagerTasksPlanner` 对应的 plain SQL 实现。
package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant

object ManagerBookingTaskPlannerPlainSql:
  def listTasks(connection: Connection, input: ManagerTasksPlannerRequest): IO[ManagerTaskListResponse] =
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
        ManagerTaskListResponse(PlainSqlSupport.queryList(statement)(readTask))
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

  private def readTask(resultSet: ResultSet): ManagerTaskResponse =
    val createdAt = resultSet.getTimestamp("created_at").toInstant.toString
    val snapshotLabel = Option(resultSet.getString("snapshot_json")).getOrElse(resultSet.getString("item_kind"))
    ManagerTaskResponse(
      taskId = Option(resultSet.getString("order_item_id")),
      taskType = normalizeManagerType(resultSet.getString("item_kind")),
      orderItemId = resultSet.getString("order_item_id"),
      orderId = resultSet.getString("order_id"),
      orderItemKind = resultSet.getString("item_kind"),
      detailLabel = snapshotLabel,
      supplierReviewStatus = resultSet.getString("supplier_review_status"),
      supplierReviewDecision = Option(resultSet.getString("review_decision")).map(decision =>
        SupplierReviewDecisionResponse(
          decision,
          Option(resultSet.getString("review_reason")),
          Option(resultSet.getTimestamp("reviewed_at")).map(_.toInstant.toString),
          Option(resultSet.getString("reviewed_by_manager_id"))
        )
      ),
      summaryLabel = snapshotLabel,
      bookedAmount = resultSet.getBigDecimal("booked_amount").toString,
      bookedCurrency = resultSet.getString("booked_currency"),
      requestedAt = createdAt,
      createdAt = createdAt,
      reviewedAt = Option(resultSet.getTimestamp("reviewed_at")).map(_.toInstant.toString),
      reviewedBy = Option(resultSet.getString("reviewed_by_manager_id")),
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
