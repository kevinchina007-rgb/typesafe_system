package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

trait ReviewRepository[F[_]]:
  def nextReviewId: F[ReviewId]
  def nextReviewImageId: F[ReviewImageId]
  def findReviewById(reviewId: ReviewId): F[Option[Review]]
  def findReviewByAuthorAndOrderItem(authorUserId: UserId, orderItemId: OrderItemId): F[Option[Review]]
  def listReviewsByAuthorUserId(authorUserId: UserId): F[List[Review]]
  def listReviewsByResource(resourceType: ReviewResourceType, resourceId: String): F[List[Review]]
  def listPublishedReviewsByResource(resourceType: ReviewResourceType, resourceId: String): F[List[Review]]
  def saveReview(review: Review): F[Review]
