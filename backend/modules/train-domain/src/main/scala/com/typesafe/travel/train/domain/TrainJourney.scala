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

final case class TrainJourney private (
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

object TrainJourney:
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
    validateStops(trainId, stops).map { _ =>
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

  private def validateStops(trainId: TrainId, stops: Vector[TrainStop]): Either[TrainError, Unit] =
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
