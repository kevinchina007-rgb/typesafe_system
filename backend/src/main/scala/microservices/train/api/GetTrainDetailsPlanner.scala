// GetTrainDetailsPlanner 是火车模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object GetTrainDetailsPlanner extends ConnectionApiPlan[GetTrainDetailsPlannerRequest, TrainPlannerResponse]:
  override val name: String = "GetTrainDetailsPlanner"
  override def plan(input: GetTrainDetailsPlannerRequest, connection: Connection): IO[TrainPlannerResponse] =
    TrainPlannerPlainSql.get(connection, input, java.time.Instant.now())
