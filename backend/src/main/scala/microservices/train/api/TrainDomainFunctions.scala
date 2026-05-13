package com.typesafe.travel.train.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.{Duration, Instant}

def registerNewRailwayManager(
    managerId: ManagerId,
    operatorCode: String,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): RailwayManager =
  RailwayManager(
    managerId = managerId,
    operatorCode = operatorCode.trim.toUpperCase,
    primaryEmailAddress = primaryEmailAddress,
    displayName = displayName,
    managerStatus = RailwayManagerStatus.Active,
    createdAt = createdAt
  )

def restorePersistedRailwayManager(
    managerId: ManagerId,
    operatorCode: String,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: RailwayManagerStatus,
    createdAt: Instant
): RailwayManager =
  RailwayManager(managerId, operatorCode.trim.toUpperCase, primaryEmailAddress, displayName, managerStatus, createdAt)

def trainStopDepartureOrArrivalTime(trainId: TrainId, trainStop: TrainStop): Either[TrainError, Instant] =
  trainStop.arrivalTime
    .orElse(trainStop.departureTime)
    .toRight(TrainError.TrainStationOrderWasInvalid(trainId, trainStop.stationCode, trainStop.stationCode))

def trainSeatIsBookable(trainSeat: TrainSeat): Boolean =
  trainSeat.seatStatus == TrainSeatStatus.Available

def trainSegmentSeatAllocationOverlaps(
    allocation: TrainSegmentSeatAllocation,
    fromSequenceNo: Int,
    toSequenceNo: Int
): Boolean =
  allocation.fromStopSequenceNo < toSequenceNo && allocation.toStopSequenceNo > fromSequenceNo

def ensureTrainSeatInventoryBookable(seatInventory: TrainSeatInventory): Either[TrainError, TrainSeatInventory] =
  if seatInventory.seatInventoryStatus == TrainSeatInventoryStatus.OpenForSale && seatInventory.saleableSeats.value > 0 then Right(seatInventory)
  else Left(TrainError.TrainSeatInventoryWasNotBookable(seatInventory.trainId, seatInventory.seatClass, seatInventory.seatInventoryStatus))

def trainRefundPolicySegmentMatches(policySegment: TrainRefundPolicySegment, offsetBeforeDeparture: Duration): Boolean =
  offsetBeforeDeparture.compareTo(policySegment.startOffsetBeforeDeparture) <= 0 &&
    offsetBeforeDeparture.compareTo(policySegment.endOffsetBeforeDeparture) > 0

def trainRefundPolicySegmentRefundableAmount(policySegment: TrainRefundPolicySegment, ticketMoney: Money): Money =
  policySegment.refundType match
    case TrainRefundType.FullRefund => ticketMoney
    case TrainRefundType.NonRefundable => Money.zero(ticketMoney.currency)
    case TrainRefundType.PartialRefund =>
      Money.unsafe(ticketMoney.amount * policySegment.refundRate.value, ticketMoney.currency)

def trainJourneyWindowOverlaps(trainJourneyWindow: TrainJourneyWindow, other: TrainJourneyWindow): Boolean =
  trainJourneyWindow.departureTime.isBefore(other.arrivalTime) && trainJourneyWindow.arrivalTime.isAfter(other.departureTime)

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
          seatLabel = s"$carriageNo-$seatNo",
          seatPositionType = column.positionType,
          seatStatus = TrainSeatStatus.Available
        )
      }
      Either.cond(
        materializedSeats.size == inventory.totalSeats.value,
        materializedSeats,
        TrainError.TrainSeatGenerationWasInvalid(trainId, s"Generated ${materializedSeats.size} seats but inventory declares ${inventory.totalSeats.value}")
      )

def ensureTrainJourneyBookableAt(trainJourney: TrainJourney, currentTime: Instant): Either[TrainError, TrainJourney] =
  trainJourney.trainJourneyStatus match
    case TrainJourneyStatus.Closed => Left(TrainError.TrainWasClosed(trainJourney.trainId, trainJourney.trainJourneyStatus))
    case _ if currentTime.isBefore(trainJourney.saleStartsAt) => Left(TrainError.TrainWasNotOpenForSale(trainJourney.trainId, trainJourney.saleStartsAt, currentTime))
    case _ => Right(trainJourney)

