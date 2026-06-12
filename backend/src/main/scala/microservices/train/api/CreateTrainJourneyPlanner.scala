// CreateTrainJourneyPlanner 是火车模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object CreateTrainJourneyPlanner extends ConnectionApiPlan[CreateTrainJourneyPlannerRequest, TrainPlannerResponse]:
  override val name: String = "CreateTrainJourneyPlanner"
  override def plan(input: CreateTrainJourneyPlannerRequest, connection: Connection): IO[TrainPlannerResponse] =
    TrainPlannerPlainSql.create(connection, input, java.time.Instant.now())
