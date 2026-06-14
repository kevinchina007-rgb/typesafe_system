package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.codecs.DatabaseCodecs
import com.typesafe.travel.persistence.order.TrainSeatAllocationInsertRow
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerPlannerPlainSql

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.UUID
import io.circe.Json

object TrainPlannerPlainSqlBooking:
  def bookItem(connection: Connection, input: BookTrainItemPlannerRequest, now: Instant): IO[BookTrainItemPlannerResponse] =
    TrainPlannerPlainSqlShared.ensureReferenceData(connection) *>
      TravelerPlannerPlainSql.listByOwner(connection, UserId(input.userId)).flatMap { travelers =>
        IO.blocking {
          val train = TrainPlannerPlainSqlShared.readTrain(connection, input.trainId, now)
          val trainJourney = toTrainJourney(train)
          val currentJourneyWindow = trainJourneyWindow(trainJourney).fold(error => throw new IllegalArgumentException(error.message), identity)
          val resolvedTravelerIds =
            if input.travelerIds.nonEmpty then input.travelerIds
            else travelers.filter(_.isDefaultTravelerProfile).map(_.travelerId.value)
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

  def resolveTrainRoutePricing(connection: Connection, trainId: String, fromStopId: String, toStopId: String, seatClass: String): Option[TrainPlannerPlainSqlBooking.TrainRoutePricing] =
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
          Some(TrainPlannerPlainSqlBooking.TrainRoutePricing(amount, currency))

  final case class TrainRoutePricing(amount: BigDecimal, currency: String)

  def buildTrainBookingSnapshotJson(
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

  def readTrainSeatAllocations(connection: Connection, trainId: String): Vector[TrainSegmentSeatAllocation] =
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

  def insertTrainSeatAllocationRow(connection: Connection, row: TrainSeatAllocationInsertRow): Unit =
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

  def toTrainJourney(train: TrainPlannerResponse): TrainJourney =
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

  def scalarString(connection: Connection, sql: String, values: List[String]): String =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString(1) else throw new IllegalArgumentException("Required train row was not found")
      finally resultSet.close()
    }

  def nextOrderItemSortIndex(connection: Connection, orderId: String): Int =
    PlainSqlSupport.withStatement(connection, "select coalesce(max(sort_index), 0) + 1 from order_line_items where order_id = ?") { statement =>
      statement.setString(1, orderId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getInt(1) else 1
      finally resultSet.close()
    }

  def updateOrderTotals(connection: Connection, orderId: String): Unit =
    PlainSqlSupport.withStatement(connection, "update orders set total_price_amount = (select coalesce(sum(booked_amount), 0) from order_line_items where order_id = ?), remaining_refundable_amount = (select coalesce(sum(booked_amount), 0) from order_line_items where order_id = ?) where order_id = ?") { statement =>
      statement.setString(1, orderId)
      statement.setString(2, orderId)
      statement.setString(3, orderId)
      statement.executeUpdate()
    }

  def ensureTrainTravelerAvailability(
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
