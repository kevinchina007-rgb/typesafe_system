package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ContentImagePlannerResponse(
    imageId: String,
    publicUrl: String,
    originalFileName: String,
    sortOrder: Int,
    createdAt: String
)

object ContentImagePlannerResponse:
  given sourceEncoder: Encoder[ContentImagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ContentImagePlannerResponse] = deriveDecoder

final case class ListMyReviewsPlannerRequest(userId: String)

object ListMyReviewsPlannerRequest:
  given sourceEncoder: Encoder[ListMyReviewsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListMyReviewsPlannerRequest] = deriveDecoder

final case class ListReviewsByResourcePlannerRequest(
    userId: String,
    resourceType: String,
    resourceId: String
)

object ListReviewsByResourcePlannerRequest:
  given sourceEncoder: Encoder[ListReviewsByResourcePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListReviewsByResourcePlannerRequest] = deriveDecoder

final case class GetReviewSummaryPlannerRequest(
    userId: String,
    resourceType: String,
    resourceId: String
)

object GetReviewSummaryPlannerRequest:
  given sourceEncoder: Encoder[GetReviewSummaryPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetReviewSummaryPlannerRequest] = deriveDecoder

final case class CheckReviewEligibilityPlannerRequest(
    userId: String,
    orderItemId: String
)

object CheckReviewEligibilityPlannerRequest:
  given sourceEncoder: Encoder[CheckReviewEligibilityPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CheckReviewEligibilityPlannerRequest] = deriveDecoder

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

final case class ResourceReviewSummaryPlannerResponse(
    resourceType: String,
    resourceId: String,
    averageRating: String,
    reviewCount: Int
)

object ResourceReviewSummaryPlannerResponse:
  given sourceEncoder: Encoder[ResourceReviewSummaryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ResourceReviewSummaryPlannerResponse] = deriveDecoder

final case class ReviewEligibilityPlannerResponse(
    orderId: String,
    orderItemId: String,
    canReview: Boolean,
    alreadyReviewed: Boolean,
    reason: Option[String],
    resourceSummaryTitle: String
)

object ReviewEligibilityPlannerResponse:
  given sourceEncoder: Encoder[ReviewEligibilityPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ReviewEligibilityPlannerResponse] = deriveDecoder

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
