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
        status = BlogPostStatus.Published,
        createdAt = createdAt,
        updatedAt = createdAt,
        publishedAt = Some(createdAt)
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
