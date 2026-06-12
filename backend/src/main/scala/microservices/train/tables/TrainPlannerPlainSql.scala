// TrainPlannerPlainSql 封装火车模块的plain SQL 实现。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.auth.domain.CredentialStatus
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.persistence.ReferenceDataSeeder
import com.typesafe.travel.persistence.codecs.DatabaseCodecs
import com.typesafe.travel.persistence.order.TrainSeatAllocationInsertRow
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.traveler.domain.TravelerPlannerPlainSql
import doobie.Transactor
import io.circe.Json

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Duration, Instant, LocalDate}
import java.util.UUID

object TrainPlannerPlainSql:
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

  private def normalizeTrainStationQuery(query: String): String =
    val trimmed = query.trim
    if trimmed.isEmpty then trimmed
    else stationQueryAliases.getOrElse(trimmed, trimmed)

  private def stationQueryCandidates(query: String): Vector[String] =
    val trimmed = query.trim
    if trimmed.isEmpty then Vector.empty
    else
      (Vector(trimmed) ++ stationQueryAliases.get(trimmed) ++ stationQueryReverseAliases.get(trimmed)).distinct

  private def stopMatchesQuery(stop: TrainStopPlannerResponse, query: String): Boolean =
    val stopCode = stop.stationCode.trim.toLowerCase
    val stopName = stop.stationName.trim.toLowerCase
    stationQueryCandidates(query).exists { candidate =>
      val normalizedCandidate = candidate.trim.toLowerCase
      normalizedCandidate.nonEmpty && (stopCode.contains(normalizedCandidate) || stopName.contains(normalizedCandidate))
    }

  private def trainMatchesSearch(train: TrainPlannerResponse, input: SearchTrainsPlannerRequest): Boolean =
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

  def suggestions(connection: Connection, input: TrainSuggestionPlannerRequest): IO[TrainSuggestionListPlannerResponse] =
    ensureReferenceData(connection) *>
      IO.blocking {
        val q = s"%${normalizeTrainStationQuery(input.q).toLowerCase}%"
        PlainSqlSupport.withStatement(
          connection,
          """
            select distinct t.train_id, t.train_number, s1.station_name as from_station, s2.station_name as to_station
            from trains t
            join train_stops s1 on s1.train_id = t.train_id
            join train_stops s2 on s2.train_id = t.train_id and s2.sequence_no > s1.sequence_no
            where lower(t.train_number) like ? or lower(s1.station_name) like ? or lower(s2.station_name) like ?
            order by t.train_number
            limit 10
          """
        ) { statement =>
          statement.setString(1, q)
          statement.setString(2, q)
          statement.setString(3, q)
          TrainSuggestionListPlannerResponse(
            PlainSqlSupport.queryList(statement) { row =>
              TrainSuggestionPlannerResponse(
                resourceType = "train",
                value = row.getString("train_id"),
                title = row.getString("train_number"),
                subtitle = s"${row.getString("from_station")} -> ${row.getString("to_station")}"
              )
            }
          )
        }
      }

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
    ensureReferenceData(connection) *>
      queryTrains(connection, "where t.manager_id = ? order by t.sale_starts_at, t.train_id", List(input.managerId), now).map(TrainListPlannerResponse.apply)

  def search(connection: Connection, input: SearchTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    ensureReferenceData(connection) *>
      searchTrains(connection, input, now)

  def get(connection: Connection, input: TrainByIdPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    ensureReferenceData(connection) *>
      queryTrains(connection, "where t.train_id = ?", List(input.trainId), now).map(_.headOption.getOrElse(throw new IllegalArgumentException(s"Train '${input.trainId}' was not found")))

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
      readTrain(connection, trainId, now)
    }

  def bookItem(connection: Connection, input: BookTrainItemPlannerRequest, now: Instant): IO[BookTrainItemPlannerResponse] =
    ensureReferenceData(connection) *>
      TravelerPlannerPlainSql.listByOwner(connection, UserId(input.userId)).flatMap { travelers =>
      IO.blocking {
      val train = readTrain(connection, input.trainId, now)
      val trainJourney = toTrainJourney(train)
      val currentJourneyWindow = trainJourneyWindow(trainJourney).fold(error => throw new IllegalArgumentException(error.message), identity)
      val resolvedTravelerIds =
        if input.travelerIds.nonEmpty then input.travelerIds
        else
          travelers.filter(_.isDefaultTravelerProfile).map(_.travelerId.value)
      if resolvedTravelerIds.isEmpty then
        throw new IllegalArgumentException("train_traveler_required")
      val orderItemId = s"order-item-${UUID.randomUUID().toString.take(12)}"
      val sortIndex = nextOrderItemSortIndex(connection, input.orderId)
      val fromStop = stopIdByStationCode(connection, input.trainId, input.fromStationCode)
      val toStop = stopIdByStationCode(connection, input.trainId, input.toStationCode)
      val inventoryId = inventoryIdBySeatClass(connection, input.trainId, input.seatClass)
      val pricing = resolveTrainRoutePricing(connection, input.trainId, fromStop, toStop, input.seatClass)
        .getOrElse(throw new IllegalArgumentException(s"Train '${input.trainId}' is missing a route price from '${input.fromStationCode}' to '${input.toStationCode}' for seat '${input.seatClass}'"))
      val unitPrice = pricing.amount
      val travelerCount = resolvedTravelerIds.size.max(1)
      val amount = unitPrice * BigDecimal(travelerCount)
      val fromStopPlan = trainJourney.stops.find(_.stationCode.value == input.fromStationCode).getOrElse(
        throw new IllegalArgumentException(s"Train '${input.trainId}' does not contain station '${input.fromStationCode}'")
      )
      val toStopPlan = trainJourney.stops.find(_.stationCode.value == input.toStationCode).getOrElse(
        throw new IllegalArgumentException(s"Train '${input.trainId}' does not contain station '${input.toStationCode}'")
      )
      val seatInventoryPlan = trainJourney.seatInventories.find(_.seatClass.value == input.seatClass).getOrElse(
        throw new IllegalArgumentException(s"Train '${input.trainId}' does not have seat inventory '${input.seatClass}'")
      )
      val travelerIds = resolvedTravelerIds.map(TravelerId.apply).toVector
      ensureTrainTravelerAvailability(connection, travelerIds, currentJourneyWindow, trainJourney)
      val seatAllocationPlan =
        allocateTrainJourneySeats(
          trainJourney,
          travelerIds,
          fromStopPlan,
          toStopPlan,
          seatInventoryPlan,
          input.seatPreference.map(TrainSeatPreference.fromText),
          readTrainSeatAllocations(connection, input.trainId)
        ).fold(error => throw new IllegalArgumentException(error.message), identity)
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into order_line_items (
            order_item_id, order_id, item_kind, item_status, booked_amount, booked_currency, snapshot_json, sort_index,
            supplier_review_status, train_id, train_from_stop_id, train_to_stop_id, train_seat_inventory_id, seat_class,
            traveler_ids_json, unit_amount, unit_currency
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, orderItemId)
        statement.setString(2, input.orderId)
        statement.setString(3, "Train")
        statement.setString(4, "Active")
        statement.setBigDecimal(5, amount.bigDecimal)
        statement.setString(6, "CNY")
        statement.setString(
          7,
          buildTrainBookingSnapshotJson(
            train,
            input,
            unitPrice,
            amount,
            pricing.currency,
            resolvedTravelerIds.toVector,
            seatAllocationPlan.assignments
          )
        )
        statement.setInt(8, sortIndex)
        statement.setString(9, "NotSubmitted")
        statement.setString(10, input.trainId)
        statement.setString(11, fromStop)
        statement.setString(12, toStop)
        statement.setString(13, inventoryId)
        statement.setString(14, input.seatClass)
        statement.setString(15, resolvedTravelerIds.mkString("[\"", "\",\"", "\"]"))
        statement.setBigDecimal(16, unitPrice.bigDecimal)
        statement.setString(17, pricing.currency)
        statement.executeUpdate()
      }
      seatAllocationPlan.assignments.foreach { assignment =>
        insertTrainSeatAllocationRow(
          connection,
          TrainSeatAllocationInsertRow(
            allocationId = s"train-allocation-${UUID.randomUUID().toString.take(12)}",
            seatId = assignment.seatId.value,
            trainId = input.trainId,
            orderId = input.orderId,
            orderItemId = orderItemId,
            travelerId = assignment.travelerId.value,
            fromStopSequenceNo = fromStopPlan.sequenceNo,
            toStopSequenceNo = toStopPlan.sequenceNo,
            carriageNo = assignment.carriageNo,
            seatNo = assignment.seatNo,
            seatLabel = assignment.seatLabel,
            seatPositionType = assignment.seatPositionType.toString,
            createdAt = now
          )
        )
      }
      updateOrderTotals(connection, input.orderId)
      BookTrainItemPlannerResponse(input.orderId, orderItemId)
      }
    }

  private val selectTrainSql = "select t.train_id, t.manager_id, t.train_number, t.sale_starts_at, t.status, t.created_at from trains t "

  private def searchTrains(connection: Connection, input: SearchTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    val baseSql =
      """
        where (? is null or exists (select 1 from train_stops ds where ds.train_id = t.train_id and cast(coalesce(ds.departure_time, ds.arrival_time) as date) = cast(? as date)))
        order by t.sale_starts_at, t.train_id
      """
    queryTrains(
      connection,
      baseSql,
      List(input.date.orNull, input.date.orNull),
      now
    ).flatMap { response =>
      val filteredResponse = response.filter(train => trainMatchesSearch(train, input))
      if filteredResponse.nonEmpty then IO.pure(TrainListPlannerResponse(filteredResponse))
      else
        ensureReferenceData(connection) *>
          queryTrains(
            connection,
            baseSql,
            List(input.date.orNull, input.date.orNull),
            now
          ).map(response => TrainListPlannerResponse(response.filter(train => trainMatchesSearch(train, input))))
    }

  private def ensureReferenceData(connection: Connection): IO[Unit] =
    IO.blocking(
      ReferenceDataSeeder.seedIfNeeded(
        Transactor.fromConnection[IO](connection, None)
      )
    )

  private def queryTrains(connection: Connection, whereSql: String, values: List[String], now: Instant): IO[List[TrainPlannerResponse]] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, selectTrainSql + whereSql) { statement =>
        values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
        PlainSqlSupport.queryList(statement)(row => readTrain(connection, row.getString("train_id"), now))
      }
    }

  private def readTrain(connection: Connection, trainId: String, now: Instant): TrainPlannerResponse =
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

  private def readStops(connection: Connection, trainId: String): List[TrainStopPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select stop_id, station_code, station_name, sequence_no, arrival_time, departure_time from train_stops where train_id = ? order by sequence_no") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement) { row =>
        TrainStopPlannerResponse(row.getString("stop_id"), row.getString("station_code"), row.getString("station_name"), row.getInt("sequence_no"), Option(row.getTimestamp("arrival_time")).map(_.toInstant.toString), Option(row.getTimestamp("departure_time")).map(_.toInstant.toString))
      }
    }

  private def readInventories(connection: Connection, trainId: String, now: Instant): List[TrainSeatInventoryPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select inventory_id, seat_class, total_seats, saleable_seats, status from train_seat_inventories where train_id = ? order by inventory_id") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement) { row =>
        val inventoryId = row.getString("inventory_id")
        TrainSeatInventoryPlannerResponse(inventoryId, row.getString("seat_class"), row.getInt("total_seats"), (row.getInt("saleable_seats") - reservedQuantity(connection, inventoryId, now)).max(0), row.getString("status"))
      }
    }

  private def readSeats(connection: Connection, trainId: String): List[TrainSeatPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select seat_id, carriage_no, seat_no, seat_label, seat_class, seat_position_type, status from train_seats where train_id = ? order by carriage_no, row_no, seat_code") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement)(row => TrainSeatPlannerResponse(row.getString("seat_id"), row.getInt("carriage_no"), row.getString("seat_no"), row.getString("seat_label"), row.getString("seat_class"), row.getString("seat_position_type"), row.getString("status")))
    }

  private def readPrices(connection: Connection, trainId: String): List[TrainSegmentPricePlannerResponse] =
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

  private def readPolicies(connection: Connection, trainId: String): List[TrainRefundPolicyPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select start_offset_minutes_before_departure, end_offset_minutes_before_departure, refund_type, refund_rate from train_refund_policy_segments where train_id = ? order by start_offset_minutes_before_departure desc") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement)(row => TrainRefundPolicyPlannerResponse(row.getLong("start_offset_minutes_before_departure"), row.getLong("end_offset_minutes_before_departure"), row.getString("refund_type"), row.getBigDecimal("refund_rate").toString))
    }

  private def reservedQuantity(connection: Connection, inventoryId: String, now: Instant): Int =
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

  private def stopIdByStationCode(connection: Connection, trainId: String, stationCode: String): String =
    scalarString(connection, "select stop_id from train_stops where train_id = ? and station_code = ?", List(trainId, stationCode))

  private def inventoryIdBySeatClass(connection: Connection, trainId: String, seatClass: String): String =
    scalarString(connection, "select inventory_id from train_seat_inventories where train_id = ? and seat_class = ? order by inventory_id limit 1", List(trainId, seatClass))

  private final case class TrainRoutePricing(amount: BigDecimal, currency: String)

  private def resolveTrainRoutePricing(connection: Connection, trainId: String, fromStopId: String, toStopId: String, seatClass: String): Option[TrainRoutePricing] =
    if List(trainId, fromStopId, toStopId, seatClass).exists(value => value == null || value.trim.isEmpty) then None
    else
      val orderedStops = PlainSqlSupport.withStatement(connection, "select stop_id, sequence_no from train_stops where train_id = ? order by sequence_no asc") { statement =>
        statement.setString(1, trainId)
        PlainSqlSupport.queryList(statement) { row =>
          (row.getString("stop_id"), row.getInt("sequence_no"))
        }
      }.sortBy(_._2)

      val stopSequenceById = orderedStops.toMap
      val fromSequenceNo = stopSequenceById.getOrElse(fromStopId, return None)
      val toSequenceNo = stopSequenceById.getOrElse(toStopId, return None)
      if toSequenceNo <= fromSequenceNo then None
      else
        val routeStops = orderedStops.filter { case (_, sequenceNo) => sequenceNo >= fromSequenceNo && sequenceNo <= toSequenceNo }
        val segmentPrices = PlainSqlSupport.withStatement(
          connection,
          "select from_stop_id, to_stop_id, amount, currency from train_segment_prices where train_id = ? and seat_class = ?"
        ) { statement =>
          statement.setString(1, trainId)
          statement.setString(2, seatClass)
          PlainSqlSupport.queryList(statement) { row =>
            ((row.getString("from_stop_id"), row.getString("to_stop_id")), (BigDecimal(row.getBigDecimal("amount")), row.getString("currency")))
          }.toMap
        }

        val segmentAmounts = routeStops.sliding(2).toList.flatMap {
          case List((leftStopId, _), (rightStopId, _)) => segmentPrices.get((leftStopId, rightStopId))
          case _ => Nil
        }

        if segmentAmounts.size != math.max(routeStops.size - 1, 0) || segmentAmounts.isEmpty then None
        else
          val currency = segmentAmounts.headOption.map(_._2).getOrElse("CNY")
          val amount = segmentAmounts.foldLeft(BigDecimal(0)) { case (acc, (segmentAmount, _)) => acc + segmentAmount }
          Some(TrainRoutePricing(amount, currency))

  private def buildTrainBookingSnapshotJson(
      train: TrainPlannerResponse,
      input: BookTrainItemPlannerRequest,
      unitPrice: BigDecimal,
      totalPrice: BigDecimal,
      currency: String,
      travelerIds: Vector[String],
      seatAssignments: Vector[TrainTravelerSeatAssignment]
  ): String =
    val fromStop = train.stops.find(_.stationCode == input.fromStationCode)
    val toStop = train.stops.find(_.stationCode == input.toStationCode)
    val fromStopId = fromStop.map(_.stopId).getOrElse(input.fromStationCode)
    val toStopId = toStop.map(_.stopId).getOrElse(input.toStationCode)
    val fromStopSequenceNo = fromStop.map(_.sequenceNo)
    val toStopSequenceNo = toStop.map(_.sequenceNo)
    val departureTime = fromStop.flatMap(_.departureTime).orElse(fromStop.flatMap(_.arrivalTime)).getOrElse("")
    val arrivalTime = toStop.flatMap(_.arrivalTime).orElse(toStop.flatMap(_.departureTime)).getOrElse("")
    val seatAssignmentsJson =
      Json.fromValues(
        seatAssignments.map { assignment =>
          Json.obj(
            "travelerId" -> Json.fromString(assignment.travelerId.value),
            "seatId" -> Json.fromString(assignment.seatId.value),
            "carriageNo" -> Json.fromInt(assignment.carriageNo),
            "seatNo" -> Json.fromString(assignment.seatNo),
            "seatLabel" -> Json.fromString(assignment.seatLabel),
            "seatPositionType" -> Json.fromString(assignment.seatPositionType.toString)
          )
        }
      )

    Json
      .obj(
        "trainId" -> Json.fromString(input.trainId),
        "trainNumber" -> Json.fromString(train.trainNumber),
        "fromStopId" -> Json.fromString(fromStopId),
        "fromStopSequenceNo" -> fromStopSequenceNo.fold(Json.Null)(Json.fromInt),
        "fromStationCode" -> Json.fromString(input.fromStationCode),
        "fromStationName" -> Json.fromString(fromStop.map(_.stationName).getOrElse(input.fromStationCode)),
        "toStopId" -> Json.fromString(toStopId),
        "toStopSequenceNo" -> toStopSequenceNo.fold(Json.Null)(Json.fromInt),
        "toStationCode" -> Json.fromString(input.toStationCode),
        "toStationName" -> Json.fromString(toStop.map(_.stationName).getOrElse(input.toStationCode)),
        "departureTime" -> (if departureTime.nonEmpty then Json.fromString(departureTime) else Json.Null),
        "arrivalTime" -> (if arrivalTime.nonEmpty then Json.fromString(arrivalTime) else Json.Null),
        "seatInventoryId" -> Json.fromString(train.seatInventories.find(_.seatClass == input.seatClass).map(_.inventoryId).getOrElse(input.seatClass)),
        "seatClass" -> Json.fromString(input.seatClass),
        "requestedSeatPreference" -> input.seatPreference.map(preference => Json.fromString(preference.trim)).getOrElse(Json.Null),
        "seatAssignments" -> seatAssignmentsJson,
        "travelerIds" -> Json.fromValues(travelerIds.map(Json.fromString)),
        "saleStartsAt" -> Json.fromString(train.saleStartsAt),
        "unitPriceAmount" -> Json.fromBigDecimal(unitPrice),
        "unitPriceCurrency" -> Json.fromString(currency),
        "totalPriceAmount" -> Json.fromBigDecimal(totalPrice),
        "totalPriceCurrency" -> Json.fromString(currency),
        "departureStation" -> Json.fromString(fromStop.map(_.stationName).getOrElse(input.fromStationCode)),
        "arrivalStation" -> Json.fromString(toStop.map(_.stationName).getOrElse(input.toStationCode))
      )
      .noSpaces

  private def readTrainSeatAllocations(connection: Connection, trainId: String): Vector[TrainSegmentSeatAllocation] =
    PlainSqlSupport.withStatement(connection, "select seat_id, order_id, order_item_id, from_stop_sequence_no, to_stop_sequence_no from train_seat_allocations where train_id = ? order by created_at, allocation_id") { statement =>
      statement.setString(1, trainId)
      PlainSqlSupport.queryList(statement) { row =>
        TrainSegmentSeatAllocation(
          seatId = TrainSeatId(row.getString("seat_id")),
          orderId = OrderId(row.getString("order_id")),
          orderItemId = OrderItemId(row.getString("order_item_id")),
          fromStopSequenceNo = row.getInt("from_stop_sequence_no"),
          toStopSequenceNo = row.getInt("to_stop_sequence_no")
        )
      }.toVector
    }

  private def insertTrainSeatAllocationRow(connection: Connection, row: TrainSeatAllocationInsertRow): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into train_seat_allocations(
          allocation_id, seat_id, train_id, order_id, order_item_id, traveler_id, from_stop_sequence_no, to_stop_sequence_no,
          carriage_no, seat_no, seat_label, seat_position_type, created_at
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, row.allocationId)
      statement.setString(2, row.seatId)
      statement.setString(3, row.trainId)
      statement.setString(4, row.orderId)
      statement.setString(5, row.orderItemId)
      statement.setString(6, row.travelerId)
      statement.setInt(7, row.fromStopSequenceNo)
      statement.setInt(8, row.toStopSequenceNo)
      statement.setInt(9, row.carriageNo)
      statement.setString(10, row.seatNo)
      statement.setString(11, row.seatLabel)
      statement.setString(12, row.seatPositionType)
      statement.setTimestamp(13, Timestamp.from(row.createdAt))
      statement.executeUpdate()
    }

  private def toTrainJourney(train: TrainPlannerResponse): TrainJourney =
    val stops = train.stops.map { stop =>
      TrainStop(
        stopId = TrainStopId(stop.stopId),
        stationCode = TrainStationCode.create(stop.stationCode).fold(throw _, identity),
        stationName = TrainStationName.create(stop.stationName).fold(throw _, identity),
        sequenceNo = stop.sequenceNo,
        arrivalTime = stop.arrivalTime.map(Instant.parse),
        departureTime = stop.departureTime.map(Instant.parse)
      )
    }.toVector
    val seatInventories = train.seatInventories.map { inventory =>
      TrainSeatInventory(
        inventoryId = TrainSeatInventoryId(inventory.inventoryId),
        trainId = TrainId(train.trainId),
        seatClass = TrainSeatClass.create(inventory.seatClass).fold(throw _, identity),
        totalSeats = SeatCount.create(inventory.totalSeats).fold(throw _, identity),
        saleableSeats = SeatCount.create(inventory.saleableSeats).fold(throw _, identity),
        seatInventoryStatus = TrainSeatInventoryStatus.fromText(inventory.status)
      )
    }.toVector
    val seatInventoryByClass = seatInventories.map(inventory => inventory.seatClass.value -> inventory.inventoryId).toMap
    val seats = train.seats.map { seat =>
      val seatClass = TrainSeatClass.create(seat.seatClass).fold(throw _, identity)
      val inventoryId = seatInventoryByClass.getOrElse(seatClass.value, throw new IllegalArgumentException(s"Train '${train.trainId}' is missing seat inventory '${seat.seatClass}'"))
      val parsedRowNo = seat.seatNo.take(2).toIntOption.getOrElse(throw new IllegalArgumentException(s"Train '${train.trainId}' has invalid seat number '${seat.seatNo}'"))
      val seatCode = seat.seatNo.drop(2)
      TrainSeat(
        seatId = TrainSeatId(seat.seatId),
        trainId = TrainId(train.trainId),
        inventoryId = inventoryId,
        seatClass = seatClass,
        carriageNo = seat.carriageNo,
        rowNo = TrainSeatRowNo.create(parsedRowNo).fold(throw _, identity),
        seatCode = seatCode,
        seatNo = seat.seatNo,
        seatLabel = seat.seatLabel,
        seatPositionType = TrainSeatPositionType.fromText(seat.seatPositionType),
        seatStatus = TrainSeatStatus.fromText(seat.status)
      )
    }.toVector
    val segmentPrices = train.segmentPrices.map { price =>
      val fromStopCode = price.fromStationCode
      val toStopCode = price.toStationCode
      val fromStopId = stops.find(_.stationCode.value == fromStopCode).map(_.stopId).getOrElse(throw new IllegalArgumentException(s"Train '${train.trainId}' is missing stop '$fromStopCode'"))
      val toStopId = stops.find(_.stationCode.value == toStopCode).map(_.stopId).getOrElse(throw new IllegalArgumentException(s"Train '${train.trainId}' is missing stop '$toStopCode'"))
      val currency = Currency.fromText(price.currency)
      TrainSegmentPrice(
        segmentPriceId = TrainSegmentPriceId(s"${train.trainId}-${fromStopCode}-${toStopCode}-${price.seatClass}"),
        trainId = TrainId(train.trainId),
        fromStopId = fromStopId,
        toStopId = toStopId,
        seatClass = TrainSeatClass.create(price.seatClass).fold(throw _, identity),
        price = Money.create(BigDecimal(price.amount), currency).fold(throw _, identity)
      )
    }.toVector
    restorePersistedTrainJourney(
      trainId = TrainId(train.trainId),
      managerId = ManagerId("train-booking"),
      trainNumber = TrainNumber.create(train.trainNumber).fold(throw _, identity),
      saleStartsAt = Instant.parse(train.saleStartsAt),
      trainJourneyStatus = TrainJourneyStatus.fromText(train.status),
      stops = stops,
      seatInventories = seatInventories,
      seats = seats,
      segmentPrices = segmentPrices,
      refundPolicySegments = Vector.empty,
      createdAt = Instant.parse(train.saleStartsAt)
    )

  private def scalarString(connection: Connection, sql: String, values: List[String]): String =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString(1) else throw new IllegalArgumentException("Required train row was not found")
      finally resultSet.close()
    }

  private def nextOrderItemSortIndex(connection: Connection, orderId: String): Int =
    PlainSqlSupport.withStatement(connection, "select coalesce(max(sort_index), 0) + 1 from order_line_items where order_id = ?") { statement =>
      statement.setString(1, orderId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getInt(1) else 1
      finally resultSet.close()
    }

  private def updateOrderTotals(connection: Connection, orderId: String): Unit =
    PlainSqlSupport.withStatement(connection, "update orders set total_price_amount = (select coalesce(sum(booked_amount), 0) from order_line_items where order_id = ?), remaining_refundable_amount = (select coalesce(sum(booked_amount), 0) from order_line_items where order_id = ?) where order_id = ?") { statement =>
      statement.setString(1, orderId)
      statement.setString(2, orderId)
      statement.setString(3, orderId)
      statement.executeUpdate()
    }

  private def ensureTrainTravelerAvailability(
      connection: Connection,
      travelerIds: Vector[TravelerId],
      currentJourneyWindow: TrainJourneyWindow,
      currentTrainJourney: TrainJourney
  ): Unit =
    val requestedTravelerIds = travelerIds.map(_.value).map(_.trim).filter(_.nonEmpty).distinct.toSet
    if requestedTravelerIds.nonEmpty then
      PlainSqlSupport.withStatement(
        connection,
        """
          select o.order_id, o.status, li.snapshot_json
          from orders o
          join order_line_items li on li.order_id = o.order_id
          where li.item_kind = 'Train'
            and o.status in ('PendingPayment', 'Confirmed')
          order by o.created_at, o.order_id, li.sort_index
        """
      ) { statement =>
        val resultSet = statement.executeQuery()
        try
          while resultSet.next() do
            val snapshotJson = Option(resultSet.getString("snapshot_json")).getOrElse("{}")
            DatabaseCodecs.decodeTrainBookingSnapshot(snapshotJson).fold(
              _ => (),
              existingSnapshot =>
                val existingJourneyWindow = TrainJourneyWindow(existingSnapshot.departureTime, existingSnapshot.arrivalTime)
                val overlappingTravelers = existingSnapshot.travelerIds.map(_.value).toSet.intersect(requestedTravelerIds)
                if overlappingTravelers.nonEmpty && trainJourneyWindowOverlaps(currentJourneyWindow, existingJourneyWindow) then
                  throw new IllegalArgumentException(
                    s"Traveler(s) ${overlappingTravelers.toList.sorted.mkString(", ")} already have an active train order that overlaps with train '${currentTrainJourney.trainNumber.value}'"
                  )
            )
        finally resultSet.close()
      }
