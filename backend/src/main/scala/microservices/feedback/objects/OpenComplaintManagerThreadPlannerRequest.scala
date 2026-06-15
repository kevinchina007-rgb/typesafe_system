// OpenComplaintManagerThreadPlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class OpenComplaintManagerThreadPlannerRequest(
    complaintMessageId: String,
    siteAdminActorId: String
)
object OpenComplaintManagerThreadPlannerRequest:
  given sourceEncoder: Encoder[OpenComplaintManagerThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[OpenComplaintManagerThreadPlannerRequest] = deriveDecoder
