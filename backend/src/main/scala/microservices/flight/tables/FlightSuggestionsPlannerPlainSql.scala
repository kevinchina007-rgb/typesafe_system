// FlightSuggestionsPlannerPlainSql 封装航班模块的plain SQL 实现。

package com.typesafe.travel.flight.tables

import cats.effect.IO
import com.typesafe.travel.flight.objects.FlightSuggestionsPlannerRequest
import com.typesafe.travel.flight.tables.FlightPlannerRow

import java.sql.Connection

object FlightSuggestionsPlannerPlainSql:
  def suggestFlights(connection: Connection, request: FlightSuggestionsPlannerRequest): IO[List[FlightPlannerRow]] =
    IO.blocking {
      val like = s"%${request.q.trim.toUpperCase}%"
      val statement = connection.prepareStatement(
        FlightPlainSqlRows.selectFlightsSql +
          """
            where upper(f.flight_number) like ? or upper(f.departure_airport) like ? or upper(f.arrival_airport) like ? or upper(coalesce(a.name, '')) like ?
            order by f.departure_time, f.flight_id
            limit 12
          """
      )
      try
        (1 to 4).foreach(index => statement.setString(index, like))
        val resultSet = statement.executeQuery()
        try FlightPlainSqlRows.readFlightRows(resultSet)
        finally resultSet.close()
      finally statement.close()
    }
