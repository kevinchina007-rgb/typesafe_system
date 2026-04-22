package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.advertising.domain.*
import com.typesafe.travel.shared.kernel.ManagerId

import java.util.UUID
import scala.collection.concurrent.TrieMap

final class InMemoryAdvertisementRepository[F[_]: Sync] private (
    advertisementState: TrieMap[AdvertisementId, Advertisement],
    reviewState: TrieMap[AdvertisementId, Vector[AdvertisementReview]]
) extends AdvertisementRepository[F]:
  override def nextAdvertisementId: F[AdvertisementId] =
    Sync[F].delay(AdvertisementId(s"advertisement-${UUID.randomUUID().toString.take(12)}"))

  override def nextAdvertisementReviewId: F[AdvertisementReviewId] =
    Sync[F].delay(AdvertisementReviewId(s"advertisement-review-${UUID.randomUUID().toString.take(12)}"))

  override def findAdvertisementById(advertisementId: AdvertisementId): F[Option[Advertisement]] =
    Sync[F].delay(advertisementState.get(advertisementId))

  override def listAdvertisementsByOwner(managerId: ManagerId, ownerType: AdvertisementOwnerType): F[List[Advertisement]] =
    Sync[F].delay(
      advertisementState.values.filter(advertisement => advertisement.owner.managerId == managerId && advertisement.owner.ownerType == ownerType).toList
    )

  override def listAdvertisementsByReviewStatus(reviewStatus: AdvertisementReviewStatus): F[List[Advertisement]] =
    Sync[F].delay(advertisementState.values.filter(_.reviewStatus == reviewStatus).toList)

  override def listAdvertisementsByPlacement(placement: AdvertisementPlacement): F[List[Advertisement]] =
    Sync[F].delay(advertisementState.values.filter(_.displayPolicy.placement == placement).toList)

  override def listReviews(advertisementId: AdvertisementId): F[List[AdvertisementReview]] =
    Sync[F].delay(reviewState.getOrElse(advertisementId, Vector.empty).toList.sortBy(_.reviewedAt))

  override def saveAdvertisement(advertisement: Advertisement): F[Advertisement] =
    Sync[F].delay {
      advertisementState.put(advertisement.advertisementId, advertisement)
      advertisement
    }

  override def saveReview(review: AdvertisementReview): F[AdvertisementReview] =
    Sync[F].delay {
      val existing = reviewState.getOrElse(review.advertisementId, Vector.empty)
      reviewState.put(review.advertisementId, existing :+ review)
      review
    }

object InMemoryAdvertisementRepository:
  def create[F[_]: Sync]: InMemoryAdvertisementRepository[F] =
    new InMemoryAdvertisementRepository[F](TrieMap.empty, TrieMap.empty)
