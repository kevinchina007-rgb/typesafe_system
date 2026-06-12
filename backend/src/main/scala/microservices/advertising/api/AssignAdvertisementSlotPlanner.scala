// AssignAdvertisementSlotPlanner 是广告模块的分配入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object AssignAdvertisementSlotPlanner extends ConnectionApiPlan[AdvertisementSlotAssignmentRequest, AdvertisementResponse]:
  override val name: String = "AssignAdvertisementSlotPlanner"

  override def plan(input: AdvertisementSlotAssignmentRequest, connection: Connection): IO[AdvertisementResponse] =
    AdvertisementPlainSql.assignSlot(connection, input, Instant.now())

