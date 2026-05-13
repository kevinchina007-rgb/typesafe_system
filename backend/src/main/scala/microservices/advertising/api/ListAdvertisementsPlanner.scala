package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object ListAdvertisementsPlanner extends ConnectionApiPlan[ListAdvertisementsRequest, ListAdvertisementsResponse]:
  override val name: String = "ListAdvertisementsPlanner"

  override def plan(input: ListAdvertisementsRequest, connection: Connection): IO[ListAdvertisementsResponse] =
    AdvertisementPlainSql.list(connection, input)
