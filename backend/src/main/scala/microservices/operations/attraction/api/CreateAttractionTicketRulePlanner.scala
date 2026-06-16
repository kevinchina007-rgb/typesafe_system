// CreateAttractionTicketRulePlanner 只负责 attraction 票种规则创建这一条后端链路，前端只需要同名请求对象，不需要镜像这里的规则写入逻辑。
package com.typesafe.travel.operations.domain

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateAttractionTicketRulePlanner extends ConnectionApiPlan[CreateAttractionTicketRulePlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketRulePlanner"
  override def plan(input: CreateAttractionTicketRulePlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketRule(connection, input, Instant.now())





