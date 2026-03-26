package com.typesafe.travel.persistence.flight

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.time.{Instant, OffsetDateTime}

final class DoobieFlightRepository[F[_]: Async](
    transactor: Transactor[F]
) extends FlightRepository[F]:
  override def findAirlineById(airlineId: AirlineId): F[Option[Airline]] =
    sql"""
      select airline_id, name, code, status, created_at
      from airlines
      where airline_id = ${airlineId.value}
    """
      .query[(String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildAirline))

  override def findFlightById(flightId: FlightId): F[Option[Flight]] =
    sql"""
      select
        flight_id, airline_id, flight_number, departure_airport, arrival_airport,
        departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at
      from flights
      where flight_id = ${flightId.value}
    """
      .query[FlightRow]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildFlight))

  override def searchFlights(flightSearchCriteria: FlightSearchCriteria): F[List[Flight]] =
    sql"""
      select
        flight_id, airline_id, flight_number, departure_airport, arrival_airport,
        departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at
      from flights
      where (${flightSearchCriteria.departureAirport.map(_.value)} is null or departure_airport = ${flightSearchCriteria.departureAirport.map(_.value)})
        and (${flightSearchCriteria.arrivalAirport.map(_.value)} is null or arrival_airport = ${flightSearchCriteria.arrivalAirport.map(_.value)})
        and (${flightSearchCriteria.departureDate} is null or cast(departure_time as date) = ${flightSearchCriteria.departureDate})
      order by departure_time, flight_id
    """
      .query[FlightRow]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildFlight))

  private def buildAirline(row: (String, String, String, String, Instant)): F[Airline] =
    val (airlineIdValue, airlineNameValue, airlineCodeValue, airlineStatusValue, createdAtValue) = row
    for
      airlineName <- Async[F].fromEither(AirlineName.create(airlineNameValue))
      airlineCode <- Async[F].fromEither(AirlineCode.create(airlineCodeValue))
    yield Airline.restorePersistedAirline(
      airlineId = AirlineId(airlineIdValue),
      airlineName = airlineName,
      airlineCode = airlineCode,
      airlineStatus = AirlineStatus.valueOf(airlineStatusValue),
      createdAt = createdAtValue
    )

  private def buildFlight(flightRow: FlightRow): F[Flight] =
    for
      flightNumber <- Async[F].fromEither(FlightNumber.create(flightRow.flightNumber))
      departureAirport <- Async[F].fromEither(AirportCode.create(flightRow.departureAirport))
      arrivalAirport <- Async[F].fromEither(AirportCode.create(flightRow.arrivalAirport))
      basePriceCurrency <- Async[F].fromEither(Either.catchNonFatal(Currency.valueOf(flightRow.basePriceCurrency)))
      basePrice <- Async[F].fromEither(Money.create(flightRow.basePriceAmount, basePriceCurrency))
      cabinInventories <- loadCabinInventories(FlightId(flightRow.flightId))
      flightSchedule <- Async[F].fromEither(FlightSchedule.create(flightRow.departureTime, flightRow.arrivalTime))
      builtFlight = Flight.restorePersistedFlight(
        flightId = FlightId(flightRow.flightId),
        airlineId = AirlineId(flightRow.airlineId),
        flightNumber = flightNumber,
        departureAirport = departureAirport,
        arrivalAirport = arrivalAirport,
        flightSchedule = flightSchedule,
        flightStatus = FlightStatus.valueOf(flightRow.status),
        basePrice = basePrice,
        cabinInventories = cabinInventories,
        createdAt = flightRow.createdAt
      )
    yield builtFlight

  private def loadCabinInventories(flightId: FlightId): F[Vector[CabinInventory]] =
    sql"""
      select inventory_id, flight_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status
      from flight_cabin_inventories
      where flight_id = ${flightId.value}
      order by inventory_id
    """
      .query[(String, String, String, Int, BigDecimal, String, String)]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildCabinInventory).map(_.toVector))

  private def buildCabinInventory(row: (String, String, String, Int, BigDecimal, String, String)): F[CabinInventory] =
    val (inventoryIdValue, flightIdValue, cabinClassValue, availableSeatsValue, unitPriceAmountValue, unitPriceCurrencyValue, inventoryStatusValue) = row
    for
      cabinClass <- Async[F].fromEither(CabinClass.create(cabinClassValue))
      availableSeats <- Async[F].fromEither(SeatCount.create(availableSeatsValue))
      unitPriceCurrency <- Async[F].fromEither(Either.catchNonFatal(Currency.valueOf(unitPriceCurrencyValue)))
      unitPrice <- Async[F].fromEither(Money.create(unitPriceAmountValue, unitPriceCurrency))
    yield CabinInventory.createCabinInventory(
      cabinInventoryId = CabinInventoryId(inventoryIdValue),
      flightId = FlightId(flightIdValue),
      cabinClass = cabinClass,
      availableSeats = availableSeats,
      unitPrice = unitPrice,
      inventoryStatus = InventoryStatus.valueOf(inventoryStatusValue)
    )

  private final case class FlightRow(
      flightId: String,
      airlineId: String,
      flightNumber: String,
      departureAirport: String,
      arrivalAirport: String,
      departureTime: OffsetDateTime,
      arrivalTime: OffsetDateTime,
      status: String,
      basePriceAmount: BigDecimal,
      basePriceCurrency: String,
      createdAt: Instant
  )
