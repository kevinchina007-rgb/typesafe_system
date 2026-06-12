// Advertisement 定义广告模块的数据模型。

package com.typesafe.travel.advertising.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class AdvertisementId(value: String) extends AnyVal
object AdvertisementId:
  given sourceEncoder: Encoder[AdvertisementId] = Encoder.encodeString.contramap(_.value)
  given sourceDecoder: Decoder[AdvertisementId] = Decoder.decodeString.map(AdvertisementId.apply)

final case class AdvertisementReviewId(value: String) extends AnyVal
object AdvertisementReviewId:
  given sourceEncoder: Encoder[AdvertisementReviewId] = Encoder.encodeString.contramap(_.value)
  given sourceDecoder: Decoder[AdvertisementReviewId] = Decoder.decodeString.map(AdvertisementReviewId.apply)

final case class AdvertisementOwnerType(value: String):
  override def toString: String = value

object AdvertisementOwnerType:
  val AirlineManager: AdvertisementOwnerType = AdvertisementOwnerType("Airline")
  val HotelManager: AdvertisementOwnerType = AdvertisementOwnerType("HotelManager")
  val TrainManager: AdvertisementOwnerType = AdvertisementOwnerType("Train")
  val AttractionManager: AdvertisementOwnerType = AdvertisementOwnerType("AttractionManager")
  given sourceEncoder: Encoder[AdvertisementOwnerType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AdvertisementOwnerType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AdvertisementOwnerType =
    value.trim.toLowerCase match
      case "airline" | "airlinemanager" => AirlineManager
      case "train" | "trainmanager" | "railway" | "railwaymanager" => TrainManager
      case "attractionmanager" | "attraction" => AttractionManager
      case _                                     => HotelManager

final case class AdvertisementTargetResourceType(value: String):
  override def toString: String = value

object AdvertisementTargetResourceType:
  val Flight: AdvertisementTargetResourceType = AdvertisementTargetResourceType("Flight")
  val Hotel: AdvertisementTargetResourceType = AdvertisementTargetResourceType("Hotel")
  val Train: AdvertisementTargetResourceType = AdvertisementTargetResourceType("Train")
  val Attraction: AdvertisementTargetResourceType = AdvertisementTargetResourceType("Attraction")
  given sourceEncoder: Encoder[AdvertisementTargetResourceType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AdvertisementTargetResourceType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AdvertisementTargetResourceType =
    value.trim.toLowerCase match
      case "flight"     => Flight
      case "train"      => Train
      case "attraction" => Attraction
      case _             => Hotel

final case class AdvertisementPlacement(value: String):
  override def toString: String = value

object AdvertisementPlacement:
  val FlightBookingPage: AdvertisementPlacement = AdvertisementPlacement("FlightBookingPage")
  val HotelBookingPage: AdvertisementPlacement = AdvertisementPlacement("HotelBookingPage")
  val TrainBookingPage: AdvertisementPlacement = AdvertisementPlacement("TrainBookingPage")
  val AttractionBookingPage: AdvertisementPlacement = AdvertisementPlacement("AttractionBookingPage")
  given sourceEncoder: Encoder[AdvertisementPlacement] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AdvertisementPlacement] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AdvertisementPlacement =
    value.trim.toLowerCase match
      case "flightbookingpage"     => FlightBookingPage
      case "trainbookingpage"      => TrainBookingPage
      case "attractionbookingpage" => AttractionBookingPage
      case _                        => HotelBookingPage

final case class AdvertisementAudience(value: String):
  override def toString: String = value

object AdvertisementAudience:
  val BookingUser: AdvertisementAudience = AdvertisementAudience("BookingUser")
  given sourceEncoder: Encoder[AdvertisementAudience] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AdvertisementAudience] = Decoder.decodeString.map(_ => BookingUser)

final case class AdvertisementReviewStatus(value: String):
  override def toString: String = value

