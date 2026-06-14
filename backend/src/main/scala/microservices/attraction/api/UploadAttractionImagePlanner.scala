// UploadAttractionImagePlanner 是景点模块的上传入口，负责请求校验、流程编排和结果返回�?
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.attraction.api.AttractionImagePlanner

import java.sql.Connection

object UploadAttractionImagePlanner extends ConnectionApiPlan[UploadAttractionImagePlannerRequest, UploadAttractionImagePlannerResponse]:
  override val name: String = "UploadAttractionImagePlanner"
  override def plan(input: UploadAttractionImagePlannerRequest, connection: Connection): IO[UploadAttractionImagePlannerResponse] =
    AttractionImagePlanner.uploadImage(connection, input)




