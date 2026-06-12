// BookingDatabaseCodecs 定义数据库 codec 映射。

package com.typesafe.travel.persistence.codecs

import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import io.circe.{Json}
import io.circe.syntax.*

import java.time.{Instant, LocalDate, OffsetDateTime}
import scala.util.Try

object BookingDatabaseCodecs:
  def encodeOrderLineItemSnapshot(orderLineItem: OrderLineItem): String =
    orderLineItem match
      case flightOrderItem: FlightOrderItem =>
        DatabaseCodecs.SerializedFlightBookingSnapshot(
          airlineId = flightOrderItem.flightBookingSnapshot.airlineId.value,
          airlineName = flightOrderItem.flightBookingSnapshot.airlineName.value,
          airlineCode = flightOrderItem.flightBookingSnapshot.airlineCode.value,
          flightId = flightOrderItem.flightBookingSnapshot.flightId.value,
          flightNumber = flightOrderItem.flightBookingSnapshot.flightNumber.value,
          departureAt = flightOrderItem.flightBookingSnapshot.flightSchedule.departureAt.toString,
          arrivalAt = flightOrderItem.flightBookingSnapshot.flightSchedule.arrivalAt.toString,
          departureAirportCode = flightOrderItem.flightBookingSnapshot.departureAirportCode.value,
          arrivalAirportCode = flightOrderItem.flightBookingSnapshot.arrivalAirportCode.value,
          cabinClass = flightOrderItem.flightBookingSnapshot.cabinClass.value,
          travelerIds = flightOrderItem.flightBookingSnapshot.travelerIds.map(_.value),
          unitPriceAmount = flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.amount,
          unitPriceCurrency = flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.currency.toString
        ).asJson.noSpaces
      case hotelOrderItem: HotelOrderItem =>
        DatabaseCodecs.SerializedHotelBookingSnapshot(
          hotelId = hotelOrderItem.hotelBookingSnapshot.hotelId.value,
          hotelName = hotelOrderItem.hotelBookingSnapshot.hotelName.value,
          hotelLocation = hotelOrderItem.hotelBookingSnapshot.hotelLocation.value,
          roomTypeId = hotelOrderItem.hotelBookingSnapshot.roomTypeId.value,
          roomTypeName = hotelOrderItem.hotelBookingSnapshot.roomTypeName.value,
          checkInDate = hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkIn.toString,
          checkOutDate = hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkOut.toString,
          guestTravelerIds = hotelOrderItem.hotelBookingSnapshot.guestTravelerIds.map(_.value),
          roomCount = hotelOrderItem.hotelBookingSnapshot.roomCount.value,
          unitPriceAmount = hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.amount,
          unitPriceCurrency = hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.currency.toString
        ).asJson.noSpaces
      case trainOrderItem: TrainOrderItem =>
        TrainDatabaseCodecs.encodeTrainOrderItemSnapshot(trainOrderItem)
      case attractionOrderItem: AttractionOrderItem =>
        DatabaseCodecs.SerializedAttractionTicketSnapshot(
          attractionId = attractionOrderItem.attractionTicketSnapshot.attractionId.value,
          managerId = attractionOrderItem.attractionTicketSnapshot.managerId.value,
          attractionName = attractionOrderItem.attractionTicketSnapshot.attractionName,
          ticketTypeId = attractionOrderItem.attractionTicketSnapshot.ticketTypeId.value,
          ticketTypeName = attractionOrderItem.attractionTicketSnapshot.ticketTypeName,
          sessionId = attractionOrderItem.attractionTicketSnapshot.sessionId.map(_.value),
          sessionName = attractionOrderItem.attractionTicketSnapshot.sessionName,
          sessionStartsAt = attractionOrderItem.attractionTicketSnapshot.sessionStartsAt.map(_.toString),
          sessionEndsAt = attractionOrderItem.attractionTicketSnapshot.sessionEndsAt.map(_.toString),
          useDate = attractionOrderItem.attractionTicketSnapshot.useDate.toString,
          travelerIds = attractionOrderItem.attractionTicketSnapshot.travelerIds.map(_.value),
          unitPriceAmount = attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.amount,
          unitPriceCurrency = attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.currency.toString,
          ruleSummaries = attractionOrderItem.attractionTicketSnapshot.ruleSummaries,
          eligibilityValidatedAt = attractionOrderItem.attractionTicketSnapshot.eligibilityValidatedAt.toString
        ).asJson.noSpaces

  def decodeFlightBookingSnapshot(serializedValue: String): Either[Throwable, FlightBookingSnapshot] =
    io.circe.parser.decode[DatabaseCodecs.SerializedFlightBookingSnapshot](serializedValue).flatMap { serializedFlightBookingSnapshot =>
      for
        airlineName <- AirlineName.create(serializedFlightBookingSnapshot.airlineName)
        airlineCode <- AirlineCode.create(serializedFlightBookingSnapshot.airlineCode)
        flightNumber <- FlightNumber.create(serializedFlightBookingSnapshot.flightNumber)
        departureAirportCode <- AirportCode.create(serializedFlightBookingSnapshot.departureAirportCode)
        arrivalAirportCode <- AirportCode.create(serializedFlightBookingSnapshot.arrivalAirportCode)
        cabinClass <- CabinClass.create(serializedFlightBookingSnapshot.cabinClass)
        flightSchedule <- FlightSchedule.create(
          departureAt = OffsetDateTime.parse(serializedFlightBookingSnapshot.departureAt),
          arrivalAt = OffsetDateTime.parse(serializedFlightBookingSnapshot.arrivalAt)
        )
        unitPriceCurrency <- DatabaseCodecs.parseCurrency(serializedFlightBookingSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedFlightBookingSnapshot.unitPriceAmount, unitPriceCurrency)
      yield FlightBookingSnapshot(
        airlineId = AirlineId(serializedFlightBookingSnapshot.airlineId),
        airlineName = airlineName,
        airlineCode = airlineCode,
        flightId = FlightId(serializedFlightBookingSnapshot.flightId),
        flightNumber = flightNumber,
        flightSchedule = flightSchedule,
        departureAirportCode = departureAirportCode,
        arrivalAirportCode = arrivalAirportCode,
        cabinClass = cabinClass,
        travelerIds = serializedFlightBookingSnapshot.travelerIds.map(TravelerId.apply),
        unitPriceSnapshot = unitPriceSnapshot
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode flight booking snapshot: ${error.getMessage}", error))

  def decodeHotelBookingSnapshot(serializedValue: String): Either[Throwable, HotelBookingSnapshot] =
    io.circe.parser.decode[DatabaseCodecs.SerializedHotelBookingSnapshot](serializedValue).flatMap { serializedHotelBookingSnapshot =>
      for
        hotelName <- HotelName.create(serializedHotelBookingSnapshot.hotelName)
        hotelLocation <- HotelLocation.create(serializedHotelBookingSnapshot.hotelLocation)
        roomTypeName <- RoomTypeName.create(serializedHotelBookingSnapshot.roomTypeName)
        stayPeriod <- StayPeriod.create(
          checkIn = LocalDate.parse(serializedHotelBookingSnapshot.checkInDate),
          checkOut = LocalDate.parse(serializedHotelBookingSnapshot.checkOutDate)
        )
        roomCount <- RoomCount.create(serializedHotelBookingSnapshot.roomCount)
        unitPriceCurrency <- DatabaseCodecs.parseCurrency(serializedHotelBookingSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedHotelBookingSnapshot.unitPriceAmount, unitPriceCurrency)
      yield HotelBookingSnapshot(
        hotelId = HotelId(serializedHotelBookingSnapshot.hotelId),
        hotelName = hotelName,
        hotelLocation = hotelLocation,
        roomTypeId = RoomTypeId(serializedHotelBookingSnapshot.roomTypeId),
        roomTypeName = roomTypeName,
        stayPeriod = stayPeriod,
        guestTravelerIds = serializedHotelBookingSnapshot.guestTravelerIds.map(TravelerId.apply),
        roomCount = roomCount,
        unitPriceSnapshot = unitPriceSnapshot
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode hotel booking snapshot: ${error.getMessage}", error))

  def decodeAttractionTicketSnapshot(serializedValue: String): Either[Throwable, AttractionTicketSnapshot] =
    io.circe.parser.decode[DatabaseCodecs.SerializedAttractionTicketSnapshot](serializedValue).flatMap { serializedAttractionTicketSnapshot =>
      for
        unitPriceCurrency <- DatabaseCodecs.parseCurrency(serializedAttractionTicketSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedAttractionTicketSnapshot.unitPriceAmount, unitPriceCurrency)
      yield AttractionTicketSnapshot(
        attractionId = AttractionId(serializedAttractionTicketSnapshot.attractionId),
        managerId = ManagerId(serializedAttractionTicketSnapshot.managerId),
        attractionName = serializedAttractionTicketSnapshot.attractionName,
        ticketTypeId = TicketTypeId(serializedAttractionTicketSnapshot.ticketTypeId),
        ticketTypeName = serializedAttractionTicketSnapshot.ticketTypeName,
        sessionId = serializedAttractionTicketSnapshot.sessionId.map(AttractionTicketSessionId.apply),
        sessionName = serializedAttractionTicketSnapshot.sessionName,
        sessionStartsAt = serializedAttractionTicketSnapshot.sessionStartsAt.map(Instant.parse),
        sessionEndsAt = serializedAttractionTicketSnapshot.sessionEndsAt.map(Instant.parse),
        useDate = LocalDate.parse(serializedAttractionTicketSnapshot.useDate),
        travelerIds = serializedAttractionTicketSnapshot.travelerIds.map(TravelerId.apply),
        unitPriceSnapshot = unitPriceSnapshot,
        ruleSummaries = serializedAttractionTicketSnapshot.ruleSummaries,
        eligibilityValidatedAt = Instant.parse(serializedAttractionTicketSnapshot.eligibilityValidatedAt)
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode attraction ticket snapshot: ${error.getMessage}", error))

  def parseCurrency(currencyValue: String): Either[Throwable, Currency] =
    Try(Currency.fromText(currencyValue)).toEither

  def parseJson(jsonValue: String): Either[Throwable, Json] =
    io.circe.parser.parse(jsonValue).left.map(error => new IllegalArgumentException(error.getMessage, error))
