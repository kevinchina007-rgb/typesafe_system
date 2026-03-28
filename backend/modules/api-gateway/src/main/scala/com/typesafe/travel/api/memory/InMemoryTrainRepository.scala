package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryTrainRepository[F[_]: Sync] private (
    railwayManagerState: TrieMap[ManagerId, RailwayManager],
    trainState: TrieMap[TrainId, TrainJourney],
    managerSequence: AtomicLong,
    trainSequence: AtomicLong,
    stopSequence: AtomicLong,
    seatInventorySequence: AtomicLong,
    segmentPriceSequence: AtomicLong,
    refundPolicySequence: AtomicLong
) extends TrainRepository[F]:
  override def nextManagerId: F[ManagerId] =
    Sync[F].delay(ManagerId(s"train-manager-${managerSequence.incrementAndGet()}"))

  override def nextTrainId: F[TrainId] =
    Sync[F].delay(TrainId(s"train-${trainSequence.incrementAndGet()}"))

  override def nextTrainStopId: F[TrainStopId] =
    Sync[F].delay(TrainStopId(s"train-stop-${stopSequence.incrementAndGet()}"))

  override def nextTrainSeatInventoryId: F[TrainSeatInventoryId] =
    Sync[F].delay(TrainSeatInventoryId(s"train-seat-${seatInventorySequence.incrementAndGet()}"))

  override def nextTrainSegmentPriceId: F[TrainSegmentPriceId] =
    Sync[F].delay(TrainSegmentPriceId(s"train-segment-${segmentPriceSequence.incrementAndGet()}"))

  override def nextTrainRefundPolicySegmentId: F[TrainRefundPolicySegmentId] =
    Sync[F].delay(TrainRefundPolicySegmentId(s"train-policy-${refundPolicySequence.incrementAndGet()}"))

  override def findRailwayManagerByEmail(emailAddress: EmailAddress): F[Option[RailwayManager]] =
    Sync[F].delay(railwayManagerState.values.find(_.primaryEmailAddress == emailAddress))

  override def findRailwayManagerById(managerId: ManagerId): F[Option[RailwayManager]] =
    Sync[F].delay(railwayManagerState.get(managerId))

  override def saveRailwayManager(railwayManager: RailwayManager): F[RailwayManager] =
    Sync[F].delay {
      railwayManagerState.put(railwayManager.managerId, railwayManager)
      railwayManager
    }

  override def findTrainById(trainId: TrainId): F[Option[TrainJourney]] =
    Sync[F].delay(trainState.get(trainId))

  override def findTrainBySeatInventoryId(trainSeatInventoryId: TrainSeatInventoryId): F[Option[TrainJourney]] =
    Sync[F].delay(trainState.values.find(_.seatInventories.exists(_.inventoryId == trainSeatInventoryId)))

  override def searchTrains(trainSearchCriteria: TrainSearchCriteria): F[List[TrainJourney]] =
    Sync[F].delay {
      trainState.values.toList
        .filter(matchesSearch(_, trainSearchCriteria))
        .sortBy(_.saleStartsAt)
    }

  override def saveTrain(trainJourney: TrainJourney): F[TrainJourney] =
    Sync[F].delay {
      trainState.put(trainJourney.trainId, trainJourney)
      trainJourney
    }

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

object InMemoryTrainRepository:
  def create[F[_]: Sync]: InMemoryTrainRepository[F] =
    new InMemoryTrainRepository[F](
      railwayManagerState = TrieMap.empty,
      trainState = TrieMap.empty,
      managerSequence = AtomicLong(100),
      trainSequence = AtomicLong(100),
      stopSequence = AtomicLong(1000),
      seatInventorySequence = AtomicLong(1000),
      segmentPriceSequence = AtomicLong(1000),
      refundPolicySequence = AtomicLong(1000)
    )
