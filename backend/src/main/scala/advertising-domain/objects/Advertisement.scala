package com.typesafe.travel.advertising.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

final case class AdvertisementId(value: String) extends AnyVal
final case class AdvertisementReviewId(value: String) extends AnyVal

final case class AdvertisementOwnerType(value: String):
  override def toString: String = value

object AdvertisementOwnerType:
  val HotelManager: AdvertisementOwnerType = AdvertisementOwnerType("HotelManager")
  val AttractionManager: AdvertisementOwnerType = AdvertisementOwnerType("AttractionManager")

  def fromText(value: String): AdvertisementOwnerType =
    value.trim.toLowerCase match
      case "attractionmanager" | "attraction" => AttractionManager
      case _                                     => HotelManager

final case class AdvertisementTargetResourceType(value: String):
  override def toString: String = value

object AdvertisementTargetResourceType:
  val Hotel: AdvertisementTargetResourceType = AdvertisementTargetResourceType("Hotel")
  val Attraction: AdvertisementTargetResourceType = AdvertisementTargetResourceType("Attraction")

  def fromText(value: String): AdvertisementTargetResourceType =
    value.trim.toLowerCase match
      case "attraction" => Attraction
      case _             => Hotel

final case class AdvertisementPlacement(value: String):
  override def toString: String = value

object AdvertisementPlacement:
  val HotelBookingPage: AdvertisementPlacement = AdvertisementPlacement("HotelBookingPage")
  val AttractionBookingPage: AdvertisementPlacement = AdvertisementPlacement("AttractionBookingPage")

  def fromText(value: String): AdvertisementPlacement =
    value.trim.toLowerCase match
      case "attractionbookingpage" => AttractionBookingPage
      case _                        => HotelBookingPage

final case class AdvertisementAudience(value: String):
  override def toString: String = value

object AdvertisementAudience:
  val BookingUser: AdvertisementAudience = AdvertisementAudience("BookingUser")

final case class AdvertisementReviewStatus(value: String):
  override def toString: String = value

object AdvertisementReviewStatus:
  val Draft: AdvertisementReviewStatus = AdvertisementReviewStatus("Draft")
  val PendingReview: AdvertisementReviewStatus = AdvertisementReviewStatus("PendingReview")
  val Approved: AdvertisementReviewStatus = AdvertisementReviewStatus("Approved")
  val Rejected: AdvertisementReviewStatus = AdvertisementReviewStatus("Rejected")

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

  def fromText(value: String): AdvertisementReviewDecision =
    value.trim.toLowerCase match
      case "rejected" => Rejected
      case _           => Approved

final case class AdvertisementPriority(value: Int) extends AnyVal

final case class AdvertisementOwner(
    managerId: ManagerId,
    ownerType: AdvertisementOwnerType,
    displayName: String
)

final case class AdvertisementContent(
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String
)

final case class AdvertisementTargetResource(
    resourceType: AdvertisementTargetResourceType,
    resourceId: String,
    resourceSummaryTitle: String,
    landingTarget: String
)

final case class AdvertisementDisplayPolicy(
    placement: AdvertisementPlacement,
    audience: AdvertisementAudience,
    startAt: Instant,
    endAt: Instant
)

final case class Advertisement(
    advertisementId: AdvertisementId,
    owner: AdvertisementOwner,
    content: AdvertisementContent,
    targetResource: AdvertisementTargetResource,
    reviewStatus: AdvertisementReviewStatus,
    deliveryStatus: AdvertisementDeliveryStatus,
    displayPolicy: AdvertisementDisplayPolicy,
    priority: AdvertisementPriority,
    rejectionNote: Option[String],
    createdAt: Instant,
    updatedAt: Instant
)

final case class AdvertisementReview(
    reviewId: AdvertisementReviewId,
    advertisementId: AdvertisementId,
    reviewerManagerId: ManagerId,
    decision: AdvertisementReviewDecision,
    reviewNote: Option[String],
    reviewedAt: Instant
)

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

private def validateAdvertisementWindow(startAt: Instant, endAt: Instant): Either[AdvertisementError, Unit] =
  Either.cond(!endAt.isBefore(startAt), (), AdvertisementError.AdvertisementWindowWasInvalid(startAt, endAt))

private def validatePlacementMatchesTarget(
    placement: AdvertisementPlacement,
    targetResourceType: AdvertisementTargetResourceType
): Either[AdvertisementError, Unit] =
  val isValid =
    (placement == AdvertisementPlacement.HotelBookingPage && targetResourceType == AdvertisementTargetResourceType.Hotel) ||
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
    rejectionNote: Option[String],
    createdAt: Instant,
    updatedAt: Instant
): Advertisement =
  Advertisement(advertisementId, owner, content, targetResource, reviewStatus, deliveryStatus, displayPolicy, priority, rejectionNote, createdAt, updatedAt)

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
          rejectionNote = rejectionNote,
          updatedAt = updatedAt
        )
      )
    case _ => Left(AdvertisementError.AdvertisementWasNotPendingReview(advertisement.advertisementId))

def pauseAdvertisement(advertisement: Advertisement, updatedAt: Instant): Advertisement =
  advertisement.copy(deliveryStatus = AdvertisementDeliveryStatus.Paused, updatedAt = updatedAt)

def resumeAdvertisement(advertisement: Advertisement, updatedAt: Instant): Advertisement =
  advertisement.copy(deliveryStatus = AdvertisementDeliveryStatus.Active, updatedAt = updatedAt)

def advertisementCanDisplay(advertisement: Advertisement, currentTime: Instant): Boolean =
  advertisement.reviewStatus == AdvertisementReviewStatus.Approved &&
    advertisement.deliveryStatus == AdvertisementDeliveryStatus.Active &&
    !currentTime.isBefore(advertisement.displayPolicy.startAt) &&
    !currentTime.isAfter(advertisement.displayPolicy.endAt)
