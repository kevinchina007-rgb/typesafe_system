// GetBlogProfilePlanner：博客域获取博客主页入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object GetBlogProfilePlanner extends ConnectionApiPlan[BlogProfilePlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "GetBlogProfilePlanner"
  override def plan(input: BlogProfilePlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    BlogPlannerPlainSql.findProfile(connection, input)
