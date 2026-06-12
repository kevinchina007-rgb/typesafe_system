// CreateBlogPostPlanner 是内容模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object CreateBlogPostPlanner extends ConnectionApiPlan[CreateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "CreateBlogPostPlanner"
  override def plan(input: CreateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    val draft = SaveBlogDraftPlannerRequest(
      userId = input.userId,
      postId = None,
      title = input.title,
      summary = input.summary,
      coverText = input.coverText.getOrElse(input.summary),
      content = input.content,
      images = input.images,
      tags = input.tags.getOrElse(Nil),
      travelCity = input.travelCity,
      travelCities = input.travelCities
    )
    validateBlogDraft(draft, requireTags = true) *>
      BlogPlannerPlainSql
        .insertPost(connection, draft, BlogPostStatus.Published.toString, Instant.now())
        .flatMap(postId => BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId)))
