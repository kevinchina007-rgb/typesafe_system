// TrainPlannerPlainSqlManager 只负责 train 模块管理端的后端 SQL 写入与查询，前端只会看到 planner 请求/响应，不会镜像这一层。
package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.auth.domain.CredentialStatus
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.UUID

object TrainPlannerPlainSqlManager:
  def registerManager(connection: Connection, input: RegisterRailwayManagerPlannerRequest, passwordHash: String, now: Instant): IO[TrainAdminSessionPlannerResponse] =
    IO.blocking {
      val managerId = s"train-manager-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(
        connection,
        "insert into railway_managers(manager_id, operator_code, email, display_name, status, created_at) values (?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, managerId)
        statement.setString(2, input.operatorCode)
        statement.setString(3, input.email.trim)
        statement.setString(4, input.displayName.trim)
        statement.setString(5, "Active")
        statement.setTimestamp(6, Timestamp.from(now))
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(
        connection,
        "insert into manager_credentials(credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, s"credential-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, "Train")
        statement.setString(3, managerId)
        statement.setString(4, input.email.trim)
        statement.setString(5, passwordHash)
        statement.setString(6, CredentialStatus.Active.toString)
        statement.setTimestamp(7, Timestamp.from(now))
        statement.setTimestamp(8, Timestamp.from(now))
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
      TrainAdminSessionPlannerResponse(managerId, input.operatorCode, input.email.trim, input.displayName.trim, "Active", Nil)
    }

  def listManaged(connection: Connection, input: ListManagedTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSqlShared.ensureReferenceData(connection) *>
      TrainPlannerPlainSqlShared.queryTrains(connection, "where t.manager_id = ? order by t.sale_starts_at, t.train_id", List(input.managerId), now).map(TrainListPlannerResponse.apply)

  def create(connection: Connection, input: CreateTrainJourneyPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    IO.blocking {
      val trainId = s"train-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(connection, "insert into trains(train_id, manager_id, train_number, sale_starts_at, status, created_at) values (?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, trainId)
        statement.setString(2, input.managerId)
        statement.setString(3, input.trainNumber.trim)
        statement.setTimestamp(4, Timestamp.from(Instant.parse(input.saleStartsAt)))
        statement.setString(5, "Draft")
        statement.setTimestamp(6, Timestamp.from(now))
        statement.executeUpdate()
      }
      val stopIdsByStationCode = scala.collection.mutable.Map.empty[String, String]
      input.stops.zipWithIndex.foreach { case (stop, index) =>
        val stopId = s"train-stop-${UUID.randomUUID().toString.take(12)}"
        stopIdsByStationCode += stop.stationCode -> stopId
        PlainSqlSupport.withStatement(connection, "insert into train_stops(stop_id, train_id, station_code, station_name, sequence_no, arrival_time, departure_time) values (?, ?, ?, ?, ?, ?, ?)") { statement =>
          statement.setString(1, stopId)
          statement.setString(2, trainId)
          statement.setString(3, stop.stationCode.trim)
          statement.setString(4, stop.stationName.trim)
          statement.setInt(5, index + 1)
          statement.setTimestamp(6, stop.arrivalTime.map(value => Timestamp.from(Instant.parse(value))).orNull)
          statement.setTimestamp(7, stop.departureTime.map(value => Timestamp.from(Instant.parse(value))).orNull)
          statement.executeUpdate()
        }
      }
      input.seatInventories.foreach { inventory =>
        PlainSqlSupport.withStatement(connection, "insert into train_seat_inventories(inventory_id, train_id, seat_class, total_seats, saleable_seats, status) values (?, ?, ?, ?, ?, ?)") { statement =>
          statement.setString(1, s"train-seat-${UUID.randomUUID().toString.take(12)}")
          statement.setString(2, trainId)
          statement.setString(3, inventory.seatClass.trim)
          statement.setInt(4, inventory.totalSeats)
          statement.setInt(5, inventory.saleableSeats)
          statement.setString(6, "Active")
          statement.executeUpdate()
        }
      }
      input.segmentPrices.foreach { price =>
        PlainSqlSupport.withStatement(connection, "insert into train_segment_prices(segment_price_id, train_id, from_stop_id, to_stop_id, seat_class, amount, currency) values (?, ?, ?, ?, ?, ?, ?)") { statement =>
          statement.setString(1, s"train-segment-${UUID.randomUUID().toString.take(12)}")
          statement.setString(2, trainId)
          statement.setString(3, stopIdsByStationCode.getOrElse(price.fromStationCode, throw new IllegalArgumentException("from station was not found")))
          statement.setString(4, stopIdsByStationCode.getOrElse(price.toStationCode, throw new IllegalArgumentException("to station was not found")))
          statement.setString(5, price.seatClass.trim)
          statement.setBigDecimal(6, BigDecimal(price.amount).bigDecimal)
          statement.setString(7, price.currency.trim)
          statement.executeUpdate()
        }
      }
      input.refundPolicies.foreach { policy =>
        PlainSqlSupport.withStatement(connection, "insert into train_refund_policy_segments(policy_segment_id, train_id, start_offset_minutes_before_departure, end_offset_minutes_before_departure, refund_type, refund_rate) values (?, ?, ?, ?, ?, ?)") { statement =>
          statement.setString(1, s"train-policy-${UUID.randomUUID().toString.take(12)}")
          statement.setString(2, trainId)
          statement.setLong(3, policy.startOffsetMinutesBeforeDeparture)
          statement.setLong(4, policy.endOffsetMinutesBeforeDeparture)
          statement.setString(5, policy.refundType.trim)
          statement.setBigDecimal(6, BigDecimal(policy.refundRate).bigDecimal)
          statement.executeUpdate()
        }
      }
      TrainPlannerPlainSqlShared.readTrain(connection, trainId, now)
    }
