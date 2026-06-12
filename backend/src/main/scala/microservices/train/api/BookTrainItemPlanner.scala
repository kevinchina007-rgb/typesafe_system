// BookTrainItemPlanner 是火车模块的预订入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object BookTrainItemPlanner extends ConnectionApiPlan[BookTrainItemPlannerRequest, BookTrainItemPlannerResponse]:
  override val name: String = "BookTrainItemPlanner"
  override def plan(input: BookTrainItemPlannerRequest, connection: Connection): IO[BookTrainItemPlannerResponse] =
    TrainPlannerPlainSql.bookItem(connection, input, java.time.Instant.now())
