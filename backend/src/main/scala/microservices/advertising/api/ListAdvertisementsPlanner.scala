// ListAdvertisementsPlanner 是广告模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection

object ListAdvertisementsPlanner extends ConnectionApiPlan[ListAdvertisementsRequest, ListAdvertisementsResponse]:
  override val name: String = "ListAdvertisementsPlanner"

  override def plan(input: ListAdvertisementsRequest, connection: Connection): IO[ListAdvertisementsResponse] =
    AdvertisementPlainSql.list(connection, input)
