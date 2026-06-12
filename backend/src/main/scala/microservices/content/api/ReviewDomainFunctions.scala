// ReviewDomainFunctions 定义内容模块的领域辅助函数。

package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

def updateReview(
    review: Review,
    editorUserId: UserId,
    rating: Rating,
    title: String,
    content: String,
    imageRefs: List[ReviewImageRef],
    updatedAt: Instant
): Either[ReviewError, Review] =
  if review.authorUserId != editorUserId then Left(ReviewError.ReviewAuthorMismatch(review.reviewId, editorUserId))
  else
    val normalizedTitle = title.trim
    val normalizedContent = content.trim
    if normalizedTitle.isEmpty then Left(ReviewError.ReviewTitleWasInvalid(review.reviewId))
    else if normalizedContent.isEmpty then Left(ReviewError.ReviewContentWasInvalid(review.reviewId))
    else if imageRefs.lengthCompare(6) > 0 then Left(ReviewError.ReviewImageCountWasInvalid(review.reviewId, 6))
    else
      Right(
        review.copy(
          rating = rating,
          title = normalizedTitle,
          content = normalizedContent,
          imageRefs = imageRefs.sortBy(_.sortOrder),
          updatedAt = updatedAt
        )
      )


def deleteReview(review: Review, editorUserId: UserId, updatedAt: Instant): Either[ReviewError, Review] =
  if review.authorUserId != editorUserId then Left(ReviewError.ReviewAuthorMismatch(review.reviewId, editorUserId))
  else Right(review.copy(status = ReviewStatus.Deleted, updatedAt = updatedAt))


def createPublishedReview(
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
    createdAt: Instant
): Either[ReviewError, Review] =
  val normalizedTitle = title.trim
  val normalizedContent = content.trim
  if normalizedTitle.isEmpty then Left(ReviewError.ReviewTitleWasInvalid(reviewId))
  else if normalizedContent.isEmpty then Left(ReviewError.ReviewContentWasInvalid(reviewId))
  else if imageRefs.lengthCompare(6) > 0 then Left(ReviewError.ReviewImageCountWasInvalid(reviewId, 6))
  else
    Right(
      Review(
        reviewId = reviewId,
        authorUserId = authorUserId,
        resourceType = resourceType,
        resourceId = resourceId,
        orderId = orderId,
        orderItemId = orderItemId,
        rating = rating,
        title = normalizedTitle,
        content = normalizedContent,
        imageRefs = imageRefs.sortBy(_.sortOrder),
        status = ReviewStatus.Published,
        createdAt = createdAt,
        updatedAt = createdAt
      )
    )


def restorePersistedReview(
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
): Review =
  Review(
    reviewId = reviewId,
    authorUserId = authorUserId,
    resourceType = resourceType,
    resourceId = resourceId,
    orderId = orderId,
    orderItemId = orderItemId,
    rating = rating,
    title = title,
    content = content,
    imageRefs = imageRefs.sortBy(_.sortOrder),
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
  )
