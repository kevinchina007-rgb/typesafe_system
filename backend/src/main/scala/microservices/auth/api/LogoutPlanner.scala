// LogoutPlanner 鏄璇佹ā鍧楃殑閫€鍑虹櫥褰曞叆鍙ｏ紝璐熻矗璇锋眰鏍￠獙銆佹祦绋嬬紪鎺掑拰缁撴灉杩斿洖銆?
package com.typesafe.travel.auth.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql

import java.sql.Connection

object LogoutPlanner extends ConnectionApiPlan[SessionPlannerRequest, AuthStatusPlannerResponse]:
  override val name: String = "LogoutPlanner"
  override def plan(input: SessionPlannerRequest, connection: Connection): IO[AuthStatusPlannerResponse] =
    AuthPlannerPlainSql.logout(connection, input.sessionId)


