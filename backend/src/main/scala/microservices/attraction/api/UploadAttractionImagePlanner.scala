// UploadAttractionImagePlanner 只负责 attraction 图片上传入口，前端只镜像请求/响应对象与调用方式，不镜像二进制保存细节。
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import java.sql.Connection

object UploadAttractionImagePlanner extends ConnectionApiPlan[UploadAttractionImagePlannerRequest, UploadAttractionImagePlannerResponse]:
  override val name: String = "UploadAttractionImagePlanner"
  override def plan(input: UploadAttractionImagePlannerRequest, connection: Connection): IO[UploadAttractionImagePlannerResponse] =
    AttractionImagePlanner.uploadImage(connection, input)




