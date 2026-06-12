// GetAdvertisementDeliverySettingsPlanner 是广告模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object GetAdvertisementDeliverySettingsPlanner extends ConnectionApiPlan[GetAdvertisementDeliverySettingsRequest, AdvertisementDeliverySettingsResponse]:
  override val name: String = "GetAdvertisementDeliverySettingsPlanner"

  override def plan(input: GetAdvertisementDeliverySettingsRequest, connection: Connection): IO[AdvertisementDeliverySettingsResponse] =
    AdvertisementPlainSql.getDeliverySettings(connection, input, Instant.now())
