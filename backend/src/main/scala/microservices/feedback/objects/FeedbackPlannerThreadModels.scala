package com.typesafe.travel.feedback.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListFeedbackThreadsPlannerRequest(
    userId: Option[String],
    managerType: Option[String],
    scopeId: Option[String],
    channel: Option[String],
    managerActorId: Option[String],
    siteAdminActorId: Option[String]
)
object ListFeedbackThreadsPlannerRequest:
  given sourceEncoder: Encoder[ListFeedbackThreadsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListFeedbackThreadsPlannerRequest] = deriveDecoder

final case class EnsureReviewFeedbackThreadPlannerRequest(reviewId: String, userId: String)
object EnsureReviewFeedbackThreadPlannerRequest:
  given sourceEncoder: Encoder[EnsureReviewFeedbackThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EnsureReviewFeedbackThreadPlannerRequest] = deriveDecoder

final case class EnsureOrderCancellationThreadPlannerRequest(userId: String, orderId: String)
object EnsureOrderCancellationThreadPlannerRequest:
  given sourceEncoder: Encoder[EnsureOrderCancellationThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EnsureOrderCancellationThreadPlannerRequest] = deriveDecoder

final case class FeedbackThreadDetailsPlannerResponse(
    thread: FeedbackThread,
    messages: List[FeedbackMessage],
    managerActorLogoAssetPath: Option[String],
    siteAdminActorLogoAssetPath: Option[String]
)
object FeedbackThreadDetailsPlannerResponse:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackThreadDetailsPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackThreadDetailsPlannerResponse] = deriveDecoder

final case class FeedbackThreadListPlannerResponse(threads: List[FeedbackThreadDetailsPlannerResponse])
object FeedbackThreadListPlannerResponse:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackThreadListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackThreadListPlannerResponse] = deriveDecoder

