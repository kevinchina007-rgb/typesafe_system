// UpdateSiteAdminManagerProfilePlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.SiteAdminManagerPlainSql

import java.sql.Connection

object UpdateSiteAdminManagerProfilePlanner extends ConnectionApiPlan[UpdateSiteAdminManagerProfilePlannerRequest, ManagerSessionPlannerResponse]:
  override val name: String = "UpdateSiteAdminManagerProfilePlanner"
  override def plan(input: UpdateSiteAdminManagerProfilePlannerRequest, connection: Connection): IO[ManagerSessionPlannerResponse] =
    for
      _ <- validateUpdateSiteAdminProfile(input)
      updated <- SiteAdminManagerPlainSql.updateSiteAdminProfile(connection, input.managerId, input.displayName, input.logoAssetPath)
    yield updated
