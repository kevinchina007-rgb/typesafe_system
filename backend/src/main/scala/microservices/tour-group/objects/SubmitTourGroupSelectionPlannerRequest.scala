// TourGroupPlannerModels 定义团体游模块的请求和响应模型。

package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class SubmitTourGroupSelectionPlannerRequest(userId: String, selectionId: String)
object SubmitTourGroupSelectionPlannerRequest:
  given sourceEncoder: Encoder[SubmitTourGroupSelectionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SubmitTourGroupSelectionPlannerRequest] = deriveDecoder
