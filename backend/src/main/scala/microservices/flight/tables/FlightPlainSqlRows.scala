// FlightPlainSqlRows contains backend-only ResultSet-to-row mapping for the flight planner queries.
// These row mappers are an implementation detail of the table layer: they bridge SQL result sets to planner-facing row models.
// The frontend does not mirror this file because it never touches JDBC ResultSet objects or SQL column mapping.
package com.typesafe.travel.flight.tables

import java.sql.ResultSet

private[flight] object FlightPlainSqlRows:
  val selectFlightsSql: String =
    """
      select f.flight_id, f.airline_id, coalesce(a.name, '') as airline_name, coalesce(a.code, '') as airline_code,
             a.logo_asset_path as airline_logo_path, f.flight_number, f.aircraft_model,
             f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time,
             f.status, f.base_price_amount, f.base_price_currency, f.created_at
      from flights f
      left join airlines a on a.airline_id = f.airline_id
    """

  def readFlightRows(resultSet: ResultSet): List[FlightPlannerRow] =
    val rows = List.newBuilder[FlightPlannerRow]
    while resultSet.next() do rows += readFlightRow(resultSet)
    rows.result()

  def readFlightRow(resultSet: ResultSet): FlightPlannerRow =
    FlightPlannerRow(
      flightId = resultSet.getString("flight_id"),
      airlineId = resultSet.getString("airline_id"),
      airlineName = resultSet.getString("airline_name"),
      airlineCode = resultSet.getString("airline_code"),
      airlineLogoPath = Option(resultSet.getString("airline_logo_path")),
      flightNumber = resultSet.getString("flight_number"),
      aircraftModel = resultSet.getString("aircraft_model"),
      departureAirport = resultSet.getString("departure_airport"),
      arrivalAirport = resultSet.getString("arrival_airport"),
      departureTime = resultSet.getObject("departure_time", classOf[java.time.OffsetDateTime]),
      arrivalTime = resultSet.getObject("arrival_time", classOf[java.time.OffsetDateTime]),
      status = resultSet.getString("status"),
      basePriceAmount = resultSet.getBigDecimal("base_price_amount"),
      basePriceCurrency = resultSet.getString("base_price_currency"),
      createdAt = resultSet.getTimestamp("created_at").toInstant
    )