object AdvertisementReviewStatus:
  val Draft: AdvertisementReviewStatus = AdvertisementReviewStatus("Draft")
  val PendingReview: AdvertisementReviewStatus = AdvertisementReviewStatus("PendingReview")
  val Approved: AdvertisementReviewStatus = AdvertisementReviewStatus("Approved")
  val Rejected: AdvertisementReviewStatus = AdvertisementReviewStatus("Rejected")
  given sourceEncoder: Encoder[AdvertisementReviewStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AdvertisementReviewStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AdvertisementReviewStatus =
    value.trim.toLowerCase match
      case "pendingreview" => PendingReview
      case "approved"      => Approved
      case "rejected"      => Rejected
      case _                => Draft

final case class AdvertisementDeliveryStatus(value: String):
  override def toString: String = value

object AdvertisementDeliveryStatus:
  val Scheduled: AdvertisementDeliveryStatus = AdvertisementDeliveryStatus("Scheduled")
  val Active: AdvertisementDeliveryStatus = AdvertisementDeliveryStatus("Active")
  val Paused: AdvertisementDeliveryStatus = AdvertisementDeliveryStatus("Paused")
  val Expired: AdvertisementDeliveryStatus = AdvertisementDeliveryStatus("Expired")
  given sourceEncoder: Encoder[AdvertisementDeliveryStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AdvertisementDeliveryStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AdvertisementDeliveryStatus =
    value.trim.toLowerCase match
      case "active"    => Active
      case "paused"    => Paused
      case "expired"   => Expired
      case _            => Scheduled

final case class AdvertisementReviewDecision(value: String):
  override def toString: String = value

object AdvertisementReviewDecision:
  val Approved: AdvertisementReviewDecision = AdvertisementReviewDecision("Approved")
  val Rejected: AdvertisementReviewDecision = AdvertisementReviewDecision("Rejected")
  given sourceEncoder: Encoder[AdvertisementReviewDecision] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AdvertisementReviewDecision] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AdvertisementReviewDecision =
    value.trim.toLowerCase match
      case "rejected" => Rejected
      case _           => Approved

final case class AdvertisementPriority(value: Int) extends AnyVal
object AdvertisementPriority:
  given sourceEncoder: Encoder[AdvertisementPriority] = Encoder.encodeInt.contramap(_.value)
  given sourceDecoder: Decoder[AdvertisementPriority] = Decoder.decodeInt.map(AdvertisementPriority.apply)

final case class AdvertisementOwner(
    managerId: ManagerId,
    ownerType: AdvertisementOwnerType,
    displayName: String
)
object AdvertisementOwner:
  import AdvertisementSourceJsonCodecs.given
  given sourceEncoder: Encoder[AdvertisementOwner] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementOwner] = deriveDecoder

final case class AdvertisementContent(
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String
)
object AdvertisementContent:
  given sourceEncoder: Encoder[AdvertisementContent] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementContent] = deriveDecoder

final case class AdvertisementTargetResource(
    resourceType: AdvertisementTargetResourceType,
    resourceId: String,
    resourceSummaryTitle: String,
    landingTarget: String
)
object AdvertisementTargetResource:
  given sourceEncoder: Encoder[AdvertisementTargetResource] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementTargetResource] = deriveDecoder

final case class AdvertisementDisplayPolicy(
    placement: AdvertisementPlacement,
    audience: AdvertisementAudience,
    startAt: Instant,
    endAt: Instant
)
object AdvertisementDisplayPolicy:
  import AdvertisementSourceJsonCodecs.given
  given sourceEncoder: Encoder[AdvertisementDisplayPolicy] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementDisplayPolicy] = deriveDecoder

final case class Advertisement(
    advertisementId: AdvertisementId,
    owner: AdvertisementOwner,
    content: AdvertisementContent,
    targetResource: AdvertisementTargetResource,
    reviewStatus: AdvertisementReviewStatus,
    deliveryStatus: AdvertisementDeliveryStatus,
    displayPolicy: AdvertisementDisplayPolicy,
    priority: AdvertisementPriority,
    slotIndex: Option[Int],
    rejectionNote: Option[String],
    createdAt: Instant,
    updatedAt: Instant
)
object Advertisement:
  import AdvertisementSourceJsonCodecs.given
  given sourceEncoder: Encoder[Advertisement] = deriveEncoder
  given sourceDecoder: Decoder[Advertisement] = deriveDecoder

final case class AdvertisementReview(
    reviewId: AdvertisementReviewId,
    advertisementId: AdvertisementId,
    reviewerManagerId: ManagerId,
    decision: AdvertisementReviewDecision,
    reviewNote: Option[String],
    reviewedAt: Instant
)
object AdvertisementReview:
  import AdvertisementSourceJsonCodecs.given
  given sourceEncoder: Encoder[AdvertisementReview] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementReview] = deriveDecoder

sealed trait AdvertisementError extends DomainError:
  def message: String

