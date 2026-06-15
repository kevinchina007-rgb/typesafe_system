package com.typesafe.travel.feedback.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ComplaintMessageSnapshot(
    messageId: String,
    senderRole: FeedbackSenderRole,
    senderDisplayName: String,
    content: String,
    messageType: FeedbackMessageType,
    createdAt: String
)
object ComplaintMessageSnapshot:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[ComplaintMessageSnapshot] = deriveEncoder
  given sourceDecoder: Decoder[ComplaintMessageSnapshot] = deriveDecoder

final case class ComplaintCardPayload(
    complaintId: String,
    sourceThreadId: String,
    managerThreadId: Option[String],
    userExplanation: String,
    summary: String,
    targetDisplayName: String,
    selectedMessages: List[ComplaintMessageSnapshot],
    createdAt: String
)
object ComplaintCardPayload:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[ComplaintCardPayload] = deriveEncoder
  given sourceDecoder: Decoder[ComplaintCardPayload] = deriveDecoder
