// BlogPlannerModels 定义内容模块的请求和响应模型。

package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogSuggestionRequest(q: String)
object BlogSuggestionRequest:
  given sourceEncoder: Encoder[BlogSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionRequest] = deriveDecoder

final case class BlogImagePlannerResponse(
    imageId: String,
    publicUrl: String,
    originalFileName: String,
    contentType: String,
    byteSize: Long,
    sortOrder: Int
)
object BlogImagePlannerResponse:
  given sourceEncoder: Encoder[BlogImagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogImagePlannerResponse] = deriveDecoder

final case class BlogTagPlannerResponse(tagType: String, tagValue: String)
object BlogTagPlannerResponse:
  given sourceEncoder: Encoder[BlogTagPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogTagPlannerResponse] = deriveDecoder

final case class BlogPostSummaryResponse(
    postId: String,
    authorUserId: String,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    title: String,
    summary: String,
    coverImageUrl: Option[String],
    coverText: String,
    travelCity: Option[String],
    travelCities: List[String],
    status: String,
    createdAt: String,
    updatedAt: String,
    publishedAt: Option[String],
    commentCount: Long,
    likeCount: Long,
    favoriteCount: Long,
    likedByCurrentUser: Boolean,
    favoritedByCurrentUser: Boolean,
    isMyPost: Boolean,
    canEdit: Boolean,
    canArchive: Boolean,
    images: List[BlogImagePlannerResponse],
    tags: List[BlogTagPlannerResponse],
    searchResultSnippet: Option[String]
)
object BlogPostSummaryResponse:
  given sourceEncoder: Encoder[BlogPostSummaryResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostSummaryResponse] = deriveDecoder

final case class BlogCommentResponse(
    commentId: String,
    postId: String,
    authorUserId: String,
    authorDisplayName: String,
    authorAvatarUrl: Option[String],
    content: String,
    parentCommentId: Option[String],
    replyToUserId: Option[String],
    replyToDisplayName: Option[String],
    createdAt: String,
    likeCount: Long,
    likedByCurrentUser: Boolean,
    isMyComment: Boolean,
    canDelete: Boolean
)
object BlogCommentResponse:
  given sourceEncoder: Encoder[BlogCommentResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogCommentResponse] = deriveDecoder

final case class BlogPostResponse(post: BlogPostSummaryResponse, content: String, comments: List[BlogCommentResponse])
object BlogPostResponse:
  given sourceEncoder: Encoder[BlogPostResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostResponse] = deriveDecoder

final case class BlogPostListPlannerResponse(posts: List[BlogPostSummaryResponse])
object BlogPostListPlannerResponse:
  given sourceEncoder: Encoder[BlogPostListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostListPlannerResponse] = deriveDecoder

final case class ListBlogPostsPlannerRequest(
    scope: Option[String],
    q: Option[String],
    userId: Option[String],
    tagType: Option[String],
    tagValue: Option[String],
    travelCity: Option[String],
    travelCities: Option[List[String]]
)
object ListBlogPostsPlannerRequest:
  given sourceEncoder: Encoder[ListBlogPostsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogPostsPlannerRequest] = deriveDecoder

final case class BlogPostByIdPlannerRequest(postId: String, userId: Option[String])
object BlogPostByIdPlannerRequest:
  given sourceEncoder: Encoder[BlogPostByIdPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostByIdPlannerRequest] = deriveDecoder

final case class SaveBlogDraftPlannerRequest(
    userId: String,
    postId: Option[String],
    title: String,
    summary: String,
    coverText: String,
    content: String,
    images: List[BlogImagePlannerResponse],
    tags: List[BlogTagPlannerResponse],
    travelCity: Option[String],
    travelCities: Option[List[String]]
)
object SaveBlogDraftPlannerRequest:
  given sourceEncoder: Encoder[SaveBlogDraftPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SaveBlogDraftPlannerRequest] = deriveDecoder

final case class CreateBlogPostPlannerRequest(
    userId: String,
    title: String,
    summary: String,
    content: String,
    images: List[BlogImagePlannerResponse],
    tags: Option[List[BlogTagPlannerResponse]],
    coverText: Option[String],
    travelCity: Option[String],
    travelCities: Option[List[String]]
)
object CreateBlogPostPlannerRequest:
  given sourceEncoder: Encoder[CreateBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateBlogPostPlannerRequest] = deriveDecoder

final case class UpdateBlogPostPlannerRequest(
    postId: String,
    userId: String,
    title: String,
    summary: String,
    content: String,
    images: List[BlogImagePlannerResponse],
    tags: Option[List[BlogTagPlannerResponse]],
    coverText: Option[String],
    travelCity: Option[String],
    travelCities: Option[List[String]]
)
object UpdateBlogPostPlannerRequest:
  given sourceEncoder: Encoder[UpdateBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateBlogPostPlannerRequest] = deriveDecoder

