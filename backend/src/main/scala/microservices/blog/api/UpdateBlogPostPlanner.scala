// UpdateBlogPostPlanner：博客域更新博客文章入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object UpdateBlogPostPlanner extends ConnectionApiPlan[UpdateBlogPostPlannerRequest, BlogPostResponse]:
  override val name: String = "UpdateBlogPostPlanner"
  override def plan(input: UpdateBlogPostPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    BlogPlannerPlainSql.findPostAuthor(connection, input.postId).flatMap { authorUserId =>
      if authorUserId != input.userId then IO.raiseError(new IllegalArgumentException("Only the author can edit this post"))
      else
        val draft = SaveBlogDraftPlannerRequest(
          userId = input.userId,
          postId = Some(input.postId),
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
    }
