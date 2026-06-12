// CheckReviewEligibilityPlanner 是内容模块的检查入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object CheckReviewEligibilityPlanner extends ConnectionApiPlan[CheckReviewEligibilityPlannerRequest, ReviewEligibilityPlannerResponse]:
  override val name: String = "CheckReviewEligibilityPlanner"
  override def plan(input: CheckReviewEligibilityPlannerRequest, connection: Connection): IO[ReviewEligibilityPlannerResponse] =
    ReviewPlannerPlainSql.eligibility(connection, input)
