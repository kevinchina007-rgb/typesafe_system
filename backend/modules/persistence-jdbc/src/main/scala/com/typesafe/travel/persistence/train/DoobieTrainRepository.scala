package com.typesafe.travel.persistence.train

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import doobie.*
import doobie.implicits.*

import java.time.{Duration, Instant, LocalDate}
import java.util.UUID

final class DoobieTrainRepository[F[_]: Async](
    transactor: Transactor[F]
) extends TrainRepository[F]:
  override def nextManagerId: F[ManagerId] =
    Sync[F].delay(ManagerId(s"train-manager-${UUID.randomUUID().toString.take(12)}"))

  override def nextTrainId: F[TrainId] =
    Sync[F].delay(TrainId(s"train-${UUID.randomUUID().toString.take(12)}"))

  override def nextTrainStopId: F[TrainStopId] =
    Sync[F].delay(TrainStopId(s"train-stop-${UUID.randomUUID().toString.take(12)}"))

  override def nextTrainSeatInventoryId: F[TrainSeatInventoryId] =
    Sync[F].delay(TrainSeatInventoryId(s"train-seat-${UUID.randomUUID().toString.take(12)}"))

  override def nextTrainSegmentPriceId: F[TrainSegmentPriceId] =
    Sync[F].delay(TrainSegmentPriceId(s"train-segment-${UUID.randomUUID().toString.take(12)}"))

  override def nextTrainRefundPolicySegmentId: F[TrainRefundPolicySegmentId] =
    Sync[F].delay(TrainRefundPolicySegmentId(s"train-policy-${UUID.randomUUID().toString.take(12)}"))

  override def findRailwayManagerByEmail(emailAddress: EmailAddress): F[Option[RailwayManager]] =
    sql"""
      select manager_id, operator_code, email, display_name, status, created_at
      from railway_managers
      where email = ${emailAddress.value}
    """.query[(String, String, String, String, String, Instant)].option.transact(transactor).flatMap(_.traverse(buildRailwayManager))

  override def findRailwayManagerById(managerId: ManagerId): F[Option[RailwayManager]] =
    sql"""
      select manager_id, operator_code, email, display_name, status, created_at
      from railway_managers
      where manager_id = ${managerId.value}
    """.query[(String, String, String, String, String, Instant)].option.transact(transactor).flatMap(_.traverse(buildRailwayManager))

  override def saveRailwayManager(railwayManager: RailwayManager): F[RailwayManager] =
    (
      for
        updatedRowCount <- sql"""
          update railway_managers
          set operator_code = ${railwayManager.operatorCode},
              email = ${railwayManager.primaryEmailAddress.value},
              display_name = ${railwayManager.displayName.value},
              status = ${railwayManager.managerStatus.toString},
              created_at = ${railwayManager.createdAt}
          where manager_id = ${railwayManager.managerId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into railway_managers(manager_id, operator_code, email, display_name, status, created_at)
            values(
              ${railwayManager.managerId.value},
              ${railwayManager.operatorCode},
              ${railwayManager.primaryEmailAddress.value},
              ${railwayManager.displayName.value},
              ${railwayManager.managerStatus.toString},
              ${railwayManager.createdAt}
            )
          """.update.run.void
      yield ()
    ).transact(transactor).as(railwayManager)

  override def findTrainById(trainId: TrainId): F[Option[TrainJourney]] =
    sql"""
      select train_id, manager_id, train_number, sale_starts_at, status, created_at
      from trains
      where train_id = ${trainId.value}
    """.query[(String, String, String, Instant, String, Instant)].option.transact(transactor).flatMap(_.traverse(buildTrainJourney))

  override def findTrainBySeatInventoryId(trainSeatInventoryId: TrainSeatInventoryId): F[Option[TrainJourney]] =
    sql"""
      select t.train_id, t.manager_id, t.train_number, t.sale_starts_at, t.status, t.created_at
      from trains t
      inner join train_seat_inventories tsi on tsi.train_id = t.train_id
      where tsi.inventory_id = ${trainSeatInventoryId.value}
    """.query[(String, String, String, Instant, String, Instant)].option.transact(transactor).flatMap(_.traverse(buildTrainJourney))

  override def searchTrains(trainSearchCriteria: TrainSearchCriteria): F[List[TrainJourney]] =
    sql"""
      select train_id, manager_id, train_number, sale_starts_at, status, created_at
      from trains
      order by sale_starts_at, train_id
    """.query[(String, String, String, Instant, String, Instant)].to[List].transact(transactor)
      .flatMap(_.traverse(buildTrainJourney))
      .map(_.filter(matchesSearch(_, trainSearchCriteria)))

  override def saveTrain(trainJourney: TrainJourney): F[TrainJourney] =
    (
      for
        updatedRowCount <- sql"""
          update trains
          set manager_id = ${trainJourney.managerId.value},
              train_number = ${trainJourney.trainNumber.value},
              sale_starts_at = ${trainJourney.saleStartsAt},
              status = ${trainJourney.trainJourneyStatus.toString},
              created_at = ${trainJourney.createdAt}
          where train_id = ${trainJourney.trainId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into trains(train_id, manager_id, train_number, sale_starts_at, status, created_at)
            values(
              ${trainJourney.trainId.value},
              ${trainJourney.managerId.value},
              ${trainJourney.trainNumber.value},
              ${trainJourney.saleStartsAt},
              ${trainJourney.trainJourneyStatus.toString},
              ${trainJourney.createdAt}
            )
          """.update.run.void
        _ <- sql"delete from train_refund_policy_segments where train_id = ${trainJourney.trainId.value}".update.run
        _ <- sql"delete from train_segment_prices where train_id = ${trainJourney.trainId.value}".update.run
        _ <- sql"delete from train_seat_inventories where train_id = ${trainJourney.trainId.value}".update.run
        _ <- sql"delete from train_stops where train_id = ${trainJourney.trainId.value}".update.run
        _ <- trainJourney.stops.traverse_ { stop =>
          sql"""
            insert into train_stops(stop_id, train_id, station_code, station_name, sequence_no, arrival_time, departure_time)
            values(
              ${stop.stopId.value},
              ${trainJourney.trainId.value},
              ${stop.stationCode.value},
              ${stop.stationName.value},
              ${stop.sequenceNo},
              ${stop.arrivalTime},
              ${stop.departureTime}
            )
          """.update.run
        }
        _ <- trainJourney.seatInventories.traverse_ { seatInventory =>
          sql"""
            insert into train_seat_inventories(inventory_id, train_id, seat_class, total_seats, saleable_seats, status)
            values(
              ${seatInventory.inventoryId.value},
              ${trainJourney.trainId.value},
              ${seatInventory.seatClass.value},
              ${seatInventory.totalSeats.value},
              ${seatInventory.saleableSeats.value},
              ${seatInventory.seatInventoryStatus.toString}
            )
          """.update.run
        }
        _ <- trainJourney.segmentPrices.traverse_ { segmentPrice =>
          sql"""
            insert into train_segment_prices(segment_price_id, train_id, from_stop_id, to_stop_id, seat_class, amount, currency)
            values(
              ${segmentPrice.segmentPriceId.value},
              ${trainJourney.trainId.value},
              ${segmentPrice.fromStopId.value},
              ${segmentPrice.toStopId.value},
              ${segmentPrice.seatClass.value},
              ${segmentPrice.price.amount},
              ${segmentPrice.price.currency.toString}
            )
          """.update.run
        }
        _ <- trainJourney.refundPolicySegments.traverse_ { refundPolicy =>
          sql"""
            insert into train_refund_policy_segments(
              policy_segment_id, train_id, start_offset_minutes_before_departure, end_offset_minutes_before_departure, refund_type, refund_rate
            )
            values(
              ${refundPolicy.policySegmentId.value},
              ${trainJourney.trainId.value},
              ${refundPolicy.startOffsetBeforeDeparture.toMinutes},
              ${refundPolicy.endOffsetBeforeDeparture.toMinutes},
              ${refundPolicy.refundType.toString},
              ${refundPolicy.refundRate.value}
            )
          """.update.run
        }
      yield ()
    ).transact(transactor).as(trainJourney)

  private def buildRailwayManager(row: (String, String, String, String, String, Instant)): F[RailwayManager] =
    val (managerIdValue, operatorCodeValue, emailValue, displayNameValue, statusValue, createdAtValue) = row
    for
      emailAddress <- Async[F].fromEither(EmailAddress.create(emailValue))
      displayName <- Async[F].fromEither(PersonName.create(displayNameValue))
    yield restorePersistedRailwayManager(
      managerId = ManagerId(managerIdValue),
      operatorCode = operatorCodeValue,
      primaryEmailAddress = emailAddress,
      displayName = displayName,
      managerStatus = RailwayManagerStatus.valueOf(statusValue),
      createdAt = createdAtValue
    )

  private def buildTrainJourney(row: (String, String, String, Instant, String, Instant)): F[TrainJourney] =
    val (trainIdValue, managerIdValue, trainNumberValue, saleStartsAtValue, statusValue, createdAtValue) = row
    for
      trainNumber <- Async[F].fromEither(TrainNumber.create(trainNumberValue))
      stops <- loadStops(TrainId(trainIdValue))
      seatInventories <- loadSeatInventories(TrainId(trainIdValue))
      segmentPrices <- loadSegmentPrices(TrainId(trainIdValue))
      refundPolicies <- loadRefundPolicies(TrainId(trainIdValue))
    yield restorePersistedTrainJourney(
      trainId = TrainId(trainIdValue),
      managerId = ManagerId(managerIdValue),
      trainNumber = trainNumber,
      saleStartsAt = saleStartsAtValue,
      trainJourneyStatus = TrainJourneyStatus.valueOf(statusValue),
      stops = stops,
      seatInventories = seatInventories,
      segmentPrices = segmentPrices,
      refundPolicySegments = refundPolicies,
      createdAt = createdAtValue
    )

  private def loadStops(trainId: TrainId): F[Vector[TrainStop]] =
    sql"""
      select stop_id, station_code, station_name, sequence_no, arrival_time, departure_time
      from train_stops
      where train_id = ${trainId.value}
      order by sequence_no
    """.query[(String, String, String, Int, Option[Instant], Option[Instant])].to[List].transact(transactor)
      .flatMap(_.traverse { case (stopIdValue, stationCodeValue, stationNameValue, sequenceNoValue, arrivalTimeValue, departureTimeValue) =>
        for
          stationCode <- Async[F].fromEither(TrainStationCode.create(stationCodeValue))
          stationName <- Async[F].fromEither(TrainStationName.create(stationNameValue))
        yield TrainStop(TrainStopId(stopIdValue), stationCode, stationName, sequenceNoValue, arrivalTimeValue, departureTimeValue)
      }.map(_.toVector))

  private def loadSeatInventories(trainId: TrainId): F[Vector[TrainSeatInventory]] =
    sql"""
      select inventory_id, seat_class, total_seats, saleable_seats, status
      from train_seat_inventories
      where train_id = ${trainId.value}
      order by inventory_id
    """.query[(String, String, Int, Int, String)].to[List].transact(transactor)
      .flatMap(_.traverse { case (inventoryIdValue, seatClassValue, totalSeatsValue, saleableSeatsValue, statusValue) =>
        for
          seatClass <- Async[F].fromEither(TrainSeatClass.create(seatClassValue))
          totalSeats <- Async[F].fromEither(SeatCount.create(totalSeatsValue))
          saleableSeats <- Async[F].fromEither(SeatCount.create(saleableSeatsValue))
        yield TrainSeatInventory(
          inventoryId = TrainSeatInventoryId(inventoryIdValue),
          trainId = trainId,
          seatClass = seatClass,
          totalSeats = totalSeats,
          saleableSeats = saleableSeats,
          seatInventoryStatus = TrainSeatInventoryStatus.valueOf(statusValue)
        )
      }.map(_.toVector))

  private def loadSegmentPrices(trainId: TrainId): F[Vector[TrainSegmentPrice]] =
    sql"""
      select segment_price_id, from_stop_id, to_stop_id, seat_class, amount, currency
      from train_segment_prices
      where train_id = ${trainId.value}
      order by segment_price_id
    """.query[(String, String, String, String, BigDecimal, String)].to[List].transact(transactor)
      .flatMap(_.traverse { case (segmentPriceIdValue, fromStopIdValue, toStopIdValue, seatClassValue, amountValue, currencyValue) =>
        for
          seatClass <- Async[F].fromEither(TrainSeatClass.create(seatClassValue))
          currency <- Async[F].fromEither(DatabaseCodecs.parseCurrency(currencyValue))
          price <- Async[F].fromEither(Money.create(amountValue, currency))
        yield TrainSegmentPrice(
          segmentPriceId = TrainSegmentPriceId(segmentPriceIdValue),
          trainId = trainId,
          fromStopId = TrainStopId(fromStopIdValue),
          toStopId = TrainStopId(toStopIdValue),
          seatClass = seatClass,
          price = price
        )
      }.map(_.toVector))

  private def loadRefundPolicies(trainId: TrainId): F[Vector[TrainRefundPolicySegment]] =
    sql"""
      select policy_segment_id, start_offset_minutes_before_departure, end_offset_minutes_before_departure, refund_type, refund_rate
      from train_refund_policy_segments
      where train_id = ${trainId.value}
      order by start_offset_minutes_before_departure desc, policy_segment_id
    """.query[(String, Long, Long, String, BigDecimal)].to[List].transact(transactor)
      .flatMap(_.traverse { case (policySegmentIdValue, startOffsetValue, endOffsetValue, refundTypeValue, refundRateValue) =>
        for
          refundRate <- Async[F].fromEither(RefundRate.create(refundRateValue))
        yield TrainRefundPolicySegment(
          policySegmentId = TrainRefundPolicySegmentId(policySegmentIdValue),
          trainId = trainId,
          startOffsetBeforeDeparture = Duration.ofMinutes(startOffsetValue),
          endOffsetBeforeDeparture = Duration.ofMinutes(endOffsetValue),
          refundType = TrainRefundType.valueOf(refundTypeValue),
          refundRate = refundRate
        )
      }.map(_.toVector))

  private def matchesSearch(trainJourney: TrainJourney, trainSearchCriteria: TrainSearchCriteria): Boolean =
    val sortedStops = trainJourney.stops.sortBy(_.sequenceNo)
    val fromStopOpt = trainSearchCriteria.fromStationCode.flatMap(stationCode => sortedStops.find(_.stationCode == stationCode))
    val toStopOpt = trainSearchCriteria.toStationCode.flatMap(stationCode => sortedStops.find(_.stationCode == stationCode))

    trainSearchCriteria.departureDate.forall { departureDate =>
      fromStopOpt.flatMap(_.departureTime).exists(_.atZone(java.time.ZoneOffset.UTC).toLocalDate == departureDate)
    } &&
    trainSearchCriteria.fromStationCode.forall(_ => fromStopOpt.nonEmpty) &&
    trainSearchCriteria.toStationCode.forall(_ => toStopOpt.nonEmpty) &&
    ((fromStopOpt, toStopOpt) match
      case (Some(fromStop), Some(toStop)) => fromStop.sequenceNo < toStop.sequenceNo
      case _                              => true)

