// UpdateTourGroupChatSettingsPlanner 是团体游模块的更新入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql
import com.typesafe.travel.tourgroup.domain.{TourGroupChatPlainSql, TourGroupPlannerPlainSql}

import java.sql.Connection
import java.time.Instant

object UpdateTourGroupChatSettingsPlanner extends ConnectionApiPlan[UpdateTourGroupChatSettingsPlannerInput, TourGroupChatSettingsResponse]:

  override val name: String = "UpdateTourGroupChatSettingsPlanner"

  override def plan(input: UpdateTourGroupChatSettingsPlannerInput, connection: Connection): IO[TourGroupChatSettingsResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      group <- TourGroupPlannerPlainSql.get(connection, GetTourGroupDetailsPlannerRequest(input.groupId))
      isOrganizer = group.group.organizerUserId == currentUser.userId
      response <- TourGroupChatPlainSql.updateChatSettings(connection, input.groupId, currentUser.userId, input.allowMemberDirectChat, isOrganizer, Instant.now())
    yield response
