package com.typesafe.travel.api.memory

import cats.Applicative
import cats.syntax.all.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

final class InMemoryFlightRepository[F[_]: Applicative] private (
    airlines: Map[AirlineId, Airline],
    flights: Map[FlightId, Flight]
) extends FlightRepository[F]:
  override def findAirlineById(airlineId: AirlineId): F[Option[Airline]] =
    airlines.get(airlineId).pure[F]

  override def findFlightById(flightId: FlightId): F[Option[Flight]] =
    flights.get(flightId).pure[F]

  override def searchFlights(flightSearchCriteria: FlightSearchCriteria): F[List[Flight]] =
    flights.values.toList
      .filter(_.matchesSearch(flightSearchCriteria.departureAirport, flightSearchCriteria.arrivalAirport, flightSearchCriteria.departureDate))
      .sortBy(_.flightSchedule.departureAt.toInstant)
      .pure[F]

object InMemoryFlightRepository:
  def create[F[_]: Applicative]: InMemoryFlightRepository[F] =
    val chinaEasternAirline =
      Airline.createAirline(
        airlineId = AirlineId("airline-mu"),
        airlineName = AirlineName.unsafe("China Eastern"),
        airlineCode = AirlineCode.unsafe("MU"),
        createdAt = Instant.parse("2026-03-25T00:00:00Z")
      )

    val springAirline =
      Airline.createAirline(
        airlineId = AirlineId("airline-9c"),
        airlineName = AirlineName.unsafe("Spring Airlines"),
        airlineCode = AirlineCode.unsafe("9C"),
        createdAt = Instant.parse("2026-03-25T00:00:00Z")
      )

    val sampleFlights = List(
      buildFlight(
        flightId = FlightId("flight-mu5123"),
        airlineId = chinaEasternAirline.airlineId,
        flightNumber = FlightNumber.unsafe("MU5123"),
        departureAirport = AirportCode.unsafe("SHA"),
        arrivalAirport = AirportCode.unsafe("HGH"),
        departureDate = LocalDate.parse("2026-04-05"),
        departureHour = 8,
        arrivalHour = 9,
        basePriceAmount = BigDecimal(680),
        inventoryPrefix = "mu5123"
      ),
      buildFlight(
        flightId = FlightId("flight-mu5210"),
        airlineId = chinaEasternAirline.airlineId,
        flightNumber = FlightNumber.unsafe("MU5210"),
        departureAirport = AirportCode.unsafe("PVG"),
        arrivalAirport = AirportCode.unsafe("NRT"),
        departureDate = LocalDate.parse("2026-04-05"),
        departureHour = 10,
        arrivalHour = 14,
        basePriceAmount = BigDecimal(2400),
        inventoryPrefix = "mu5210"
      ),
      buildFlight(
        flightId = FlightId("flight-9c8821"),
        airlineId = springAirline.airlineId,
        flightNumber = FlightNumber.unsafe("9C8821"),
        departureAirport = AirportCode.unsafe("PVG"),
        arrivalAirport = AirportCode.unsafe("ICN"),
        departureDate = LocalDate.parse("2026-04-05"),
        departureHour = 13,
        arrivalHour = 16,
        basePriceAmount = BigDecimal(1900),
        inventoryPrefix = "9c8821"
      )
    )

    new InMemoryFlightRepository[F](
      airlines = Map(chinaEasternAirline.airlineId -> chinaEasternAirline, springAirline.airlineId -> springAirline),
      flights = sampleFlights.map(flight => flight.flightId -> flight).toMap
    )

  private def buildFlight(
      flightId: FlightId,
      airlineId: AirlineId,
      flightNumber: FlightNumber,
      departureAirport: AirportCode,
      arrivalAirport: AirportCode,
      departureDate: LocalDate,
      departureHour: Int,
      arrivalHour: Int,
      basePriceAmount: BigDecimal,
      inventoryPrefix: String
  ): Flight =
    Flight
      .createFlight(
        flightId = flightId,
        airlineId = airlineId,
        flightNumber = flightNumber,
        departureAirport = departureAirport,
        arrivalAirport = arrivalAirport,
        flightSchedule = FlightSchedule.unsafe(
          OffsetDateTime.of(departureDate.getYear, departureDate.getMonthValue, departureDate.getDayOfMonth, departureHour, 0, 0, 0, ZoneOffset.ofHours(8)),
          OffsetDateTime.of(departureDate.getYear, departureDate.getMonthValue, departureDate.getDayOfMonth, arrivalHour, 0, 0, 0, ZoneOffset.ofHours(8))
        ),
        basePrice = Money.unsafe(basePriceAmount, Currency.CNY),
        cabinInventories = Vector(
          CabinInventory.createCabinInventory(
            cabinInventoryId = CabinInventoryId(s"$inventoryPrefix-economy"),
            flightId = flightId,
            cabinClass = CabinClass.unsafe("economy"),
            availableSeats = SeatCount.unsafe(6),
            unitPrice = Money.unsafe(basePriceAmount, Currency.CNY),
            inventoryStatus = InventoryStatus.Open
          ),
          CabinInventory.createCabinInventory(
            cabinInventoryId = CabinInventoryId(s"$inventoryPrefix-business"),
            flightId = flightId,
            cabinClass = CabinClass.unsafe("business"),
            availableSeats = SeatCount.unsafe(2),
            unitPrice = Money.unsafe(basePriceAmount + 1200, Currency.CNY),
            inventoryStatus = InventoryStatus.Open
          )
        ),
        createdAt = Instant.parse("2026-03-25T00:00:00Z")
      )
      .toOption
      .get
