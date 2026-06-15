// CreateOrderCancellationMessageRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateOrderCancellationMessageRequest(threadId: String, orderId: String, reason: String)
object CreateOrderCancellationMessageRequest:
  given sourceEncoder: Encoder[CreateOrderCancellationMessageRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateOrderCancellationMessageRequest] = deriveDecoder
