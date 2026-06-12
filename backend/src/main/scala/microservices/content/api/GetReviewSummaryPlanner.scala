// GetReviewSummaryPlanner 是内容模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object GetReviewSummaryPlanner extends ConnectionApiPlan[GetReviewSummaryPlannerRequest, ResourceReviewSummaryPlannerResponse]:
  override val name: String = "GetReviewSummaryPlanner"
  override def plan(input: GetReviewSummaryPlannerRequest, connection: Connection): IO[ResourceReviewSummaryPlannerResponse] =
    ReviewPlannerPlainSql.summary(connection, input)
