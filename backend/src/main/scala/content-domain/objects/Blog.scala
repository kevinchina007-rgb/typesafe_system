package com.typesafe.travel.content.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

final case class BlogPostStatus(value: String):
  override def toString: String = value

object BlogPostStatus:
  val Draft: BlogPostStatus = BlogPostStatus("Draft")
  val PendingReview: BlogPostStatus = BlogPostStatus("PendingReview")
  val Published: BlogPostStatus = BlogPostStatus("Published")
  val Rejected: BlogPostStatus = BlogPostStatus("Rejected")
  val Archived: BlogPostStatus = BlogPostStatus("Archived")

  def fromText(value: String): BlogPostStatus =
    value.trim.toLowerCase match
      case "pendingreview" | "pending-review" => PendingReview
      case "rejected" => Rejected
      case "draft" => Draft
      case "archived" => Archived
      case _ => Published

final case class BlogCommentStatus(value: String):
  override def toString: String = value

object BlogCommentStatus:
  val Visible: BlogCommentStatus = BlogCommentStatus("Visible")
  val Deleted: BlogCommentStatus = BlogCommentStatus("Deleted")

  def fromText(value: String): BlogCommentStatus =
    value.trim.toLowerCase match
      case "deleted" => Deleted
      case _ => Visible

sealed trait BlogError extends DomainError:
  def message: String

object BlogError:
  final case class BlogPostWasNotFound(postId: BlogId) extends BlogError:
    override val message: String = s"Blog post '${postId.value}' was not found"

  final case class BlogPostWasNotPublished(postId: BlogId, status: BlogPostStatus) extends BlogError:
    override val message: String = s"Blog post '${postId.value}' is not visible in status $status"

  final case class BlogPostTitleWasInvalid(postId: BlogId) extends BlogError:
    override val message: String = s"Blog post '${postId.value}' must have a title"

  final case class BlogPostSummaryWasInvalid(postId: BlogId) extends BlogError:
    override val message: String = s"Blog post '${postId.value}' must have a summary"

  final case class BlogPostContentWasInvalid(postId: BlogId) extends BlogError:
    override val message: String = s"Blog post '${postId.value}' must have content"

  final case class BlogCommentContentWasInvalid(commentId: BlogCommentId) extends BlogError:
    override val message: String = s"Blog comment '${commentId.value}' must have content"

  final case class BlogCommentWasNotFound(commentId: BlogCommentId) extends BlogError:
    override val message: String = s"Blog comment '${commentId.value}' was not found"

  final case class BlogPostAuthorMismatch(postId: BlogId, userId: UserId) extends BlogError:
    override val message: String = s"User '${userId.value}' cannot manage blog post '${postId.value}'"

  final case class BlogCommentAuthorMismatch(commentId: BlogCommentId, userId: UserId) extends BlogError:
    override val message: String = s"User '${userId.value}' cannot manage blog comment '${commentId.value}'"

  final case class BlogImageCountWasInvalid(postId: BlogId, maximumImageCount: Int) extends BlogError:
    override val message: String = s"Blog post '${postId.value}' exceeded the maximum of $maximumImageCount images"

final case class BlogImageRef(
    imageId: BlogImageId,
    publicUrl: String,
    originalFileName: String,
    sortOrder: Int,
    createdAt: Instant
)

final case class BlogPost(
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

final case class BlogComment(
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
