// ListBlogFollowingPlanner 是内容模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection

object ListBlogFollowingPlanner extends ConnectionApiPlan[ListBlogProfileUsersPlannerRequest, BlogProfileUserListPlannerResponse]:
  override val name: String = "ListBlogFollowingPlanner"
  override def plan(input: ListBlogProfileUsersPlannerRequest, connection: Connection): IO[BlogProfileUserListPlannerResponse] =
    for
      hidden <- BlogPlannerPlainSql.profileRelationsHidden(connection, input.profileUserId, input.viewerUserId)
      response <- if hidden then IO.pure(BlogProfileUserListPlannerResponse(Nil)) else BlogPlannerPlainSql.listFollowing(connection, input)
    yield response
