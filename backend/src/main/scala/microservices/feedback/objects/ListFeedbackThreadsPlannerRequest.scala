// ListFeedbackThreadsPlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ListFeedbackThreadsPlannerRequest(
    userId: Option[String],
    managerType: Option[String],
    scopeId: Option[String],
    channel: Option[String],
    managerActorId: Option[String],
    siteAdminActorId: Option[String]
)
object ListFeedbackThreadsPlannerRequest:
  given sourceEncoder: Encoder[ListFeedbackThreadsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListFeedbackThreadsPlannerRequest] = deriveDecoder
