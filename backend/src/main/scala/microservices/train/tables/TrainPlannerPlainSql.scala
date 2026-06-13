// TrainPlannerPlainSql 灏佽鐏溅妯″潡鐨刾lain SQL 瀹炵幇銆?
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
  import com.typesafe.travel.train.domain.TrainPlannerPlainSqlSupport.*
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



