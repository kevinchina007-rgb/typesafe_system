package com.typesafe.travel.train.domain

import cats.syntax.all.*
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
  for
    _ <- validateTrainStops(trainId, stops)
    _ <- validateTrainSeats(trainId, seatInventories, seats)
  yield
    TrainJourney(
      trainId = trainId,
      managerId = managerId,
      trainNumber = trainNumber,
      saleStartsAt = saleStartsAt,
      trainJourneyStatus = TrainJourneyStatus.OnSale,
      stops = stops.sortBy(_.sequenceNo),
      seatInventories = seatInventories,
      seats = seats.sortBy(seat => (seat.carriageNo, seat.rowNo.value, seat.seatCode)),
      segmentPrices = segmentPrices,
      refundPolicySegments = refundPolicySegments.sortBy(_.startOffsetBeforeDeparture.toMinutes).reverse,
      createdAt = createdAt
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
  TrainJourney(
    trainId = trainId,
    managerId = managerId,
    trainNumber = trainNumber,
    saleStartsAt = saleStartsAt,
    trainJourneyStatus = trainJourneyStatus,
    stops = stops.sortBy(_.sequenceNo),
    seatInventories = seatInventories,
    seats = seats.sortBy(seat => (seat.carriageNo, seat.rowNo.value, seat.seatCode)),
    segmentPrices = segmentPrices,
    refundPolicySegments = refundPolicySegments.sortBy(_.startOffsetBeforeDeparture.toMinutes).reverse,
    createdAt = createdAt
  )

def generateTrainSeats(
    trainId: TrainId,
    inventory: TrainSeatInventory,
    carriageCount: Int,
    rowsPerCarriage: Int,
    layoutColumns: Vector[TrainSeatLayoutColumn],
    seatIds: Vector[TrainSeatId]
): Either[TrainError, Vector[TrainSeat]] =
  if carriageCount <= 0 || rowsPerCarriage <= 0 || layoutColumns.isEmpty then
    Left(TrainError.TrainSeatGenerationWasInvalid(trainId, "carriageCount, rowsPerCarriage, and layoutColumns must all be positive"))
  else
    val preparedSeatSpecs =
      (1 to carriageCount).toVector.flatMap { carriageNo =>
        (1 to rowsPerCarriage).toVector.flatMap { rowNo =>
          layoutColumns.map { column =>
            (column, carriageNo, rowNo, f"$rowNo%02d${column.code}")
          }
        }
      }
    if preparedSeatSpecs.size != seatIds.size then
      Left(TrainError.TrainSeatGenerationWasInvalid(trainId, s"Prepared ${preparedSeatSpecs.size} seats but got ${seatIds.size} ids"))
    else
      val materializedSeats = preparedSeatSpecs.zip(seatIds).map { case ((column, carriageNo, rowNo, seatNo), seatId) =>
        TrainSeat(
          seatId = seatId,
          trainId = trainId,
          inventoryId = inventory.inventoryId,
          seatClass = inventory.seatClass,
          carriageNo = carriageNo,
          rowNo = TrainSeatRowNo.create(rowNo).fold(throw _, identity),
          seatCode = column.code,
          seatNo = seatNo,
          seatLabel = s"${carriageNo}车$seatNo",
          seatPositionType = column.positionType,
          seatStatus = TrainSeatStatus.Available
        )
      }
      Either.cond(
        materializedSeats.size == inventory.totalSeats.value,
        materializedSeats,
        TrainError.TrainSeatGenerationWasInvalid(trainId, s"Generated ${materializedSeats.size} seats but inventory declares ${inventory.totalSeats.value}")
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

private def validateTrainSeats(
    trainId: TrainId,
    seatInventories: Vector[TrainSeatInventory],
    seats: Vector[TrainSeat]
): Either[TrainError, Unit] =
  val seatsByInventoryId = seats.groupBy(_.inventoryId)
  seatInventories.traverse_ { inventory =>
    val generatedSeats = seatsByInventoryId.getOrElse(inventory.inventoryId, Vector.empty)
    Either.cond(
      generatedSeats.size == inventory.totalSeats.value,
      (),
      TrainError.TrainSeatGenerationWasInvalid(
        trainId,
        s"Inventory '${inventory.inventoryId.value}' expected ${inventory.totalSeats.value} seats but found ${generatedSeats.size}"
      )
    )
  }
