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
        s"广告「${advertisement.title}」被驳回，原因：$note。你可以保留这条历史记录，修改后重新提交。",
        now
      )
    yield advertisement
