// 本文件定义 feedback 域的 `FeedbackThread` 实体，承载线程的基础身份、归属、状态和未读计数。
package com.typesafe.travel.feedback.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class FeedbackThread(
    threadId: SupportTicketId,
    kind: FeedbackThreadKind,
    managerType: FeedbackManagerType,
    ownerUserId: Option[UserId],
    ownerUserDisplayName: String,
    title: String,
    subtitle: String,
    resourceType: String,
    resourceSummaryTitle: String,
    orderId: Option[OrderId],
    orderItemId: Option[OrderItemId],
    reviewId: Option[ReviewId],
    relatedThreadId: Option[SupportTicketId],
    managerActorId: Option[String],
    siteAdminActorId: Option[String],
    unreadByUser: Int,
    unreadByManager: Int,
    unreadBySiteAdmin: Int,
    createdAt: Instant,
    updatedAt: Instant
)
object FeedbackThread:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[FeedbackThread] = deriveEncoder
  given sourceDecoder: Decoder[FeedbackThread] = deriveDecoder
