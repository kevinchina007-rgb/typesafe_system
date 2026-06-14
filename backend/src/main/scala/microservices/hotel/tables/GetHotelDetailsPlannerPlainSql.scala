// GetHotelDetailsPlannerPlainSql 封装酒店模块的plain SQL 实现。

package com.typesafe.travel.hotel.tables

import cats.effect.IO
import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object GetHotelDetailsPlannerPlainSql:
  def details(connection: Connection, request: HotelDetailsPlannerRequest): IO[Option[Hotel]] =
    IO.blocking {
      HotelDetailsSqlSupport.getHotel(connection, request.hotelId)
    }
