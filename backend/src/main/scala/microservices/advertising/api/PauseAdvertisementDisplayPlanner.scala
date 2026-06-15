// 本文件是广告暂停展示入口，只服务后端广告审核流程，不对应前端镜像文件。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object PauseAdvertisementDisplayPlanner extends ConnectionApiPlan[AdvertisementReviewDecisionRequest, AdvertisementResponse]:
  override val name: String = "PauseAdvertisementDisplayPlanner"

  override def plan(input: AdvertisementReviewDecisionRequest, connection: Connection): IO[AdvertisementResponse] =
    val now = Instant.now()
    val note = input.reviewNote.map(_.trim).filter(_.nonEmpty).getOrElse("Paused by site admin")
    for
      advertisement <- AdvertisementPlainSql.pauseDisplayBySiteAdmin(connection, input.copy(reviewNote = Some(note)), now)
      _ <- AdvertisementFeedbackNotifications.notifyOwner(
        connection,
        advertisement,
        input.reviewerManagerId,
        s"Advertisement '${advertisement.title}' was paused from display for review note: $note.",
        now
      )
    yield advertisement
