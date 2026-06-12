// ExploreSearchPlanner 是内容模块的探索入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.ExplorePlannerPlainSql

import java.sql.Connection

object ExploreSearchPlanner extends ConnectionApiPlan[ExploreSearchPlannerRequest, ExploreSearchListPlannerResponse]:
  override val name: String = "ExploreSearchPlanner"
  override def plan(input: ExploreSearchPlannerRequest, connection: Connection): IO[ExploreSearchListPlannerResponse] =
    ExplorePlannerPlainSql.search(connection, input)
