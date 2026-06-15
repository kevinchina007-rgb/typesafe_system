// 本文件定义 UploadUserAvatarPlanner，是 identity 模块的头像上传入口，只负责请求校验、流程编排和结果返回。
package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection

object UploadUserAvatarPlanner extends ConnectionApiPlan[UploadUserAvatarPlannerRequest, UserPlannerResponse]:
  override val name: String = "UploadUserAvatarPlanner"

  override def plan(input: UploadUserAvatarPlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    UserPlannerPlainSql.uploadAvatar(connection, input)
