package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}

import java.time.{Instant, LocalDate, OffsetDateTime}
import scala.util.Try

private[domain] object OrderSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[LocalDate] = Encoder.encodeString.contramap(_.toString)
  given Decoder[LocalDate] = Decoder.decodeString.emap(value => Try(LocalDate.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[OffsetDateTime] = Encoder.encodeString.contramap(_.toString)
  given Decoder[OffsetDateTime] = Decoder.decodeString.emap(value => Try(OffsetDateTime.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[Currency] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Currency] = Decoder.decodeString.emap(value => Try(Currency.fromText(value)).toEither.left.map(_.getMessage))

  given Encoder[Money] =
    Encoder.instance(money =>
      Json.obj(
        "amount" -> Json.fromBigDecimal(money.amount),
        "currency" -> Json.fromString(money.currency.toString)
      )
    )

  given Decoder[Money] =
    Decoder.instance { cursor =>
      for
        amount <- cursor.downField("amount").as[BigDecimal]
        currency <- cursor.downField("currency").as[Currency]
        money <- Money.create(amount, currency).left.map(error => DecodingFailure(error.getMessage, cursor.history))
      yield money
    }

  given Encoder[FlightSchedule] =
    Encoder.instance(schedule =>
      Json.obj(
        "departureAt" -> Json.fromString(schedule.departureAt.toString),
        "arrivalAt" -> Json.fromString(schedule.arrivalAt.toString)
      )
    )

  given Decoder[FlightSchedule] =
    Decoder.instance { cursor =>
      for
        departureAt <- cursor.downField("departureAt").as[OffsetDateTime]
        arrivalAt <- cursor.downField("arrivalAt").as[OffsetDateTime]
        schedule <- FlightSchedule.create(departureAt, arrivalAt).left.map(error => DecodingFailure(error.getMessage, cursor.history))
      yield schedule
    }

  given Encoder[StayPeriod] =
    Encoder.instance(stayPeriod =>
      Json.obj(
        "checkIn" -> Json.fromString(stayPeriod.checkIn.toString),
        "checkOut" -> Json.fromString(stayPeriod.checkOut.toString)
      )
    )

  given Decoder[StayPeriod] =
    Decoder.instance { cursor =>
      for
        checkIn <- cursor.downField("checkIn").as[LocalDate]
        checkOut <- cursor.downField("checkOut").as[LocalDate]
        stayPeriod <- StayPeriod.create(checkIn, checkOut).left.map(error => DecodingFailure(error.getMessage, cursor.history))
      yield stayPeriod
    }

  given Encoder[RoomCount] = Encoder.encodeInt.contramap(_.value)
  given Decoder[RoomCount] = Decoder.decodeInt.emap(value => RoomCount.create(value).left.map(_.getMessage))

  given Encoder[AirlineId] = stringValueEncoder(_.value)
  given Decoder[AirlineId] = stringValueDecoder(AirlineId.apply)
  given Encoder[FlightId] = stringValueEncoder(_.value)
  given Decoder[FlightId] = stringValueDecoder(FlightId.apply)
  given Encoder[HotelId] = stringValueEncoder(_.value)
  given Decoder[HotelId] = stringValueDecoder(HotelId.apply)
  given Encoder[RoomTypeId] = stringValueEncoder(_.value)
  given Decoder[RoomTypeId] = stringValueDecoder(RoomTypeId.apply)
  given Encoder[TrainId] = stringValueEncoder(_.value)
  given Decoder[TrainId] = stringValueDecoder(TrainId.apply)
  given Encoder[TrainStopId] = stringValueEncoder(_.value)
  given Decoder[TrainStopId] = stringValueDecoder(TrainStopId.apply)
  given Encoder[TrainSeatInventoryId] = stringValueEncoder(_.value)
  given Decoder[TrainSeatInventoryId] = stringValueDecoder(TrainSeatInventoryId.apply)
  given Encoder[TrainSeatId] = stringValueEncoder(_.value)
  given Decoder[TrainSeatId] = stringValueDecoder(TrainSeatId.apply)
  given Encoder[AttractionId] = stringValueEncoder(_.value)
  given Decoder[AttractionId] = stringValueDecoder(AttractionId.apply)
  given Encoder[TicketTypeId] = stringValueEncoder(_.value)
  given Decoder[TicketTypeId] = stringValueDecoder(TicketTypeId.apply)
  given Encoder[AttractionTicketSessionId] = stringValueEncoder(_.value)
  given Decoder[AttractionTicketSessionId] = stringValueDecoder(AttractionTicketSessionId.apply)
  given Encoder[OrderId] = stringValueEncoder(_.value)
  given Decoder[OrderId] = stringValueDecoder(OrderId.apply)
  given Encoder[OrderItemId] = stringValueEncoder(_.value)
  given Decoder[OrderItemId] = stringValueDecoder(OrderItemId.apply)
  given Encoder[PaymentId] = stringValueEncoder(_.value)
  given Decoder[PaymentId] = stringValueDecoder(PaymentId.apply)
  given Encoder[RefundId] = stringValueEncoder(_.value)
  given Decoder[RefundId] = stringValueDecoder(RefundId.apply)
  given Encoder[UserId] = stringValueEncoder(_.value)
  given Decoder[UserId] = stringValueDecoder(UserId.apply)
  given Encoder[TravelerId] = stringValueEncoder(_.value)
  given Decoder[TravelerId] = stringValueDecoder(TravelerId.apply)
  given Encoder[ManagerId] = stringValueEncoder(_.value)
  given Decoder[ManagerId] = stringValueDecoder(ManagerId.apply)

  given Encoder[AirlineName] = stringValueEncoder(_.value)
  given Decoder[AirlineName] = stringValueDecoderWithValidation(AirlineName.create)
  given Encoder[AirlineCode] = stringValueEncoder(_.value)
  given Decoder[AirlineCode] = stringValueDecoderWithValidation(AirlineCode.create)
  given Encoder[FlightNumber] = stringValueEncoder(_.value)
  given Decoder[FlightNumber] = stringValueDecoderWithValidation(FlightNumber.create)
  given Encoder[AirportCode] = stringValueEncoder(_.value)
  given Decoder[AirportCode] = stringValueDecoderWithValidation(AirportCode.create)
  given Encoder[CabinClass] = stringValueEncoder(_.value)
  given Decoder[CabinClass] = stringValueDecoderWithValidation(CabinClass.create)
  given Encoder[HotelName] = stringValueEncoder(_.value)
  given Decoder[HotelName] = stringValueDecoderWithValidation(HotelName.create)
  given Encoder[HotelLocation] = stringValueEncoder(_.value)
  given Decoder[HotelLocation] = stringValueDecoderWithValidation(HotelLocation.create)
  given Encoder[RoomTypeName] = stringValueEncoder(_.value)
  given Decoder[RoomTypeName] = stringValueDecoderWithValidation(RoomTypeName.create)

  given Encoder[TrainNumber] = stringValueEncoder(_.value)
  given Decoder[TrainNumber] = stringValueDecoderWithValidation(TrainNumber.create)
  given Encoder[TrainStationCode] = stringValueEncoder(_.value)
  given Decoder[TrainStationCode] = stringValueDecoderWithValidation(TrainStationCode.create)
  given Encoder[TrainStationName] = stringValueEncoder(_.value)
  given Decoder[TrainStationName] = stringValueDecoderWithValidation(TrainStationName.create)
  given Encoder[TrainSeatClass] = stringValueEncoder(_.value)
  given Decoder[TrainSeatClass] = stringValueDecoderWithValidation(TrainSeatClass.create)
  given Encoder[TrainSeatPreference] = Encoder.encodeString.contramap(_.toString)
  given Decoder[TrainSeatPreference] = Decoder.decodeString.map(TrainSeatPreference.fromText)
  given Encoder[TrainSeatPositionType] = Encoder.encodeString.contramap(_.toString)
  given Decoder[TrainSeatPositionType] = Decoder.decodeString.map(TrainSeatPositionType.fromText)

  given Encoder[TrainTravelerSeatAssignment] =
    Encoder.forProduct6("travelerId", "seatId", "carriageNo", "seatNo", "seatLabel", "seatPositionType") { assignment =>
      (assignment.travelerId, assignment.seatId, assignment.carriageNo, assignment.seatNo, assignment.seatLabel, assignment.seatPositionType)
    }

  given Decoder[TrainTravelerSeatAssignment] =
    Decoder.forProduct6("travelerId", "seatId", "carriageNo", "seatNo", "seatLabel", "seatPositionType")(TrainTravelerSeatAssignment.apply)

  private def stringValueEncoder[A](value: A => String): Encoder[A] =
    Encoder.encodeString.contramap(value)

  private def stringValueDecoder[A](build: String => A): Decoder[A] =
    Decoder.decodeString.map(build)

  private def stringValueDecoderWithValidation[A](build: String => Either[Throwable, A]): Decoder[A] =
    Decoder.decodeString.emap(value => build(value).left.map(_.getMessage))

