package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.content.BlogPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object BlogSuggestionsPlanner extends ConnectionApiPlan[BlogSuggestionRequest, BlogSuggestionListPlannerResponse]:
  override val name: String = "BlogSuggestionsPlanner"
  override def plan(input: BlogSuggestionRequest, connection: Connection): IO[BlogSuggestionListPlannerResponse] =
    BlogPlannerPlainSql.suggestions(connection, input)

object ListBlogPostsPlanner extends ConnectionApiPlan[ListBlogPostsPlannerRequest, BlogPostListPlannerResponse]:
  override val name: String = "ListBlogPostsPlanner"
  override def plan(input: ListBlogPostsPlannerRequest, connection: Connection): IO[BlogPostListPlannerResponse] =
    BlogPlannerPlainSql.list(connection, input)

object GetBlogPostPlanner extends ConnectionApiPlan[BlogPostByIdPlannerRequest, BlogPost]:
  override val name: String = "GetBlogPostPlanner"
  override def plan(input: BlogPostByIdPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.details(connection, input)

object CreateBlogPostPlanner extends ConnectionApiPlan[CreateBlogPostPlannerRequest, BlogPost]:
  override val name: String = "CreateBlogPostPlanner"
  override def plan(input: CreateBlogPostPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.create(connection, input, Instant.now())

object UpdateBlogPostPlanner extends ConnectionApiPlan[UpdateBlogPostPlannerRequest, BlogPost]:
  override val name: String = "UpdateBlogPostPlanner"
  override def plan(input: UpdateBlogPostPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.update(connection, input, Instant.now())

object ApproveBlogPostPlanner extends ConnectionApiPlan[ModerateBlogPostPlannerRequest, BlogPost]:
  override val name: String = "ApproveBlogPostPlanner"
  override def plan(input: ModerateBlogPostPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.approve(connection, input, Instant.now())

object RejectBlogPostPlanner extends ConnectionApiPlan[ModerateBlogPostPlannerRequest, BlogPost]:
  override val name: String = "RejectBlogPostPlanner"
  override def plan(input: ModerateBlogPostPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.reject(connection, input, Instant.now())

object ArchiveBlogPostPlanner extends ConnectionApiPlan[BlogPostByIdPlannerRequest, BlogPost]:
  override val name: String = "ArchiveBlogPostPlanner"
  override def plan(input: BlogPostByIdPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.archive(connection, input, Instant.now())

object AddBlogCommentPlanner extends ConnectionApiPlan[BlogCommentPlannerRequest, BlogPost]:
  override val name: String = "AddBlogCommentPlanner"
  override def plan(input: BlogCommentPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.addComment(connection, input, Instant.now())

object DeleteBlogCommentPlanner extends ConnectionApiPlan[DeleteBlogCommentPlannerRequest, BlogPost]:
  override val name: String = "DeleteBlogCommentPlanner"
  override def plan(input: DeleteBlogCommentPlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.deleteComment(connection, input)

object LikeBlogPostPlanner extends ConnectionApiPlan[BlogLikePlannerRequest, BlogPost]:
  override val name: String = "LikeBlogPostPlanner"
  override def plan(input: BlogLikePlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.like(connection, input, Instant.now())

object UnlikeBlogPostPlanner extends ConnectionApiPlan[BlogLikePlannerRequest, BlogPost]:
  override val name: String = "UnlikeBlogPostPlanner"
  override def plan(input: BlogLikePlannerRequest, connection: Connection): IO[BlogPost] =
    BlogPlannerPlainSql.unlike(connection, input)
