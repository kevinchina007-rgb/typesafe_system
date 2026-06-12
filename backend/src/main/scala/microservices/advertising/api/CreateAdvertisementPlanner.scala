// CreateAdvertisementPlanner 是广告模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object CreateAdvertisementPlanner extends ConnectionApiPlan[CreateAdvertisementRequest, AdvertisementResponse]:
  override val name: String = "CreateAdvertisementPlanner"

  override def plan(input: CreateAdvertisementRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.insert(connection, input, Instant.now())
