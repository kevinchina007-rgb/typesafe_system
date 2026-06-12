// GetFlightDetailsPlannerPlainSql 封装航班模块的plain SQL 实现。

package com.typesafe.travel.flight.tables

import cats.effect.IO
import com.typesafe.travel.flight.objects.{CabinInventoryPlannerRow, FlightPlannerRow}

import java.sql.Connection

object GetFlightDetailsPlannerPlainSql:
  def findFlight(connection: Connection, flightId: String): IO[Option[FlightPlannerRow]] =
    IO.blocking {
      val statement = connection.prepareStatement(FlightPlainSqlRows.selectFlightsSql + " where f.flight_id = ?")
      try
        statement.setString(1, flightId)
        val resultSet = statement.executeQuery()
        try Option.when(resultSet.next())(FlightPlainSqlRows.readFlightRow(resultSet))
        finally resultSet.close()
      finally statement.close()
    }

  def listCabins(connection: Connection, flightId: String): IO[List[CabinInventoryPlannerRow]] =
    IO.blocking {
      FlightCabinPlainSqlRows.listCabins(connection, flightId)
    }
