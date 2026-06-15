// 本文件只为 advertising 模块的后端内部值对象提供 JSON codec（例如 Instant、ManagerId）。它属于服务端基础设施层，不需要前端镜像。
package com.typesafe.travel.advertising.domain

import com.typesafe.travel.shared.kernel.ManagerId
import io.circe.{Decoder, Encoder}

import java.time.Instant
import scala.util.Try

object AdvertisementSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[ManagerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ManagerId] = Decoder.decodeString.map(ManagerId.apply)

