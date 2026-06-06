package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object ApproveAdvertisementPlanner extends ConnectionApiPlan[AdvertisementReviewDecisionRequest, AdvertisementResponse]:
  override val name: String = "ApproveAdvertisementPlanner"

  override def plan(input: AdvertisementReviewDecisionRequest, connection: Connection): IO[AdvertisementResponse] =
    val now = Instant.now()
    for
      advertisement <- AdvertisementPlainSql.approve(connection, input, now)
      _ <- AdvertisementFeedbackNotifications.notifyOwner(
        connection,
        advertisement,
        input.reviewerManagerId,
        s"广告「${advertisement.title}」已通过审核，可以进入投放。",
        now
      )
    yield advertisement
