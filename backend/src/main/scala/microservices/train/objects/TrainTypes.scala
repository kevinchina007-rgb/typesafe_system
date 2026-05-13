package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.Instant

final case class TrainJourneyStatus(value: String):
  override def toString: String = value

object TrainJourneyStatus:
  val Draft: TrainJourneyStatus = TrainJourneyStatus("Draft")
  val OnSale: TrainJourneyStatus = TrainJourneyStatus("OnSale")
  val Closed: TrainJourneyStatus = TrainJourneyStatus("Closed")

  def fromText(value: String): TrainJourneyStatus =
    value.trim.toLowerCase match
      case "draft" => Draft
      case "onsale" | "on_sale" => OnSale
      case "closed" => Closed
      case _ => Draft

  given sourceEncoder: Encoder[TrainJourneyStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainJourneyStatus] = Decoder.decodeString.map(fromText)

final case class TrainSeatInventoryStatus(value: String):
  override def toString: String = value

object TrainSeatInventoryStatus:
  val OpenForSale: TrainSeatInventoryStatus = TrainSeatInventoryStatus("OpenForSale")
  val SoldOut: TrainSeatInventoryStatus = TrainSeatInventoryStatus("SoldOut")
  val Closed: TrainSeatInventoryStatus = TrainSeatInventoryStatus("Closed")

  def fromText(value: String): TrainSeatInventoryStatus =
    value.trim.toLowerCase match
      case "openforsale" | "open_for_sale" => OpenForSale
      case "soldout" | "sold_out" => SoldOut
      case "closed" => Closed
      case _ => Closed

  given sourceEncoder: Encoder[TrainSeatInventoryStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatInventoryStatus] = Decoder.decodeString.map(fromText)

final case class TrainRefundType(value: String):
  override def toString: String = value

object TrainRefundType:
  val FullRefund: TrainRefundType = TrainRefundType("FullRefund")
  val PartialRefund: TrainRefundType = TrainRefundType("PartialRefund")
  val NonRefundable: TrainRefundType = TrainRefundType("NonRefundable")

  def fromText(value: String): TrainRefundType =
    value.trim.toLowerCase match
      case "fullrefund" | "full_refund" | "full" => FullRefund
      case "partialrefund" | "partial_refund" | "partial" => PartialRefund
      case "nonrefundable" | "non_refundable" | "none" => NonRefundable
      case _ => NonRefundable

  given sourceEncoder: Encoder[TrainRefundType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainRefundType] = Decoder.decodeString.map(fromText)

final case class TrainSeatPositionType(value: String):
  override def toString: String = value

object TrainSeatPositionType:
  val Window: TrainSeatPositionType = TrainSeatPositionType("Window")
  val Aisle: TrainSeatPositionType = TrainSeatPositionType("Aisle")
  val Middle: TrainSeatPositionType = TrainSeatPositionType("Middle")
  val Other: TrainSeatPositionType = TrainSeatPositionType("Other")

  def fromText(value: String): TrainSeatPositionType =
    value.trim.toLowerCase match
      case "window" => Window
      case "aisle" => Aisle
      case "middle" => Middle
      case "other" => Other
      case _ => Other

  given sourceEncoder: Encoder[TrainSeatPositionType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatPositionType] = Decoder.decodeString.map(fromText)

final case class TrainSeatStatus(value: String):
  override def toString: String = value

object TrainSeatStatus:
  val Available: TrainSeatStatus = TrainSeatStatus("Available")
  val Unavailable: TrainSeatStatus = TrainSeatStatus("Unavailable")

  def fromText(value: String): TrainSeatStatus =
    value.trim.toLowerCase match
      case "available" => Available
      case "unavailable" => Unavailable
      case _ => Unavailable

  given sourceEncoder: Encoder[TrainSeatStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatStatus] = Decoder.decodeString.map(fromText)

final case class TrainSeatPreference(value: String):
  override def toString: String = value

object TrainSeatPreference:
  val Window: TrainSeatPreference = TrainSeatPreference("Window")
  val Aisle: TrainSeatPreference = TrainSeatPreference("Aisle")
  val Middle: TrainSeatPreference = TrainSeatPreference("Middle")
  val NoPreference: TrainSeatPreference = TrainSeatPreference("NoPreference")

  def fromText(value: String): TrainSeatPreference =
    value.trim.toLowerCase match
      case "window" => Window
      case "aisle" => Aisle
      case "middle" => Middle
      case "nopreference" | "no_preference" => NoPreference
      case _ => NoPreference

  given sourceEncoder: Encoder[TrainSeatPreference] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TrainSeatPreference] = Decoder.decodeString.map(fromText)

