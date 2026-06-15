// SaveBlogDraftPlannerRequest：博客域保存博客草稿请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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