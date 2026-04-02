package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.inventory.domain.*
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
        currentTime <- currentInstantF
        hotelResponseDtos <- hotels.traverse(hotel => loadRemainingHotelRooms(hotel, stayPeriod, currentTime).map(remainingRooms => HotelResponseDto.fromDomain(hotel, stayPeriod, remainingRooms)))
        response <- Ok(HotelListResponseDto(hotelResponseDtos).asJson)
      yield response

    case GET -> Root / "api" / "hotels" / hotelIdValue :? CheckInDateQueryParamMatcher(checkInDateValue) +&
        CheckOutDateQueryParamMatcher(checkOutDateValue) =>
      for
        stayPeriod <- parseOptionalStayPeriod(checkInDateValue, checkOutDateValue)
        hotel <- hotelBookingApplicationService.getHotelDetails(HotelId(hotelIdValue))
        currentTime <- currentInstantF
        remainingRooms <- loadRemainingHotelRooms(hotel, stayPeriod, currentTime)
        response <- Ok(HotelResponseDto.fromDomain(hotel, stayPeriod, remainingRooms).asJson)
      yield response

    case request @ POST -> Root / "api" / "hotels" / "book" =>
      for
        currentUserId <- requireCurrentUserId(request)
        addHotelItemRequestDto <- request.as[BookHotelRequestDto]
        roomCount <- fromEither(RoomCount.create(addHotelItemRequestDto.roomCount))
        updatedOrder <- hotelBookingApplicationService.createHotelOrder(
          actingUserId = currentUserId,
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

  private def loadRemainingHotelRooms(
      hotel: Hotel,
      stayPeriod: Option[StayPeriod],
      currentTime: java.time.Instant
  ): F[Map[String, Int]] =
    stayPeriod match
      case None => Map.empty[String, Int].pure[F]
      case Some(period) =>
        hotel.roomTypes.toList
          .traverse { roomType =>
            inventoryReservationRepository
              .findReservationsByResource(ReservationResourceType.HotelRoomType, roomType.roomTypeId.value)
              .map { reservations =>
                val stayDates =
                  Iterator.iterate(period.checkIn)(_.plusDays(1))
                    .takeWhile(_.isBefore(period.checkOut))
                    .toList
                val perDateRemaining = stayDates.flatMap { stayDate =>
                  roomType.findRoomInventoryByDate(stayDate).map { roomInventory =>
                    val reservedQuantity = reservations.foldLeft(0) { (currentQuantity, reservation) =>
                      val overlapsStayDate =
                        reservation.checkInDate.exists(checkIn => !stayDate.isBefore(checkIn)) &&
                          reservation.checkOutDate.exists(checkOut => stayDate.isBefore(checkOut))
                      if overlapsStayDate && (reservation.reservationStatus == ReservationStatus.Confirmed || reservation.isActiveAt(currentTime)) then
                        currentQuantity + reservation.quantity
                      else currentQuantity
                    }
                    (roomInventory.availableRooms.value - reservedQuantity).max(0)
                  }
                }
                roomType.roomTypeId.value -> perDateRemaining.minOption.getOrElse(0)
              }
          }
          .map(_.toMap)
