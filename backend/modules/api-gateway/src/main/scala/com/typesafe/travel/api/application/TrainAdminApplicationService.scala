package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*

import java.time.{Duration, Instant}

final case class TrainAdminSession(
    railwayManager: RailwayManager,
    managedTrains: List[TrainJourney]
)

trait TrainAdminApplicationService[F[_]]:
  def registerRailwayManager(
      operatorCode: String,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[TrainAdminSession]
  def loginRailwayManager(primaryEmailAddress: EmailAddress): F[TrainAdminSession]
  def listManagedTrains(managerId: ManagerId): F[List[TrainJourney]]
  def createTrainJourney(
      managerId: ManagerId,
      trainNumber: TrainNumber,
      saleStartsAt: Instant,
      stops: List[CreateTrainStopInput],
      seatConfigs: List[CreateTrainSeatInventoryInput],
      segmentPrices: List[CreateTrainSegmentPriceInput],
      refundPolicies: List[CreateTrainRefundPolicyInput],
      createdAt: Instant
  ): F[TrainJourney]

final case class CreateTrainStopInput(
    stationCode: TrainStationCode,
    stationName: TrainStationName,
    arrivalTime: Option[Instant],
    departureTime: Option[Instant]
)

final case class CreateTrainSeatInventoryInput(
    seatClass: TrainSeatClass,
    totalSeats: SeatCount,
    saleableSeats: SeatCount
)

final case class CreateTrainSegmentPriceInput(
    fromStationCode: TrainStationCode,
    toStationCode: TrainStationCode,
    seatClass: TrainSeatClass,
    price: Money
)

final case class CreateTrainRefundPolicyInput(
    startOffsetBeforeDeparture: Duration,
    endOffsetBeforeDeparture: Duration,
    refundType: TrainRefundType,
    refundRate: RefundRate
)

final class LiveTrainAdminApplicationService[F[_]: MonadThrow](
    trainService: TrainService[F],
    trainRepository: TrainRepository[F]
) extends TrainAdminApplicationService[F]:
  override def registerRailwayManager(
      operatorCode: String,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[TrainAdminSession] =
    trainService
      .registerRailwayManager(operatorCode, primaryEmailAddress, displayName, createdAt)
      .map(manager => TrainAdminSession(manager, Nil))

  override def loginRailwayManager(primaryEmailAddress: EmailAddress): F[TrainAdminSession] =
    for
      railwayManager <- trainService.loginRailwayManager(primaryEmailAddress)
      managedTrains <- listManagedTrains(railwayManager.managerId)
    yield TrainAdminSession(railwayManager, managedTrains)

  override def listManagedTrains(managerId: ManagerId): F[List[TrainJourney]] =
    trainService
      .browseTrains(TrainSearchCriteria(None, None, None))
      .map(_.filter(_.managerId == managerId))

  override def createTrainJourney(
      managerId: ManagerId,
      trainNumber: TrainNumber,
      saleStartsAt: Instant,
      stops: List[CreateTrainStopInput],
      seatConfigs: List[CreateTrainSeatInventoryInput],
      segmentPrices: List[CreateTrainSegmentPriceInput],
      refundPolicies: List[CreateTrainRefundPolicyInput],
      createdAt: Instant
  ): F[TrainJourney] =
    for
      _ <- trainRepository.findRailwayManagerById(managerId).flatMap(_.liftTo[F](TrainError.RailwayManagerWasNotFoundById(managerId)))
      trainId <- trainRepository.nextTrainId
      persistedStops <- stops.zipWithIndex.traverse { case (stopInput, index) =>
        trainRepository.nextTrainStopId.map { stopId =>
          TrainStop(
            stopId = stopId,
            stationCode = stopInput.stationCode,
            stationName = stopInput.stationName,
            sequenceNo = index,
            arrivalTime = stopInput.arrivalTime,
            departureTime = stopInput.departureTime
          )
        }
      }
      persistedSeatConfigs <- seatConfigs.traverse { seatConfig =>
        trainRepository.nextTrainSeatInventoryId.map { inventoryId =>
          TrainSeatInventory(
            inventoryId = inventoryId,
            trainId = trainId,
            seatClass = seatConfig.seatClass,
            totalSeats = seatConfig.totalSeats,
            saleableSeats = seatConfig.saleableSeats,
            seatInventoryStatus = TrainSeatInventoryStatus.OpenForSale
          )
        }
      }
      persistedSegmentPrices <- segmentPrices.traverse { segmentPrice =>
        for
          segmentPriceId <- trainRepository.nextTrainSegmentPriceId
          fromStop <- persistedStops.find(_.stationCode == segmentPrice.fromStationCode).liftTo[F](TrainError.TrainStopWasNotFound(trainId, segmentPrice.fromStationCode))
          toStop <- persistedStops.find(_.stationCode == segmentPrice.toStationCode).liftTo[F](TrainError.TrainStopWasNotFound(trainId, segmentPrice.toStationCode))
        yield TrainSegmentPrice(
          segmentPriceId = segmentPriceId,
          trainId = trainId,
          fromStopId = fromStop.stopId,
          toStopId = toStop.stopId,
          seatClass = segmentPrice.seatClass,
          price = segmentPrice.price
        )
      }
      persistedRefundPolicies <- refundPolicies.traverse { refundPolicy =>
        trainRepository.nextTrainRefundPolicySegmentId.map { policyId =>
          TrainRefundPolicySegment(
            policySegmentId = policyId,
            trainId = trainId,
            startOffsetBeforeDeparture = refundPolicy.startOffsetBeforeDeparture,
            endOffsetBeforeDeparture = refundPolicy.endOffsetBeforeDeparture,
            refundType = refundPolicy.refundType,
            refundRate = refundPolicy.refundRate
          )
        }
      }
      trainJourney <- TrainJourney
        .createTrainJourney(
          trainId = trainId,
          managerId = managerId,
          trainNumber = trainNumber,
          saleStartsAt = saleStartsAt,
          stops = persistedStops.toVector,
          seatInventories = persistedSeatConfigs.toVector,
          segmentPrices = persistedSegmentPrices.toVector,
          refundPolicySegments = persistedRefundPolicies.toVector,
          createdAt = createdAt
        )
        .liftTo[F]
      savedTrain <- trainService.saveTrain(trainJourney)
    yield savedTrain
