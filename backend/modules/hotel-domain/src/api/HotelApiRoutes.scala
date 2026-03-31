package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.LocalDate

trait HotelApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def hotelRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "hotels" :? HotelLocationQueryParamMatcher(locationValue) +&
        CheckInDateQueryParamMatcher(checkInDateValue) +&
        CheckOutDateQueryParamMatcher(checkOutDateValue) =>
      for
        locationQuery <- parseOptionalSearchText(locationValue)
        stayPeriod <- parseOptionalStayPeriod(checkInDateValue, checkOutDateValue)
        hotels <- hotelBookingApplicationService.browseHotels(locationQuery, stayPeriod)
        response <- Ok(HotelListResponseDto(hotels.map(hotel => HotelResponseDto.fromDomain(hotel, stayPeriod))).asJson)
      yield response

    case GET -> Root / "api" / "hotels" / hotelIdValue :? CheckInDateQueryParamMatcher(checkInDateValue) +&
        CheckOutDateQueryParamMatcher(checkOutDateValue) =>
      for
        stayPeriod <- parseOptionalStayPeriod(checkInDateValue, checkOutDateValue)
        hotel <- hotelBookingApplicationService.getHotelDetails(HotelId(hotelIdValue))
        response <- Ok(HotelResponseDto.fromDomain(hotel, stayPeriod).asJson)
      yield response

    case request @ POST -> Root / "api" / "hotels" / "book" =>
      for
        addHotelItemRequestDto <- request.as[BookHotelRequestDto]
        roomCount <- fromEither(RoomCount.create(addHotelItemRequestDto.roomCount))
        updatedOrder <- hotelBookingApplicationService.createHotelOrder(
          actingUserId = UserId(addHotelItemRequestDto.buyerUserId),
          roomTypeId = RoomTypeId(addHotelItemRequestDto.roomTypeId),
          guestTravelerIds = addHotelItemRequestDto.guestTravelerIds.map(TravelerId.apply),
          checkInDate = LocalDate.parse(addHotelItemRequestDto.checkInDate),
          checkOutDate = LocalDate.parse(addHotelItemRequestDto.checkOutDate),
          roomCount = roomCount
        )
        orderResponseDto <- toOrderResponseDto(updatedOrder)
        response <- Created(orderResponseDto.asJson)
      yield response
  }
