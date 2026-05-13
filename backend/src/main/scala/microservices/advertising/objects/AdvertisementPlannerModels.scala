package com.typesafe.travel.advertising.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AdvertisementReviewResponse(
    reviewId: String,
    reviewerManagerId: String,
    decision: String,
    reviewNote: Option[String],
    reviewedAt: String
)

object AdvertisementReviewResponse:
  given sourceEncoder: Encoder[AdvertisementReviewResponse] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementReviewResponse] = deriveDecoder

final case class AdvertisementResponse(
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
    slotIndex: Option[Int],
    startAt: String,
    endAt: String,
    rejectionNote: Option[String],
    createdAt: String,
    updatedAt: String,
    reviews: List[AdvertisementReviewResponse]
)

object AdvertisementResponse:
  given sourceEncoder: Encoder[AdvertisementResponse] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementResponse] = deriveDecoder

final case class ListAdvertisementsRequest(
    placement: Option[String],
    reviewStatus: Option[String],
    reviewStatuses: Option[List[String]],
    ownerManagerId: Option[String],
    ownerType: Option[String],
    deliverableOnly: Option[Boolean],
    currentTime: Option[String]
)

object ListAdvertisementsRequest:
  given sourceEncoder: Encoder[ListAdvertisementsRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListAdvertisementsRequest] = deriveDecoder

final case class ListAdvertisementsResponse(
    advertisements: List[AdvertisementResponse]
)

object ListAdvertisementsResponse:
  given sourceEncoder: Encoder[ListAdvertisementsResponse] = deriveEncoder
  given sourceDecoder: Decoder[ListAdvertisementsResponse] = deriveDecoder

final case class CreateAdvertisementRequest(
    ownerManagerId: String,
    ownerType: String,
    ownerDisplayName: String,
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    targetResourceType: String,
    targetResourceId: String,
    resourceSummaryTitle: Option[String],
    landingTarget: Option[String],
    placement: String,
    priority: Int,
    startAt: String,
    endAt: String
)

object CreateAdvertisementRequest:
  given sourceEncoder: Encoder[CreateAdvertisementRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAdvertisementRequest] = deriveDecoder

final case class UpdateAdvertisementRequest(
    advertisementId: String,
    ownerManagerId: String,
    ownerType: String,
    title: String,
    subtitle: String,
    description: String,
    imageUrl: Option[String],
    ctaLabel: String,
    targetResourceType: String,
    targetResourceId: String,
    resourceSummaryTitle: Option[String],
    landingTarget: Option[String],
    placement: String,
    priority: Int,
    startAt: String,
    endAt: String
)

object UpdateAdvertisementRequest:
  given sourceEncoder: Encoder[UpdateAdvertisementRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateAdvertisementRequest] = deriveDecoder

final case class AdvertisementOwnerActionRequest(
    advertisementId: String,
    ownerManagerId: String,
    ownerType: String
)

object AdvertisementOwnerActionRequest:
  given sourceEncoder: Encoder[AdvertisementOwnerActionRequest] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementOwnerActionRequest] = deriveDecoder

final case class AdvertisementReviewDecisionRequest(
    advertisementId: String,
    reviewerManagerId: String,
    reviewNote: Option[String]
)

object AdvertisementReviewDecisionRequest:
  given sourceEncoder: Encoder[AdvertisementReviewDecisionRequest] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementReviewDecisionRequest] = deriveDecoder

final case class AdvertisementSlotAssignmentRequest(
    advertisementId: String,
    reviewerManagerId: String,
    slotIndex: Int
)

object AdvertisementSlotAssignmentRequest:
  given sourceEncoder: Encoder[AdvertisementSlotAssignmentRequest] = deriveEncoder
  given sourceDecoder: Decoder[AdvertisementSlotAssignmentRequest] = deriveDecoder

final case class UploadAdvertisementImageRequest(
    originalFileName: String,
    mimeType: String,
    fileContentBase64: String
)

object UploadAdvertisementImageRequest:
  given sourceEncoder: Encoder[UploadAdvertisementImageRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadAdvertisementImageRequest] = deriveDecoder

final case class UploadAdvertisementImageResponse(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)

object UploadAdvertisementImageResponse:
  given sourceEncoder: Encoder[UploadAdvertisementImageResponse] = deriveEncoder
  given sourceDecoder: Decoder[UploadAdvertisementImageResponse] = deriveDecoder
