// BookHotelPlanner 是酒店模块的预订入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.hotel.api

import com.typesafe.travel.hotel.objects.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.hotel.tables.BookHotelPlannerPlainSql
import com.typesafe.travel.hotel.tables.HotelRoomTypeSqlSupport
import com.typesafe.travel.shared.kernel.{RoomCount, RoomTypeId, StayPeriod}

import java.sql.Connection
import java.time.{Instant, LocalDate}
import java.util.UUID

object BookHotelPlanner extends ConnectionApiPlan[BookHotelPlannerRequest, HotelBookingPlannerResponse]:
  override val name: String = "BookHotelPlanner"

  override def plan(input: BookHotelPlannerRequest, connection: Connection): IO[HotelBookingPlannerResponse] =
    for
      stayPeriod <- IO.fromEither(StayPeriod.create(LocalDate.parse(input.checkInDate), LocalDate.parse(input.checkOutDate)))
      roomCount <- IO.fromEither(RoomCount.create(input.roomCount))
      _ <- if roomCount.value > 0 then IO.unit else IO.raiseError(new IllegalArgumentException("Room count must be greater than zero"))
      _ <- if input.guestTravelerIds.nonEmpty then IO.unit else IO.raiseError(new IllegalArgumentException("At least one guest traveler is required for hotel booking"))
      hotel <- IO.fromOption(HotelRoomTypeSqlSupport.loadHotelByRoomTypeId(connection, input.roomTypeId))(new IllegalArgumentException(s"Room type '${input.roomTypeId}' was not found"))
      roomTypeId = RoomTypeId(input.roomTypeId)
      roomType <- IO.fromEither(findRoomTypeById(hotel, roomTypeId).left.map(error => new IllegalArgumentException(error.message)))
      _ <- IO.fromEither(
        ensureHotelRoomTypeBookableForStay(
          hotel,
          roomTypeId,
          stayPeriod,
          roomCount,
          input.guestTravelerIds.size
        ).left.map(error => new IllegalArgumentException(error.message))
      )
      response <- IO.blocking {
        val orderId = s"order-${UUID.randomUUID().toString.take(12)}"
        val orderItemId = s"order-item-${UUID.randomUUID().toString.take(12)}"
        val totalNightCount = math.toIntExact(stayPeriod.stayNightCount * roomCount.value.toLong)
        val total = roomType.basePrice.multiply(totalNightCount).fold(throw _, identity)
        val totalAmount = total.amount.bigDecimal
        val totalCurrency = total.currency.toString
        BookHotelPlannerPlainSql.insertOrder(connection, orderId, input.userId, totalAmount, totalCurrency, Instant.now())
        BookHotelPlannerPlainSql.insertOrderItem(
          connection,
          orderItemId,
          orderId,
          hotel,
          roomType,
          input,
          totalAmount.toPlainString,
          totalCurrency
        )
        BookHotelPlannerResponseMapper.toHotelBookingPlannerResponse(orderId, orderItemId, "Draft", totalAmount.toPlainString, totalCurrency)
      }
    yield response
