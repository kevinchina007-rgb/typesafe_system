package com.typesafe.travel.persistence

import cats.effect.IO

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress
import doobie.*
import doobie.implicits.*

import java.sql.{Connection, Date, ResultSet, Timestamp}
import java.time.{Instant, LocalDate}
import java.time.temporal.ChronoUnit

object ReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      airlineCount <- sql"select count(*) from airlines".query[Long].unique.transact(transactor)
      airlineManagerCount <- sql"select count(*) from airline_managers".query[Long].unique.transact(transactor)
      _ <- if airlineCount == 0 then seedFlights(transactor) else IO.unit
      _ <- seedHotelDemoData(transactor)
      _ <- if airlineManagerCount == 0 then seedAirlineManagers(transactor) else IO.unit
    yield ()

  private def seedFlights(transactor: Transactor[IO]): IO[Unit] =
    val insertAirlines =
      List(
        sql"""
          insert into airlines (airline_id, name, code, status, created_at)
          values ('airline-mu', '奶龙航空', 'MU', 'Active', timestamp '2026-03-25 00:00:00')
        """.update.run,
        sql"""
          insert into airlines (airline_id, name, code, status, created_at)
          values ('airline-9c', '科比航空', '9C', 'Active', timestamp '2026-03-25 00:00:00')
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
        values ('manager-airline-mu', 'airline-mu', 'ops@mu.example', '奶龙航空运营', 'Active', timestamp '2026-03-27 00:00:00')
      """.update.run,
      sql"""
        insert into airline_managers (manager_id, airline_id, email, display_name, status, created_at)
        values ('manager-airline-9c', 'airline-9c', 'ops@9c.example', '科比航空运营', 'Active', timestamp '2026-03-27 00:00:00')
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

  private val hotelDemoPassword = "Hotel2026!"
  private val hotelDemoCreatedAt = Instant.parse("2026-05-18T00:00:00Z")
  private val hotelInventoryStartDate = LocalDate.parse("2026-06-01")
  private val hotelInventoryEndDate = LocalDate.parse("2026-07-31")
  private val hotelInventoryDates =
    Iterator.iterate(hotelInventoryStartDate)(_.plusDays(1)).takeWhile(!_.isAfter(hotelInventoryEndDate)).toVector
  private val hotelRoomTypeTemplates = List(
    RoomTypeTemplate("standard", "标准大床房", 2, "KING", 18, BigDecimal(0)),
    RoomTypeTemplate("twin", "高级双床房", 2, "TWIN", 14, BigDecimal(180)),
    RoomTypeTemplate("suite", "家庭套房", 4, "SUITE", 8, BigDecimal(420))
  )
  private def hotelSeed(hotelId: String, hotelName: String, managerId: String, managerEmail: String, managerDisplayName: String): HotelSeed =
    HotelSeed(hotelId, hotelName, managerId, managerEmail, managerDisplayName)
  private val hotelCitySeeds = List(
    HotelCitySeed(
      "北京",
      List(
        hotelSeed("hotel-beijing-guomen", "北京国门酒店", "manager-hotel-beijing-guomen", "ops-beijing-guomen@hotel.example", "北京国门酒店管理员"),
        hotelSeed("hotel-beijing-daxing", "北京大兴酒店", "manager-hotel-beijing-daxing", "ops-beijing-daxing@hotel.example", "北京大兴酒店管理员")
      )
    ),
    HotelCitySeed(
      "上海",
      List(
        hotelSeed("hotel-sh-bund", "上海外滩酒店", "manager-hotel-bund", "ops-shanghai-bund@hotel.example", "上海外滩酒店管理员"),
        hotelSeed("hotel-shanghai-hongqiao", "上海虹桥酒店", "manager-hotel-shanghai-hongqiao", "ops-shanghai-hongqiao@hotel.example", "上海虹桥酒店管理员")
      )
    ),
    HotelCitySeed(
      "广州",
      List(
        hotelSeed("hotel-guangzhou-pearl", "广州珠江酒店", "manager-hotel-guangzhou-pearl", "ops-guangzhou-pearl@hotel.example", "广州珠江酒店管理员"),
        hotelSeed("hotel-guangzhou-baiyun", "广州白云酒店", "manager-hotel-guangzhou-baiyun", "ops-guangzhou-baiyun@hotel.example", "广州白云酒店管理员")
      )
    ),
    HotelCitySeed(
      "深圳",
      List(
        hotelSeed("hotel-shenzhen-bay", "深圳湾酒店", "manager-hotel-shenzhen-bay", "ops-shenzhen-bay@hotel.example", "深圳湾酒店管理员"),
        hotelSeed("hotel-shenzhen-nanshan", "深圳南山酒店", "manager-hotel-shenzhen-nanshan", "ops-shenzhen-nanshan@hotel.example", "深圳南山酒店管理员")
      )
    ),
    HotelCitySeed(
      "成都",
      List(
        hotelSeed("hotel-chengdu-tianfu", "成都天府酒店", "manager-hotel-chengdu-tianfu", "ops-chengdu-tianfu@hotel.example", "成都天府酒店管理员"),
        hotelSeed("hotel-chengdu-jinjiang", "成都锦江酒店", "manager-hotel-chengdu-jinjiang", "ops-chengdu-jinjiang@hotel.example", "成都锦江酒店管理员")
      )
    ),
    HotelCitySeed(
      "重庆",
      List(
        hotelSeed("hotel-chongqing-shancheng", "重庆山城酒店", "manager-hotel-chongqing-shancheng", "ops-chongqing-shancheng@hotel.example", "重庆山城酒店管理员"),
        hotelSeed("hotel-chongqing-jiangjing", "重庆江景酒店", "manager-hotel-chongqing-jiangjing", "ops-chongqing-jiangjing@hotel.example", "重庆江景酒店管理员")
      )
    ),
    HotelCitySeed(
      "杭州",
      List(
        hotelSeed("hotel-hz-westlake", "杭州西湖酒店", "manager-hotel-westlake", "ops-hangzhou-westlake@hotel.example", "杭州西湖酒店管理员"),
        hotelSeed("hotel-hangzhou-qiantang", "杭州钱塘酒店", "manager-hotel-hangzhou-qiantang", "ops-hangzhou-qiantang@hotel.example", "杭州钱塘酒店管理员")
      )
    ),
    HotelCitySeed(
      "南京",
      List(
        hotelSeed("hotel-nanjing-qinhuai", "南京秦淮酒店", "manager-hotel-nanjing-qinhuai", "ops-nanjing-qinhuai@hotel.example", "南京秦淮酒店管理员"),
        hotelSeed("hotel-nanjing-zijin", "南京紫金酒店", "manager-hotel-nanjing-zijin", "ops-nanjing-zijin@hotel.example", "南京紫金酒店管理员")
      )
    ),
    HotelCitySeed(
      "武汉",
      List(
        hotelSeed("hotel-wuhan-jianghan", "武汉江汉酒店", "manager-hotel-wuhan-jianghan", "ops-wuhan-jianghan@hotel.example", "武汉江汉酒店管理员"),
        hotelSeed("hotel-wuhan-donghu", "武汉东湖酒店", "manager-hotel-wuhan-donghu", "ops-wuhan-donghu@hotel.example", "武汉东湖酒店管理员")
      )
    ),
    HotelCitySeed(
      "西安",
      List(
        hotelSeed("hotel-xian-gudu", "西安古都酒店", "manager-hotel-xian-gudu", "ops-xian-gudu@hotel.example", "西安古都酒店管理员"),
        hotelSeed("hotel-xian-changan", "西安长安酒店", "manager-hotel-xian-changan", "ops-xian-changan@hotel.example", "西安长安酒店管理员")
      )
    ),
    HotelCitySeed(
      "天津",
      List(
        hotelSeed("hotel-tianjin-haihe", "天津海河酒店", "manager-hotel-tianjin-haihe", "ops-tianjin-haihe@hotel.example", "天津海河酒店管理员"),
        hotelSeed("hotel-tianjin-binhai", "天津滨海酒店", "manager-hotel-tianjin-binhai", "ops-tianjin-binhai@hotel.example", "天津滨海酒店管理员")
      )
    ),
    HotelCitySeed(
      "郑州",
      List(
        hotelSeed("hotel-zhengzhou-zhongyuan", "郑州中原酒店", "manager-hotel-zhengzhou-zhongyuan", "ops-zhengzhou-zhongyuan@hotel.example", "郑州中原酒店管理员"),
        hotelSeed("hotel-zhengzhou-airport", "郑州航空港酒店", "manager-hotel-zhengzhou-airport", "ops-zhengzhou-airport@hotel.example", "郑州航空港酒店管理员")
      )
    ),
    HotelCitySeed(
      "长沙",
      List(
        hotelSeed("hotel-changsha-yuelu", "长沙岳麓酒店", "manager-hotel-changsha-yuelu", "ops-changsha-yuelu@hotel.example", "长沙岳麓酒店管理员"),
        hotelSeed("hotel-changsha-xiangjiang", "长沙湘江酒店", "manager-hotel-changsha-xiangjiang", "ops-changsha-xiangjiang@hotel.example", "长沙湘江酒店管理员")
      )
    ),
    HotelCitySeed(
      "青岛",
      List(
        hotelSeed("hotel-qingdao-seaview", "青岛海景酒店", "manager-hotel-qingdao-seaview", "ops-qingdao-seaview@hotel.example", "青岛海景酒店管理员"),
        hotelSeed("hotel-qingdao-jiaodong", "青岛胶东酒店", "manager-hotel-qingdao-jiaodong", "ops-qingdao-jiaodong@hotel.example", "青岛胶东酒店管理员")
      )
    ),
    HotelCitySeed(
      "厦门",
      List(
        hotelSeed("hotel-xiamen-gulangyu", "厦门鼓浪屿酒店", "manager-hotel-xiamen-gulangyu", "ops-xiamen-gulangyu@hotel.example", "厦门鼓浪屿酒店管理员"),
        hotelSeed("hotel-xiamen-bay", "厦门海湾酒店", "manager-hotel-xiamen-bay", "ops-xiamen-bay@hotel.example", "厦门海湾酒店管理员")
      )
    )
  )

  private def seedHotelDemoData(transactor: Transactor[IO]): IO[Unit] =
    hotelCitySeeds.zipWithIndex.traverse_ { case (citySeed, cityIndex) =>
      citySeed.hotels.zipWithIndex.traverse_ { case (hotelSeed, hotelIndex) =>
        seedHotelRecord(transactor, citySeed.cityName, cityIndex, hotelSeed, hotelIndex)
      }
    }

  private def seedHotelRecord(
      transactor: Transactor[IO],
      cityName: String,
      cityIndex: Int,
      hotelSeed: HotelSeed,
      hotelIndex: Int
  ): IO[Unit] =
    for
      passwordHash <- hashPasswordForLoginEmail(hotelDemoPassword, EmailAddress.unsafe(hotelSeed.managerEmail))
      statements = hotelSeedStatements(cityName, cityIndex, hotelSeed, hotelIndex, passwordHash)
      _ <- statements.sequence.transact(transactor).void
    yield ()

  private def hotelSeedStatements(
      cityName: String,
      cityIndex: Int,
      hotelSeed: HotelSeed,
      hotelIndex: Int,
      passwordHash: String
  ): List[ConnectionIO[Int]] =
    val hotelIdPrefix = hotelSeed.hotelId.stripPrefix("hotel-")
    val baseNightlyPrice = BigDecimal(460 + cityIndex * 35 + hotelIndex * 20)
    val roomTypeStatements =
      hotelRoomTypeTemplates.zipWithIndex.map { case (template, roomTypeIndex) =>
        val roomTypeId = s"roomtype-$hotelIdPrefix-${template.suffix}"
        val roomTypeBasePrice = (baseNightlyPrice + template.basePriceOffset + BigDecimal(roomTypeIndex * 25)).setScale(2, BigDecimal.RoundingMode.HALF_UP)
        insertOrUpdateHotelRoomType(roomTypeId, hotelSeed.hotelId, template.roomTypeName, template.capacity, template.bedType, roomTypeBasePrice, "OpenForBooking")
      }
    val inventoryStatements =
      hotelRoomTypeTemplates.zipWithIndex.flatMap { case (template, roomTypeIndex) =>
        val roomTypeId = s"roomtype-$hotelIdPrefix-${template.suffix}"
        hotelInventoryDates.zipWithIndex.map { case (inventoryDate, dayIndex) =>
          val nightlyPrice =
            (baseNightlyPrice + template.basePriceOffset + BigDecimal(roomTypeIndex * 25) + BigDecimal((dayIndex % 7) * 12) + BigDecimal((cityIndex + hotelIndex) * 5))
              .setScale(2, BigDecimal.RoundingMode.HALF_UP)
          val availableRooms = Math.max(5, template.baseAvailableRooms - (dayIndex % 4))
          insertOrUpdateHotelRoomInventory(
            s"roominv-$hotelIdPrefix-${template.suffix}-$inventoryDate",
            roomTypeId,
            inventoryDate,
            availableRooms,
            nightlyPrice,
            "Available"
          )
        }
      }
    List(
      insertOrUpdateHotel(hotelSeed.hotelId, hotelSeed.hotelName, cityName, "Active", hotelDemoCreatedAt),
      insertOrUpdateHotelManager(hotelSeed.managerId, hotelSeed.hotelId, hotelSeed.managerEmail, hotelSeed.managerDisplayName, "Active", hotelDemoCreatedAt),
      insertOrUpdateManagerCredential("Hotel", hotelSeed.managerId, hotelSeed.managerEmail, passwordHash, hotelDemoCreatedAt)
    ) ++ roomTypeStatements ++ inventoryStatements

  private final case class HotelCitySeed(cityName: String, hotels: List[HotelSeed])
  private final case class HotelSeed(
      hotelId: String,
      hotelName: String,
      managerId: String,
      managerEmail: String,
      managerDisplayName: String
  )
  private final case class RoomTypeTemplate(
      suffix: String,
      roomTypeName: String,
      capacity: Int,
      bedType: String,
      baseAvailableRooms: Int,
      basePriceOffset: BigDecimal
  )

  private def insertOrUpdateHotel(hotelId: String, name: String, location: String, status: String, createdAt: Instant): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into hotels (hotel_id, name, location, status, created_at)
      values ($hotelId, $name, $location, $status, cast($createdAtValue as timestamptz))
      on conflict (hotel_id) do update set
        name = excluded.name,
        location = excluded.location,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run

  private def insertOrUpdateHotelManager(
      managerId: String,
      hotelId: String,
      email: String,
      displayName: String,
      status: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into hotel_managers (manager_id, hotel_id, email, display_name, status, created_at)
      values ($managerId, $hotelId, $email, $displayName, $status, cast($createdAtValue as timestamptz))
      on conflict (manager_id) do update set
        hotel_id = excluded.hotel_id,
        email = excluded.email,
        display_name = excluded.display_name,
        status = excluded.status,
        created_at = excluded.created_at
    """.update.run

  private def insertOrUpdateManagerCredential(
      managerType: String,
      managerId: String,
      email: String,
      passwordHash: String,
      createdAt: Instant
  ): ConnectionIO[Int] =
    val createdAtValue = createdAt.toString
    sql"""
      insert into manager_credentials (
        credential_id, manager_type, manager_id, login_email, password_hash, status, created_at, updated_at, password_updated_at
      ) values (
        ${s"credential-${managerType.toLowerCase}-$managerId"},
        $managerType,
        $managerId,
        $email,
        $passwordHash,
        ${"Active"},
        cast($createdAtValue as timestamptz),
        cast($createdAtValue as timestamptz),
        cast($createdAtValue as timestamptz)
      )
      on conflict (manager_type, manager_id) do update set
        login_email = excluded.login_email,
        password_hash = excluded.password_hash,
        status = excluded.status,
        updated_at = excluded.updated_at,
        password_updated_at = excluded.password_updated_at
    """.update.run

  private def insertOrUpdateHotelRoomType(
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
        $roomTypeId, $hotelId, $roomTypeName, $capacity, $bedType, $basePriceAmount, ${"CNY"}, $status
      )
      on conflict (room_type_id) do update set
        hotel_id = excluded.hotel_id,
        name = excluded.name,
        capacity = excluded.capacity,
        bed_type = excluded.bed_type,
        base_price_amount = excluded.base_price_amount,
        base_price_currency = excluded.base_price_currency,
        status = excluded.status
    """.update.run

  private def insertOrUpdateHotelRoomInventory(
      inventoryId: String,
      roomTypeId: String,
      inventoryDate: LocalDate,
      availableRooms: Int,
      unitPriceAmount: BigDecimal,
      status: String
  ): ConnectionIO[Int] =
    val inventoryDateValue = inventoryDate.toString
    sql"""
      insert into hotel_room_inventories (
        inventory_id, room_type_id, inventory_date, available_rooms, unit_price_amount, unit_price_currency, status
      ) values (
        $inventoryId, $roomTypeId, cast($inventoryDateValue as date), $availableRooms, $unitPriceAmount, ${"CNY"}, $status
      )
      on conflict (inventory_id) do update set
        room_type_id = excluded.room_type_id,
        inventory_date = excluded.inventory_date,
        available_rooms = excluded.available_rooms,
        unit_price_amount = excluded.unit_price_amount,
        unit_price_currency = excluded.unit_price_currency,
        status = excluded.status
    """.update.run

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
