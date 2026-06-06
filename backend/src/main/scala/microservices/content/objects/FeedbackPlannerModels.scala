package com.typesafe.travel.content.domain

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

final case class SendFeedbackMessagePlannerRequest(threadId: String, senderDisplayName: String, senderRole: String, body: String)
object SendFeedbackMessagePlannerRequest:
  given sourceEncoder: Encoder[SendFeedbackMessagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SendFeedbackMessagePlannerRequest] = deriveDecoder

final case class CreateOrderCancellationMessageRequest(threadId: String, orderId: String, reason: String)
object CreateOrderCancellationMessageRequest:
  given sourceEncoder: Encoder[CreateOrderCancellationMessageRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateOrderCancellationMessageRequest] = deriveDecoder

final case class HandleOrderCancellationRequest(threadId: String, messageId: String, status: String, managerNote: Option[String], handledBy: Option[String], handlerRole: Option[String])
object HandleOrderCancellationRequest:
  given sourceEncoder: Encoder[HandleOrderCancellationRequest] = deriveEncoder
  given sourceDecoder: Decoder[HandleOrderCancellationRequest] = deriveDecoder

final case class MarkFeedbackThreadReadPlannerRequest(threadId: String, audience: String)
object MarkFeedbackThreadReadPlannerRequest:
  given sourceEncoder: Encoder[MarkFeedbackThreadReadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[MarkFeedbackThreadReadPlannerRequest] = deriveDecoder

final case class EscalateFeedbackThreadPlannerRequest(threadId: String, senderDisplayName: String, body: String)
object EscalateFeedbackThreadPlannerRequest:
  given sourceEncoder: Encoder[EscalateFeedbackThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EscalateFeedbackThreadPlannerRequest] = deriveDecoder

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
