package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
