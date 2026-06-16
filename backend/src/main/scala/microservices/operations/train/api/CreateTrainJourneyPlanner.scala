package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.train.domain.TrainPlannerResponse

import java.sql.Connection

object CreateTrainJourneyPlanner extends ConnectionApiPlan[CreateTrainJourneyPlannerRequest, TrainPlannerResponse]:
  override val name: String = "CreateTrainJourneyPlanner"
  override def plan(input: CreateTrainJourneyPlannerRequest, connection: Connection): IO[TrainPlannerResponse] =
    TrainManagerPlainSql.create(connection, input, java.time.Instant.now())
