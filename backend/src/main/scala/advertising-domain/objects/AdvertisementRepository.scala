package com.typesafe.travel.advertising.domain

import cats.effect.kernel.Sync
import com.typesafe.travel.shared.kernel.ManagerId

import java.time.Instant

trait AdvertisementRepository[F[_]]:
  def nextAdvertisementId: F[AdvertisementId]
  def nextAdvertisementReviewId: F[AdvertisementReviewId]
  def findAdvertisementById(advertisementId: AdvertisementId): F[Option[Advertisement]]
  def listAdvertisementsByOwner(managerId: ManagerId, ownerType: AdvertisementOwnerType): F[List[Advertisement]]
  def listAdvertisementsByReviewStatus(reviewStatus: AdvertisementReviewStatus): F[List[Advertisement]]
  def listAdvertisementsByPlacement(placement: AdvertisementPlacement): F[List[Advertisement]]
  def listReviews(advertisementId: AdvertisementId): F[List[AdvertisementReview]]
  def saveAdvertisement(advertisement: Advertisement): F[Advertisement]
  def saveReview(review: AdvertisementReview): F[AdvertisementReview]

object AdvertisementRepository:
  def sortAdvertisementsByPriority(advertisements: List[Advertisement]): List[Advertisement] =
    advertisements.sortBy(advertisement => (-advertisement.priority.value, advertisement.updatedAt.toEpochMilli))
