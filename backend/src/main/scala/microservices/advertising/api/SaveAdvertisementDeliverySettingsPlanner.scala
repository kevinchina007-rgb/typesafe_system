// 本文件是广告投放设置保存入口，只负责写入投放设置。
package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object SaveAdvertisementDeliverySettingsPlanner extends ConnectionApiPlan[SaveAdvertisementDeliverySettingsRequest, AdvertisementDeliverySettingsResponse]:
  override val name: String = "SaveAdvertisementDeliverySettingsPlanner"

  override def plan(input: SaveAdvertisementDeliverySettingsRequest, connection: Connection): IO[AdvertisementDeliverySettingsResponse] =
    if input.rotationIntervalSeconds < 1 then
      IO.raiseError(new IllegalArgumentException("rotation_interval_seconds_invalid"))
    else
      AdvertisementPlainSql.saveDeliverySettings(connection, input, Instant.now())
