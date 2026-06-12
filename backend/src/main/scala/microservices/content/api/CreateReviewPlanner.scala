// CreateReviewPlanner 是内容模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object CreateReviewPlanner extends ConnectionApiPlan[CreateReviewPlannerRequest, ReviewPlannerResponse]:
  override val name: String = "CreateReviewPlanner"
  override def plan(input: CreateReviewPlannerRequest, connection: Connection): IO[ReviewPlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.create(connection, input, now))
