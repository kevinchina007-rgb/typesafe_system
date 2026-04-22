package com.typesafe.travel.train.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Duration, Instant}

final case class TrainStop(
    stopId: TrainStopId,
    stationCode: TrainStationCode,
    stationName: TrainStationName,
    sequenceNo: Int,
    arrivalTime: Option[Instant],
    departureTime: Option[Instant]
)

object TrainStop:
  def departureOrArrivalTime(trainId: TrainId, trainStop: TrainStop): Either[TrainError, Instant] =
    trainStop.arrivalTime
      .orElse(trainStop.departureTime)
      .toRight(TrainError.TrainStationOrderWasInvalid(trainId, trainStop.stationCode, trainStop.stationCode))

final case class TrainSegmentPrice(
    segmentPriceId: TrainSegmentPriceId,
    trainId: TrainId,
    fromStopId: TrainStopId,
    toStopId: TrainStopId,
    seatClass: TrainSeatClass,
    price: Money
)

final case class TrainRefundPolicySegment(
    policySegmentId: TrainRefundPolicySegmentId,
    trainId: TrainId,
    startOffsetBeforeDeparture: Duration,
    endOffsetBeforeDeparture: Duration,
    refundType: TrainRefundType,
    refundRate: RefundRate
):
  def matches(offsetBeforeDeparture: Duration): Boolean =
    offsetBeforeDeparture.compareTo(startOffsetBeforeDeparture) <= 0 &&
    offsetBeforeDeparture.compareTo(endOffsetBeforeDeparture) > 0

  def refundableAmount(ticketMoney: Money): Money =
    refundType match
      case TrainRefundType.FullRefund => ticketMoney
      case TrainRefundType.NonRefundable => Money.zero(ticketMoney.currency)
      case TrainRefundType.PartialRefund =>
        Money.unsafe(ticketMoney.amount * refundRate.value, ticketMoney.currency)

final case class TrainQuote(
    fromStop: TrainStop,
    toStop: TrainStop,
    seatInventory: TrainSeatInventory,
    unitPrice: Money,
    departureTime: Instant,
    arrivalTime: Instant
)

final case class TrainSeatAllocationPlan(
    assignments: Vector[TrainTravelerSeatAssignment],
    requestedPreference: Option[TrainSeatPreference],
    preferenceSatisfied: Boolean,
    adjacencySatisfied: Boolean
)

final case class TrainJourneyWindow(
    departureTime: Instant,
    arrivalTime: Instant
):
  def overlaps(other: TrainJourneyWindow): Boolean =
    departureTime.isBefore(other.arrivalTime) && arrivalTime.isAfter(other.departureTime)

