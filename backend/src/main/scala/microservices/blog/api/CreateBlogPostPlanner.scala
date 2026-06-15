// CreateBlogPostPlanner：博客域创建博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

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
