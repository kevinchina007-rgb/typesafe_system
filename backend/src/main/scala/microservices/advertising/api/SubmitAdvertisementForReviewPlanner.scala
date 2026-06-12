// SubmitAdvertisementForReviewPlanner 是广告模块的提交入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object SubmitAdvertisementForReviewPlanner extends ConnectionApiPlan[AdvertisementOwnerActionRequest, AdvertisementResponse]:
  override val name: String = "SubmitAdvertisementForReviewPlanner"

  override def plan(input: AdvertisementOwnerActionRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.submitForReview(connection, input, Instant.now())

