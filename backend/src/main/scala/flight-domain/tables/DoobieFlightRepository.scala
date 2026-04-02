package com.typesafe.travel.persistence.flight

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*
import java.time.{Instant, OffsetDateTime}
import java.util.UUID

final class DoobieFlightRepository[F[_]: Async](
    transactor: Transactor[F]
) extends FlightRepository[F]:
  override def nextAirlineId: F[AirlineId] =
    Sync[F].delay(AirlineId(s"airline-${UUID.randomUUID().toString.take(12)}"))

  override def nextFlightId: F[FlightId] =
    Sync[F].delay(FlightId(s"flight-${UUID.randomUUID().toString.take(12)}"))

  override def nextCabinInventoryId: F[CabinInventoryId] =
    Sync[F].delay(CabinInventoryId(s"inventory-${UUID.randomUUID().toString.take(12)}"))

  def saveAirline(airline: Airline): F[Airline] =
    val upsertAirline =
      for
        updatedRowCount <- sql"""
          update airlines
          set
            name = ${airline.airlineName.value},
            code = ${airline.airlineCode.value},
            status = ${airline.airlineStatus.toString},
            created_at = ${airline.createdAt}
          where airline_id = ${airline.airlineId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into airlines (airline_id, name, code, status, created_at)
            values (
              ${airline.airlineId.value},
              ${airline.airlineName.value},
              ${airline.airlineCode.value},
              ${airline.airlineStatus.toString},
              ${airline.createdAt}
            )
          """.update.run.void
      yield ()

    upsertAirline.transact(transactor).as(airline)

  def saveFlight(flight: Flight): F[Flight] =
    val upsertFlight =
      for
        updatedRowCount <- sql"""
          update flights
          set
            airline_id = ${flight.airlineId.value},
            flight_number = ${flight.flightNumber.value},
            departure_airport = ${flight.departureAirport.value},
            arrival_airport = ${flight.arrivalAirport.value},
            departure_time = ${flight.flightSchedule.departureAt},
            arrival_time = ${flight.flightSchedule.arrivalAt},
            status = ${flight.flightStatus.toString},
            base_price_amount = ${flight.basePrice.amount},
            base_price_currency = ${flight.basePrice.currency.toString},
            created_at = ${flight.createdAt}
          where flight_id = ${flight.flightId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into flights (
              flight_id, airline_id, flight_number, departure_airport, arrival_airport,
              departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at
            ) values (
              ${flight.flightId.value},
              ${flight.airlineId.value},
              ${flight.flightNumber.value},
              ${flight.departureAirport.value},
              ${flight.arrivalAirport.value},
              ${flight.flightSchedule.departureAt},
              ${flight.flightSchedule.arrivalAt},
              ${flight.flightStatus.toString},
              ${flight.basePrice.amount},
              ${flight.basePrice.currency.toString},
              ${flight.createdAt}
            )
          """.update.run.void
        _ <- sql"delete from flight_cabin_inventories where flight_id = ${flight.flightId.value}".update.run
        _ <- flight.cabinInventories.traverse_ { cabinInventory =>
          sql"""
            insert into flight_cabin_inventories (
              inventory_id, flight_id, cabin_class, available_seats, unit_price_amount, unit_price_currency,
              status, version_number, updated_at
            ) values (
              ${cabinInventory.cabinInventoryId.value},
              ${flight.flightId.value},
              ${cabinInventory.cabinClass.value},
              ${cabinInventory.availableSeats.value},
              ${cabinInventory.unitPrice.amount},
              ${cabinInventory.unitPrice.currency.toString},
              ${cabinInventory.inventoryStatus.toString},
              ${0},
              ${Option.empty[Instant]}
            )
          """.update.run
        }
      yield ()

    upsertFlight.transact(transactor).as(flight)

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
    val conditions = List(
      flightSearchCriteria.departureAirport.map(airportCode => fr"departure_airport = ${airportCode.value}"),
      flightSearchCriteria.arrivalAirport.map(airportCode => fr"arrival_airport = ${airportCode.value}"),
      flightSearchCriteria.departureDate.map(departureDate => fr"cast(departure_time as date) = $departureDate")
    ).flatten

    val whereFragment =
      conditions match
        case Nil => Fragment.empty
        case head :: tail => fr"where" ++ (head ++ tail.foldLeft(Fragment.empty)((accumulator, fragment) => accumulator ++ fr"and" ++ fragment))

    val query =
      (fr"""
        select
          flight_id, airline_id, flight_number, departure_airport, arrival_airport,
          departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at
        from flights
      """ ++ whereFragment ++ fr"order by departure_time, flight_id")

    query.query[FlightRow].to[List].transact(transactor).flatMap(_.traverse(buildFlight))

  private def buildAirline(row: (String, String, String, String, Instant)): F[Airline] =
    val (airlineIdValue, airlineNameValue, airlineCodeValue, airlineStatusValue, createdAtValue) = row
    for
      airlineName <- Async[F].fromEither(AirlineName.create(airlineNameValue))
      airlineCode <- Async[F].fromEither(AirlineCode.create(airlineCodeValue))
    yield restorePersistedAirline(
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
      builtFlight = restorePersistedFlight(
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
    yield createCabinInventory(
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

