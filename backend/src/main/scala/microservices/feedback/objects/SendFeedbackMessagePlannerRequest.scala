// SendFeedbackMessagePlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SendFeedbackMessagePlannerRequest(threadId: String, senderDisplayName: String, senderRole: String, body: String)
object SendFeedbackMessagePlannerRequest:
  given sourceEncoder: Encoder[SendFeedbackMessagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SendFeedbackMessagePlannerRequest] = deriveDecoder
