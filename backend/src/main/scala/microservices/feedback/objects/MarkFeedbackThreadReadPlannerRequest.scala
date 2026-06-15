// MarkFeedbackThreadReadPlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class MarkFeedbackThreadReadPlannerRequest(threadId: String, audience: String)
object MarkFeedbackThreadReadPlannerRequest:
  given sourceEncoder: Encoder[MarkFeedbackThreadReadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[MarkFeedbackThreadReadPlannerRequest] = deriveDecoder
