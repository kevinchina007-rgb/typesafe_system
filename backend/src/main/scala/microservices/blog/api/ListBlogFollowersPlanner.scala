// ListBlogFollowersPlanner：博客域博客关注者列表入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection

object ListBlogFollowersPlanner extends ConnectionApiPlan[ListBlogProfileUsersPlannerRequest, BlogProfileUserListPlannerResponse]:
  override val name: String = "ListBlogFollowersPlanner"
  override def plan(input: ListBlogProfileUsersPlannerRequest, connection: Connection): IO[BlogProfileUserListPlannerResponse] =
    for
      hidden <- BlogPlannerPlainSql.profileRelationsHidden(connection, input.profileUserId, input.viewerUserId)
      response <- if hidden then IO.pure(BlogProfileUserListPlannerResponse(Nil)) else BlogPlannerPlainSql.listFollowers(connection, input)
    yield response
