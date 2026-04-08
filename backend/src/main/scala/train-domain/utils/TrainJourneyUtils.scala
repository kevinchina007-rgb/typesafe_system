package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

def createTrainJourney(
    trainId: TrainId,
    managerId: ManagerId,
    trainNumber: TrainNumber,
    saleStartsAt: Instant,
    stops: Vector[TrainStop],
    seatInventories: Vector[TrainSeatInventory],
    seats: Vector[TrainSeat],
    segmentPrices: Vector[TrainSegmentPrice],
    refundPolicySegments: Vector[TrainRefundPolicySegment],
    createdAt: Instant
): Either[TrainError, TrainJourney] =
  TrainJourney.create(
    trainId,
    managerId,
    trainNumber,
    saleStartsAt,
    stops,
    seatInventories,
    seats,
    segmentPrices,
    refundPolicySegments,
    createdAt
  )

def restorePersistedTrainJourney(
    trainId: TrainId,
    managerId: ManagerId,
    trainNumber: TrainNumber,
    saleStartsAt: Instant,
    trainJourneyStatus: TrainJourneyStatus,
    stops: Vector[TrainStop],
    seatInventories: Vector[TrainSeatInventory],
    seats: Vector[TrainSeat],
    segmentPrices: Vector[TrainSegmentPrice],
    refundPolicySegments: Vector[TrainRefundPolicySegment],
    createdAt: Instant
): TrainJourney =
  TrainJourney.restore(
    trainId,
    managerId,
    trainNumber,
    saleStartsAt,
    trainJourneyStatus,
    stops,
    seatInventories,
    seats,
    segmentPrices,
    refundPolicySegments,
    createdAt
  )

def generateTrainSeats(
    trainId: TrainId,
    inventory: TrainSeatInventory,
    carriageCount: Int,
    rowsPerCarriage: Int,
    layoutColumns: Vector[TrainSeatLayoutColumn],
    seatIds: Vector[TrainSeatId]
): Either[TrainError, Vector[TrainSeat]] =
  TrainJourney.generateSeats(trainId, inventory, carriageCount, rowsPerCarriage, layoutColumns, seatIds)
