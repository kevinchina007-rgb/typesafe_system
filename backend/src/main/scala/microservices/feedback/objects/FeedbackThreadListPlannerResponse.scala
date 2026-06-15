// FeedbackThreadListPlannerResponse：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FeedbackThreadListPlannerResponse(threads: List[FeedbackThreadDetailsPlannerResponse])
object FeedbackThreadListPlannerResponse:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackThreadListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackThreadListPlannerResponse] = deriveDecoder
