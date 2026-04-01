package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.Instant

final class BlogSpec extends FunSuite:
  private val createdAt = Instant.parse("2026-04-01T00:00:00Z")

  test("published blog post requires non-empty title, summary, and content") {
    val result =
      createPublishedBlogPost(
        postId = BlogId("post-1"),
        authorUserId = UserId("user-1"),
        title = "  ",
        summary = "summary",
        content = "content",
        imageRefs = Nil,
        createdAt = createdAt
      )

    assert(result.left.exists(_.isInstanceOf[BlogError.BlogPostTitleWasInvalid]))
  }

  test("published blog post is publicly visible") {
    val post =
      createPublishedBlogPost(
        postId = BlogId("post-2"),
        authorUserId = UserId("user-1"),
        title = "Trip to Hangzhou",
        summary = "Weekend notes",
        content = "Good route and hotel.",
        imageRefs = Nil,
        createdAt = createdAt
      ).fold(throw _, identity)

    assertEquals(post.isPubliclyVisible, true)
    assertEquals(post.publishedAt, Some(createdAt))
  }

  test("comment requires non-empty content") {
    val result =
      createVisibleBlogComment(
        commentId = BlogCommentId("comment-1"),
        postId = BlogId("post-2"),
        authorUserId = UserId("user-1"),
        content = "   ",
        createdAt = createdAt
      )

    assert(result.left.exists(_.isInstanceOf[BlogError.BlogCommentContentWasInvalid]))
  }

  test("author can update and archive own blog post") {
    val post =
      createPublishedBlogPost(
        postId = BlogId("post-3"),
        authorUserId = UserId("user-1"),
        title = "Trip to Suzhou",
        summary = "Canals",
        content = "Original content",
        imageRefs = Nil,
        createdAt = createdAt
      ).fold(throw _, identity)

    val updatedPost =
      updateBlogPost(
        post = post,
        editorUserId = UserId("user-1"),
        title = "Trip to Suzhou Updated",
        summary = "Canals updated",
        content = "Updated content",
        imageRefs = Nil,
        updatedAt = createdAt.plusSeconds(60)
      ).fold(throw _, identity)

    val archivedPost =
      archiveBlogPost(
        post = updatedPost,
        editorUserId = UserId("user-1"),
        updatedAt = createdAt.plusSeconds(120)
      ).fold(throw _, identity)

    assertEquals(updatedPost.title, "Trip to Suzhou Updated")
    assertEquals(archivedPost.status, BlogPostStatus.Archived)
  }

  test("only comment author can delete comment") {
    val comment =
      createVisibleBlogComment(
        commentId = BlogCommentId("comment-2"),
        postId = BlogId("post-2"),
        authorUserId = UserId("user-1"),
        content = "Visible comment",
        createdAt = createdAt
      ).fold(throw _, identity)

    val rejectedDelete = deleteBlogComment(comment, UserId("user-2"))
    val deletedComment = deleteBlogComment(comment, UserId("user-1")).fold(throw _, identity)

    assert(rejectedDelete.left.exists(_.isInstanceOf[BlogError.BlogCommentAuthorMismatch]))
    assertEquals(deletedComment.status, BlogCommentStatus.Deleted)
  }
