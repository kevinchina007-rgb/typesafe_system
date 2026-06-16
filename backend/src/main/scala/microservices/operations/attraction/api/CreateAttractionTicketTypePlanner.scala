// CreateAttractionTicketTypePlanner 只负责 attraction 票种创建这一条后端链路，前端同名请求对象只承载参数，不应复制此实现。
package com.typesafe.travel.operations.domain

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateAttractionTicketTypePlanner extends ConnectionApiPlan[CreateAttractionTicketTypePlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketTypePlanner"
  override def plan(input: CreateAttractionTicketTypePlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketType(connection, input, Instant.now())





