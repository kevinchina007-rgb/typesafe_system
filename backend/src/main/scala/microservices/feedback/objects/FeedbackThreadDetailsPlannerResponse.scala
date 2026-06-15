// FeedbackThreadDetailsPlannerResponse：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class FeedbackThreadDetailsPlannerResponse(
    thread: FeedbackThread,
    messages: List[FeedbackMessage],
    managerActorLogoAssetPath: Option[String],
    siteAdminActorLogoAssetPath: Option[String]
)
object FeedbackThreadDetailsPlannerResponse:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackThreadDetailsPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackThreadDetailsPlannerResponse] = deriveDecoder
