// ComplaintMessageSnapshot：feedback 域后端对象定义。
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
