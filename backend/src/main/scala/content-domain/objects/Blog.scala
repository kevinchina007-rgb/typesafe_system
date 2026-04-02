package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum BlogPostStatus:
  case Draft, Published, Archived

enum BlogCommentStatus:
  case Visible, Deleted

enum BlogError(val message: String) extends DomainError:
  case BlogPostWasNotFound(postId: BlogId)
      extends BlogError(s"Blog post '${postId.value}' was not found")
  case BlogPostWasNotPublished(postId: BlogId, status: BlogPostStatus)
      extends BlogError(s"Blog post '${postId.value}' is not visible in status $status")
  case BlogPostTitleWasInvalid(postId: BlogId)
      extends BlogError(s"Blog post '${postId.value}' must have a title")
  case BlogPostSummaryWasInvalid(postId: BlogId)
      extends BlogError(s"Blog post '${postId.value}' must have a summary")
  case BlogPostContentWasInvalid(postId: BlogId)
      extends BlogError(s"Blog post '${postId.value}' must have content")
  case BlogCommentContentWasInvalid(commentId: BlogCommentId)
      extends BlogError(s"Blog comment '${commentId.value}' must have content")
  case BlogCommentWasNotFound(commentId: BlogCommentId)
      extends BlogError(s"Blog comment '${commentId.value}' was not found")
  case BlogPostAuthorMismatch(postId: BlogId, userId: UserId)
      extends BlogError(s"User '${userId.value}' cannot manage blog post '${postId.value}'")
  case BlogCommentAuthorMismatch(commentId: BlogCommentId, userId: UserId)
      extends BlogError(s"User '${userId.value}' cannot manage blog comment '${commentId.value}'")
  case BlogImageCountWasInvalid(postId: BlogId, maximumImageCount: Int)
      extends BlogError(s"Blog post '${postId.value}' exceeded the maximum of $maximumImageCount images")

final case class BlogImageRef(
    imageId: BlogImageId,
    publicUrl: String,
    originalFileName: String,
    sortOrder: Int,
    createdAt: Instant
)

final case class BlogPost private[domain] (
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
):
  def isPubliclyVisible: Boolean =
    status == BlogPostStatus.Published

  def isEditableBy(userId: UserId): Boolean =
    authorUserId == userId

  def canBeArchivedBy(userId: UserId): Boolean =
    authorUserId == userId && status != BlogPostStatus.Archived

final case class BlogComment private[domain] (
    commentId: BlogCommentId,
    postId: BlogId,
    authorUserId: UserId,
    content: String,
    status: BlogCommentStatus,
    createdAt: Instant
):
  def isVisible: Boolean =
    status == BlogCommentStatus.Visible

  def isDeletableBy(userId: UserId): Boolean =
    authorUserId == userId && status == BlogCommentStatus.Visible

final case class BlogLike(
    likeId: BlogLikeId,
    postId: BlogId,
    userId: UserId,
    createdAt: Instant
)

