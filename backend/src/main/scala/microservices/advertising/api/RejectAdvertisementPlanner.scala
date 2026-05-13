package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object RejectAdvertisementPlanner extends ConnectionApiPlan[AdvertisementReviewDecisionRequest, AdvertisementResponse]:
  override val name: String = "RejectAdvertisementPlanner"

  override def plan(input: AdvertisementReviewDecisionRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.reject(connection, input, Instant.now())

