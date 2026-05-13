package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object ApproveAdvertisementPlanner extends ConnectionApiPlan[AdvertisementReviewDecisionRequest, AdvertisementResponse]:
  override val name: String = "ApproveAdvertisementPlanner"

  override def plan(input: AdvertisementReviewDecisionRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.approve(connection, input, Instant.now())

