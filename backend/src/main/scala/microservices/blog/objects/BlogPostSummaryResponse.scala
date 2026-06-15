// BlogPostSummaryResponse：博客域博客文章摘要返回对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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