// 本文件是广告提交审核入口，只负责把广告送入审核流程。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object SubmitAdvertisementForReviewPlanner extends ConnectionApiPlan[AdvertisementOwnerActionRequest, AdvertisementResponse]:
  override val name: String = "SubmitAdvertisementForReviewPlanner"

  override def plan(input: AdvertisementOwnerActionRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.submitForReview(connection, input, Instant.now())
