// EscalateFeedbackThreadPlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class EscalateFeedbackThreadPlannerRequest(threadId: String, senderDisplayName: String, body: String)
object EscalateFeedbackThreadPlannerRequest:
  given sourceEncoder: Encoder[EscalateFeedbackThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EscalateFeedbackThreadPlannerRequest] = deriveDecoder
