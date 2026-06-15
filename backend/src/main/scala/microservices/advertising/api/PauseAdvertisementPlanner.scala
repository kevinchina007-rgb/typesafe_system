// 本文件是广告暂停入口，只负责广告暂停这一件事。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object PauseAdvertisementPlanner extends ConnectionApiPlan[AdvertisementOwnerActionRequest, AdvertisementResponse]:
  override val name: String = "PauseAdvertisementPlanner"

  override def plan(input: AdvertisementOwnerActionRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.pause(connection, input, Instant.now())
