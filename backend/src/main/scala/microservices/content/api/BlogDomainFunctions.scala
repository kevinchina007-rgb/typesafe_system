// BlogDomainFunctions 定义内容模块的领域辅助函数。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.shared.kernel.*

import java.time.Instant

def validateBlogDraft(input: SaveBlogDraftPlannerRequest, requireTags: Boolean): IO[Unit] =
  if input.title.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Blog title must not be empty"))
  else if input.summary.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Blog summary must not be empty"))
  else if input.content.trim.isEmpty then IO.raiseError(new IllegalArgumentException("Blog content must not be empty"))
  else if requireTags && input.tags.isEmpty then IO.raiseError(new IllegalArgumentException("Blog tags must not be empty"))
  else IO.unit

def createPublishedBlogPost(
    postId: BlogId,
    authorUserId: UserId,
    title: String,
    summary: String,
    content: String,
    imageRefs: List[BlogImageRef],
    createdAt: Instant
): Either[BlogError, BlogPost] =
  val normalizedTitle = title.trim
  val normalizedSummary = summary.trim
  val normalizedContent = content.trim
  if normalizedTitle.isEmpty then Left(BlogError.BlogPostTitleWasInvalid(postId))
  else if normalizedSummary.isEmpty then Left(BlogError.BlogPostSummaryWasInvalid(postId))
  else if normalizedContent.isEmpty then Left(BlogError.BlogPostContentWasInvalid(postId))
  else if imageRefs.lengthCompare(6) > 0 then Left(BlogError.BlogImageCountWasInvalid(postId, 6))
  else
    Right(
      BlogPost(
        postId = postId,
        authorUserId = authorUserId,
        title = normalizedTitle,
        summary = normalizedSummary,
        content = normalizedContent,
        imageRefs = imageRefs.sortBy(_.sortOrder),
        status = BlogPostStatus.PendingReview,
        createdAt = createdAt,
        updatedAt = createdAt,
        publishedAt = None
      )
    )


def restorePersistedBlogPost(
    postId: BlogId,
    authorUserId: UserId,
    title: String,
    summary: String,
    content: String,
    imageRefs: List[BlogImageRef],
    status: BlogPostStatus,
    createdAt: Instant,
    updatedAt: Instant,
    publishedAt: Option[Instant]
): BlogPost =
  BlogPost(
    postId = postId,
    authorUserId = authorUserId,
    title = title,
    summary = summary,
    content = content,
    imageRefs = imageRefs.sortBy(_.sortOrder),
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    publishedAt = publishedAt
  )


def updateBlogPost(
    post: BlogPost,
    editorUserId: UserId,
    title: String,
    summary: String,
    content: String,
    imageRefs: List[BlogImageRef],
    updatedAt: Instant
): Either[BlogError, BlogPost] =
  if post.authorUserId != editorUserId then Left(BlogError.BlogPostAuthorMismatch(post.postId, editorUserId))
  else
    val normalizedTitle = title.trim
    val normalizedSummary = summary.trim
    val normalizedContent = content.trim
    if normalizedTitle.isEmpty then Left(BlogError.BlogPostTitleWasInvalid(post.postId))
    else if normalizedSummary.isEmpty then Left(BlogError.BlogPostSummaryWasInvalid(post.postId))
    else if normalizedContent.isEmpty then Left(BlogError.BlogPostContentWasInvalid(post.postId))
    else if imageRefs.lengthCompare(6) > 0 then Left(BlogError.BlogImageCountWasInvalid(post.postId, 6))
    else
      Right(
        post.copy(
          title = normalizedTitle,
          summary = normalizedSummary,
          content = normalizedContent,
          imageRefs = imageRefs.sortBy(_.sortOrder),
          updatedAt = updatedAt
        )
      )


def archiveBlogPost(
    post: BlogPost,
    editorUserId: UserId,
    updatedAt: Instant
): Either[BlogError, BlogPost] =
  if post.authorUserId != editorUserId then Left(BlogError.BlogPostAuthorMismatch(post.postId, editorUserId))
  else
    Right(
      post.copy(
        status = BlogPostStatus.Archived,
        updatedAt = updatedAt
      )
    )

def blogPostIsPubliclyVisible(post: BlogPost): Boolean =
  post.status == BlogPostStatus.Published

def blogPostIsEditableBy(post: BlogPost, userId: UserId): Boolean =
  post.authorUserId == userId

def blogPostCanBeArchivedBy(post: BlogPost, userId: UserId): Boolean =
  post.authorUserId == userId && post.status != BlogPostStatus.Archived

def approveBlogPost(
    post: BlogPost,
    approvedAt: Instant
): BlogPost =
  post.copy(
    status = BlogPostStatus.Published,
    updatedAt = approvedAt,
    publishedAt = post.publishedAt.orElse(Some(approvedAt))
  )

def rejectBlogPost(
    post: BlogPost,
    rejectedAt: Instant
): BlogPost =
  post.copy(
    status = BlogPostStatus.Rejected,
    updatedAt = rejectedAt
  )


def createVisibleBlogComment(
    commentId: BlogCommentId,
    postId: BlogId,
    authorUserId: UserId,
    content: String,
    createdAt: Instant
): Either[BlogError, BlogComment] =
  val normalizedContent = content.trim
  if normalizedContent.isEmpty then Left(BlogError.BlogCommentContentWasInvalid(commentId))
  else
    Right(
      BlogComment(
        commentId = commentId,
        postId = postId,
        authorUserId = authorUserId,
        content = normalizedContent,
        status = BlogCommentStatus.Visible,
        createdAt = createdAt
      )
    )


def restorePersistedBlogComment(
    commentId: BlogCommentId,
    postId: BlogId,
    authorUserId: UserId,
    content: String,
    status: BlogCommentStatus,
    createdAt: Instant
): BlogComment =
  BlogComment(
    commentId = commentId,
    postId = postId,
    authorUserId = authorUserId,
    content = content,
    status = status,
    createdAt = createdAt
  )


def deleteBlogComment(
    comment: BlogComment,
    editorUserId: UserId
): Either[BlogError, BlogComment] =
  if comment.authorUserId != editorUserId then Left(BlogError.BlogCommentAuthorMismatch(comment.commentId, editorUserId))
  else
    Right(comment.copy(status = BlogCommentStatus.Deleted))

def blogCommentIsVisible(comment: BlogComment): Boolean =
  comment.status == BlogCommentStatus.Visible

def blogCommentIsDeletableBy(comment: BlogComment, userId: UserId): Boolean =
  comment.authorUserId == userId && comment.status == BlogCommentStatus.Visible


def createBlogLike(
    likeId: BlogLikeId,
    postId: BlogId,
    userId: UserId,
    createdAt: Instant
): BlogLike =
  BlogLike(
    likeId = likeId,
    postId = postId,
    userId = userId,
    createdAt = createdAt
  )


def createPendingReviewBlogPost(
    postId: BlogId,
    authorUserId: UserId,
    title: String,
    summary: String,
    content: String,
    imageRefs: List[BlogImageRef],
    createdAt: Instant
): Either[BlogError, BlogPost] =
  createPublishedBlogPost(postId, authorUserId, title, summary, content, imageRefs, createdAt)
