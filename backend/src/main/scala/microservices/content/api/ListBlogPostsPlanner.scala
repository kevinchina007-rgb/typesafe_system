// ListBlogPostsPlanner 是内容模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection

object ListBlogPostsPlanner extends ConnectionApiPlan[ListBlogPostsPlannerRequest, BlogPostListPlannerResponse]:
  override val name: String = "ListBlogPostsPlanner"
  override def plan(input: ListBlogPostsPlannerRequest, connection: Connection): IO[BlogPostListPlannerResponse] =
    val scope = input.scope.map(_.trim.toLowerCase).getOrElse("home")
    val status =
      scope match
        case "mine" | "drafts" => None
        case "profile" => Some(BlogPostStatus.Published.toString)
        case "favorites" => Some(BlogPostStatus.Published.toString)
        case "pending" => Some(BlogPostStatus.Draft.toString)
        case "reviewed" => None
        case _ => Some(BlogPostStatus.Published.toString)
    val authorUserId = Option.when(scope == "mine" || scope == "drafts" || scope == "profile")(input.userId.getOrElse(throw new IllegalArgumentException("userId is required")))
    val favoriteUserId = Option.when(scope == "favorites")(input.userId.getOrElse(throw new IllegalArgumentException("userId is required")))
    BlogPlannerPlainSql
      .listPosts(connection, status, authorUserId, input.q, input.tagType, input.tagValue, input.travelCity, input.travelCities, favoriteUserId, input.userId)
      .map(posts => BlogPostListPlannerResponse(if scope == "drafts" then posts.filter(_.status == BlogPostStatus.Draft.toString) else posts))
