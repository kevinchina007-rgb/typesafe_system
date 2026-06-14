// CreateAttractionTicketSessionPlanner 只负责 attraction 票种时段创建这一条后端链路，前端只需要同名请求/响应对象，不需要镜像底层数据库写入。
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateAttractionTicketSessionPlanner extends ConnectionApiPlan[CreateAttractionTicketSessionPlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketSessionPlanner"
  override def plan(input: CreateAttractionTicketSessionPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketSession(connection, input, Instant.now())



