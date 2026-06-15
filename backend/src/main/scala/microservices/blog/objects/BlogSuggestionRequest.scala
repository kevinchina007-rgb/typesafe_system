// BlogSuggestionRequest：博客域博客推荐请求对象。

package com.typesafe.travel.blog.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class BlogSuggestionRequest(q: String)
object BlogSuggestionRequest:
  given sourceEncoder: Encoder[BlogSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlogSuggestionRequest] = deriveDecoder