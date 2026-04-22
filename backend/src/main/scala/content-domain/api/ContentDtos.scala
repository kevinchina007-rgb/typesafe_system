package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.*
import com.typesafe.travel.content.domain.{BlogImageRef, ReviewImageRef}

final case class ContentImageResponseDto(
    imageId: String,
    publicUrl: String,
    originalFileName: String,
    sortOrder: Int,
    createdAt: String
)

final case class CreateBlogPostRequestDto(
    userId: String,
    title: String,
    summary: String,
    content: String,
    images: List[ContentImageResponseDto]
)

final case class UpdateBlogPostRequestDto(
    userId: String,
    title: String,
    summary: String,
    content: String,
    images: List[ContentImageResponseDto]
)

final case class CreateBlogCommentRequestDto(userId: String, content: String)
final case class BlogLikeRequestDto(userId: String)
final case class DeleteBlogCommentRequestDto(userId: String)

final case class BlogCommentResponseDto(
    commentId: String,
    postId: String,
    authorUserId: String,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    content: String,
    createdAt: String,
    isMyComment: Boolean,
    canDelete: Boolean
)

final case class BlogPostSummaryResponseDto(
    postId: String,
    authorUserId: String,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    title: String,
    summary: String,
    status: String,
    createdAt: String,
    updatedAt: String,
    publishedAt: Option[String],
    commentCount: Int,
    likeCount: Int,
    likedByCurrentUser: Boolean,
    isMyPost: Boolean,
    canEdit: Boolean,
    canArchive: Boolean,
    images: List[ContentImageResponseDto],
    searchResultSnippet: Option[String]
)

final case class BlogPostResponseDto(post: BlogPostSummaryResponseDto, content: String, comments: List[BlogCommentResponseDto])
final case class BlogPostListResponseDto(posts: List[BlogPostSummaryResponseDto])

object BlogCommentResponseDto:
  def fromView(view: BlogCommentView): BlogCommentResponseDto =
    BlogCommentResponseDto(
      commentId = view.commentId.value,
      postId = view.postId.value,
      authorUserId = view.authorUserId.value,
      authorDisplayName = view.authorDisplayName,
      authorAvatarUrl = view.authorAvatarUrl,
      content = view.content,
      createdAt = view.createdAt.toString,
      isMyComment = view.isMyComment,
      canDelete = view.canDelete
    )

object BlogPostSummaryResponseDto:
  def fromView(view: BlogPostView): BlogPostSummaryResponseDto =
    BlogPostSummaryResponseDto(
      postId = view.postId.value,
      authorUserId = view.authorUserId.value,
      authorDisplayName = view.authorDisplayName,
      authorAvatarUrl = view.authorAvatarUrl,
      title = view.title,
      summary = view.summary,
      status = view.status,
      createdAt = view.createdAt.toString,
      updatedAt = view.updatedAt.toString,
      publishedAt = view.publishedAt.map(_.toString),
      commentCount = view.commentCount,
      likeCount = view.likeCount,
      likedByCurrentUser = view.likedByCurrentUser,
      isMyPost = view.isMyPost,
      canEdit = view.canEdit,
      canArchive = view.canArchive,
      images = view.imageRefs.map(ContentImageResponseDto.fromBlogImageRef),
      searchResultSnippet = view.searchResultSnippet
    )

object BlogPostResponseDto:
  def fromView(view: BlogPostDetailsView): BlogPostResponseDto =
    BlogPostResponseDto(
      post = BlogPostSummaryResponseDto.fromView(view.post),
      content = view.content,
      comments = view.comments.map(BlogCommentResponseDto.fromView)
    )

final case class CreateReviewRequestDto(
    userId: String,
    orderId: String,
    orderItemId: String,
    rating: Int,
    title: String,
    content: String,
    images: List[ContentImageResponseDto]
)

final case class UpdateReviewRequestDto(
    userId: String,
    rating: Int,
    title: String,
    content: String,
    images: List[ContentImageResponseDto]
)

final case class DeleteReviewRequestDto(userId: String)

final case class ResourceReviewSummaryResponseDto(
    resourceType: String,
    resourceId: String,
    averageRating: String,
    reviewCount: Int
)

final case class ReviewEligibilityResponseDto(
    orderId: String,
    orderItemId: String,
    canReview: Boolean,
    alreadyReviewed: Boolean,
    reason: Option[String],
    resourceSummaryTitle: String
)

final case class ReviewResponseDto(
    reviewId: String,
    authorUserId: String,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    resourceType: String,
    resourceId: String,
    resourceSummaryTitle: String,
    resourceSummarySubtitle: String,
    orderId: String,
    orderItemId: String,
    rating: Int,
    title: String,
    content: String,
    status: String,
    createdAt: String,
    updatedAt: String,
    isMyReview: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    images: List[ContentImageResponseDto]
)