final case class PublishBlogPostPlannerRequest(postId: String, userId: String)
object PublishBlogPostPlannerRequest:
  given sourceEncoder: Encoder[PublishBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[PublishBlogPostPlannerRequest] = deriveDecoder

final case class ModerateBlogPostPlannerRequest(postId: String)
object ModerateBlogPostPlannerRequest:
  given sourceEncoder: Encoder[ModerateBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ModerateBlogPostPlannerRequest] = deriveDecoder

final case class BlogCommentPlannerRequest(
    postId: String,
    userId: String,
    content: String,
    parentCommentId: Option[String],
    replyToUserId: Option[String]
)
object BlogCommentPlannerRequest:
  given sourceEncoder: Encoder[BlogCommentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogCommentPlannerRequest] = deriveDecoder

final case class BlogLikePlannerRequest(postId: String, userId: String)
object BlogLikePlannerRequest:
  given sourceEncoder: Encoder[BlogLikePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogLikePlannerRequest] = deriveDecoder

final case class BlogCommentLikePlannerRequest(commentId: String, userId: String)
object BlogCommentLikePlannerRequest:
  given sourceEncoder: Encoder[BlogCommentLikePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogCommentLikePlannerRequest] = deriveDecoder

final case class BlogFavoritePlannerRequest(postId: String, userId: String)
object BlogFavoritePlannerRequest:
  given sourceEncoder: Encoder[BlogFavoritePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogFavoritePlannerRequest] = deriveDecoder

final case class BlogUserInteractionPlannerRequest(userId: String, targetUserId: String)
object BlogUserInteractionPlannerRequest:
  given sourceEncoder: Encoder[BlogUserInteractionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogUserInteractionPlannerRequest] = deriveDecoder

final case class DeleteBlogCommentPlannerRequest(commentId: String, userId: String)
object DeleteBlogCommentPlannerRequest:
  given sourceEncoder: Encoder[DeleteBlogCommentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteBlogCommentPlannerRequest] = deriveDecoder

final case class BlogNotificationResponse(
    notificationId: String,
    receiverUserId: String,
    actorUserId: Option[String],
    actorDisplayName: Option[String],
    actorAvatarUrl: Option[String],
    notificationType: String,
    postId: Option[String],
    commentId: Option[String],
    content: String,
    isRead: Boolean,
    createdAt: String
)
object BlogNotificationResponse:
  given sourceEncoder: Encoder[BlogNotificationResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogNotificationResponse] = deriveDecoder

final case class BlogNotificationListPlannerResponse(notifications: List[BlogNotificationResponse])
object BlogNotificationListPlannerResponse:
  given sourceEncoder: Encoder[BlogNotificationListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogNotificationListPlannerResponse] = deriveDecoder

final case class ListBlogNotificationsPlannerRequest(userId: String)
object ListBlogNotificationsPlannerRequest:
  given sourceEncoder: Encoder[ListBlogNotificationsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogNotificationsPlannerRequest] = deriveDecoder

final case class BlogProfilePlannerRequest(viewerUserId: Option[String], profileUserId: String)
object BlogProfilePlannerRequest:
  given sourceEncoder: Encoder[BlogProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfilePlannerRequest] = deriveDecoder

final case class BlogProfilePlannerResponse(
    userId: String,
    nickname: String,
    avatarUrl: Option[String],
    followerCount: Long,
    followingCount: Long,
    receivedLikeCount: Long,
    isFollowing: Boolean,
    hideRelations: Boolean,
    relationListHidden: Boolean
)
object BlogProfilePlannerResponse:
  given sourceEncoder: Encoder[BlogProfilePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfilePlannerResponse] = deriveDecoder

final case class BlogProfileUserResponse(
    userId: String,
    nickname: String,
    avatarUrl: Option[String],
    followerCount: Long,
    followingCount: Long,
    receivedLikeCount: Long,
    isFollowing: Boolean,
    hideRelations: Boolean,
    relationListHidden: Boolean
)
object BlogProfileUserResponse:
  given sourceEncoder: Encoder[BlogProfileUserResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfileUserResponse] = deriveDecoder

final case class BlogProfileUserListPlannerResponse(users: List[BlogProfileUserResponse])
object BlogProfileUserListPlannerResponse:
  given sourceEncoder: Encoder[BlogProfileUserListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfileUserListPlannerResponse] = deriveDecoder

final case class ListBlogProfileUsersPlannerRequest(profileUserId: String, viewerUserId: Option[String])
object ListBlogProfileUsersPlannerRequest:
  given sourceEncoder: Encoder[ListBlogProfileUsersPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogProfileUsersPlannerRequest] = deriveDecoder

final case class UpdateBlogProfilePrivacyPlannerRequest(userId: String, hideRelations: Boolean)
object UpdateBlogProfilePrivacyPlannerRequest:
  given sourceEncoder: Encoder[UpdateBlogProfilePrivacyPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateBlogProfilePrivacyPlannerRequest] = deriveDecoder

final case class BlogSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object BlogSuggestionPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionPlannerResponse] = deriveDecoder

final case class BlogSuggestionListPlannerResponse(suggestions: List[BlogSuggestionPlannerResponse])
object BlogSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionListPlannerResponse] = deriveDecoder
