// CreateAttractionTicketRulePlanner 是景点模块的创建入口，负责请求校验、流程编排和结果返回�?
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateAttractionTicketRulePlanner extends ConnectionApiPlan[CreateAttractionTicketRulePlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketRulePlanner"
  override def plan(input: CreateAttractionTicketRulePlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketRule(connection, input, Instant.now())



