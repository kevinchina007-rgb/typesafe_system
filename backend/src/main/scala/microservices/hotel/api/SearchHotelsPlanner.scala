// SearchHotelsPlanner 是酒店模块的搜索入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.hotel.tables.SearchHotelsPlannerPlainSql
import com.typesafe.travel.shared.kernel.HotelLocation
import com.typesafe.travel.shared.kernel.StayPeriod

import java.sql.Connection
import java.time.LocalDate

object SearchHotelsPlanner extends ConnectionApiPlan[HotelSearchPlannerRequest, HotelListPlannerResponse]:
  override val name: String = "SearchHotelsPlanner"

  override def plan(input: HotelSearchPlannerRequest, connection: Connection): IO[HotelListPlannerResponse] =
    val locationFilter = input.location.map(_.trim).filter(_.nonEmpty).map(HotelLocation.unsafe)
    val stayPeriod = parseStayPeriod(input.checkInDate, input.checkOutDate)
    for
      hotels <- SearchHotelsPlannerPlainSql.list(connection, input)
    yield HotelListPlannerResponse(
      hotels
        .filter(hotel => hotelIsSearchMatch(hotel, locationFilter, stayPeriod))
        .map(hotel => HotelPlannerResponseMapper.toHotelPlannerResponse(hotel, stayPeriod))
    )

  private def parseStayPeriod(checkInDate: Option[String], checkOutDate: Option[String]): Option[StayPeriod] =
    (checkInDate.map(_.trim).filter(_.nonEmpty), checkOutDate.map(_.trim).filter(_.nonEmpty)) match
      case (Some(checkInText), Some(checkOutText)) =>
        Some(
          StayPeriod
            .create(LocalDate.parse(checkInText), LocalDate.parse(checkOutText))
            .fold(throw _, identity)
        )
      case _ => None
