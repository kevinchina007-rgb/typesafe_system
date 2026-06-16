// 本文件封装 `ListManagerRefundTasksPlanner` 对应的 plain SQL 实现。
package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Timestamp}
import java.time.Instant

object ManagerRefundTaskPlannerPlainSql:
  def listRefundTasks(connection: Connection, input: ManagerScopedPlannerRequest): IO[ManagerRefundTaskListResponse] =
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
        ManagerRefundTaskListResponse(PlainSqlSupport.queryList(statement) { resultSet =>
          ManagerRefundTaskResponse(
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

  private def normalizeManagerType(value: String): String =
    value.trim.toLowerCase match
      case "hotel" => "Hotel"
      case "attraction" => "Attraction"
      case "train" => "Train"
      case "siteadmin" | "site-admin" => "SiteAdmin"
      case _ => "Airline"
