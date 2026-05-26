package com.typesafe.travel.persistence.flight

import cats.effect.IO
import com.typesafe.travel.flight.domain.{CabinInventoryPlannerRow, FlightPlannerRow}

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
