// GetAttractionDetailsPlanner 只负责 attraction 详情查询入口，前端应镜像同名请求/响应对象，但不镜像这里的后端数据库读取实现。
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object GetAttractionDetailsPlanner extends ConnectionApiPlan[GetAttractionDetailsPlannerRequest, Attraction]:
  override val name: String = "GetAttractionDetailsPlanner"
  override def plan(input: GetAttractionDetailsPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.details(connection, input)



