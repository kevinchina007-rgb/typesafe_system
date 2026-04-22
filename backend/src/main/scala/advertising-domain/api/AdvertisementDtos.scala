package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.{AdvertisementReviewView, AdvertisementView}

final case class CreateAdvertisementRequestDto(
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    targetResourceType: String,
    targetResourceId: String,
    placement: String,
    priority: Int,
    startAt: String,
    endAt: String
)

final case class UpdateAdvertisementRequestDto(
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    targetResourceId: String,
    placement: String,
    priority: Int,
    startAt: String,
    endAt: String
)

final case class AdvertisementReviewDecisionRequestDto(reviewNote: Option[String])

final case class AdvertisementImageUploadResponseDto(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)

final case class AdvertisementReviewResponseDto(
    reviewId: String,
    reviewerManagerId: String,
    decision: String,
    reviewNote: Option[String],
    reviewedAt: String
)

final case class AdvertisementResponseDto(
    advertisementId: String,
    ownerManagerId: String,
    ownerType: String,
    ownerDisplayName: String,
    targetResourceType: String,
    targetResourceId: String,
    resourceSummaryTitle: String,
    landingTarget: String,
    placement: String,
    audience: String,
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    reviewStatus: String,
    deliveryStatus: String,
    priority: Int,
    startAt: String,
    endAt: String,
    rejectionNote: Option[String],
    createdAt: String,
    updatedAt: String,
    reviews: List[AdvertisementReviewResponseDto]
)

final case class AdvertisementListResponseDto(advertisements: List[AdvertisementResponseDto])

object AdvertisementReviewResponseDto:
  def fromView(view: AdvertisementReviewView): AdvertisementReviewResponseDto =
    AdvertisementReviewResponseDto(
      reviewId = view.reviewId,
      reviewerManagerId = view.reviewerManagerId,
      decision = view.decision,
      reviewNote = view.reviewNote,
      reviewedAt = view.reviewedAt.toString
    )

object AdvertisementResponseDto:
  def fromView(view: AdvertisementView): AdvertisementResponseDto =
    AdvertisementResponseDto(
      advertisementId = view.advertisementId,
      ownerManagerId = view.ownerManagerId,
      ownerType = view.ownerType,
      ownerDisplayName = view.ownerDisplayName,
      targetResourceType = view.targetResourceType,
      targetResourceId = view.targetResourceId,
      resourceSummaryTitle = view.resourceSummaryTitle,
      landingTarget = view.landingTarget,
      placement = view.placement,
      audience = view.audience,
      title = view.title,
      subtitle = view.subtitle,
      description = view.description,
      imageUrl = view.imageUrl,
      ctaLabel = view.ctaLabel,
      reviewStatus = view.reviewStatus,
      deliveryStatus = view.deliveryStatus,
      priority = view.priority,
      startAt = view.startAt.toString,
      endAt = view.endAt.toString,
      rejectionNote = view.rejectionNote,
      createdAt = view.createdAt.toString,
      updatedAt = view.updatedAt.toString,
      reviews = view.reviews.map(AdvertisementReviewResponseDto.fromView)
    )