object AdvertisementError:
  final case class AdvertisementWasNotFound(advertisementId: AdvertisementId) extends AdvertisementError:
    override val message: String = s"Advertisement '${advertisementId.value}' was not found"

  final case class AdvertisementOwnerMismatch(advertisementId: AdvertisementId, managerId: ManagerId) extends AdvertisementError:
    override val message: String = s"Manager '${managerId.value}' cannot modify advertisement '${advertisementId.value}'"

  final case class AdvertisementPlacementDidNotMatchTarget(
      placement: AdvertisementPlacement,
      resourceType: AdvertisementTargetResourceType
  ) extends AdvertisementError:
    override val message: String = s"Placement '${placement.value}' does not match resource '${resourceType.value}'"

  final case class AdvertisementWindowWasInvalid(startAt: Instant, endAt: Instant) extends AdvertisementError:
    override val message: String = s"Advertisement window '$startAt'..'$endAt' is invalid"

  final case class AdvertisementWasNotReadyForReview(advertisementId: AdvertisementId) extends AdvertisementError:
    override val message: String = s"Advertisement '${advertisementId.value}' is not ready for review"

  final case class AdvertisementWasNotPendingReview(advertisementId: AdvertisementId) extends AdvertisementError:
    override val message: String = s"Advertisement '${advertisementId.value}' is not pending review"

  final case class AdvertisementTargetResourceWasMissing(resourceType: AdvertisementTargetResourceType, resourceId: String) extends AdvertisementError:
    override val message: String = s"Advertisement target '${resourceType.value}:${resourceId}' was not found"

  final case class AdvertisementOwnerTypeWasUnsupported(ownerType: String) extends AdvertisementError:
    override val message: String = s"Advertisement owner type '$ownerType' is not supported"

  final case class AdvertisementSlotIndexWasInvalid(slotIndex: Int) extends AdvertisementError:
    override val message: String = s"Advertisement slot '$slotIndex' is invalid"

  final case class AdvertisementWasNotApproved(advertisementId: AdvertisementId) extends AdvertisementError:
    override val message: String = s"Advertisement '${advertisementId.value}' is not approved"

private def validateAdvertisementWindow(startAt: Instant, endAt: Instant): Either[AdvertisementError, Unit] =
  Either.cond(!endAt.isBefore(startAt), (), AdvertisementError.AdvertisementWindowWasInvalid(startAt, endAt))

private def validatePlacementMatchesTarget(
    placement: AdvertisementPlacement,
    targetResourceType: AdvertisementTargetResourceType
): Either[AdvertisementError, Unit] =
  val isValid =
    (placement == AdvertisementPlacement.FlightBookingPage && targetResourceType == AdvertisementTargetResourceType.Flight) ||
      (placement == AdvertisementPlacement.HotelBookingPage && targetResourceType == AdvertisementTargetResourceType.Hotel) ||
      (placement == AdvertisementPlacement.TrainBookingPage && targetResourceType == AdvertisementTargetResourceType.Train) ||
      (placement == AdvertisementPlacement.AttractionBookingPage && targetResourceType == AdvertisementTargetResourceType.Attraction)
  Either.cond(isValid, (), AdvertisementError.AdvertisementPlacementDidNotMatchTarget(placement, targetResourceType))

def createDraftAdvertisement(
    advertisementId: AdvertisementId,
    owner: AdvertisementOwner,
    content: AdvertisementContent,
    targetResource: AdvertisementTargetResource,
    displayPolicy: AdvertisementDisplayPolicy,
    priority: AdvertisementPriority,
    createdAt: Instant
): Either[AdvertisementError, Advertisement] =
  for
    _ <- validateAdvertisementWindow(displayPolicy.startAt, displayPolicy.endAt)
    _ <- validatePlacementMatchesTarget(displayPolicy.placement, targetResource.resourceType)
  yield Advertisement(
    advertisementId = advertisementId,
    owner = owner,
    content = content,
    targetResource = targetResource,
    reviewStatus = AdvertisementReviewStatus.Draft,
    deliveryStatus = AdvertisementDeliveryStatus.Scheduled,
    displayPolicy = displayPolicy,
    priority = priority,
    slotIndex = None,
    rejectionNote = None,
    createdAt = createdAt,
    updatedAt = createdAt
  )

def restorePersistedAdvertisement(
    advertisementId: AdvertisementId,
    owner: AdvertisementOwner,
    content: AdvertisementContent,
    targetResource: AdvertisementTargetResource,
    reviewStatus: AdvertisementReviewStatus,
    deliveryStatus: AdvertisementDeliveryStatus,
    displayPolicy: AdvertisementDisplayPolicy,
    priority: AdvertisementPriority,
    slotIndex: Option[Int],
    rejectionNote: Option[String],
    createdAt: Instant,
    updatedAt: Instant
): Advertisement =
  Advertisement(advertisementId, owner, content, targetResource, reviewStatus, deliveryStatus, displayPolicy, priority, slotIndex, rejectionNote, createdAt, updatedAt)

def restorePersistedAdvertisementReview(
    reviewId: AdvertisementReviewId,
    advertisementId: AdvertisementId,
    reviewerManagerId: ManagerId,
    decision: AdvertisementReviewDecision,
    reviewNote: Option[String],
    reviewedAt: Instant
): AdvertisementReview =
  AdvertisementReview(reviewId, advertisementId, reviewerManagerId, decision, reviewNote, reviewedAt)

