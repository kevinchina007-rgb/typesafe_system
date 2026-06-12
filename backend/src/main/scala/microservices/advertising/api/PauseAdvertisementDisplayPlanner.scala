// PauseAdvertisementDisplayPlanner 是广告模块的暂停入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object PauseAdvertisementDisplayPlanner extends ConnectionApiPlan[AdvertisementReviewDecisionRequest, AdvertisementResponse]:
  override val name: String = "PauseAdvertisementDisplayPlanner"

  override def plan(input: AdvertisementReviewDecisionRequest, connection: Connection): IO[AdvertisementResponse] =
    val now = Instant.now()
    val note = input.reviewNote.map(_.trim).filter(_.nonEmpty).getOrElse("网站管理者已暂时取消展示")
    for
      advertisement <- AdvertisementPlainSql.pauseDisplayBySiteAdmin(connection, input.copy(reviewNote = Some(note)), now)
      _ <- AdvertisementFeedbackNotifications.notifyOwner(
        connection,
        advertisement,
        input.reviewerManagerId,
        s"广告「${advertisement.title}」已被暂时取消展示，原因：$note。审核记录会保留，后续可重新安排展示。",
        now
      )
    yield advertisement
