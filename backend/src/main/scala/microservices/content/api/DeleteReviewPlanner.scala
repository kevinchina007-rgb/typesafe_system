// DeleteReviewPlanner 是内容模块的删除入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object DeleteReviewPlanner extends ConnectionApiPlan[DeleteReviewPlannerRequest, ReviewDeletedPlannerResponse]:
  override val name: String = "DeleteReviewPlanner"
  override def plan(input: DeleteReviewPlannerRequest, connection: Connection): IO[ReviewDeletedPlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.delete(connection, input, now))
