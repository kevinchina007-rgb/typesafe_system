// GetHotelDetailsPlanner 是酒店模块的获取入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.hotel.tables.GetHotelDetailsPlannerPlainSql
import com.typesafe.travel.shared.kernel.StayPeriod

import java.sql.Connection
import java.time.LocalDate

object GetHotelDetailsPlanner extends ConnectionApiPlan[HotelDetailsPlannerRequest, HotelPlannerResponse]:
  override val name: String = "GetHotelDetailsPlanner"

  override def plan(input: HotelDetailsPlannerRequest, connection: Connection): IO[HotelPlannerResponse] =
    val stayPeriod = parseStayPeriod(input.checkInDate, input.checkOutDate)
    for
      maybeHotel <- GetHotelDetailsPlannerPlainSql.details(connection, input)
      hotel <- IO.fromOption(maybeHotel)(new IllegalArgumentException(s"Hotel '${input.hotelId}' was not found"))
    yield GetHotelDetailsPlannerResponseMapper.toHotelPlannerResponse(hotel, stayPeriod)

  private def parseStayPeriod(checkInDate: Option[String], checkOutDate: Option[String]): Option[StayPeriod] =
    (checkInDate.map(_.trim).filter(_.nonEmpty), checkOutDate.map(_.trim).filter(_.nonEmpty)) match
      case (Some(checkInText), Some(checkOutText)) =>
        Some(
          StayPeriod
            .create(LocalDate.parse(checkInText), LocalDate.parse(checkOutText))
            .fold(throw _, identity)
        )
      case _ => None
