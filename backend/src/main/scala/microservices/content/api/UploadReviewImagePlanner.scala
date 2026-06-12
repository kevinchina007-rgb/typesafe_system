// UploadReviewImagePlanner 是内容模块的上传入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.content.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object UploadReviewImagePlanner extends ConnectionApiPlan[UploadReviewImagePlannerRequest, ContentImagePlannerResponse]:
  override val name: String = "UploadReviewImagePlanner"
  override def plan(input: UploadReviewImagePlannerRequest, connection: Connection): IO[ContentImagePlannerResponse] =
    IO.realTimeInstant.flatMap(now => ReviewPlannerPlainSql.uploadImage(connection, input, now))
