// UpdateBlogProfilePrivacyPlanner：博客域更新博客主页隐私入口。

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
