package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object UpdateAdvertisementPlanner extends ConnectionApiPlan[UpdateAdvertisementRequest, AdvertisementResponse]:
  override val name: String = "UpdateAdvertisementPlanner"

  override def plan(input: UpdateAdvertisementRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.update(connection, input, Instant.now())
