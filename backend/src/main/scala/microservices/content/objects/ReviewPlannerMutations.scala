package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateReviewPlannerRequest(
    userId: String,
    orderId: String,
    orderItemId: String,
    rating: Int,
    title: String,
    content: String,
    images: List[ContentImagePlannerResponse]
)
object CreateReviewPlannerRequest:
  given sourceEncoder: Encoder[CreateReviewPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateReviewPlannerRequest] = deriveDecoder

final case class UpdateReviewPlannerRequest(
    userId: String,
    reviewId: String,
    rating: Int,
    title: String,
    content: String,
    images: List[ContentImagePlannerResponse]
)
object UpdateReviewPlannerRequest:
  given sourceEncoder: Encoder[UpdateReviewPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateReviewPlannerRequest] = deriveDecoder

final case class DeleteReviewPlannerRequest(
    userId: String,
    reviewId: String
)
object DeleteReviewPlannerRequest:
  given sourceEncoder: Encoder[DeleteReviewPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteReviewPlannerRequest] = deriveDecoder

final case class UploadReviewImagePlannerRequest(
    userId: String,
    originalFileName: String,
    contentType: String,
    base64Content: String
)
object UploadReviewImagePlannerRequest:
  given sourceEncoder: Encoder[UploadReviewImagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadReviewImagePlannerRequest] = deriveDecoder

final case class ReviewPlannerResponse(
    reviewId: String,
    authorUserId: String,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    resourceType: String,
    resourceId: String,
    resourceSummaryTitle: String,
    resourceSummarySubtitle: String,
    orderId: String,
    orderItemId: String,
    rating: Int,
    title: String,
    content: String,
    status: String,
    createdAt: String,
    updatedAt: String,
    isMyReview: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    images: List[ContentImagePlannerResponse]
)
object ReviewPlannerResponse:
  given sourceEncoder: Encoder[ReviewPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ReviewPlannerResponse] = deriveDecoder

final case class ReviewListPlannerResponse(reviews: List[ReviewPlannerResponse])
object ReviewListPlannerResponse:
  given sourceEncoder: Encoder[ReviewListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ReviewListPlannerResponse] = deriveDecoder

final case class ReviewDeletedPlannerResponse(deleted: Boolean)
object ReviewDeletedPlannerResponse:
  given sourceEncoder: Encoder[ReviewDeletedPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ReviewDeletedPlannerResponse] = deriveDecoder

