// ListBlogPostsPlannerRequest：博客域博客文章列表请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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