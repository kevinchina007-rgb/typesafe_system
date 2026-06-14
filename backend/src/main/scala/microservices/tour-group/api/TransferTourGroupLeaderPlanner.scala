// TransferTourGroupLeaderPlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object TransferTourGroupLeaderPlanner extends ConnectionApiPlan[TransferTourGroupLeaderPlannerRequest, TourGroupDetailsResponse]:
  override val name: String = "TransferTourGroupLeaderPlanner"
  override def plan(input: TransferTourGroupLeaderPlannerRequest, connection: Connection): IO[TourGroupDetailsResponse] =
    TourGroupPlannerPlainSql.transferOrganizer(connection, input, java.time.Instant.now())