final case class TrainJourney(
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
):
  def ensureBookableAt(currentTime: Instant): Either[TrainError, TrainJourney] =
    trainJourneyStatus match
      case TrainJourneyStatus.Closed => Left(TrainError.TrainWasClosed(trainId, trainJourneyStatus))
      case _ if currentTime.isBefore(saleStartsAt) => Left(TrainError.TrainWasNotOpenForSale(trainId, saleStartsAt, currentTime))
      case _ => Right(this)

  def findStopByStationCode(stationCode: TrainStationCode): Either[TrainError, TrainStop] =
    stops.find(_.stationCode == stationCode).toRight(TrainError.TrainStopWasNotFound(trainId, stationCode))

  def quote(
      fromStationCode: TrainStationCode,
      toStationCode: TrainStationCode,
      seatClass: TrainSeatClass,
      currentTime: Instant
  ): Either[TrainError, TrainQuote] =
    for
      _ <- ensureBookableAt(currentTime)
      fromStop <- findStopByStationCode(fromStationCode)
      toStop <- findStopByStationCode(toStationCode)
      _ <- if fromStop.sequenceNo < toStop.sequenceNo then Right(()) else Left(TrainError.TrainStationOrderWasInvalid(trainId, fromStationCode, toStationCode))
      seatInventory <- seatInventories.find(_.seatClass == seatClass).toRight(TrainError.TrainSeatInventoryWasNotFound(trainId, seatClass))
      _ <- seatInventory.ensureBookable
      unitPrice <- sumSegmentPrice(fromStop, toStop, seatClass)
      departureTime <- fromStop.departureTime.toRight(TrainError.TrainStationOrderWasInvalid(trainId, fromStationCode, toStationCode))
      arrivalTime <- toStop.arrivalTime.orElse(toStop.departureTime).toRight(TrainError.TrainStationOrderWasInvalid(trainId, fromStationCode, toStationCode))
    yield TrainQuote(fromStop, toStop, seatInventory, unitPrice, departureTime, arrivalTime)

  def allocateSeats(
      travelerIds: Vector[TravelerId],
      fromStop: TrainStop,
      toStop: TrainStop,
      seatInventory: TrainSeatInventory,
      seatPreference: Option[TrainSeatPreference],
      existingAllocations: Vector[TrainSegmentSeatAllocation]
  ): Either[TrainError, TrainSeatAllocationPlan] =
    val availableSeats =
      seats
        .filter(seat => seat.inventoryId == seatInventory.inventoryId && seat.isBookable)
        .filterNot(seat =>
          existingAllocations.exists(allocation =>
            allocation.seatId == seat.seatId && allocation.overlaps(fromStop.sequenceNo, toStop.sequenceNo)
          )
        )
        .sortBy(seat => (seat.carriageNo, seat.rowNo.value, seat.seatCode))

    if availableSeats.size < travelerIds.size then Left(TrainError.TrainSeatAllocationWasNotAvailable(trainId, seatInventory.seatClass, travelerIds.size))
    else
      val preferredSeats =
        seatPreference match
          case Some(TrainSeatPreference.NoPreference) | None => availableSeats
          case Some(preference) =>
            val matchingSeats = availableSeats.filter(_.seatPositionType == positionTypeForPreference(preference))
            if matchingSeats.nonEmpty then matchingSeats else availableSeats
      val adjacencyCandidate = chooseAdjacentSeats(preferredSeats, travelerIds.size).orElse(chooseSameCarriageCluster(preferredSeats, travelerIds.size))
      val selectedSeats = adjacencyCandidate.getOrElse(preferredSeats.take(travelerIds.size))
      val preferenceSatisfied = seatPreference.forall(_ == TrainSeatPreference.NoPreference) ||
        selectedSeats.forall(_.seatPositionType == positionTypeForPreference(seatPreference.get)) ||
        !availableSeats.exists(_.seatPositionType == positionTypeForPreference(seatPreference.get))
      val adjacencySatisfied = selectedSeats.size > 1 && areAdjacent(selectedSeats)
      Right(
        TrainSeatAllocationPlan(
          assignments = travelerIds.zip(selectedSeats).map { case (travelerId, seat) =>
            TrainTravelerSeatAssignment(travelerId, seat.seatId, seat.carriageNo, seat.seatNo, seat.seatLabel, seat.seatPositionType)
          },
          requestedPreference = seatPreference,
          preferenceSatisfied = preferenceSatisfied,
          adjacencySatisfied = adjacencySatisfied
        )
      )

  def calculateRefundAmount(
      departureTime: Instant,
      ticketMoney: Money,
      refundRequestedAt: Instant
  ): Either[TrainError, Money] =
    val offsetBeforeDeparture = Duration.between(refundRequestedAt, departureTime)
    refundPolicySegments
      .find(_.matches(offsetBeforeDeparture))
      .map(_.refundableAmount(ticketMoney))
      .toRight(TrainError.TrainRefundPolicyDidNotMatch(trainId, refundRequestedAt))

  def journeyWindow: Either[TrainError, TrainJourneyWindow] =
    for
      departureTime <- stops.headOption.flatMap(_.departureTime).toRight(TrainError.TrainHadTooFewStops(trainId))
      arrivalTime <- stops.lastOption.flatMap(stop => stop.arrivalTime.orElse(stop.departureTime)).toRight(TrainError.TrainHadTooFewStops(trainId))
    yield TrainJourneyWindow(departureTime, arrivalTime)

  private def sumSegmentPrice(
      fromStop: TrainStop,
      toStop: TrainStop,
      seatClass: TrainSeatClass
  ): Either[TrainError, Money] =
    val travelStops = stops.filter(stop => stop.sequenceNo >= fromStop.sequenceNo && stop.sequenceNo <= toStop.sequenceNo).sortBy(_.sequenceNo)
    travelStops
      .sliding(2)
      .toVector
      .traverse {
        case Vector(leftStop, rightStop) =>
          segmentPrices
            .find(price => price.seatClass == seatClass && price.fromStopId == leftStop.stopId && price.toStopId == rightStop.stopId)
            .map(_.price)
            .toRight(TrainError.TrainSegmentPriceWasMissing(trainId, seatClass, leftStop.stationCode, rightStop.stationCode))
        case _ =>
          Left(TrainError.TrainStationOrderWasInvalid(trainId, fromStop.stationCode, toStop.stationCode))
      }
      .map(_.foldLeft(Money.zero(segmentPrices.headOption.map(_.price.currency).getOrElse(Currency.CNY))) { (acc, price) =>
        acc.add(price).fold(throw _, identity)
      })

  private def positionTypeForPreference(seatPreference: TrainSeatPreference): TrainSeatPositionType =
    seatPreference match
      case TrainSeatPreference.Window => TrainSeatPositionType.Window
      case TrainSeatPreference.Aisle => TrainSeatPositionType.Aisle
      case TrainSeatPreference.Middle => TrainSeatPositionType.Middle
      case TrainSeatPreference.NoPreference => TrainSeatPositionType.Other

  private def chooseAdjacentSeats(availableSeats: Vector[TrainSeat], requestedQuantity: Int): Option[Vector[TrainSeat]] =
    availableSeats
      .groupBy(seat => (seat.carriageNo, seat.rowNo.value))
      .values
      .iterator
      .map(_.sortBy(_.seatCode))
      .flatMap(_.sliding(requestedQuantity))
      .find(candidate => candidate.size == requestedQuantity && areAdjacent(candidate.toVector))
      .map(_.toVector)

  private def chooseSameCarriageCluster(availableSeats: Vector[TrainSeat], requestedQuantity: Int): Option[Vector[TrainSeat]] =
    availableSeats
      .groupBy(_.carriageNo)
      .values
      .iterator
      .filter(_.size >= requestedQuantity)
      .map(_.sortBy(seat => (seat.rowNo.value, seat.seatCode)).take(requestedQuantity).toVector)
      .toList
      .sortBy(candidate => candidate.map(_.rowNo.value).max - candidate.map(_.rowNo.value).min)
      .headOption

  private def areAdjacent(candidateSeats: Vector[TrainSeat]): Boolean =
    candidateSeats.nonEmpty &&
      candidateSeats.map(_.carriageNo).distinct.size == 1 &&
      candidateSeats.map(_.rowNo.value).distinct.size == 1 &&
      candidateSeats
        .sortBy(_.seatCode)
        .sliding(2)
        .forall {
          case Vector(leftSeat, rightSeat) => rightSeat.seatCode.headOption.exists(_.toInt - leftSeat.seatCode.headOption.getOrElse('A').toInt == 1) || rightSeat.seatCode.compareTo(leftSeat.seatCode) == 1
          case _ => true
        }

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
