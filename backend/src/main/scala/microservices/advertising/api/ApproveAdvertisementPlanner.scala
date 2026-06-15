// 本文件是广告审核通过入口，只服务后端广告审核流程，不对应前端镜像文件。
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
        s"Advertisement '${advertisement.title}' passed review and can be delivered.",
        now
      )
    yield advertisement
