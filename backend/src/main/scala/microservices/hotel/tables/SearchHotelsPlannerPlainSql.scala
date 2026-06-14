// SearchHotelsPlannerPlainSql 封装酒店模块的plain SQL 实现。

package com.typesafe.travel.hotel.tables

import cats.effect.IO
import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object SearchHotelsPlannerPlainSql:
  def list(connection: Connection, request: HotelSearchPlannerRequest): IO[List[Hotel]] =
    IO.blocking {
      HotelSearchSqlSupport.listHotels(connection, request.location)
    }
