// 本文件是广告投放设置读取入口，只负责查询和返回投放设置。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object GetAdvertisementDeliverySettingsPlanner extends ConnectionApiPlan[GetAdvertisementDeliverySettingsRequest, AdvertisementDeliverySettingsResponse]:
  override val name: String = "GetAdvertisementDeliverySettingsPlanner"

  override def plan(input: GetAdvertisementDeliverySettingsRequest, connection: Connection): IO[AdvertisementDeliverySettingsResponse] =
    AdvertisementPlainSql.getDeliverySettings(connection, input, Instant.now())
