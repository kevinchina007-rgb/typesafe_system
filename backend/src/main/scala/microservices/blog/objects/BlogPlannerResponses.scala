package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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

final case class BlogSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object BlogSuggestionPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionPlannerResponse] = deriveDecoder

final case class BlogSuggestionListPlannerResponse(suggestions: List[BlogSuggestionPlannerResponse])
object BlogSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionListPlannerResponse] = deriveDecoder

