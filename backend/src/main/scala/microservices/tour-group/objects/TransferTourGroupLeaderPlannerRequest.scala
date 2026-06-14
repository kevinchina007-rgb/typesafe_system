// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TransferTourGroupLeaderPlannerRequest(groupId: String, organizerUserId: String, targetUserId: String)
object TransferTourGroupLeaderPlannerRequest:
  given sourceEncoder: Encoder[TransferTourGroupLeaderPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TransferTourGroupLeaderPlannerRequest] = deriveDecoder