def updateDraftAdvertisement(
    advertisement: Advertisement,
    content: AdvertisementContent,
    targetResource: AdvertisementTargetResource,
    displayPolicy: AdvertisementDisplayPolicy,
    priority: AdvertisementPriority,
    updatedAt: Instant
): Either[AdvertisementError, Advertisement] =
  for
    _ <- validateAdvertisementWindow(displayPolicy.startAt, displayPolicy.endAt)
    _ <- validatePlacementMatchesTarget(displayPolicy.placement, targetResource.resourceType)
  yield advertisement.copy(
    content = content,
    targetResource = targetResource,
    displayPolicy = displayPolicy,
    priority = priority,
    slotIndex = None,
    rejectionNote = None,
    reviewStatus = AdvertisementReviewStatus.Draft,
    deliveryStatus = AdvertisementDeliveryStatus.Scheduled,
    updatedAt = updatedAt
  )

def submitAdvertisementForReview(advertisement: Advertisement, updatedAt: Instant): Either[AdvertisementError, Advertisement] =
  for
    _ <- validateAdvertisementWindow(advertisement.displayPolicy.startAt, advertisement.displayPolicy.endAt)
    _ <- validatePlacementMatchesTarget(advertisement.displayPolicy.placement, advertisement.targetResource.resourceType)
  yield advertisement.copy(
    reviewStatus = AdvertisementReviewStatus.PendingReview,
    deliveryStatus = AdvertisementDeliveryStatus.Scheduled,
    updatedAt = updatedAt
  )

def approveAdvertisement(advertisement: Advertisement, updatedAt: Instant): Either[AdvertisementError, Advertisement] =
  advertisement.reviewStatus match
    case AdvertisementReviewStatus.PendingReview =>
      Right(
        advertisement.copy(
          reviewStatus = AdvertisementReviewStatus.Approved,
          deliveryStatus = AdvertisementDeliveryStatus.Active,
          slotIndex = None,
          rejectionNote = None,
          updatedAt = updatedAt
        )
      )
    case _ => Left(AdvertisementError.AdvertisementWasNotPendingReview(advertisement.advertisementId))

def rejectAdvertisement(advertisement: Advertisement, rejectionNote: Option[String], updatedAt: Instant): Either[AdvertisementError, Advertisement] =
  advertisement.reviewStatus match
    case AdvertisementReviewStatus.PendingReview =>
      Right(
        advertisement.copy(
          reviewStatus = AdvertisementReviewStatus.Rejected,
          deliveryStatus = AdvertisementDeliveryStatus.Paused,
          slotIndex = None,
          rejectionNote = rejectionNote,
          updatedAt = updatedAt
        )
      )
    case _ => Left(AdvertisementError.AdvertisementWasNotPendingReview(advertisement.advertisementId))

def pauseAdvertisement(advertisement: Advertisement, updatedAt: Instant): Advertisement =
  advertisement.copy(deliveryStatus = AdvertisementDeliveryStatus.Paused, slotIndex = None, updatedAt = updatedAt)

def resumeAdvertisement(advertisement: Advertisement, updatedAt: Instant): Advertisement =
  advertisement.copy(deliveryStatus = AdvertisementDeliveryStatus.Active, updatedAt = updatedAt)

def clearAdvertisementSlot(advertisement: Advertisement, updatedAt: Instant): Advertisement =
  advertisement.copy(slotIndex = None, updatedAt = updatedAt)

def assignAdvertisementSlot(advertisement: Advertisement, slotIndex: Int, updatedAt: Instant): Either[AdvertisementError, Advertisement] =
  if slotIndex < 1 || slotIndex > 4 then Left(AdvertisementError.AdvertisementSlotIndexWasInvalid(slotIndex))
  else if advertisement.reviewStatus != AdvertisementReviewStatus.Approved then Left(AdvertisementError.AdvertisementWasNotApproved(advertisement.advertisementId))
  else Right(advertisement.copy(slotIndex = Some(slotIndex), deliveryStatus = AdvertisementDeliveryStatus.Active, updatedAt = updatedAt))

def advertisementCanDisplay(advertisement: Advertisement, currentTime: Instant): Boolean =
  advertisement.reviewStatus == AdvertisementReviewStatus.Approved &&
    advertisement.deliveryStatus == AdvertisementDeliveryStatus.Active &&
    advertisement.slotIndex.nonEmpty &&
    !currentTime.isBefore(advertisement.displayPolicy.startAt) &&
    !currentTime.isAfter(advertisement.displayPolicy.endAt)
