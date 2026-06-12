// SearchTrainsPlanner 是火车模块的搜索入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object SearchTrainsPlanner extends ConnectionApiPlan[SearchTrainsPlannerRequest, TrainListPlannerResponse]:
  override val name: String = "SearchTrainsPlanner"
  override def plan(input: SearchTrainsPlannerRequest, connection: Connection): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSql.search(connection, input, java.time.Instant.now())
