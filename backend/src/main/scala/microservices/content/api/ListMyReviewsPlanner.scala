// ListMyReviewsPlanner 是内容模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object ListMyReviewsPlanner extends ConnectionApiPlan[ListMyReviewsPlannerRequest, ReviewListPlannerResponse]:
  override val name: String = "ListMyReviewsPlanner"
  override def plan(input: ListMyReviewsPlannerRequest, connection: Connection): IO[ReviewListPlannerResponse] =
    ReviewPlannerPlainSql.listMy(connection, input.userId)
