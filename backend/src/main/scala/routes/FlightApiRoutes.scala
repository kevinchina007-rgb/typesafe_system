package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

trait FlightApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def flightRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "flights" :? DepartureAirportQueryParamMatcher(departureAirportValue) +&
        ArrivalAirportQueryParamMatcher(arrivalAirportValue) +&
        DepartureDateQueryParamMatcher(departureDateValue) =>
      for
        departureAirportQuery <- parseOptionalSearchText(departureAirportValue)
        arrivalAirportQuery <- parseOptionalSearchText(arrivalAirportValue)
        departureDate <- parseOptionalDate(departureDateValue)
        flights <- flightBookingApplicationService.browseFlights(departureAirportQuery, arrivalAirportQuery, departureDate)
        currentTime <- currentInstantF
        flightResponseDtos <- flights.traverse { case (airline, flight) =>
          loadRemainingFlightSeats(flight, currentTime).map(remainingSeats => FlightResponseDto.fromDomain(airline, flight, remainingSeats))
        }
        response <- Ok(FlightListResponseDto(flightResponseDtos).asJson)
      yield response

    case GET -> Root / "api" / "flights" / flightIdValue =>
      flightBookingApplicationService
        .getFlightDetails(FlightId(flightIdValue))
        .flatMap { case (airline, flight) =>
          currentInstantF.flatMap { currentTime =>
            loadRemainingFlightSeats(flight, currentTime).flatMap(remainingSeats => Ok(FlightResponseDto.fromDomain(airline, flight, remainingSeats).asJson))
          }
        }

    case request @ POST -> Root / "api" / "flights" / "book" =>
      for
        currentUserId <- requireCurrentUserId(request)
        addFlightItemRequestDto <- request.as[BookFlightRequestDto]
        selectedCabinClass <- fromEither(OrderDtoMappers.toCabinClass(addFlightItemRequestDto.cabinClass))
        updatedOrder <- flightBookingApplicationService.createFlightOrder(
          actingUserId = currentUserId,
          flightId = FlightId(addFlightItemRequestDto.flightId),
          travelerIds = addFlightItemRequestDto.travelerIds.map(TravelerId.apply),
          cabinClass = selectedCabinClass
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Created(orderResponseDto.asJson)
      yield response
  }

  private def loadRemainingFlightSeats(flight: Flight, currentTime: java.time.Instant): F[Map[String, Int]] =
    flight.cabinInventories.toList
      .traverse { cabinInventory =>
        inventoryReservationRepository
          .findReservationsByResource(ReservationResourceType.FlightCabinInventory, cabinInventory.cabinInventoryId.value)
          .map { reservations =>
            val reservedQuantity = reservations.foldLeft(0) { (currentQuantity, reservation) =>
              if reservation.reservationStatus == ReservationStatus.Confirmed || reservation.isActiveAt(currentTime) then currentQuantity + reservation.quantity
              else currentQuantity
            }
            cabinInventory.cabinInventoryId.value -> (cabinInventory.availableSeats.value - reservedQuantity).max(0)
          }
      }
      .map(_.toMap)
