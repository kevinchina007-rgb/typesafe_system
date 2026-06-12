// ListAttractionsPlanner 是景点模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.attraction.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object ListAttractionsPlanner extends ConnectionApiPlan[ListAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListAttractionsPlanner"
  override def plan(input: ListAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.list(connection, input)
