// ReferenceDataSeederFlights 负责数据库基础设施相关实现。

package com.typesafe.travel.persistence

import cats.effect.IO
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

object ReferenceDataSeederFlights:
  def seedFlights(transactor: Transactor[IO]): IO[Unit] =
    val insertAirlines =
      List(
        sql"""
          insert into airlines (airline_id, name, code, status, created_at)
          values ('airline-mu', '濂堕緳鑸┖', 'MU', 'Active', timestamp '2026-03-25 00:00:00')
        """.update.run,
        sql"""
          insert into airlines (airline_id, name, code, status, created_at)
          values ('airline-9c', '绉戞瘮鑸┖', '9C', 'Active', timestamp '2026-03-25 00:00:00')
        """.update.run
      )

    val insertFlights =
      List(
        sql"""
          insert into flights (
            flight_id, airline_id, flight_number, departure_airport, arrival_airport,
            departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at
          ) values (
            'flight-mu5123', 'airline-mu', 'MU5123', 'SHA', 'HGH',
            timestamp with time zone '2026-04-05 08:00:00+08:00',
            timestamp with time zone '2026-04-05 09:00:00+08:00',
            'OpenForBooking', 680, 'CNY', timestamp '2026-03-25 00:00:00'
          )
        """.update.run,
        sql"""
          insert into flights (
            flight_id, airline_id, flight_number, departure_airport, arrival_airport,
            departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at
          ) values (
            'flight-mu5210', 'airline-mu', 'MU5210', 'PVG', 'NRT',
            timestamp with time zone '2026-04-05 10:00:00+08:00',
            timestamp with time zone '2026-04-05 14:00:00+08:00',
            'OpenForBooking', 2400, 'CNY', timestamp '2026-03-25 00:00:00'
          )
        """.update.run,
        sql"""
          insert into flights (
            flight_id, airline_id, flight_number, departure_airport, arrival_airport,
            departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at
          ) values (
            'flight-9c8821', 'airline-9c', '9C8821', 'PVG', 'ICN',
            timestamp with time zone '2026-04-05 13:00:00+08:00',
            timestamp with time zone '2026-04-05 16:00:00+08:00',
            'OpenForBooking', 1900, 'CNY', timestamp '2026-03-25 00:00:00'
          )
        """.update.run
      )

    val insertInventories =
      List(
        insertCabinInventory("mu5123-economy", "flight-mu5123", "ECONOMY", 6, 680),
        insertCabinInventory("mu5123-premium-economy", "flight-mu5123", "PREMIUM_ECONOMY", 4, 980),
        insertCabinInventory("mu5123-business", "flight-mu5123", "BUSINESS", 2, 1880),
        insertCabinInventory("mu5123-first", "flight-mu5123", "FIRST", 1, 3280),
        insertCabinInventory("mu5210-economy", "flight-mu5210", "ECONOMY", 6, 2400),
        insertCabinInventory("mu5210-premium-economy", "flight-mu5210", "PREMIUM_ECONOMY", 4, 2900),
        insertCabinInventory("mu5210-business", "flight-mu5210", "BUSINESS", 2, 3600),
        insertCabinInventory("mu5210-first", "flight-mu5210", "FIRST", 1, 5200),
        insertCabinInventory("9c8821-economy", "flight-9c8821", "ECONOMY", 6, 1900),
        insertCabinInventory("9c8821-premium-economy", "flight-9c8821", "PREMIUM_ECONOMY", 4, 2300),
        insertCabinInventory("9c8821-business", "flight-9c8821", "BUSINESS", 2, 3100),
        insertCabinInventory("9c8821-first", "flight-9c8821", "FIRST", 1, 4600)
      )

    (insertAirlines ++ insertFlights ++ insertInventories).sequence.transact(transactor).void

  private def insertCabinInventory(
      inventoryId: String,
      flightId: String,
      cabinClass: String,
      availableSeats: Int,
      unitPriceAmount: BigDecimal
  ): ConnectionIO[Int] =
    sql"""
      insert into flight_cabin_inventories (
        inventory_id, flight_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status
      ) values (
        $inventoryId, $flightId, $cabinClass, $availableSeats, $unitPriceAmount, 'CNY', 'Open'
      )
    """.update.run
