// FlightCabinPlainSqlRows 封装航班模块的plain SQL 行映射。

package com.typesafe.travel.flight.tables

import com.typesafe.travel.flight.objects.CabinInventoryPlannerRow

import java.sql.{Connection, ResultSet}

private[flight] object FlightCabinPlainSqlRows:
  def listCabins(connection: Connection, flightId: String): List[CabinInventoryPlannerRow] =
    val statement = connection.prepareStatement(
      """
        select inventory_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status
        from flight_cabin_inventories
        where flight_id = ?
        order by
          case upper(cabin_class)
            when 'ECONOMY' then 1
            when 'PREMIUM_ECONOMY' then 2
            when 'BUSINESS' then 3
            when 'FIRST' then 4
            else 5
          end,
          inventory_id
      """
    )
    try
      statement.setString(1, flightId)
      val resultSet = statement.executeQuery()
      try readCabinRows(resultSet)
      finally resultSet.close()
    finally statement.close()

  private def readCabinRows(resultSet: ResultSet): List[CabinInventoryPlannerRow] =
    val rows = List.newBuilder[CabinInventoryPlannerRow]
    while resultSet.next() do
      rows += CabinInventoryPlannerRow(
        inventoryId = resultSet.getString("inventory_id"),
        cabinClass = resultSet.getString("cabin_class"),
        availableSeats = resultSet.getInt("available_seats"),
        unitPriceAmount = resultSet.getBigDecimal("unit_price_amount"),
        unitPriceCurrency = resultSet.getString("unit_price_currency"),
        status = resultSet.getString("status")
      )
    rows.result()
