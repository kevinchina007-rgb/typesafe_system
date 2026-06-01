package com.typesafe.travel.persistence.order

import cats.effect.IO
import cats.effect.kernel.Fiber
import cats.syntax.all.*
import com.typesafe.travel.persistence.{DatabaseConfig, PlainSqlSupport}

import java.sql.Timestamp
import java.time.{Duration, Instant}
import scala.concurrent.duration.*

object TrainOrderExpirySweeper:
  private val sweepInterval: FiniteDuration = 1.minute

  def start(databaseConfig: DatabaseConfig): IO[Fiber[IO, Throwable, Unit]] =
    loop(databaseConfig).handleErrorWith(error => IO.println(s"train-order-expiry-sweeper stopped: ${error.getMessage}") *> IO.never).start

  private def loop(databaseConfig: DatabaseConfig): IO[Unit] =
    IO.defer {
      sweepOnce(databaseConfig)
        .handleErrorWith(error => IO.println(s"train-order-expiry-sweeper sweep failed: ${error.getMessage}")) *>
        IO.sleep(sweepInterval) *>
        loop(databaseConfig)
    }

  private def sweepOnce(databaseConfig: DatabaseConfig): IO[Unit] =
    PlainSqlSupport.withConnection(databaseConfig) { connection =>
      val now = Instant.now()
      val nowTimestamp = Timestamp.from(now)
      val cutoffTimestamp = Timestamp.from(now.minus(Duration.ofMinutes(10)))

      val expiredOrderIds =
        PlainSqlSupport.withStatement(
          connection,
          """
            select o.order_id, o.created_at
            from orders o
            where o.order_type in ('PendingSelection', 'TrainBooking')
              and o.status in ('Draft', 'PendingSelection', 'PendingPayment')
              and o.created_at <= ?
              and exists (
                select 1
                from order_line_items li
                where li.order_id = o.order_id and li.item_kind = 'Train'
              )
            order by o.created_at, o.order_id
          """
        ) { statement =>
          statement.setTimestamp(1, cutoffTimestamp)
          PlainSqlSupport.queryList(statement)(_.getString("order_id")).distinct
        }

      expiredOrderIds.foreach { orderId =>
        PlainSqlSupport.withStatement(connection, "update orders set status = 'Cancelled', cancelled_at = coalesce(cancelled_at, ?) where order_id = ? and status in ('Draft', 'PendingSelection', 'PendingPayment')") { statement =>
          statement.setTimestamp(1, nowTimestamp)
          statement.setString(2, orderId)
          statement.executeUpdate()
        }
        PlainSqlSupport.withStatement(connection, "update orders set remaining_refundable_amount = 0 where order_id = ?") { statement =>
          statement.setString(1, orderId)
          statement.executeUpdate()
        }
        PlainSqlSupport.withStatement(connection, "update order_line_items set item_status = 'Cancelled' where order_id = ?") { statement =>
          statement.setString(1, orderId)
          statement.executeUpdate()
        }
        PlainSqlSupport.withStatement(connection, "delete from train_seat_allocations where order_id = ?") { statement =>
          statement.setString(1, orderId)
          statement.executeUpdate()
        }
      }

      IO.unit
    }
