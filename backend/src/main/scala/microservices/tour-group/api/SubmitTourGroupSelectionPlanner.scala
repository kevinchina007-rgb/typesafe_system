// SubmitTourGroupSelectionPlanner 是团体游模块的提交入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object SubmitTourGroupSelectionPlanner extends ConnectionApiPlan[SubmitTourGroupSelectionPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "SubmitTourGroupSelectionPlanner"
  override def plan(input: SubmitTourGroupSelectionPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.submitSelection(connection, input, java.time.Instant.now())
