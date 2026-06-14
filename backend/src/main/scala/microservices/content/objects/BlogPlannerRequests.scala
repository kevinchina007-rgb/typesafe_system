package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogSuggestionRequest(q: String)
object BlogSuggestionRequest:
  given sourceEncoder: Encoder[BlogSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionRequest] = deriveDecoder

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

final case class ListBlogNotificationsPlannerRequest(userId: String)
object ListBlogNotificationsPlannerRequest:
  given sourceEncoder: Encoder[ListBlogNotificationsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogNotificationsPlannerRequest] = deriveDecoder

final case class BlogProfilePlannerRequest(viewerUserId: Option[String], profileUserId: String)
object BlogProfilePlannerRequest:
  given sourceEncoder: Encoder[BlogProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogProfilePlannerRequest] = deriveDecoder

final case class ListBlogProfileUsersPlannerRequest(profileUserId: String, viewerUserId: Option[String])
object ListBlogProfileUsersPlannerRequest:
  given sourceEncoder: Encoder[ListBlogProfileUsersPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogProfileUsersPlannerRequest] = deriveDecoder

final case class UpdateBlogProfilePrivacyPlannerRequest(userId: String, hideRelations: Boolean)
object UpdateBlogProfilePrivacyPlannerRequest:
  given sourceEncoder: Encoder[UpdateBlogProfilePrivacyPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateBlogProfilePrivacyPlannerRequest] = deriveDecoder

