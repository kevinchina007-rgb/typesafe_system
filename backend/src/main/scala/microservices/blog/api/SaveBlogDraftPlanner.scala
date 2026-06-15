// SaveBlogDraftPlanner：博客域保存博客草稿入口。

package com.typesafe.travel.blog.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.blog.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object SaveBlogDraftPlanner extends ConnectionApiPlan[SaveBlogDraftPlannerRequest, BlogPostResponse]:
  override val name: String = "SaveBlogDraftPlanner"
  override def plan(input: SaveBlogDraftPlannerRequest, connection: Connection): IO[BlogPostResponse] =
    validateBlogDraft(input, requireTags = false) *>
      BlogPlannerPlainSql
        .insertPost(connection, input, BlogPostStatus.Draft.toString, Instant.now())
        .flatMap(postId => BlogPlannerPlainSql.findPost(connection, postId, Some(input.userId)))
