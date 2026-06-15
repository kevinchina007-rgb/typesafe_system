// 本文件定义 UpdateUserProfilePlanner，是 identity 模块的用户资料更新入口，只负责请求校验、流程编排和结果返回。
package com.typesafe.travel.identity.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.identity.UserPlannerPlainSql

import java.sql.Connection

object UpdateUserProfilePlanner extends ConnectionApiPlan[UpdateUserProfilePlannerRequest, UserPlannerResponse]:
  override val name: String = "UpdateUserProfilePlanner"

  override def plan(input: UpdateUserProfilePlannerRequest, connection: Connection): IO[UserPlannerResponse] =
    val normalizedNickname = input.nickname.trim
    val normalizedPhone = input.phone.trim
    if normalizedNickname.isEmpty then IO.raiseError(new IllegalArgumentException("昵称不能为空"))
    else if normalizedPhone.isEmpty then IO.raiseError(new IllegalArgumentException("手机号不能为空"))
    else UserPlannerPlainSql.updateProfile(connection, input.copy(nickname = normalizedNickname, phone = normalizedPhone))
