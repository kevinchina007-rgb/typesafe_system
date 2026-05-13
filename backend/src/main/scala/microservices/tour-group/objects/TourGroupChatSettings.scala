package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant
import TourGroupSourceJsonCodecs.given

final case class TourGroupChatSettings(
    groupId: TourGroupId,
    allowMemberDirectChat: Boolean,
    updatedAt: Instant,
    updatedByUserId: UserId
)

object TourGroupChatSettings:
  given sourceEncoder: Encoder[TourGroupChatSettings] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupChatSettings] = deriveDecoder

