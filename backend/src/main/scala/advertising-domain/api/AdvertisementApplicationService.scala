package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.advertising.domain.*
import com.typesafe.travel.attraction.domain.AttractionRepository
import com.typesafe.travel.hotel.domain.HotelRepository
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

final case class AdvertisementReviewView(
    reviewId: String,
    reviewerManagerId: String,
    decision: String,
    reviewNote: Option[String],
    reviewedAt: Instant
)

final case class AdvertisementView(
    advertisementId: String,
    ownerManagerId: String,
    ownerType: String,
    ownerDisplayName: String,
    targetResourceType: String,
    targetResourceId: String,
    resourceSummaryTitle: String,
    landingTarget: String,
    placement: String,
    audience: String,
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    reviewStatus: String,
    deliveryStatus: String,
    priority: Int,
    startAt: Instant,
    endAt: Instant,
    rejectionNote: Option[String],
    createdAt: Instant,
    updatedAt: Instant,
    reviews: List[AdvertisementReviewView]
)

final case class CreateAdvertisementCommand(
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    targetResourceType: String,
    targetResourceId: String,
    placement: String,
    priority: Int,
    startAt: Instant,
    endAt: Instant
)

final case class UpdateAdvertisementCommand(
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    targetResourceId: String,
    placement: String,
    priority: Int,
    startAt: Instant,
    endAt: Instant
)

trait AdvertisementApplicationService[F[_]]:
  def createAdvertisement(ownerManagerId: ManagerId, ownerManagerType: String, ownerDisplayName: String, command: CreateAdvertisementCommand, now: Instant): F[AdvertisementView]
  def listAdvertisementsForOwner(ownerManagerId: ManagerId, ownerManagerType: String): F[List[AdvertisementView]]
  def updateAdvertisement(ownerManagerId: ManagerId, ownerManagerType: String, advertisementId: AdvertisementId, command: UpdateAdvertisementCommand, now: Instant): F[AdvertisementView]
  def submitAdvertisementForReview(ownerManagerId: ManagerId, ownerManagerType: String, advertisementId: AdvertisementId, now: Instant): F[AdvertisementView]
  def pauseAdvertisement(ownerManagerId: ManagerId, ownerManagerType: String, advertisementId: AdvertisementId, now: Instant): F[AdvertisementView]
  def listPendingAdvertisements: F[List[AdvertisementView]]
  def listReviewedAdvertisements: F[List[AdvertisementView]]
  def approveAdvertisement(reviewerManagerId: ManagerId, advertisementId: AdvertisementId, reviewNote: Option[String], now: Instant): F[AdvertisementView]
  def rejectAdvertisement(reviewerManagerId: ManagerId, advertisementId: AdvertisementId, reviewNote: Option[String], now: Instant): F[AdvertisementView]
  def listDeliverableAdvertisements(placement: AdvertisementPlacement, now: Instant): F[List[AdvertisementView]]

