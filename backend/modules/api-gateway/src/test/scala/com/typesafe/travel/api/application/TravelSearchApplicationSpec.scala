package com.typesafe.travel.api.application

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.travel.api.memory.{InMemoryFlightRepository, InMemoryHotelRepository, InMemoryInventoryReservationRepository, InMemoryOrderRepository, InMemoryTravelerProfileRepository}
import com.typesafe.travel.flight.domain.LiveFlightService
import com.typesafe.travel.hotel.domain.LiveHotelService
import com.typesafe.travel.inventory.domain.{LiveFlightInventoryLockingService, LiveHotelInventoryLockingService, LiveReservationLifecycle}
import com.typesafe.travel.order.domain.LiveOrderService
import java.time.LocalDate
import java.time.Duration
import munit.FunSuite

final class TravelSearchApplicationSpec extends FunSuite:
  private val inMemoryFlightRepository = InMemoryFlightRepository.create[IO]
  private val inMemoryHotelRepository = InMemoryHotelRepository.create[IO]
  private val inMemoryOrderRepository = InMemoryOrderRepository.create[IO]
  private val inMemoryInventoryReservationRepository = InMemoryInventoryReservationRepository.create[IO]
  private val inMemoryTravelerProfileRepository = InMemoryTravelerProfileRepository.create[IO]
  private val reservationLifecycle = LiveReservationLifecycle[IO](inMemoryInventoryReservationRepository)
  private val flightInventoryLockingService =
    LiveFlightInventoryLockingService[IO](inMemoryInventoryReservationRepository, Duration.ofMinutes(15), reservationLifecycle)
  private val hotelInventoryLockingService =
    LiveHotelInventoryLockingService[IO](inMemoryInventoryReservationRepository, Duration.ofMinutes(15), reservationLifecycle)

  private val flightBookingApplicationService =
    LiveFlightBookingApplicationService[IO](
      flightService = LiveFlightService[IO](inMemoryFlightRepository),
      flightRepository = inMemoryFlightRepository,
      flightInventoryLockingService = flightInventoryLockingService,
      orderRepository = inMemoryOrderRepository,
      travelerProfileRepository = inMemoryTravelerProfileRepository
    )

  private val hotelBookingApplicationService =
    LiveHotelBookingApplicationService[IO](
      hotelService = LiveHotelService[IO](inMemoryHotelRepository),
      hotelRepository = inMemoryHotelRepository,
      orderRepository = inMemoryOrderRepository,
      travelerProfileRepository = inMemoryTravelerProfileRepository,
      hotelInventoryLockingService = hotelInventoryLockingService
    )

  test("flight browse supports city alias matching and date filtering") {
    val flights = flightBookingApplicationService
      .browseFlights(
        departureAirportQuery = Some("Shanghai"),
        arrivalAirportQuery = Some("Tokyo"),
        departureDate = Some(LocalDate.parse("2026-04-05"))
      )
      .unsafeRunSync()

    assertEquals(flights.map(_._2.flightId.value), List("flight-mu5210"))
  }

  test("flight browse rejects too-short latin keywords with no results") {
    val flights = flightBookingApplicationService
      .browseFlights(
        departureAirportQuery = Some("sh"),
        arrivalAirportQuery = None,
        departureDate = Some(LocalDate.parse("2026-04-05"))
      )
      .unsafeRunSync()

    assertEquals(flights, Nil)
  }

  test("hotel browse supports alias and partial location matching") {
    val hotels = hotelBookingApplicationService
      .browseHotels(
        locationQuery = Some("West Lake"),
        stayPeriod = None
      )
      .unsafeRunSync()

    assertEquals(hotels.map(_.hotelId.value), List("hotel-hz-westlake"))
  }

  test("hotel browse rejects too-short latin keywords with no results") {
    val hotels = hotelBookingApplicationService
      .browseHotels(
        locationQuery = Some("sh"),
        stayPeriod = None
      )
      .unsafeRunSync()

    assertEquals(hotels, Nil)
  }
