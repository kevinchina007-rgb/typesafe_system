package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.flight.domain.*
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
        response <- Ok(FlightListResponseDto(flights.map { case (airline, flight) => FlightResponseDto.fromDomain(airline, flight) }).asJson)
      yield response

    case GET -> Root / "api" / "flights" / flightIdValue =>
      flightBookingApplicationService
        .getFlightDetails(FlightId(flightIdValue))
        .flatMap { case (airline, flight) => Ok(FlightResponseDto.fromDomain(airline, flight).asJson) }

    case request @ POST -> Root / "api" / "flights" / "book" =>
      for
        addFlightItemRequestDto <- request.as[BookFlightRequestDto]
        selectedCabinClass <- fromEither(OrderDtoMappers.toCabinClass(addFlightItemRequestDto.cabinClass))
        updatedOrder <- flightBookingApplicationService.createFlightOrder(
          actingUserId = UserId(addFlightItemRequestDto.buyerUserId),
          flightId = FlightId(addFlightItemRequestDto.flightId),
          travelerIds = addFlightItemRequestDto.travelerIds.map(TravelerId.apply),
          cabinClass = selectedCabinClass
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Created(orderResponseDto.asJson)
      yield response
  }
