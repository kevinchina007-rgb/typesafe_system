// UpdateBlogProfilePrivacyPlanner 是内容模块的更新入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object UpdateBlogProfilePrivacyPlanner extends ConnectionApiPlan[UpdateBlogProfilePrivacyPlannerRequest, BlogProfilePlannerResponse]:
  override val name: String = "UpdateBlogProfilePrivacyPlanner"
  override def plan(input: UpdateBlogProfilePrivacyPlannerRequest, connection: Connection): IO[BlogProfilePlannerResponse] =
    for
      _ <- BlogPlannerPlainSql.updateProfilePrivacy(connection, input.userId, input.hideRelations, Instant.now())
      profile <- BlogPlannerPlainSql.findProfile(connection, BlogProfilePlannerRequest(Some(input.userId), input.userId))
    yield profile
