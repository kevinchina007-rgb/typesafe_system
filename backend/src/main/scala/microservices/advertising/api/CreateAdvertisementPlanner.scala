// 本文件是广告创建入口，只负责创建广告本体，不兼管其他动作。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object CreateAdvertisementPlanner extends ConnectionApiPlan[CreateAdvertisementRequest, AdvertisementResponse]:
  override val name: String = "CreateAdvertisementPlanner"

  override def plan(input: CreateAdvertisementRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.insert(connection, input, Instant.now())
