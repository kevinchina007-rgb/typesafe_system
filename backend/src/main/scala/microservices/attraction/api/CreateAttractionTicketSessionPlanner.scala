// CreateAttractionTicketSessionPlanner 是景点模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.attraction.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateAttractionTicketSessionPlanner extends ConnectionApiPlan[CreateAttractionTicketSessionPlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketSessionPlanner"
  override def plan(input: CreateAttractionTicketSessionPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketSession(connection, input, Instant.now())
