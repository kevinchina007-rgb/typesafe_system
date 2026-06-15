// 本文件是广告更新入口，只负责更新广告本体，不兼管其他动作。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object UpdateAdvertisementPlanner extends ConnectionApiPlan[UpdateAdvertisementRequest, AdvertisementResponse]:
  override val name: String = "UpdateAdvertisementPlanner"

  override def plan(input: UpdateAdvertisementRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.update(connection, input, Instant.now())
