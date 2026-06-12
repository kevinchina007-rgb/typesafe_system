// FlightDailyLowestPricesPlannerPlainSql 封装航班模块的plain SQL 实现。

package com.typesafe.travel.flight.tables

import cats.effect.IO
import com.typesafe.travel.flight.objects.{FlightDailyLowestPricePlannerRow, FlightDailyLowestPricesPlannerRequest}

import java.sql.Connection
import java.time.LocalDate

object FlightDailyLowestPricesPlannerPlainSql:
  def findDailyLowestPrices(connection: Connection, input: FlightDailyLowestPricesPlannerRequest): IO[List[FlightDailyLowestPricePlannerRow]] =
    IO.blocking {
      val cabinFilter = input.cabinClass.map(_.trim).filter(_.nonEmpty).filterNot(_.equalsIgnoreCase("all"))
      val sql =
        """
          select
            (f.departure_time at time zone 'Asia/Shanghai')::date as departure_date,
            min(i.unit_price_amount) as lowest_price_amount,
            min(i.unit_price_currency) as currency
          from flights f
          join flight_cabin_inventories i on i.flight_id = f.flight_id
          where f.departure_airport in (select airport_code from flight_airports where city_name = ? or airport_code = ?)
            and f.arrival_airport in (select airport_code from flight_airports where city_name = ? or airport_code = ?)
            and (f.departure_time at time zone 'Asia/Shanghai')::date >= cast(? as date)
            and (f.departure_time at time zone 'Asia/Shanghai')::date < cast(? as date)
        """ +
          cabinFilter.map(_ => " and lower(i.cabin_class) = lower(?)").getOrElse("") +
          """
          group by departure_date
          order by departure_date
          """

      val startDate = LocalDate.parse(input.startDate)
      val endDate = startDate.plusDays(input.days.toLong)
      val statement = connection.prepareStatement(sql)
      try
        statement.setString(1, input.departureAirport)
        statement.setString(2, input.departureAirport)
        statement.setString(3, input.arrivalAirport)
        statement.setString(4, input.arrivalAirport)
        statement.setString(5, startDate.toString)
        statement.setString(6, endDate.toString)
        cabinFilter.foreach(value => statement.setString(7, value))

        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[FlightDailyLowestPricePlannerRow]
          while resultSet.next() do
            rows += FlightDailyLowestPricePlannerRow(
              date = resultSet.getObject("departure_date", classOf[LocalDate]),
              lowestPriceAmount = resultSet.getBigDecimal("lowest_price_amount"),
              currency = resultSet.getString("currency")
            )
          rows.result()
        finally resultSet.close()
      finally statement.close()
    }
