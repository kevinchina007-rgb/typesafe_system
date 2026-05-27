package com.typesafe.travel.hotel.tables

import cats.effect.IO
import com.typesafe.travel.hotel.objects.*

import java.sql.Connection

object HotelSuggestionsPlannerPlainSql:
  private val selectHotelSql =
    "select hotel_id, name, location from hotels"

  def suggestions(connection: Connection, request: HotelSuggestionPlannerRequest): IO[SearchSuggestionListPlannerResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(selectHotelSql + " where lower(name) like ? or lower(location) like ? order by name, hotel_id limit 12")
      try
        val like = s"%${request.q.trim.toLowerCase}%"
        statement.setString(1, like)
        statement.setString(2, like)
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[SearchSuggestionPlannerResponse]
          while resultSet.next() do
            rows += SearchSuggestionPlannerResponse(
              resourceType = "hotel",
              value = resultSet.getString("hotel_id"),
              title = resultSet.getString("name"),
              subtitle = resultSet.getString("location")
            )
          SearchSuggestionListPlannerResponse(rows.result())
        finally resultSet.close()
      finally statement.close()
    }
