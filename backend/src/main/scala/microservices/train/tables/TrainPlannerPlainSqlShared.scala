// TrainPlannerPlainSqlShared 只存放 train 模块后端 SQL 共享实现，例如车站别名、查询拼接和 JDBC 读写辅助，属于后端内部实现细节。
package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport

import java.sql.{Connection, Timestamp}
import java.time.Instant

object TrainPlannerPlainSqlShared:
  private val stationQueryAliases: Map[String, String] = Map(
    "\u5317\u4eac\u5357" -> "BJS",
    "\u5929\u6d25\u5357" -> "TJS",
    "\u6d4e\u5357\u897f" -> "JNW",
    "\u5357\u4eac\u5357" -> "NJS",
    "\u4e0a\u6d77\u8679\u6865" -> "SHH",
    "\u6df1\u5733\u5317" -> "SZN",
    "\u676d\u5dde\u4e1c" -> "HZD",
    "\u5b81\u6ce2" -> "NGB",
    "\u6e29\u5dde\u5357" -> "WZS",
    "\u798f\u5dde\u5357" -> "FZN",
    "\u53a6\u95e8\u5317" -> "XMN",
    "\u6210\u90fd\u4e1c" -> "CDD",
    "\u91cd\u5e86\u5317" -> "CQB",
    "\u6b66\u6c49" -> "WUH",
    "\u957f\u6c99\u5357" -> "CSN",
    "\u90d1\u5dde\u4e1c" -> "ZZD",
    "\u5408\u80a5\u5357" -> "HFN"
  )
  private val stationQueryReverseAliases: Map[String, String] =
    stationQueryAliases.map(_.swap)

  private val selectTrainSql = "select t.train_id, t.manager_id, t.train_number, t.sale_starts_at, t.status, t.created_at from trains t "

  def normalizeTrainStationQuery(query: String): String =
    val trimmed = query.trim
    if trimmed.isEmpty then trimmed
    else stationQueryAliases.getOrElse(trimmed, trimmed)

  def stationQueryCandidates(query: String): Vector[String] =
    val trimmed = query.trim
    if trimmed.isEmpty then Vector.empty
    else
      (Vector(trimmed) ++ stationQueryAliases.get(trimmed) ++ stationQueryReverseAliases.get(trimmed)).distinct

  def stopMatchesQuery(stop: TrainStopPlannerResponse, query: String): Boolean =
    val stopCode = stop.stationCode.trim.toLowerCase
    val stopName = stop.stationName.trim.toLowerCase
    stationQueryCandidates(query).exists { candidate =>
      val normalizedCandidate = candidate.trim.toLowerCase
      normalizedCandidate.nonEmpty && (stopCode.contains(normalizedCandidate) || stopName.contains(normalizedCandidate))
    }

  def trainMatchesSearch(train: TrainPlannerResponse, input: SearchTrainsPlannerRequest): Boolean =
    val fromOk = input.fromStation.forall(query => train.stops.exists(stop => stopMatchesQuery(stop, query)))
    if !fromOk then false
    else
      val toOk = input.toStation.forall(query => train.stops.exists(stop => stopMatchesQuery(stop, query)))
      if !toOk then false
      else
        (input.fromStation, input.toStation) match
          case (Some(fromQuery), Some(toQuery)) =>
            val fromIndex = train.stops.indexWhere(stop => stopMatchesQuery(stop, fromQuery))
            val toIndex = train.stops.indexWhere(stop => stopMatchesQuery(stop, toQuery))
            fromIndex >= 0 && toIndex >= 0 && fromIndex < toIndex
          case _ => true

  def ensureReferenceData(connection: Connection): IO[Unit] =
    IO.unit

  def queryTrains(connection: Connection, whereSql: String, values: List[String], now: Instant): IO[List[TrainPlannerResponse]] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, selectTrainSql + whereSql) { statement =>
        values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
        PlainSqlSupport.queryList(statement)(row => readTrain(connection, row.getString("train_id"), now))
      }
    }

  def readTrain(connection: Connection, trainId: String, now: Instant): TrainPlannerResponse =
    PlainSqlSupport.withStatement(connection, selectTrainSql + " where t.train_id = ?") { statement =>
      statement.setString(1, trainId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          TrainPlannerResponse(
            trainId = trainId,
            trainNumber = resultSet.getString("train_number"),
            saleStartsAt = resultSet.getTimestamp("sale_starts_at").toInstant.toString,
            status = resultSet.getString("status"),
            stops = readStops(connection, trainId),
            seatInventories = readInventories(connection, trainId, now),
            seats = readSeats(connection, trainId),
            segmentPrices = readPrices(connection, trainId),
            refundPolicies = readPolicies(connection, trainId)
          )
        else throw new IllegalArgumentException(s"Train '$trainId' was not found")
      finally resultSet.close()
    }

  def readStops(connection: Connection, trainId: String): List[TrainStopPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select stop_id, station_code, station_name, sequence_no, arrival_time, departure_time from train_stops where train_id = ? order by sequence_no") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement) { row =>
        TrainStopPlannerResponse(row.getString("stop_id"), row.getString("station_code"), row.getString("station_name"), row.getInt("sequence_no"), Option(row.getTimestamp("arrival_time")).map(_.toInstant.toString), Option(row.getTimestamp("departure_time")).map(_.toInstant.toString))
      }
    }

  def readInventories(connection: Connection, trainId: String, now: Instant): List[TrainSeatInventoryPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select inventory_id, seat_class, total_seats, saleable_seats, status from train_seat_inventories where train_id = ? order by inventory_id") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement) { row =>
        val inventoryId = row.getString("inventory_id")
        TrainSeatInventoryPlannerResponse(inventoryId, row.getString("seat_class"), row.getInt("total_seats"), (row.getInt("saleable_seats") - reservedQuantity(connection, inventoryId, now)).max(0), row.getString("status"))
      }
    }

  def readSeats(connection: Connection, trainId: String): List[TrainSeatPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select seat_id, carriage_no, seat_no, seat_label, seat_class, seat_position_type, status from train_seats where train_id = ? order by carriage_no, row_no, seat_code") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement)(row => TrainSeatPlannerResponse(row.getString("seat_id"), row.getInt("carriage_no"), row.getString("seat_no"), row.getString("seat_label"), row.getString("seat_class"), row.getString("seat_position_type"), row.getString("status")))
    }

  def readPrices(connection: Connection, trainId: String): List[TrainSegmentPricePlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select fs.station_code as from_station_code, ts.station_code as to_station_code, p.seat_class, p.amount, p.currency
        from train_segment_prices p
        join train_stops fs on fs.stop_id = p.from_stop_id
        join train_stops ts on ts.stop_id = p.to_stop_id
        where p.train_id = ?
        order by p.segment_price_id
      """
    ) { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement)(row => TrainSegmentPricePlannerResponse(row.getString("from_station_code"), row.getString("to_station_code"), row.getString("seat_class"), row.getBigDecimal("amount").toString, row.getString("currency")))
    }

  def readPolicies(connection: Connection, trainId: String): List[TrainRefundPolicyPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select start_offset_minutes_before_departure, end_offset_minutes_before_departure, refund_type, refund_rate from train_refund_policy_segments where train_id = ? order by start_offset_minutes_before_departure desc") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement)(row => TrainRefundPolicyPlannerResponse(row.getLong("start_offset_minutes_before_departure"), row.getLong("end_offset_minutes_before_departure"), row.getString("refund_type"), row.getBigDecimal("refund_rate").toString))
    }

  def reservedQuantity(connection: Connection, inventoryId: String, now: Instant): Int =
    PlainSqlSupport.withStatement(connection, "select coalesce(sum(quantity), 0) as reserved_quantity from inventory_reservations where resource_type = ? and resource_id = ? and (status = ? or (status = ? and expires_at > ?))") { statement =>
      statement.setString(1, "TrainSeatInventory")
      statement.setString(2, inventoryId)
      statement.setString(3, "Confirmed")
      statement.setString(4, "Held")
      statement.setTimestamp(5, Timestamp.from(now))
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getInt("reserved_quantity") else 0
      finally resultSet.close()
    }
