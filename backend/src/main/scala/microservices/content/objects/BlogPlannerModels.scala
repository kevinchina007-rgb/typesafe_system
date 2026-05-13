package com.typesafe.travel.content.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogSuggestionRequest(q: String)
object BlogSuggestionRequest:
  given sourceEncoder: Encoder[BlogSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionRequest] = deriveDecoder

final case class ListBlogPostsPlannerRequest(scope: Option[String], q: Option[String], userId: Option[String])
object ListBlogPostsPlannerRequest:
  given sourceEncoder: Encoder[ListBlogPostsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListBlogPostsPlannerRequest] = deriveDecoder

final case class BlogPostByIdPlannerRequest(postId: String, userId: Option[String])
object BlogPostByIdPlannerRequest:
  given sourceEncoder: Encoder[BlogPostByIdPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostByIdPlannerRequest] = deriveDecoder

final case class CreateBlogPostPlannerRequest(userId: String, title: String, summary: String, content: String, images: List[BlogImageRef])
object CreateBlogPostPlannerRequest:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[CreateBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateBlogPostPlannerRequest] = deriveDecoder

final case class UpdateBlogPostPlannerRequest(postId: String, userId: String, title: String, summary: String, content: String, images: List[BlogImageRef])
object UpdateBlogPostPlannerRequest:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[UpdateBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateBlogPostPlannerRequest] = deriveDecoder

final case class ModerateBlogPostPlannerRequest(postId: String)
object ModerateBlogPostPlannerRequest:
  given sourceEncoder: Encoder[ModerateBlogPostPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ModerateBlogPostPlannerRequest] = deriveDecoder

final case class BlogCommentPlannerRequest(postId: String, userId: String, content: String)
object BlogCommentPlannerRequest:
  given sourceEncoder: Encoder[BlogCommentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogCommentPlannerRequest] = deriveDecoder

final case class BlogLikePlannerRequest(postId: String, userId: String)
object BlogLikePlannerRequest:
  given sourceEncoder: Encoder[BlogLikePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogLikePlannerRequest] = deriveDecoder

final case class DeleteBlogCommentPlannerRequest(commentId: String, userId: String)
object DeleteBlogCommentPlannerRequest:
  given sourceEncoder: Encoder[DeleteBlogCommentPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteBlogCommentPlannerRequest] = deriveDecoder

final case class BlogPostListPlannerResponse(posts: List[BlogPost])
object BlogPostListPlannerResponse:
  import ContentSourceJsonCodecs.given
  given sourceEncoder: Encoder[BlogPostListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogPostListPlannerResponse] = deriveDecoder

final case class BlogSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object BlogSuggestionPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionPlannerResponse] = deriveDecoder

final case class BlogSuggestionListPlannerResponse(suggestions: List[BlogSuggestionPlannerResponse])
object BlogSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[BlogSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionListPlannerResponse] = deriveDecoder
