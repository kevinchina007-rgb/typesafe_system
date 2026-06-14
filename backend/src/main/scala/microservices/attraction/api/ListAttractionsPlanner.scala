// ListAttractionsPlanner 只负责 attraction 列表查询入口，前端应镜像同名请求/响应对象，但不镜像这里的 plain SQL 或结果映射实现。
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object ListAttractionsPlanner extends ConnectionApiPlan[ListAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListAttractionsPlanner"
  override def plan(input: ListAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.list(connection, input)



