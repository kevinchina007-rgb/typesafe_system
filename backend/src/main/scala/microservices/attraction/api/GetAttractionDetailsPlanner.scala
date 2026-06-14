// GetAttractionDetailsPlanner 是景点模块的获取入口，负责请求校验、流程编排和结果返回�?
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



