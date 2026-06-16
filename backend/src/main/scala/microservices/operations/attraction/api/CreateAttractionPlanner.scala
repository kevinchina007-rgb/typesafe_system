// CreateAttractionPlanner 只负责 attraction 本体创建，属于后端业务入口层；票种、时段、规则等更细动作应拆到各自 planner，不需要前端复制实现。
package com.typesafe.travel.operations.domain

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateAttractionPlanner extends ConnectionApiPlan[CreateAttractionPlannerRequest, Attraction]:
  override val name: String = "CreateAttractionPlanner"
  override def plan(input: CreateAttractionPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.create(connection, input, Instant.now())





