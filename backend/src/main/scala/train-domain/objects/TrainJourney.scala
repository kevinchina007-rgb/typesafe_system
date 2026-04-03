package com.typesafe.travel.train.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Duration, Instant}

enum TrainJourneyStatus:
  case Draft, OnSale, Closed

enum TrainSeatInventoryStatus:
  case OpenForSale, SoldOut, Closed

enum TrainRefundType:
  case FullRefund, PartialRefund, NonRefundable

enum TrainSeatPositionType:
  case Window, Aisle, Middle, Other

enum TrainSeatStatus:
  case Available, Unavailable

enum TrainSeatPreference:
  case Window, Aisle, Middle, NoPreference

enum TrainError(val message: String) extends DomainError:
  case RailwayManagerWasNotFoundByEmail(primaryEmailAddress: EmailAddress)
      extends TrainError(s"Railway manager '${primaryEmailAddress.value}' was not found")
  case RailwayManagerWasNotFoundById(managerId: ManagerId)
      extends TrainError(s"Railway manager '${managerId.value}' was not found")
  case TrainWasNotFound(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' was not found")
  case TrainWasNotOpenForSale(trainId: TrainId, saleStartsAt: Instant, currentTime: Instant)
      extends TrainError(s"Train '${trainId.value}' is not on sale at $currentTime; sale starts at $saleStartsAt")
  case TrainWasClosed(trainId: TrainId, trainJourneyStatus: TrainJourneyStatus)
      extends TrainError(s"Train '${trainId.value}' is not bookable in status $trainJourneyStatus")
  case TrainHadTooFewStops(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' must contain at least two stops")
  case TrainStopSequenceWasInvalid(trainId: TrainId, stationCode: String)
      extends TrainError(s"Train '${trainId.value}' has an invalid stop sequence near station '$stationCode'")
  case TrainStopWasNotFound(trainId: TrainId, stationCode: TrainStationCode)
      extends TrainError(s"Train '${trainId.value}' does not contain station '${stationCode.value}'")
  case TrainStationOrderWasInvalid(trainId: TrainId, fromStationCode: TrainStationCode, toStationCode: TrainStationCode)
      extends TrainError(
        s"Train '${trainId.value}' requires from station '${fromStationCode.value}' to appear before '${toStationCode.value}'"
      )
  case TrainSegmentPriceWasMissing(trainId: TrainId, seatClass: TrainSeatClass, fromStationCode: TrainStationCode, toStationCode: TrainStationCode)
      extends TrainError(
        s"Train '${trainId.value}' is missing a segment price for seat '${seatClass.value}' from '${fromStationCode.value}' to '${toStationCode.value}'"
      )
  case TrainSeatInventoryWasNotFound(trainId: TrainId, seatClass: TrainSeatClass)
      extends TrainError(s"Train '${trainId.value}' does not have seat inventory '${seatClass.value}'")
  case TrainSeatInventoryWasNotBookable(trainId: TrainId, seatClass: TrainSeatClass, seatInventoryStatus: TrainSeatInventoryStatus)
      extends TrainError(
        s"Train '${trainId.value}' seat inventory '${seatClass.value}' is not bookable in status $seatInventoryStatus"
      )
  case TrainTravelersWereEmpty(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' requires at least one traveler")
  case TrainTravelersContainedDuplicates(trainId: TrainId)
      extends TrainError(s"Train '${trainId.value}' booking contains duplicate travelers")
  case TrainRefundPolicyDidNotMatch(trainId: TrainId, refundRequestedAt: Instant)
      extends TrainError(s"Train '${trainId.value}' has no refund policy matching refund request at $refundRequestedAt")
  case TrainSeatGenerationWasInvalid(trainId: TrainId, reason: String)
      extends TrainError(s"Train '${trainId.value}' seat generation was invalid: $reason")
  case TrainSeatAllocationWasNotAvailable(trainId: TrainId, seatClass: TrainSeatClass, requestedQuantity: Int)
      extends TrainError(s"Train '${trainId.value}' does not have enough available seats in '${seatClass.value}' for '$requestedQuantity' travelers")
  case TrainTravelerWasAlreadyBooked(trainId: TrainId, travelerId: TravelerId)
      extends TrainError(s"Traveler '${travelerId.value}' already has a ticket on train '${trainId.value}'")
  case TrainNumberConflict(trainNumber: TrainNumber, conflictingTrainId: TrainId)
      extends TrainError(s"Train number '${trainNumber.value}' conflicts with overlapping train '${conflictingTrainId.value}'")

final case class TrainStationCode private (value: String) extends AnyVal
object TrainStationCode:
  def create(value: String): Either[SharedValidationError, TrainStationCode] =
    val normalized = value.trim.toUpperCase
    if normalized.matches("^[A-Z0-9]{2,10}$") then Right(TrainStationCode(normalized))
    else Left(SharedValidationError.RequiredFieldWasEmpty("train-station-code"))

  def unsafe(value: String): TrainStationCode =
    create(value).fold(throw _, identity)

final case class TrainStationName private (value: String) extends AnyVal
object TrainStationName:
  def create(value: String): Either[SharedValidationError, TrainStationName] =
    val normalized = value.trim
    if normalized.nonEmpty && normalized.length <= 120 then Right(TrainStationName(normalized))
    else if normalized.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("train-station-name"))
    else Left(SharedValidationError.StringWasTooLong("train-station-name", 120, normalized.length))

  def unsafe(value: String): TrainStationName =
    create(value).fold(throw _, identity)

final case class TrainNumber private (value: String) extends AnyVal
object TrainNumber:
  def create(value: String): Either[SharedValidationError, TrainNumber] =
    val normalized = value.trim.toUpperCase
    if normalized.matches("^[A-Z0-9]{2,20}$") then Right(TrainNumber(normalized))
    else if normalized.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("train-number"))
    else Left(SharedValidationError.StringWasTooLong("train-number", 20, normalized.length))

  def unsafe(value: String): TrainNumber =
    create(value).fold(throw _, identity)

final case class TrainSeatClass private (value: String) extends AnyVal
object TrainSeatClass:
  private val supported = Set("SECOND_CLASS", "FIRST_CLASS", "BUSINESS_CLASS", "SLEEPER")

  def create(value: String): Either[SharedValidationError, TrainSeatClass] =
    val normalized = value.trim.toUpperCase.replace('-', '_').replace(' ', '_')
    if supported.contains(normalized) then Right(TrainSeatClass(normalized))
    else Left(SharedValidationError.CabinClassWasInvalid(normalized))

  def unsafe(value: String): TrainSeatClass =
    create(value).fold(throw _, identity)

final case class TrainSeatRowNo private (value: Int) extends AnyVal
object TrainSeatRowNo:
  def create(value: Int): Either[SharedValidationError, TrainSeatRowNo] =
    if value > 0 then Right(TrainSeatRowNo(value))
    else Left(SharedValidationError.NumberWasOutOfRange("train-seat-row-no", BigDecimal(1), BigDecimal(Int.MaxValue), BigDecimal(value)))

final case class TrainSeatLayoutColumn(
    code: String,
    sortOrder: Int,
    positionType: TrainSeatPositionType
)

final case class TrainSeat(
    seatId: TrainSeatId,
    trainId: TrainId,
    inventoryId: TrainSeatInventoryId,
    seatClass: TrainSeatClass,
    carriageNo: Int,
    rowNo: TrainSeatRowNo,
    seatCode: String,
    seatNo: String,
    seatLabel: String,
    seatPositionType: TrainSeatPositionType,
    seatStatus: TrainSeatStatus
):
  def isBookable: Boolean = seatStatus == TrainSeatStatus.Available

final case class TrainTravelerSeatAssignment(
    travelerId: TravelerId,
    seatId: TrainSeatId,
    carriageNo: Int,
    seatNo: String,
    seatLabel: String,
    seatPositionType: TrainSeatPositionType
)

final case class TrainSegmentSeatAllocation(
    seatId: TrainSeatId,
    orderId: OrderId,
    orderItemId: OrderItemId,
    fromStopSequenceNo: Int,
    toStopSequenceNo: Int
):
  def overlaps(fromSequenceNo: Int, toSequenceNo: Int): Boolean =
    fromStopSequenceNo < toSequenceNo && toStopSequenceNo > fromSequenceNo

final case class RefundRate private (value: BigDecimal) extends AnyVal
object RefundRate:
  def create(value: BigDecimal): Either[SharedValidationError, RefundRate] =
    if value >= 0 && value <= 1 then Right(RefundRate(value))
    else Left(SharedValidationError.NumberWasOutOfRange("refund-rate", BigDecimal(0), BigDecimal(1), value))

  def unsafe(value: BigDecimal): RefundRate =
    create(value).fold(throw _, identity)

final case class TrainStop(
    stopId: TrainStopId,
    stationCode: TrainStationCode,
    stationName: TrainStationName,
    sequenceNo: Int,
    arrivalTime: Option[Instant],
    departureTime: Option[Instant]
)

final case class TrainSeatInventory(
    inventoryId: TrainSeatInventoryId,
    trainId: TrainId,
    seatClass: TrainSeatClass,
    totalSeats: SeatCount,
    saleableSeats: SeatCount,
    seatInventoryStatus: TrainSeatInventoryStatus
):
  def ensureBookable: Either[TrainError, TrainSeatInventory] =
    if seatInventoryStatus == TrainSeatInventoryStatus.OpenForSale && saleableSeats.value > 0 then Right(this)
    else Left(TrainError.TrainSeatInventoryWasNotBookable(trainId, seatClass, seatInventoryStatus))

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

final case class TrainJourney private[domain] (
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
      case TrainSeatPreference.Window      => TrainSeatPositionType.Window
      case TrainSeatPreference.Aisle       => TrainSeatPositionType.Aisle
      case TrainSeatPreference.Middle      => TrainSeatPositionType.Middle
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
          case _                           => true
        }

