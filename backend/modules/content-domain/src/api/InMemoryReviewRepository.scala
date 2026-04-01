package com.typesafe.travel.api.memory

import cats.effect.kernel.{Ref, Sync}
import cats.syntax.all.*
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.shared.kernel.*

final class InMemoryReviewRepository[F[_]: Sync] private (
    reviewState: Ref[F, Map[ReviewId, Review]],
    reviewSequence: Ref[F, Long],
    reviewImageSequence: Ref[F, Long]
) extends ReviewRepository[F]:
  override def nextReviewId: F[ReviewId] =
    reviewSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> ReviewId(s"review-$nextValue")
    }

  override def nextReviewImageId: F[ReviewImageId] =
    reviewImageSequence.modify { currentValue =>
      val nextValue = currentValue + 1
      nextValue -> ReviewImageId(s"review-image-$nextValue")
    }

  override def findReviewById(reviewId: ReviewId): F[Option[Review]] =
    reviewState.get.map(_.get(reviewId))

  override def findReviewByAuthorAndOrderItem(authorUserId: UserId, orderItemId: OrderItemId): F[Option[Review]] =
    reviewState.get.map(_.values.find(review => review.authorUserId == authorUserId && review.orderItemId == orderItemId))

  override def listReviewsByAuthorUserId(authorUserId: UserId): F[List[Review]] =
    reviewState.get.map(_.values.filter(_.authorUserId == authorUserId).toList.sortBy(_.createdAt.toEpochMilli)(Ordering.Long.reverse))

  override def listReviewsByResource(resourceType: ReviewResourceType, resourceId: String): F[List[Review]] =
    reviewState.get.map(_.values.filter(review => review.resourceType == resourceType && review.resourceId == resourceId).toList.sortBy(_.createdAt.toEpochMilli)(Ordering.Long.reverse))

  override def listPublishedReviewsByResource(resourceType: ReviewResourceType, resourceId: String): F[List[Review]] =
    reviewState.get.map(
      _.values
        .filter(review => review.resourceType == resourceType && review.resourceId == resourceId && review.status == ReviewStatus.Published)
        .toList
        .sortBy(_.createdAt.toEpochMilli)(Ordering.Long.reverse)
    )

  override def saveReview(review: Review): F[Review] =
    reviewState.update(_ + (review.reviewId -> review)).as(review)

object InMemoryReviewRepository:
  def create[F[_]: Sync]: InMemoryReviewRepository[F] =
    new InMemoryReviewRepository[F](
      reviewState = Ref.unsafe[F, Map[ReviewId, Review]](Map.empty),
      reviewSequence = Ref.unsafe[F, Long](0L),
      reviewImageSequence = Ref.unsafe[F, Long](0L)
    )
