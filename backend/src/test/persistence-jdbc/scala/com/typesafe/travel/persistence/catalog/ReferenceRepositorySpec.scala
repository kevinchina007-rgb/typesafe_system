package com.typesafe.travel.persistence.catalog

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.flight.DoobieFlightRepository
import com.typesafe.travel.persistence.hotel.DoobieHotelRepository
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

final class ReferenceRepositorySpec extends FunSuite:
  test("seeded flight repository supports lookup and search") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    val flightRepository = DoobieFlightRepository[cats.effect.IO](transactor)
    val foundFlight = flightRepository.findFlightById(FlightId("flight-mu5210")).unsafeRunSync()
    val searchedFlights =
      flightRepository
        .searchFlights(
          FlightSearchCriteria(
            departureAirport = Some(AirportCode.unsafe("PVG")),
            arrivalAirport = Some(AirportCode.unsafe("NRT")),
            departureDate = Some(LocalDate.parse("2026-04-05"))
          )
        )
        .unsafeRunSync()

    assert(foundFlight.nonEmpty)
    assertEquals(searchedFlights.map(_.flightId.value), List("flight-mu5210"))
  }

  test("seeded hotel repository supports lookup and stay search") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    val hotelRepository = DoobieHotelRepository[cats.effect.IO](transactor)
    val foundHotel = hotelRepository.findHotelById(HotelId("hotel-hz-westlake")).unsafeRunSync()
    val searchedHotels =
      hotelRepository
        .searchHotels(
          HotelSearchCriteria(
            location = Some(HotelLocation.unsafe("Hangzhou")),
            stayPeriod = Some(StayPeriod.unsafe(LocalDate.parse("2026-04-05"), LocalDate.parse("2026-04-07")))
          )
        )
        .unsafeRunSync()

    assert(foundHotel.nonEmpty)
    assertEquals(searchedHotels.map(_.hotelId.value), List("hotel-hz-westlake"))
  }

  test("flight repository round-trips saved flight aggregate with cabin inventories") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    val flightRepository = DoobieFlightRepository[cats.effect.IO](transactor)
    val airline =
      restorePersistedAirline(
        airlineId = AirlineId("airline-roundtrip"),
        airlineName = AirlineName.unsafe("Roundtrip Air"),
        airlineCode = AirlineCode.unsafe("RA"),
        airlineStatus = AirlineStatus.Active,
        createdAt = Instant.parse("2026-03-26T09:00:00Z")
      )
    val flight =
      restorePersistedFlight(
        flightId = FlightId("flight-roundtrip"),
        airlineId = airline.airlineId,
        flightNumber = FlightNumber.unsafe("RA1001"),
        departureAirport = AirportCode.unsafe("PVG"),
        arrivalAirport = AirportCode.unsafe("ICN"),
        flightSchedule = FlightSchedule.unsafe(
          OffsetDateTime.of(2026, 4, 9, 9, 0, 0, 0, ZoneOffset.ofHours(8)),
          OffsetDateTime.of(2026, 4, 9, 12, 0, 0, 0, ZoneOffset.ofHours(9))
        ),
        flightStatus = FlightStatus.OpenForBooking,
        basePrice = Money.unsafe(BigDecimal(1800), Currency.CNY),
        cabinInventories = Vector(
      createCabinInventory(
            cabinInventoryId = CabinInventoryId("inventory-roundtrip-economy"),
            flightId = FlightId("flight-roundtrip"),
            cabinClass = CabinClass.unsafe("economy"),
            availableSeats = SeatCount.unsafe(12),
            unitPrice = Money.unsafe(BigDecimal(1800), Currency.CNY),
            inventoryStatus = InventoryStatus.Open
          ),
      createCabinInventory(
            cabinInventoryId = CabinInventoryId("inventory-roundtrip-business"),
            flightId = FlightId("flight-roundtrip"),
            cabinClass = CabinClass.unsafe("business"),
            availableSeats = SeatCount.unsafe(4),
            unitPrice = Money.unsafe(BigDecimal(3200), Currency.CNY),
            inventoryStatus = InventoryStatus.Open
          )
        ),
        createdAt = Instant.parse("2026-03-26T09:10:00Z")
      )
    flightRepository.saveAirline(airline).unsafeRunSync()
    flightRepository.saveFlight(flight).unsafeRunSync()
    val loadedFlight = flightRepository.findFlightById(flight.flightId).unsafeRunSync()

    assert(loadedFlight.nonEmpty)
    assertEquals(loadedFlight.map(_.flightId), Some(flight.flightId))
    assertEquals(loadedFlight.map(_.airlineId), Some(flight.airlineId))
    assertEquals(loadedFlight.map(_.flightNumber), Some(flight.flightNumber))
    assertEquals(loadedFlight.map(_.departureAirport), Some(flight.departureAirport))
    assertEquals(loadedFlight.map(_.arrivalAirport), Some(flight.arrivalAirport))
    assertEquals(loadedFlight.map(_.flightSchedule), Some(flight.flightSchedule))
    assertEquals(loadedFlight.map(_.flightStatus), Some(flight.flightStatus))
    assertEquals(loadedFlight.map(_.basePrice), Some(flight.basePrice))
    assertEquals(loadedFlight.map(_.createdAt), Some(flight.createdAt))
    assertEquals(
      loadedFlight.toList.flatMap(_.cabinInventories).sortBy(_.cabinInventoryId.value).toVector,
      flight.cabinInventories.sortBy(_.cabinInventoryId.value)
    )
  }

  test("hotel repository round-trips saved hotel aggregate with room inventories") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    val hotelRepository = DoobieHotelRepository[cats.effect.IO](transactor)
    val hotel =
      restorePersistedHotel(
        hotelId = HotelId("hotel-roundtrip"),
        hotelName = HotelName.unsafe("Roundtrip Suites"),
        hotelLocation = HotelLocation.unsafe("Shanghai"),
        hotelStatus = HotelStatus.Active,
        roomTypes = Vector(
      restorePersistedRoomType(
            roomTypeId = RoomTypeId("roomtype-roundtrip"),
            hotelId = HotelId("hotel-roundtrip"),
            roomTypeName = RoomTypeName.unsafe("Harbor View Suite"),
            roomCapacity = Capacity.unsafe(3),
            bedType = BedType.unsafe("queen"),
            basePrice = Money.unsafe(BigDecimal(1260), Currency.CNY),
            roomTypeStatus = RoomTypeStatus.OpenForBooking,
            roomInventories = Vector(
      createRoomInventory(
                roomInventoryId = RoomInventoryId("room-inventory-roundtrip-1"),
                roomTypeId = RoomTypeId("roomtype-roundtrip"),
                inventoryDate = LocalDate.parse("2026-04-09"),
                availableRooms = RoomCount.unsafe(5),
                unitPrice = Money.unsafe(BigDecimal(1300), Currency.CNY),
                roomInventoryStatus = RoomInventoryStatus.Available
              ),
      createRoomInventory(
                roomInventoryId = RoomInventoryId("room-inventory-roundtrip-2"),
                roomTypeId = RoomTypeId("roomtype-roundtrip"),
                inventoryDate = LocalDate.parse("2026-04-10"),
                availableRooms = RoomCount.unsafe(4),
                unitPrice = Money.unsafe(BigDecimal(1350), Currency.CNY),
                roomInventoryStatus = RoomInventoryStatus.Available
              )
            )
          )
        ),
        createdAt = Instant.parse("2026-03-26T09:30:00Z")
      )

    hotelRepository.saveHotel(hotel).unsafeRunSync()
    val loadedHotel = hotelRepository.findHotelById(hotel.hotelId).unsafeRunSync()

    assertEquals(loadedHotel, Some(hotel))
  }

