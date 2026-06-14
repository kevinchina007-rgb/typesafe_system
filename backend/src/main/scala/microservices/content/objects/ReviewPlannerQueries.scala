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

