// CreateFeedbackComplaintPlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateFeedbackComplaintPlannerRequest(
    sourceThreadId: String,
    selectedMessageIds: List[String],
    userExplanation: String,
    userDisplayName: String
)
object CreateFeedbackComplaintPlannerRequest:
  given sourceEncoder: Encoder[CreateFeedbackComplaintPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateFeedbackComplaintPlannerRequest] = deriveDecoder
