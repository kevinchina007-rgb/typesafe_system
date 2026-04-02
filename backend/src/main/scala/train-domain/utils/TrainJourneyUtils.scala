package com.typesafe.travel.train.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Duration, Instant}

def createTrainJourney(
    trainId: TrainId,
    managerId: ManagerId,
    trainNumber: TrainNumber,
    saleStartsAt: Instant,
    stops: Vector[TrainStop],
    seatInventories: Vector[TrainSeatInventory],
    segmentPrices: Vector[TrainSegmentPrice],
    refundPolicySegments: Vector[TrainRefundPolicySegment],
    createdAt: Instant
): Either[TrainError, TrainJourney] =
  validateTrainStops(trainId, stops).map { _ =>
    TrainJourney(
      trainId = trainId,
      managerId = managerId,
      trainNumber = trainNumber,
      saleStartsAt = saleStartsAt,
      trainJourneyStatus = TrainJourneyStatus.OnSale,
      stops = stops.sortBy(_.sequenceNo),
      seatInventories = seatInventories,
      segmentPrices = segmentPrices,
      refundPolicySegments = refundPolicySegments.sortBy(_.startOffsetBeforeDeparture.toMinutes).reverse,
      createdAt = createdAt
    )
  }


def restorePersistedTrainJourney(
    trainId: TrainId,
    managerId: ManagerId,
    trainNumber: TrainNumber,
    saleStartsAt: Instant,
    trainJourneyStatus: TrainJourneyStatus,
    stops: Vector[TrainStop],
    seatInventories: Vector[TrainSeatInventory],
    segmentPrices: Vector[TrainSegmentPrice],
    refundPolicySegments: Vector[TrainRefundPolicySegment],
    createdAt: Instant
): TrainJourney =
  TrainJourney(
    trainId = trainId,
    managerId = managerId,
    trainNumber = trainNumber,
    saleStartsAt = saleStartsAt,
    trainJourneyStatus = trainJourneyStatus,
    stops = stops.sortBy(_.sequenceNo),
    seatInventories = seatInventories,
    segmentPrices = segmentPrices,
    refundPolicySegments = refundPolicySegments.sortBy(_.startOffsetBeforeDeparture.toMinutes).reverse,
    createdAt = createdAt
  )


private def validateTrainStops(trainId: TrainId, stops: Vector[TrainStop]): Either[TrainError, Unit] =
  if stops.size < 2 then Left(TrainError.TrainHadTooFewStops(trainId))
  else
    stops
      .sortBy(_.sequenceNo)
      .sliding(2)
      .toVector
      .traverse_ {
        case Vector(leftStop, rightStop) if leftStop.sequenceNo + 1 == rightStop.sequenceNo => Right(())
        case Vector(leftStop, _) => Left(TrainError.TrainStopSequenceWasInvalid(trainId, leftStop.stationCode.value))
        case _ => Right(())
      }
