package com.typesafe.travel.persistence

import cats.effect.IO

import cats.effect.kernel.Async
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*

object ReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      airlineCount <- sql"select count(*) from airlines".query[Long].unique.transact(transactor)
      hotelCount <- sql"select count(*) from hotels".query[Long].unique.transact(transactor)
      airlineManagerCount <- sql"select count(*) from airline_managers".query[Long].unique.transact(transactor)
      hotelManagerCount <- sql"select count(*) from hotel_managers".query[Long].unique.transact(transactor)
      _ <- if airlineCount == 0 then seedFlights(transactor) else IO.unit
      _ <- if hotelCount == 0 then seedHotels(transactor) else IO.unit
      _ <- if airlineManagerCount == 0 then seedAirlineManagers(transactor) else IO.unit
      _ <- if hotelManagerCount == 0 then seedHotelManagers(transactor) else IO.unit
    yield ()

  private def seedFlights(transactor: Transactor[IO]): IO[Unit] =
    val insertAirlines =
      List(
        sql"""
          insert into airlines (airline_id, name, code, status, created_at)
          values ('airline-mu', 'China Eastern', 'MU', 'Active', timestamp '2026-03-25 00:00:00')
        """.update.run,
        sql"""
          insert into airlines (airline_id, name, code, status, created_at)
          values ('airline-9c', 'Spring Airlines', '9C', 'Active', timestamp '2026-03-25 00:00:00')
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
        insertCabinInventory("mu5123-business", "flight-mu5123", "BUSINESS", 2, 1880),
        insertCabinInventory("mu5210-economy", "flight-mu5210", "ECONOMY", 6, 2400),
        insertCabinInventory("mu5210-business", "flight-mu5210", "BUSINESS", 2, 3600),
        insertCabinInventory("9c8821-economy", "flight-9c8821", "ECONOMY", 6, 1900),
        insertCabinInventory("9c8821-business", "flight-9c8821", "BUSINESS", 2, 3100)
      )

    (insertAirlines ++ insertFlights ++ insertInventories).sequence.transact(transactor).void

  private def seedHotels(transactor: Transactor[IO]): IO[Unit] =
    val insertHotels =
      List(
        sql"""
          insert into hotels (hotel_id, name, location, status, created_at)
          values ('hotel-hz-westlake', 'West Lake Retreat', 'Hangzhou', 'Active', timestamp '2026-03-26 00:00:00')
        """.update.run,
        sql"""
          insert into hotels (hotel_id, name, location, status, created_at)
          values ('hotel-sh-bund', 'Bund Skyline Hotel', 'Shanghai', 'Active', timestamp '2026-03-26 00:00:00')
        """.update.run
      )

    val insertRoomTypes =
      List(
        insertRoomType("roomtype-westlake-deluxe", "hotel-hz-westlake", "Deluxe Twin", 2, "TWIN", 860, "OpenForBooking"),
        insertRoomType("roomtype-westlake-family", "hotel-hz-westlake", "Family Suite", 4, "FAMILY", 1280, "OpenForBooking"),
        insertRoomType("roomtype-bund-queen", "hotel-sh-bund", "City Queen", 2, "QUEEN", 980, "OpenForBooking")
      )

    val insertInventories =
      List(
        insertRoomInventory("westlake-deluxe-0", "roomtype-westlake-deluxe", "2026-04-05", 3, 880, "Available"),
        insertRoomInventory("westlake-deluxe-1", "roomtype-westlake-deluxe", "2026-04-06", 3, 920, "Available"),
        insertRoomInventory("westlake-deluxe-2", "roomtype-westlake-deluxe", "2026-04-07", 2, 950, "Available"),
        insertRoomInventory("westlake-deluxe-3", "roomtype-westlake-deluxe", "2026-04-08", 2, 980, "Available"),
        insertRoomInventory("westlake-family-0", "roomtype-westlake-family", "2026-04-05", 2, 1320, "Available"),
        insertRoomInventory("westlake-family-1", "roomtype-westlake-family", "2026-04-06", 2, 1360, "Available"),
        insertRoomInventory("westlake-family-2", "roomtype-westlake-family", "2026-04-07", 1, 1420, "Available"),
        insertRoomInventory("westlake-family-3", "roomtype-westlake-family", "2026-04-08", 1, 1450, "Available"),
        insertRoomInventory("bund-queen-0", "roomtype-bund-queen", "2026-04-05", 4, 990, "Available"),
        insertRoomInventory("bund-queen-1", "roomtype-bund-queen", "2026-04-06", 4, 1030, "Available"),
        insertRoomInventory("bund-queen-2", "roomtype-bund-queen", "2026-04-07", 3, 1070, "Available"),
        insertRoomInventory("bund-queen-3", "roomtype-bund-queen", "2026-04-08", 3, 1090, "Available")
      )

    (insertHotels ++ insertRoomTypes ++ insertInventories).sequence.transact(transactor).void

  private def seedAirlineManagers(transactor: Transactor[IO]): IO[Unit] =
    List(
      sql"""
        insert into airline_managers (manager_id, airline_id, email, display_name, status, created_at)
        values ('manager-airline-mu', 'airline-mu', 'ops@mu.example', 'China Eastern Ops', 'Active', timestamp '2026-03-27 00:00:00')
      """.update.run,
      sql"""
        insert into airline_managers (manager_id, airline_id, email, display_name, status, created_at)
        values ('manager-airline-9c', 'airline-9c', 'ops@9c.example', 'Spring Airlines Ops', 'Active', timestamp '2026-03-27 00:00:00')
      """.update.run
    ).sequence.transact(transactor).void

  private def seedHotelManagers(transactor: Transactor[IO]): IO[Unit] =
    List(
      sql"""
        insert into hotel_managers (manager_id, hotel_id, email, display_name, status, created_at)
        values ('manager-hotel-westlake', 'hotel-hz-westlake', 'ops@westlake.example', 'West Lake Ops', 'Active', timestamp '2026-03-27 00:00:00')
      """.update.run,
      sql"""
        insert into hotel_managers (manager_id, hotel_id, email, display_name, status, created_at)
        values ('manager-hotel-bund', 'hotel-sh-bund', 'ops@bund.example', 'Bund Hotel Ops', 'Active', timestamp '2026-03-27 00:00:00')
      """.update.run
    ).sequence.transact(transactor).void

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

  private def insertRoomType(
      roomTypeId: String,
      hotelId: String,
      roomTypeName: String,
      capacity: Int,
      bedType: String,
      basePriceAmount: BigDecimal,
      status: String
  ): ConnectionIO[Int] =
    sql"""
      insert into hotel_room_types (
        room_type_id, hotel_id, name, capacity, bed_type, base_price_amount, base_price_currency, status
      ) values (
        $roomTypeId, $hotelId, $roomTypeName, $capacity, $bedType, $basePriceAmount, 'CNY', $status
      )
    """.update.run

  private def insertRoomInventory(
      inventoryId: String,
      roomTypeId: String,
      inventoryDate: String,
      availableRooms: Int,
      unitPriceAmount: BigDecimal,
      status: String
  ): ConnectionIO[Int] =
    sql"""
      insert into hotel_room_inventories (
        inventory_id, room_type_id, inventory_date, available_rooms, unit_price_amount, unit_price_currency, status
      ) values (
        $inventoryId, $roomTypeId, cast($inventoryDate as date), $availableRooms, $unitPriceAmount, 'CNY', $status
      )
    """.update.run