final case class ReviewListResponseDto(reviews: List[ReviewResponseDto])
final case class CreateReviewFeedbackThreadRequestDto(reviewId: String)
final case class SendFeedbackMessageRequestDto(senderRole: String, senderDisplayName: String, body: String)
final case class MarkFeedbackThreadReadRequestDto(audience: String)
final case class EscalateFeedbackThreadRequestDto(senderDisplayName: String, body: String)

final case class FeedbackMessageResponseDto(
    messageId: String,
    senderRole: String,
    senderDisplayName: String,
    body: String,
    sentAt: String
)

final case class FeedbackThreadResponseDto(
    threadId: String,
    kind: String,
    managerType: String,
    ownerUserId: Option[String],
    ownerUserDisplayName: String,
    title: String,
    subtitle: String,
    resourceType: String,
    resourceSummaryTitle: String,
    orderId: Option[String],
    orderItemId: Option[String],
    reviewId: Option[String],
    relatedThreadId: Option[String],
    unreadByUser: Int,
    unreadByManager: Int,
    unreadBySiteAdmin: Int,
    createdAt: String,
    updatedAt: String,
    messages: List[FeedbackMessageResponseDto]
)

final case class FeedbackThreadListResponseDto(threads: List[FeedbackThreadResponseDto])

object ContentImageResponseDto:
  def fromBlogImageRef(imageRef: BlogImageRef): ContentImageResponseDto =
    ContentImageResponseDto(
      imageId = imageRef.imageId.value,
      publicUrl = imageRef.publicUrl,
      originalFileName = imageRef.originalFileName,
      sortOrder = imageRef.sortOrder,
      createdAt = imageRef.createdAt.toString
    )

  def fromReviewImageRef(imageRef: ReviewImageRef): ContentImageResponseDto =
    ContentImageResponseDto(
      imageId = imageRef.imageId.value,
      publicUrl = imageRef.publicUrl,
      originalFileName = imageRef.originalFileName,
      sortOrder = imageRef.sortOrder,
      createdAt = imageRef.createdAt.toString
    )

object ReviewEligibilityResponseDto:
  def fromView(view: ReviewEligibilityView): ReviewEligibilityResponseDto =
    ReviewEligibilityResponseDto(
      orderId = view.orderId.value,
      orderItemId = view.orderItemId.value,
      canReview = view.canReview,
      alreadyReviewed = view.alreadyReviewed,
      reason = view.reason,
      resourceSummaryTitle = view.resourceSummaryTitle
    )

object ReviewResponseDto:
  def fromView(view: ReviewView): ReviewResponseDto =
    ReviewResponseDto(
      reviewId = view.reviewId.value,
      authorUserId = view.authorUserId.value,
      authorDisplayName = view.authorDisplayName,
      authorAvatarUrl = view.authorAvatarUrl,
      resourceType = view.resourceType,
      resourceId = view.resourceId,
      resourceSummaryTitle = view.resourceSummaryTitle,
      resourceSummarySubtitle = view.resourceSummarySubtitle,
      orderId = view.orderId.value,
      orderItemId = view.orderItemId.value,
      rating = view.rating,
      title = view.title,
      content = view.content,
      status = view.status,
      createdAt = view.createdAt.toString,
      updatedAt = view.updatedAt.toString,
      isMyReview = view.isMyReview,
      canEdit = view.canEdit,
      canDelete = view.canDelete,
      images = view.imageRefs.map(ContentImageResponseDto.fromReviewImageRef)
    )

object ResourceReviewSummaryResponseDto:
  def fromView(view: ResourceReviewSummaryView): ResourceReviewSummaryResponseDto =
    ResourceReviewSummaryResponseDto(
      resourceType = view.resourceType.toString,
      resourceId = view.resourceId,
      averageRating = view.averageRating.toString(),
      reviewCount = view.reviewCount
    )

object FeedbackMessageResponseDto:
  def fromView(view: FeedbackMessageView): FeedbackMessageResponseDto =
    FeedbackMessageResponseDto(
      messageId = view.messageId,
      senderRole = view.senderRole,
      senderDisplayName = view.senderDisplayName,
      body = view.body,
      sentAt = view.sentAt.toString
    )

object FeedbackThreadResponseDto:
  def fromView(view: FeedbackThreadView): FeedbackThreadResponseDto =
    FeedbackThreadResponseDto(
      threadId = view.threadId,
      kind = view.kind,
      managerType = view.managerType,
      ownerUserId = view.ownerUserId,
      ownerUserDisplayName = view.ownerUserDisplayName,
      title = view.title,
      subtitle = view.subtitle,
      resourceType = view.resourceType,
      resourceSummaryTitle = view.resourceSummaryTitle,
      orderId = view.orderId,
      orderItemId = view.orderItemId,
      reviewId = view.reviewId,
      relatedThreadId = view.relatedThreadId,
      unreadByUser = view.unreadByUser,
      unreadByManager = view.unreadByManager,
      unreadBySiteAdmin = view.unreadBySiteAdmin,
      createdAt = view.createdAt.toString,
      updatedAt = view.updatedAt.toString,
      messages = view.messages.map(FeedbackMessageResponseDto.fromView)
    )
