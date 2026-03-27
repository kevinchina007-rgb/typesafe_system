package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryFlightRepository[F[_]: Sync] private (
    airlineState: TrieMap[AirlineId, Airline],
    flightState: TrieMap[FlightId, Flight],
    airlineSequence: AtomicLong,
    flightSequence: AtomicLong,
    inventorySequence: AtomicLong
) extends FlightRepository[F]:
  override def nextAirlineId: F[AirlineId] =
    Sync[F].delay(AirlineId(s"airline-generated-${airlineSequence.incrementAndGet()}"))

  override def nextFlightId: F[FlightId] =
    Sync[F].delay(FlightId(s"flight-generated-${flightSequence.incrementAndGet()}"))

  override def nextCabinInventoryId: F[CabinInventoryId] =
    Sync[F].delay(CabinInventoryId(s"inventory-generated-${inventorySequence.incrementAndGet()}"))

  override def findAirlineById(airlineId: AirlineId): F[Option[Airline]] =
    Sync[F].delay(airlineState.get(airlineId))

  override def findFlightById(flightId: FlightId): F[Option[Flight]] =
    Sync[F].delay(flightState.get(flightId))

  override def searchFlights(flightSearchCriteria: FlightSearchCriteria): F[List[Flight]] =
    Sync[F].delay(
      flightState.values.toList
        .filter(_.matchesSearch(flightSearchCriteria.departureAirport, flightSearchCriteria.arrivalAirport, flightSearchCriteria.departureDate))
        .sortBy(_.flightSchedule.departureAt.toInstant)
    )

  override def saveAirline(airline: Airline): F[Airline] =
    Sync[F].delay {
      airlineState.put(airline.airlineId, airline)
      airline
    }

  override def saveFlight(flight: Flight): F[Flight] =
    Sync[F].delay {
      flightState.put(flight.flightId, flight)
      flight
    }

object InMemoryFlightRepository:
  def create[F[_]: Sync]: InMemoryFlightRepository[F] =
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
      airlineState = TrieMap(chinaEasternAirline.airlineId -> chinaEasternAirline, springAirline.airlineId -> springAirline),
      flightState = TrieMap.from(sampleFlights.map(flight => flight.flightId -> flight)),
      airlineSequence = AtomicLong(100),
      flightSequence = AtomicLong(100),
      inventorySequence = AtomicLong(1000)
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
