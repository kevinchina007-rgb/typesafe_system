// 本文件是广告驳回入口，只服务后端广告审核流程，不对应前端镜像文件。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object RejectAdvertisementPlanner extends ConnectionApiPlan[AdvertisementReviewDecisionRequest, AdvertisementResponse]:
  override val name: String = "RejectAdvertisementPlanner"

  override def plan(input: AdvertisementReviewDecisionRequest, connection: Connection): IO[AdvertisementResponse] =
    val note = input.reviewNote.map(_.trim).filter(_.nonEmpty).getOrElse {
      throw new IllegalArgumentException("reject_reason_required")
    }
    val now = Instant.now()
    for
      advertisement <- AdvertisementPlainSql.reject(connection, input.copy(reviewNote = Some(note)), now)
      _ <- AdvertisementFeedbackNotifications.notifyOwner(
        connection,
        advertisement,
        input.reviewerManagerId,
        s"Advertisement '${advertisement.title}' was rejected. Reason: $note.",
        now
      )
    yield advertisement
