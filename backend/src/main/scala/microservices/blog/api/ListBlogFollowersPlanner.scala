// ListBlogFollowersPlanner 是内容模块的列表查询入口，负责请求校验、流程编排和结果返回。

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
