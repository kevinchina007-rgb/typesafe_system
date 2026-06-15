// EnsureReviewFeedbackThreadPlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class EnsureReviewFeedbackThreadPlannerRequest(reviewId: String, userId: String)
object EnsureReviewFeedbackThreadPlannerRequest:
  given sourceEncoder: Encoder[EnsureReviewFeedbackThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EnsureReviewFeedbackThreadPlannerRequest] = deriveDecoder