final class LiveAdvertisementApplicationService[F[_]: MonadThrow](
    advertisementRepository: AdvertisementRepository[F],
    hotelRepository: HotelRepository[F],
    attractionRepository: AttractionRepository[F]
) extends AdvertisementApplicationService[F]:
  override def createAdvertisement(ownerManagerId: ManagerId, ownerManagerType: String, ownerDisplayName: String, command: CreateAdvertisementCommand, now: Instant): F[AdvertisementView] =
    for
      ownerType <- MonadThrow[F].fromEither(parseOwnerType(ownerManagerType))
      targetResource <- buildTargetResource(ownerType, command.targetResourceId)
      advertisementId <- advertisementRepository.nextAdvertisementId
      advertisement <- MonadThrow[F].fromEither(
        createDraftAdvertisement(
          advertisementId = advertisementId,
          owner = AdvertisementOwner(ownerManagerId, ownerType, ownerDisplayName),
          content = AdvertisementContent(command.title, command.subtitle, command.description, command.imageUrl, command.ctaLabel),
          targetResource = targetResource.copy(landingTarget = buildLandingTarget(targetResource)),
          displayPolicy = AdvertisementDisplayPolicy(parsePlacement(command.placement), AdvertisementAudience.BookingUser, command.startAt, command.endAt),
          priority = AdvertisementPriority(command.priority),
          createdAt = now
        )
      )
      savedAdvertisement <- advertisementRepository.saveAdvertisement(advertisement)
      view <- toAdvertisementView(savedAdvertisement)
    yield view

  override def listAdvertisementsForOwner(ownerManagerId: ManagerId, ownerManagerType: String): F[List[AdvertisementView]] =
    for
      ownerType <- MonadThrow[F].fromEither(parseOwnerType(ownerManagerType))
      advertisements <- advertisementRepository.listAdvertisementsByOwner(ownerManagerId, ownerType)
      views <- advertisements.traverse(toAdvertisementView)
    yield AdvertisementRepository.sortAdvertisementsByPriority(advertisements).map(ad => views.find(_.advertisementId == ad.advertisementId.value).get)

  override def updateAdvertisement(ownerManagerId: ManagerId, ownerManagerType: String, advertisementId: AdvertisementId, command: UpdateAdvertisementCommand, now: Instant): F[AdvertisementView] =
    for
      ownerType <- MonadThrow[F].fromEither(parseOwnerType(ownerManagerType))
      advertisement <- requireOwnedAdvertisement(advertisementId, ownerManagerId, ownerType)
      targetResource <- buildTargetResource(ownerType, command.targetResourceId)
      updatedAdvertisement <- MonadThrow[F].fromEither(
        updateDraftAdvertisement(
          advertisement = advertisement,
          content = AdvertisementContent(command.title, command.subtitle, command.description, command.imageUrl, command.ctaLabel),
          targetResource = targetResource.copy(landingTarget = buildLandingTarget(targetResource)),
          displayPolicy = AdvertisementDisplayPolicy(parsePlacement(command.placement), AdvertisementAudience.BookingUser, command.startAt, command.endAt),
          priority = AdvertisementPriority(command.priority),
          updatedAt = now
        )
      )
      savedAdvertisement <- advertisementRepository.saveAdvertisement(updatedAdvertisement)
      view <- toAdvertisementView(savedAdvertisement)
    yield view

  override def submitAdvertisementForReview(ownerManagerId: ManagerId, ownerManagerType: String, advertisementId: AdvertisementId, now: Instant): F[AdvertisementView] =
    for
      ownerType <- MonadThrow[F].fromEither(parseOwnerType(ownerManagerType))
      advertisement <- requireOwnedAdvertisement(advertisementId, ownerManagerId, ownerType)
      submittedAdvertisement <- MonadThrow[F].fromEither(com.typesafe.travel.advertising.domain.submitAdvertisementForReview(advertisement, now))
      savedAdvertisement <- advertisementRepository.saveAdvertisement(submittedAdvertisement)
      view <- toAdvertisementView(savedAdvertisement)
    yield view

  override def pauseAdvertisement(ownerManagerId: ManagerId, ownerManagerType: String, advertisementId: AdvertisementId, now: Instant): F[AdvertisementView] =
    for
      ownerType <- MonadThrow[F].fromEither(parseOwnerType(ownerManagerType))
      advertisement <- requireOwnedAdvertisement(advertisementId, ownerManagerId, ownerType)
      savedAdvertisement <- advertisementRepository.saveAdvertisement(com.typesafe.travel.advertising.domain.pauseAdvertisement(advertisement, now))
      view <- toAdvertisementView(savedAdvertisement)
    yield view

  override def listPendingAdvertisements: F[List[AdvertisementView]] =
    advertisementRepository.listAdvertisementsByReviewStatus(AdvertisementReviewStatus.PendingReview).flatMap(_.traverse(toAdvertisementView))

  override def listReviewedAdvertisements: F[List[AdvertisementView]] =
    for
      approved <- advertisementRepository.listAdvertisementsByReviewStatus(AdvertisementReviewStatus.Approved)
      rejected <- advertisementRepository.listAdvertisementsByReviewStatus(AdvertisementReviewStatus.Rejected)
      views <- (approved ++ rejected).traverse(toAdvertisementView)
    yield views.sortBy(view => -view.updatedAt.toEpochMilli)

  override def approveAdvertisement(reviewerManagerId: ManagerId, advertisementId: AdvertisementId, reviewNote: Option[String], now: Instant): F[AdvertisementView] =
    for
      advertisement <- requireAdvertisement(advertisementId)
      approvedAdvertisement <- MonadThrow[F].fromEither(com.typesafe.travel.advertising.domain.approveAdvertisement(advertisement, now))
      savedAdvertisement <- advertisementRepository.saveAdvertisement(approvedAdvertisement)
      reviewId <- advertisementRepository.nextAdvertisementReviewId
      _ <- advertisementRepository.saveReview(
        AdvertisementReview(reviewId, advertisementId, reviewerManagerId, AdvertisementReviewDecision.Approved, reviewNote.map(_.trim).filter(_.nonEmpty), now)
      )
      view <- toAdvertisementView(savedAdvertisement)
    yield view

  override def rejectAdvertisement(reviewerManagerId: ManagerId, advertisementId: AdvertisementId, reviewNote: Option[String], now: Instant): F[AdvertisementView] =
    for
      advertisement <- requireAdvertisement(advertisementId)
      rejectedAdvertisement <- MonadThrow[F].fromEither(com.typesafe.travel.advertising.domain.rejectAdvertisement(advertisement, reviewNote.map(_.trim).filter(_.nonEmpty), now))
      savedAdvertisement <- advertisementRepository.saveAdvertisement(rejectedAdvertisement)
      reviewId <- advertisementRepository.nextAdvertisementReviewId
      _ <- advertisementRepository.saveReview(
        AdvertisementReview(reviewId, advertisementId, reviewerManagerId, AdvertisementReviewDecision.Rejected, reviewNote.map(_.trim).filter(_.nonEmpty), now)
      )
      view <- toAdvertisementView(savedAdvertisement)
    yield view

  override def listDeliverableAdvertisements(placement: AdvertisementPlacement, now: Instant): F[List[AdvertisementView]] =
    advertisementRepository
      .listAdvertisementsByPlacement(placement)
      .map(AdvertisementRepository.sortAdvertisementsByPriority)
      .map(_.filter(advertisementCanDisplay(_, now)).take(3))
      .flatMap(_.traverse(toAdvertisementView))

  private def requireAdvertisement(advertisementId: AdvertisementId): F[Advertisement] =
    advertisementRepository.findAdvertisementById(advertisementId).flatMap(_.liftTo[F](AdvertisementError.AdvertisementWasNotFound(advertisementId)))

  private def requireOwnedAdvertisement(
      advertisementId: AdvertisementId,
      ownerManagerId: ManagerId,
      ownerType: AdvertisementOwnerType
  ): F[Advertisement] =
    requireAdvertisement(advertisementId).flatMap { advertisement =>
      if advertisement.owner.managerId == ownerManagerId && advertisement.owner.ownerType == ownerType then advertisement.pure[F]
      else MonadThrow[F].raiseError(AdvertisementError.AdvertisementOwnerMismatch(advertisementId, ownerManagerId))
    }

  private def parseOwnerType(managerType: String): Either[AdvertisementError, AdvertisementOwnerType] =
    managerType.trim.toLowerCase match
      case "hotel" => Right(AdvertisementOwnerType.HotelManager)
      case "attraction" => Right(AdvertisementOwnerType.AttractionManager)
      case _ => Left(AdvertisementError.AdvertisementOwnerTypeWasUnsupported(managerType))

  private def parsePlacement(placement: String): AdvertisementPlacement =
    AdvertisementPlacement.fromText(placement)

  private def buildLandingTarget(targetResource: AdvertisementTargetResource): String =
    targetResource.resourceType match
      case AdvertisementTargetResourceType.Hotel => s"/hotels/${targetResource.resourceId}"
      case AdvertisementTargetResourceType.Attraction => s"/attractions/${targetResource.resourceId}"
      case _ => "/"

  private def buildTargetResource(ownerType: AdvertisementOwnerType, targetResourceId: String): F[AdvertisementTargetResource] =
    ownerType match
      case AdvertisementOwnerType.HotelManager =>
        hotelRepository.findHotelById(HotelId(targetResourceId)).flatMap {
          case Some(hotel) =>
            AdvertisementTargetResource(
              resourceType = AdvertisementTargetResourceType.Hotel,
              resourceId = hotel.hotelId.value,
              resourceSummaryTitle = hotel.hotelName.value,
              landingTarget = s"/hotels/${hotel.hotelId.value}"
            ).pure[F]
          case None => MonadThrow[F].raiseError(AdvertisementError.AdvertisementTargetResourceWasMissing(AdvertisementTargetResourceType.Hotel, targetResourceId))
        }
      case AdvertisementOwnerType.AttractionManager =>
        attractionRepository.findAttractionById(AttractionId(targetResourceId)).flatMap {
          case Some(attraction) =>
            AdvertisementTargetResource(
              resourceType = AdvertisementTargetResourceType.Attraction,
              resourceId = attraction.attractionId.value,
              resourceSummaryTitle = attraction.attractionName,
              landingTarget = s"/attractions/${attraction.attractionId.value}"
            ).pure[F]
          case None => MonadThrow[F].raiseError(AdvertisementError.AdvertisementTargetResourceWasMissing(AdvertisementTargetResourceType.Attraction, targetResourceId))
        }
      case _ => MonadThrow[F].raiseError(AdvertisementError.AdvertisementTargetResourceWasMissing(AdvertisementTargetResourceType.Hotel, targetResourceId))

  private def toAdvertisementView(advertisement: Advertisement): F[AdvertisementView] =
    advertisementRepository.listReviews(advertisement.advertisementId).map { reviews =>
      AdvertisementView(
        advertisementId = advertisement.advertisementId.value,
        ownerManagerId = advertisement.owner.managerId.value,
        ownerType = advertisement.owner.ownerType.toString,
        ownerDisplayName = advertisement.owner.displayName,
        targetResourceType = advertisement.targetResource.resourceType.toString,
        targetResourceId = advertisement.targetResource.resourceId,
        resourceSummaryTitle = advertisement.targetResource.resourceSummaryTitle,
        landingTarget = advertisement.targetResource.landingTarget,
        placement = advertisement.displayPolicy.placement.toString,
        audience = advertisement.displayPolicy.audience.toString,
        title = advertisement.content.title,
        subtitle = advertisement.content.subtitle,
        description = advertisement.content.description,
        imageUrl = advertisement.content.imageUrl,
        ctaLabel = advertisement.content.ctaLabel,
        reviewStatus = advertisement.reviewStatus.toString,
        deliveryStatus = advertisement.deliveryStatus.toString,
        priority = advertisement.priority.value,
        startAt = advertisement.displayPolicy.startAt,
        endAt = advertisement.displayPolicy.endAt,
        rejectionNote = advertisement.rejectionNote,
        createdAt = advertisement.createdAt,
        updatedAt = advertisement.updatedAt,
        reviews = reviews.map(review => AdvertisementReviewView(review.reviewId.value, review.reviewerManagerId.value, review.decision.toString, review.reviewNote, review.reviewedAt))
      )
    }

object LiveAdvertisementApplicationService:
  def apply[F[_]: MonadThrow](
      advertisementRepository: AdvertisementRepository[F],
      hotelRepository: HotelRepository[F],
      attractionRepository: AttractionRepository[F]
  ): LiveAdvertisementApplicationService[F] =
    new LiveAdvertisementApplicationService[F](advertisementRepository, hotelRepository, attractionRepository)
