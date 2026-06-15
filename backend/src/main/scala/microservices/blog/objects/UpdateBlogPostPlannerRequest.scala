// UpdateBlogPostPlannerRequest：博客域更新博客文章请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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