sealed trait TrainError extends DomainError:
  def message: String

object TrainError:
  final case class RailwayManagerWasNotFoundByEmail(primaryEmailAddress: EmailAddress) extends TrainError:
    override val message: String = s"Railway manager '${primaryEmailAddress.value}' was not found"

  final case class RailwayManagerWasNotFoundById(managerId: ManagerId) extends TrainError:
    override val message: String = s"Railway manager '${managerId.value}' was not found"

  final case class TrainWasNotFound(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' was not found"

  final case class TrainWasNotOpenForSale(trainId: TrainId, saleStartsAt: Instant, currentTime: Instant) extends TrainError:
    override val message: String = s"Train '${trainId.value}' is not on sale at $currentTime; sale starts at $saleStartsAt"

  final case class TrainWasClosed(trainId: TrainId, trainJourneyStatus: TrainJourneyStatus) extends TrainError:
    override val message: String = s"Train '${trainId.value}' is not bookable in status $trainJourneyStatus"

  final case class TrainHadTooFewStops(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' must contain at least two stops"

  final case class TrainStopSequenceWasInvalid(trainId: TrainId, stationCode: String) extends TrainError:
    override val message: String = s"Train '${trainId.value}' has an invalid stop sequence near station '$stationCode'"

  final case class TrainStopWasNotFound(trainId: TrainId, stationCode: TrainStationCode) extends TrainError:
    override val message: String = s"Train '${trainId.value}' does not contain station '${stationCode.value}'"

  final case class TrainStationOrderWasInvalid(trainId: TrainId, fromStationCode: TrainStationCode, toStationCode: TrainStationCode) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' requires from station '${fromStationCode.value}' to appear before '${toStationCode.value}'"

  final case class TrainSegmentPriceWasMissing(trainId: TrainId, seatClass: TrainSeatClass, fromStationCode: TrainStationCode, toStationCode: TrainStationCode) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' is missing a segment price for seat '${seatClass.value}' from '${fromStationCode.value}' to '${toStationCode.value}'"

  final case class TrainSeatInventoryWasNotFound(trainId: TrainId, seatClass: TrainSeatClass) extends TrainError:
    override val message: String = s"Train '${trainId.value}' does not have seat inventory '${seatClass.value}'"

  final case class TrainSeatInventoryWasNotBookable(trainId: TrainId, seatClass: TrainSeatClass, seatInventoryStatus: TrainSeatInventoryStatus) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' seat inventory '${seatClass.value}' is not bookable in status $seatInventoryStatus"

  final case class TrainTravelersWereEmpty(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' requires at least one traveler"

  final case class TrainTravelersContainedDuplicates(trainId: TrainId) extends TrainError:
    override val message: String = s"Train '${trainId.value}' booking contains duplicate travelers"

  final case class TrainRefundPolicyDidNotMatch(trainId: TrainId, refundRequestedAt: Instant) extends TrainError:
    override val message: String = s"Train '${trainId.value}' has no refund policy matching refund request at $refundRequestedAt"

  final case class TrainSeatGenerationWasInvalid(trainId: TrainId, reason: String) extends TrainError:
    override val message: String = s"Train '${trainId.value}' seat generation was invalid: $reason"

  final case class TrainSeatAllocationWasNotAvailable(trainId: TrainId, seatClass: TrainSeatClass, requestedQuantity: Int) extends TrainError:
    override val message: String =
      s"Train '${trainId.value}' does not have enough available seats in '${seatClass.value}' for '$requestedQuantity' travelers"

  final case class TrainTravelerWasAlreadyBooked(trainId: TrainId, travelerId: TravelerId) extends TrainError:
    override val message: String = s"Traveler '${travelerId.value}' already has a ticket on train '${trainId.value}'"

  final case class TrainNumberConflict(trainNumber: TrainNumber, conflictingTrainId: TrainId) extends TrainError:
    override val message: String = s"Train number '${trainNumber.value}' conflicts with overlapping train '${conflictingTrainId.value}'"
