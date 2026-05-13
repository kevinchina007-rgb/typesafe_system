package com.typesafe.travel.advertising.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.Connection
import java.time.Instant

object UploadAdvertisementImagePlanner extends ConnectionApiPlan[UploadAdvertisementImageRequest, UploadAdvertisementImageResponse]:
  override val name: String = "UploadAdvertisementImagePlanner"

  override def plan(input: UploadAdvertisementImageRequest, connection: Connection): IO[UploadAdvertisementImageResponse] =
    AdvertisementPlainSql.uploadImage(connection, input, Instant.now())

