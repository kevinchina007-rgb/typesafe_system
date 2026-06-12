// GetBlogProfilePlanner 是内容模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection

object GetBlogProfilePlanner extends ConnectionApiPlan[BlogProfilePlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "GetBlogProfilePlanner"
  override def plan(input: BlogProfilePlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    BlogPlannerPlainSql.findProfile(connection, input)
