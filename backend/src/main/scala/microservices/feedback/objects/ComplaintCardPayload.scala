// ComplaintCardPayload：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
