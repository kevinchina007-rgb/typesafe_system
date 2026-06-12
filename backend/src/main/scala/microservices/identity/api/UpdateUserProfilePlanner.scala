// UpdateUserProfilePlanner 是身份模块的更新入口，负责请求校验、流程编排和结果返回。

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
    if normalizedNickname.isEmpty then IO.raiseError(new IllegalArgumentException("閺勭數袨娑撳秷鍏樻稉铏光敄"))
    else if normalizedPhone.isEmpty then IO.raiseError(new IllegalArgumentException("闁归潧顑嗗┃鈧柛娆掓腹缁楀鎳楅幋鎺曠缂?"))
    else UserPlannerPlainSql.updateProfile(connection, input.copy(nickname = normalizedNickname, phone = normalizedPhone))
