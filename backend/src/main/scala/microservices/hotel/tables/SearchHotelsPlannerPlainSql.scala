package com.typesafe.travel.hotel.tables

import cats.effect.IO
import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object SearchHotelsPlannerPlainSql:
  def list(connection: Connection, request: HotelSearchPlannerRequest): IO[List[Hotel]] =
    IO.blocking {
      HotelReadSqlSupport.listHotels(connection, request.location)
    }
