// ListReviewsByResourcePlanner 是内容模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object ListReviewsByResourcePlanner extends ConnectionApiPlan[ListReviewsByResourcePlannerRequest, ReviewListPlannerResponse]:
  override val name: String = "ListReviewsByResourcePlanner"
  override def plan(input: ListReviewsByResourcePlannerRequest, connection: Connection): IO[ReviewListPlannerResponse] =
    ReviewPlannerPlainSql.listByResource(connection, input)
