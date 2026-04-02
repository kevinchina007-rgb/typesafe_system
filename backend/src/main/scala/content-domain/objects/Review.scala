package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum ReviewResourceType:
  case Flight, Hotel, Train, Attraction

enum ReviewStatus:
  case Published, Deleted

enum ReviewError(val message: String) extends DomainError:
  case ReviewWasNotFound(reviewId: ReviewId)
      extends ReviewError(s"Review '${reviewId.value}' was not found")
  case ReviewTitleWasInvalid(reviewId: ReviewId)
      extends ReviewError(s"Review '${reviewId.value}' must have a title")
  case ReviewContentWasInvalid(reviewId: ReviewId)
      extends ReviewError(s"Review '${reviewId.value}' must have content")
  case ReviewAlreadyExistsForOrderItem(authorUserId: UserId, orderItemId: OrderItemId)
      extends ReviewError(s"User '${authorUserId.value}' has already reviewed order item '${orderItemId.value}'")
  case ReviewWasNotAllowed(authorUserId: UserId, orderItemId: OrderItemId, reason: String)
      extends ReviewError(s"User '${authorUserId.value}' cannot review order item '${orderItemId.value}': $reason")
  case ReviewAuthorMismatch(reviewId: ReviewId, userId: UserId)
      extends ReviewError(s"User '${userId.value}' cannot manage review '${reviewId.value}'")
  case ReviewImageCountWasInvalid(reviewId: ReviewId, maximumImageCount: Int)
      extends ReviewError(s"Review '${reviewId.value}' exceeded the maximum of $maximumImageCount images")

final case class ReviewImageRef(
    imageId: ReviewImageId,
    publicUrl: String,
    originalFileName: String,
    sortOrder: Int,
    createdAt: Instant
)

final case class Review private[domain] (
    reviewId: ReviewId,
    authorUserId: UserId,
    resourceType: ReviewResourceType,
    resourceId: String,
    orderId: OrderId,
    orderItemId: OrderItemId,
    rating: Rating,
    title: String,
    content: String,
    imageRefs: List[ReviewImageRef],
    status: ReviewStatus,
    createdAt: Instant,
    updatedAt: Instant
)

