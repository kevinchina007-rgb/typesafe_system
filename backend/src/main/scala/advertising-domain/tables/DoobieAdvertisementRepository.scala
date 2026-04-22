package com.typesafe.travel.persistence.advertising

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.advertising.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.ManagerId
import doobie.*
import doobie.implicits.*

import java.time.Instant
import java.util.UUID

final class DoobieAdvertisementRepository[F[_]: Async](transactor: Transactor[F]) extends AdvertisementRepository[F]:
  override def nextAdvertisementId: F[AdvertisementId] =
    Sync[F].delay(AdvertisementId(s"advertisement-${UUID.randomUUID().toString.take(12)}"))

  override def nextAdvertisementReviewId: F[AdvertisementReviewId] =
    Sync[F].delay(AdvertisementReviewId(s"advertisement-review-${UUID.randomUUID().toString.take(12)}"))

  override def findAdvertisementById(advertisementId: AdvertisementId): F[Option[Advertisement]] =
    selectAdvertisements(fr"advertisement_id = ${advertisementId.value}").option.transact(transactor).map(_.map(toAdvertisement))

  override def listAdvertisementsByOwner(managerId: ManagerId, ownerType: AdvertisementOwnerType): F[List[Advertisement]] =
    selectAdvertisements(fr"owner_manager_id = ${managerId.value} and owner_type = ${ownerType.toString}").to[List].transact(transactor).map(_.map(toAdvertisement))

  override def listAdvertisementsByReviewStatus(reviewStatus: AdvertisementReviewStatus): F[List[Advertisement]] =
    selectAdvertisements(fr"review_status = ${reviewStatus.toString}").to[List].transact(transactor).map(_.map(toAdvertisement))

  override def listAdvertisementsByPlacement(placement: AdvertisementPlacement): F[List[Advertisement]] =
    selectAdvertisements(fr"placement = ${placement.toString}").to[List].transact(transactor).map(_.map(toAdvertisement))

  override def listReviews(advertisementId: AdvertisementId): F[List[AdvertisementReview]] =
    sql"""
      select review_id, advertisement_id, reviewer_manager_id, decision, review_note, reviewed_at
      from advertisement_reviews
      where advertisement_id = ${advertisementId.value}
      order by reviewed_at desc
    """.query[(String, String, String, String, Option[String], Instant)].to[List].transact(transactor).map(_.map(toReview))

  override def saveAdvertisement(advertisement: Advertisement): F[Advertisement] =
    (
      for
        updatedRows <- sql"""
          update advertisements
          set owner_manager_id = ${advertisement.owner.managerId.value},
              owner_type = ${advertisement.owner.ownerType.toString},
              owner_display_name = ${advertisement.owner.displayName},
              target_resource_type = ${advertisement.targetResource.resourceType.toString},
              target_resource_id = ${advertisement.targetResource.resourceId},
              resource_summary_title = ${advertisement.targetResource.resourceSummaryTitle},
              landing_target = ${advertisement.targetResource.landingTarget},
              placement = ${advertisement.displayPolicy.placement.toString},
              audience = ${advertisement.displayPolicy.audience.toString},
              title = ${advertisement.content.title},
              subtitle = ${advertisement.content.subtitle},
              description = ${advertisement.content.description},
              image_url = ${advertisement.content.imageUrl},
              cta_label = ${advertisement.content.ctaLabel},
              review_status = ${advertisement.reviewStatus.toString},
              delivery_status = ${advertisement.deliveryStatus.toString},
              priority = ${advertisement.priority.value},
              start_at = ${advertisement.displayPolicy.startAt},
              end_at = ${advertisement.displayPolicy.endAt},
              rejection_note = ${advertisement.rejectionNote},
              created_at = ${advertisement.createdAt},
              updated_at = ${advertisement.updatedAt}
          where advertisement_id = ${advertisement.advertisementId.value}
        """.update.run
        _ <- if updatedRows > 0 then ().pure[ConnectionIO]
        else sql"""
          insert into advertisements(
            advertisement_id, owner_manager_id, owner_type, owner_display_name,
            target_resource_type, target_resource_id, resource_summary_title, landing_target,
            placement, audience, title, subtitle, description, image_url, cta_label,
            review_status, delivery_status, priority, start_at, end_at, rejection_note, created_at, updated_at
          ) values (
            ${advertisement.advertisementId.value},
            ${advertisement.owner.managerId.value},
            ${advertisement.owner.ownerType.toString},
            ${advertisement.owner.displayName},
            ${advertisement.targetResource.resourceType.toString},
            ${advertisement.targetResource.resourceId},
            ${advertisement.targetResource.resourceSummaryTitle},
            ${advertisement.targetResource.landingTarget},
            ${advertisement.displayPolicy.placement.toString},
            ${advertisement.displayPolicy.audience.toString},
            ${advertisement.content.title},
            ${advertisement.content.subtitle},
            ${advertisement.content.description},
            ${advertisement.content.imageUrl},
            ${advertisement.content.ctaLabel},
            ${advertisement.reviewStatus.toString},
            ${advertisement.deliveryStatus.toString},
            ${advertisement.priority.value},
            ${advertisement.displayPolicy.startAt},
            ${advertisement.displayPolicy.endAt},
            ${advertisement.rejectionNote},
            ${advertisement.createdAt},
            ${advertisement.updatedAt}
          )
        """.update.run.void
      yield advertisement
    ).transact(transactor)

  override def saveReview(review: AdvertisementReview): F[AdvertisementReview] =
    sql"""
      insert into advertisement_reviews(review_id, advertisement_id, reviewer_manager_id, decision, review_note, reviewed_at)
      values (
        ${review.reviewId.value},
        ${review.advertisementId.value},
        ${review.reviewerManagerId.value},
        ${review.decision.toString},
        ${review.reviewNote},
        ${review.reviewedAt}
      )
    """.update.run.transact(transactor).as(review)

  private type AdvertisementRow = (
      String, String, String, String, String, String, String, String, String, String,
      String, String, String, Option[String], String, String, String, Int, Instant, Instant, Option[String], Instant, Instant
  )

  private def selectAdvertisements(whereFragment: Fragment): Query0[AdvertisementRow] =
    (fr"""
      select advertisement_id, owner_manager_id, owner_type, owner_display_name,
             target_resource_type, target_resource_id, resource_summary_title, landing_target,
             placement, audience, title, subtitle, description, image_url, cta_label,
             review_status, delivery_status, priority, start_at, end_at, rejection_note, created_at, updated_at
      from advertisements
      where
    """ ++ whereFragment ++ fr"order by priority desc, updated_at desc").query[AdvertisementRow]

  private def toAdvertisement(row: AdvertisementRow): Advertisement =
    val (
      advertisementId,
      ownerManagerId,
      ownerType,
      ownerDisplayName,
      targetResourceType,
      targetResourceId,
      resourceSummaryTitle,
      landingTarget,
      placement,
      audience,
      title,
      subtitle,
      description,
      imageUrl,
      ctaLabel,
      reviewStatus,
      deliveryStatus,
      priority,
      startAt,
      endAt,
      rejectionNote,
      createdAt,
      updatedAt
    ) = row

    restorePersistedAdvertisement(
      advertisementId = AdvertisementId(advertisementId),
      owner = AdvertisementOwner(ManagerId(ownerManagerId), AdvertisementOwnerType.fromText(ownerType), ownerDisplayName),
      content = AdvertisementContent(title, subtitle, description, imageUrl, ctaLabel),
      targetResource = AdvertisementTargetResource(AdvertisementTargetResourceType.fromText(targetResourceType), targetResourceId, resourceSummaryTitle, landingTarget),
      reviewStatus = AdvertisementReviewStatus.fromText(reviewStatus),
      deliveryStatus = AdvertisementDeliveryStatus.fromText(deliveryStatus),
      displayPolicy = AdvertisementDisplayPolicy(AdvertisementPlacement.fromText(placement), AdvertisementAudience.BookingUser, startAt, endAt),
      priority = AdvertisementPriority(priority),
      rejectionNote = rejectionNote,
      createdAt = createdAt,
      updatedAt = updatedAt
    )

  private def toReview(row: (String, String, String, String, Option[String], Instant)): AdvertisementReview =
    restorePersistedAdvertisementReview(
      reviewId = AdvertisementReviewId(row._1),
      advertisementId = AdvertisementId(row._2),
      reviewerManagerId = ManagerId(row._3),
      decision = AdvertisementReviewDecision.fromText(row._4),
      reviewNote = row._5,
      reviewedAt = row._6
    )

object DoobieAdvertisementRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieAdvertisementRepository[F] =
    new DoobieAdvertisementRepository[F](transactor)
