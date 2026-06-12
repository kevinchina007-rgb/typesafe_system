// UpdateReviewPlanner 是内容模块的更新入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object UpdateReviewPlanner extends ConnectionApiPlan[UpdateReviewPlannerRequest, ReviewPlannerResponse]:
  override val name: String = "UpdateReviewPlanner"
  override def plan(input: UpdateReviewPlannerRequest, connection: Connection): IO[ReviewPlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.update(connection, input, now))
