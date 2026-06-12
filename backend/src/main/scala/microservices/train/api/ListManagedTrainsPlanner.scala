// ListManagedTrainsPlanner 是火车模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object ListManagedTrainsPlanner extends ConnectionApiPlan[ListManagedTrainsPlannerRequest, TrainListPlannerResponse]:
  override val name: String = "ListManagedTrainsPlanner"
  override def plan(input: ListManagedTrainsPlannerRequest, connection: Connection): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSql.listManaged(connection, input, java.time.Instant.now())
