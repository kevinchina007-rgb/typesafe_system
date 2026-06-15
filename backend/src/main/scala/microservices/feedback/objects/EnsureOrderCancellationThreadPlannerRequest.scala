// EnsureOrderCancellationThreadPlannerRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class EnsureOrderCancellationThreadPlannerRequest(userId: String, orderId: String)
object EnsureOrderCancellationThreadPlannerRequest:
  given sourceEncoder: Encoder[EnsureOrderCancellationThreadPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[EnsureOrderCancellationThreadPlannerRequest] = deriveDecoder