def findTrainStopByStationCode(trainJourney: TrainJourney, stationCode: TrainStationCode): Either[TrainError, TrainStop] =
  trainJourney.stops.find(_.stationCode == stationCode).toRight(TrainError.TrainStopWasNotFound(trainJourney.trainId, stationCode))

def quoteTrainJourney(
    trainJourney: TrainJourney,
    fromStationCode: TrainStationCode,
    toStationCode: TrainStationCode,
    seatClass: TrainSeatClass,
    currentTime: Instant
): Either[TrainError, TrainQuote] =
  for
    _ <- ensureTrainJourneyBookableAt(trainJourney, currentTime)
    fromStop <- findTrainStopByStationCode(trainJourney, fromStationCode)
    toStop <- findTrainStopByStationCode(trainJourney, toStationCode)
    _ <- if fromStop.sequenceNo < toStop.sequenceNo then Right(()) else Left(TrainError.TrainStationOrderWasInvalid(trainJourney.trainId, fromStationCode, toStationCode))
    seatInventory <- trainJourney.seatInventories.find(_.seatClass == seatClass).toRight(TrainError.TrainSeatInventoryWasNotFound(trainJourney.trainId, seatClass))
    _ <- ensureTrainSeatInventoryBookable(seatInventory)
    unitPrice <- sumTrainSegmentPrice(trainJourney, fromStop, toStop, seatClass)
    departureTime <- fromStop.departureTime.toRight(TrainError.TrainStationOrderWasInvalid(trainJourney.trainId, fromStationCode, toStationCode))
    arrivalTime <- toStop.arrivalTime.orElse(toStop.departureTime).toRight(TrainError.TrainStationOrderWasInvalid(trainJourney.trainId, fromStationCode, toStationCode))
  yield TrainQuote(fromStop, toStop, seatInventory, unitPrice, departureTime, arrivalTime)

def allocateTrainJourneySeats(
    trainJourney: TrainJourney,
    travelerIds: Vector[TravelerId],
    fromStop: TrainStop,
    toStop: TrainStop,
    seatInventory: TrainSeatInventory,
    seatPreference: Option[TrainSeatPreference],
    existingAllocations: Vector[TrainSegmentSeatAllocation]
): Either[TrainError, TrainSeatAllocationPlan] =
  val availableSeats =
    trainJourney.seats
      .filter(seat => seat.inventoryId == seatInventory.inventoryId && trainSeatIsBookable(seat))
      .filterNot(seat =>
        existingAllocations.exists(allocation =>
          allocation.seatId == seat.seatId && trainSegmentSeatAllocationOverlaps(allocation, fromStop.sequenceNo, toStop.sequenceNo)
        )
      )
      .sortBy(seat => (seat.carriageNo, seat.rowNo.value, seat.seatCode))

  if availableSeats.size < travelerIds.size then Left(TrainError.TrainSeatAllocationWasNotAvailable(trainJourney.trainId, seatInventory.seatClass, travelerIds.size))
  else
    val preferredSeats =
      seatPreference match
        case Some(TrainSeatPreference.NoPreference) | None => availableSeats
        case Some(preference) =>
          val matchingSeats = availableSeats.filter(_.seatPositionType == trainSeatPositionTypeForPreference(preference))
          if matchingSeats.nonEmpty then matchingSeats else availableSeats
    val adjacencyCandidate = chooseAdjacentTrainSeats(preferredSeats, travelerIds.size).orElse(chooseSameCarriageTrainSeatCluster(preferredSeats, travelerIds.size))
    val selectedSeats = adjacencyCandidate.getOrElse(preferredSeats.take(travelerIds.size))
    val preferenceSatisfied = seatPreference.forall(_ == TrainSeatPreference.NoPreference) ||
      selectedSeats.forall(_.seatPositionType == trainSeatPositionTypeForPreference(seatPreference.get)) ||
      !availableSeats.exists(_.seatPositionType == trainSeatPositionTypeForPreference(seatPreference.get))
    val adjacencySatisfied = selectedSeats.size > 1 && trainSeatsAreAdjacent(selectedSeats)
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

