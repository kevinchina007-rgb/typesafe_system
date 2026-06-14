package com.typesafe.travel.persistence.order

import cats.effect.IO
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object OrderPlannerCancellationSql:
  def cancel(connection: Connection, input: OrderIdPlannerRequest, now: Instant): IO[OrderPlannerResponse] =
    OrderPlannerPlainSqlSupport.expireTrainOrdersIfNeeded(connection, now) *>
      IO.blocking(OrderPlannerPlainSqlSupport.findRequired(connection, input.orderId)).flatMap { orderBeforeCancel =>
        OrderPlannerPlainSqlSupport.updateStatus(connection, input.orderId, "Cancelled", Some("cancelled_at"), Some(now)).flatTap { _ =>
          PlainSqlSupport.withStatement(connection, "update orders set remaining_refundable_amount = 0 where order_id = ?") { statement =>
            statement.setString(1, input.orderId)
            statement.executeUpdate()
          }
          OrderPlannerPlainSqlSupport.releaseTrainSeatAllocations(connection, input.orderId)
          if orderBeforeCancel.status == "Confirmed" then IO.blocking(AttractionPlannerPlainSql.restoreInventoryForCancelledOrder(connection, input.orderId))
          else IO.unit
        }
      }
