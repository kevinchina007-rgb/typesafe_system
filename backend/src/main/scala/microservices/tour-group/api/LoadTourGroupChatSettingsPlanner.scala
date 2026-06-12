// LoadTourGroupChatSettingsPlanner 是团体游模块的业务入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql
import com.typesafe.travel.tourgroup.domain.{TourGroupChatPlainSql, TourGroupPlannerPlainSql}

import java.sql.Connection
import java.time.Instant

object LoadTourGroupChatSettingsPlanner extends ConnectionApiPlan[LoadTourGroupChatSettingsPlannerRequest, TourGroupChatSettingsPlannerResponse]:

  override val name: String = "LoadTourGroupChatSettingsPlanner"

  override def plan(input: LoadTourGroupChatSettingsPlannerRequest, connection: Connection): IO[TourGroupChatSettingsPlannerResponse] =
    for
      currentUser <- AuthPlannerPlainSql.currentUser(connection, input.sessionId, Instant.now())
      group <- TourGroupPlannerPlainSql.get(connection, TourGroupByIdPlannerRequest(input.groupId))
      isOrganizer = group.group.organizerUserId == currentUser.userId
      response <- TourGroupChatPlainSql.loadChatSettings(connection, input.groupId, currentUser.userId, isOrganizer, Instant.now())
    yield response
