package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, OffsetDateTime, ZoneOffset}
import munit.FunSuite

final class FlightSpec extends FunSuite:
  private val createdAt = Instant.parse("2026-03-25T00:00:00Z")

  private val testFlightId = FlightId("flight-vertical-1")

  private val economyCabinInventory =
    createCabinInventory(
      cabinInventoryId = CabinInventoryId("inventory-1"),
      flightId = testFlightId,
      cabinClass = CabinClass.unsafe("economy"),
      availableSeats = SeatCount.unsafe(8),
      unitPrice = Money.unsafe(BigDecimal(1200), Currency.CNY),
      inventoryStatus = InventoryStatus.Open
    )

  test("flight rejects identical departure and arrival airport") {
    val result =
      createFlight(
        flightId = testFlightId,
        airlineId = AirlineId("airline-1"),
        flightNumber = FlightNumber.unsafe("MU5123"),
        departureAirport = AirportCode.unsafe("PVG"),
        arrivalAirport = AirportCode.unsafe("PVG"),
        flightSchedule = FlightSchedule.unsafe(
          OffsetDateTime.of(2026, 4, 10, 8, 0, 0, 0, ZoneOffset.UTC),
          OffsetDateTime.of(2026, 4, 10, 12, 0, 0, 0, ZoneOffset.UTC)
        ),
        basePrice = Money.unsafe(BigDecimal(1000), Currency.CNY),
        cabinInventories = Vector(economyCabinInventory),
        createdAt = createdAt
      )

    assert(result.swap.exists(_.isInstanceOf[FlightError.DepartureAirportMatchedArrivalAirport]))
  }

  test("open flight can return a bookable cabin inventory") {
    val createdFlight =
      createFlight(
          flightId = testFlightId,
          airlineId = AirlineId("airline-1"),
          flightNumber = FlightNumber.unsafe("MU5123"),
          departureAirport = AirportCode.unsafe("PVG"),
          arrivalAirport = AirportCode.unsafe("NRT"),
          flightSchedule = FlightSchedule.unsafe(
            OffsetDateTime.of(2026, 4, 10, 8, 0, 0, 0, ZoneOffset.UTC),
            OffsetDateTime.of(2026, 4, 10, 12, 0, 0, 0, ZoneOffset.UTC)
          ),
          basePrice = Money.unsafe(BigDecimal(1000), Currency.CNY),
          cabinInventories = Vector(economyCabinInventory),
          createdAt = createdAt
        ).toOption.get

    val result = createdFlight.ensureBookableCabinInventory(CabinClass.unsafe("economy"))

    assertEquals(result.map(_.cabinClass.value), Right("ECONOMY"))
  }

  test("sold out cabin inventory is not bookable") {
    val soldOutFlight =
      createFlight(
          flightId = testFlightId,
          airlineId = AirlineId("airline-1"),
          flightNumber = FlightNumber.unsafe("MU5123"),
          departureAirport = AirportCode.unsafe("PVG"),
          arrivalAirport = AirportCode.unsafe("NRT"),
          flightSchedule = FlightSchedule.unsafe(
            OffsetDateTime.of(2026, 4, 10, 8, 0, 0, 0, ZoneOffset.UTC),
            OffsetDateTime.of(2026, 4, 10, 12, 0, 0, 0, ZoneOffset.UTC)
          ),
          basePrice = Money.unsafe(BigDecimal(1000), Currency.CNY),
          cabinInventories = Vector(
            createCabinInventory(
              cabinInventoryId = CabinInventoryId("inventory-1-soldout"),
              flightId = testFlightId,
              cabinClass = CabinClass.unsafe("economy"),
              availableSeats = SeatCount.unsafe(0),
              unitPrice = Money.unsafe(BigDecimal(1200), Currency.CNY),
              inventoryStatus = InventoryStatus.SoldOut
            )
          ),
          createdAt = createdAt
        ).toOption.get

    val result = soldOutFlight.ensureBookableCabinInventory(CabinClass.unsafe("economy"))

    assert(result.swap.exists(_.isInstanceOf[FlightError.CabinInventoryWasNotBookable]))
  }

