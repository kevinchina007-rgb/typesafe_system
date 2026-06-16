package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.train.domain.TrainListPlannerResponse

import java.sql.Connection

object ListManagedTrainsPlanner extends ConnectionApiPlan[ListManagedTrainsPlannerRequest, TrainListPlannerResponse]:
  override val name: String = "ListManagedTrainsPlanner"
  override def plan(input: ListManagedTrainsPlannerRequest, connection: Connection): IO[TrainListPlannerResponse] =
    TrainManagerPlainSql.listManaged(connection, input, java.time.Instant.now())
