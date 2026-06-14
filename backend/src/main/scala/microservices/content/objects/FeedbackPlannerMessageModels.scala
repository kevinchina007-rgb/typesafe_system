package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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

final case class CreateFeedbackComplaintPlannerRequest(
    sourceThreadId: String,
    selectedMessageIds: List[String],
    userExplanation: String,
    userDisplayName: String
)
object CreateFeedbackComplaintPlannerRequest:
  given sourceEncoder: Encoder[CreateFeedbackComplaintPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateFeedbackComplaintPlannerRequest] = deriveDecoder

final case class OpenComplaintManagerThreadPlannerRequest(
    complaintMessageId: String,
    siteAdminActorId: String
)
object OpenComplaintManagerThreadPlannerRequest:
  given sourceEncoder: Encoder[OpenComplaintManagerThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[OpenComplaintManagerThreadPlannerRequest] = deriveDecoder