def calculateTrainRefundAmount(
    trainJourney: TrainJourney,
    departureTime: Instant,
    ticketMoney: Money,
    refundRequestedAt: Instant
): Either[TrainError, Money] =
  val offsetBeforeDeparture = Duration.between(refundRequestedAt, departureTime)
  trainJourney.refundPolicySegments
    .find(trainRefundPolicySegmentMatches(_, offsetBeforeDeparture))
    .map(trainRefundPolicySegmentRefundableAmount(_, ticketMoney))
    .toRight(TrainError.TrainRefundPolicyDidNotMatch(trainJourney.trainId, refundRequestedAt))

def trainJourneyWindow(trainJourney: TrainJourney): Either[TrainError, TrainJourneyWindow] =
  for
    departureTime <- trainJourney.stops.headOption.flatMap(_.departureTime).toRight(TrainError.TrainHadTooFewStops(trainJourney.trainId))
    arrivalTime <- trainJourney.stops.lastOption.flatMap(stop => stop.arrivalTime.orElse(stop.departureTime)).toRight(TrainError.TrainHadTooFewStops(trainJourney.trainId))
  yield TrainJourneyWindow(departureTime, arrivalTime)

private def sumTrainSegmentPrice(
    trainJourney: TrainJourney,
    fromStop: TrainStop,
    toStop: TrainStop,
    seatClass: TrainSeatClass
): Either[TrainError, Money] =
  val travelStops = trainJourney.stops.filter(stop => stop.sequenceNo >= fromStop.sequenceNo && stop.sequenceNo <= toStop.sequenceNo).sortBy(_.sequenceNo)
  travelStops
    .sliding(2)
    .toVector
    .traverse {
      case Vector(leftStop, rightStop) =>
        trainJourney.segmentPrices
          .find(price => price.seatClass == seatClass && price.fromStopId == leftStop.stopId && price.toStopId == rightStop.stopId)
          .map(_.price)
          .toRight(TrainError.TrainSegmentPriceWasMissing(trainJourney.trainId, seatClass, leftStop.stationCode, rightStop.stationCode))
      case _ =>
        Left(TrainError.TrainStationOrderWasInvalid(trainJourney.trainId, fromStop.stationCode, toStop.stationCode))
    }
    .map(_.foldLeft(Money.zero(trainJourney.segmentPrices.headOption.map(_.price.currency).getOrElse(Currency.CNY))) { (acc, price) =>
      acc.add(price).fold(throw _, identity)
    })

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

private def trainSeatPositionTypeForPreference(seatPreference: TrainSeatPreference): TrainSeatPositionType =
  seatPreference match
    case TrainSeatPreference.Window => TrainSeatPositionType.Window
    case TrainSeatPreference.Aisle => TrainSeatPositionType.Aisle
    case TrainSeatPreference.Middle => TrainSeatPositionType.Middle
    case TrainSeatPreference.NoPreference => TrainSeatPositionType.Other

private def chooseAdjacentTrainSeats(availableSeats: Vector[TrainSeat], requestedQuantity: Int): Option[Vector[TrainSeat]] =
  availableSeats
    .groupBy(seat => (seat.carriageNo, seat.rowNo.value))
    .values
    .iterator
    .map(_.sortBy(_.seatCode))
    .flatMap(_.sliding(requestedQuantity))
    .find(candidate => candidate.size == requestedQuantity && trainSeatsAreAdjacent(candidate.toVector))
    .map(_.toVector)

private def chooseSameCarriageTrainSeatCluster(availableSeats: Vector[TrainSeat], requestedQuantity: Int): Option[Vector[TrainSeat]] =
  availableSeats
    .groupBy(_.carriageNo)
    .values
    .iterator
    .filter(_.size >= requestedQuantity)
    .map(_.sortBy(seat => (seat.rowNo.value, seat.seatCode)).take(requestedQuantity).toVector)
    .toList
    .sortBy(candidate => candidate.map(_.rowNo.value).max - candidate.map(_.rowNo.value).min)
    .headOption

private def trainSeatsAreAdjacent(candidateSeats: Vector[TrainSeat]): Boolean =
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
