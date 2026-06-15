// HandleOrderCancellationRequest：feedback 域后端对象定义。
package com.typesafe.travel.feedback.domain
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class HandleOrderCancellationRequest(threadId: String, messageId: String, status: String, managerNote: Option[String], handledBy: Option[String], handlerRole: Option[String])
object HandleOrderCancellationRequest:
  given sourceEncoder: Encoder[HandleOrderCancellationRequest] = deriveEncoder
  given sourceDecoder: Decoder[HandleOrderCancellationRequest] = deriveDecoder
