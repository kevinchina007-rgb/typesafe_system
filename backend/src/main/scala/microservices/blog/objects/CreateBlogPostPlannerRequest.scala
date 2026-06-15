// CreateBlogPostPlannerRequest：博客域创建博客文章请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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