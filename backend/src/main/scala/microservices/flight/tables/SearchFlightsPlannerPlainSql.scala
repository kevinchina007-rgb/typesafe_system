package com.typesafe.travel.persistence.flight

import cats.effect.IO
import com.typesafe.travel.flight.domain.{CabinInventoryPlannerRow, FlightPlannerRow, FlightSearchRequest}

import java.sql.Connection

object SearchFlightsPlannerPlainSql:
  def searchFlights(connection: Connection, request: FlightSearchRequest): IO[List[FlightPlannerRow]] =
    IO.blocking {
      val filters = List.newBuilder[String]
      val values = List.newBuilder[String]

      filters += "f.status = 'OpenForBooking'"

      request.departureAirport.map(_.trim).filter(_.nonEmpty).foreach { value =>
        filters += "f.departure_airport in (select airport_code from flight_airports where city_name = ? or airport_code = ?)"
        values ++= List(value, value)
      }
      request.arrivalAirport.map(_.trim).filter(_.nonEmpty).foreach { value =>
        filters += "f.arrival_airport in (select airport_code from flight_airports where city_name = ? or airport_code = ?)"
        values ++= List(value, value)
      }
      request.date.map(_.trim).filter(_.nonEmpty).foreach { value =>
        filters += "(f.departure_time at time zone 'Asia/Shanghai')::date = cast(? as date)"
        values += value
      }

      val whereSql = filters.result() match
        case Nil => ""
        case nextFilters => nextFilters.mkString(" where ", " and ", "")
      val statement = connection.prepareStatement(FlightPlainSqlRows.selectFlightsSql + whereSql + " order by f.departure_time, f.flight_id")
      try
        values.result().zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
        val resultSet = statement.executeQuery()
        try FlightPlainSqlRows.readFlightRows(resultSet)
        finally resultSet.close()
      finally statement.close()
    }

  def listCabins(connection: Connection, flightId: String): IO[List[CabinInventoryPlannerRow]] =
    IO.blocking {
      FlightCabinPlainSqlRows.listCabins(connection, flightId)
    }
