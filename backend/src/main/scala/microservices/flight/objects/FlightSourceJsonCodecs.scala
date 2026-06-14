// FlightSourceJsonCodecs is a backend-only codec registry for source/internal flight value objects.
// These codecs are used when the backend reads supplier payloads, database snapshots, and internal planner payloads.
// They are intentionally not mirrored in the frontend because the browser never deserializes these raw source-layer types.
package com.typesafe.travel.flight.objects

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

import java.time.{Instant, OffsetDateTime}
import scala.util.Try

private[objects] object FlightSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[OffsetDateTime] = Encoder.encodeString.contramap(_.toString)
  given Decoder[OffsetDateTime] = Decoder.decodeString.emap(value => Try(OffsetDateTime.parse(value)).toEither.left.map(_.getMessage))
  given Encoder[Currency] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Currency] = Decoder.decodeString.emap(value => Try(Currency.fromText(value)).toEither.left.map(_.getMessage))
  given Encoder[Money] = Encoder.forProduct2("amount", "currency")(money => (money.amount, money.currency))
  given Decoder[Money] = Decoder.instance { cursor =>
    for
      amount <- cursor.downField("amount").as[BigDecimal]
      currency <- cursor.downField("currency").as[Currency]
      money <- Money.create(amount, currency).left.map(error => io.circe.DecodingFailure(error.getMessage, cursor.history))
    yield money
  }
  given Encoder[AirlineId] = Encoder.encodeString.contramap(_.value)
  given Decoder[AirlineId] = Decoder.decodeString.map(AirlineId.apply)
  given Encoder[FlightId] = Encoder.encodeString.contramap(_.value)
  given Decoder[FlightId] = Decoder.decodeString.map(FlightId.apply)
  given Encoder[CabinInventoryId] = Encoder.encodeString.contramap(_.value)
  given Decoder[CabinInventoryId] = Decoder.decodeString.map(CabinInventoryId.apply)
  given Encoder[AirlineName] = Encoder.encodeString.contramap(_.value)
  given Decoder[AirlineName] = Decoder.decodeString.emap(value => AirlineName.create(value).left.map(_.getMessage))
  given Encoder[AirlineCode] = Encoder.encodeString.contramap(_.value)
  given Decoder[AirlineCode] = Decoder.decodeString.emap(value => AirlineCode.create(value).left.map(_.getMessage))
  given Encoder[FlightNumber] = Encoder.encodeString.contramap(_.value)
  given Decoder[FlightNumber] = Decoder.decodeString.emap(value => FlightNumber.create(value).left.map(_.getMessage))
  given Encoder[AirportCode] = Encoder.encodeString.contramap(_.value)
  given Decoder[AirportCode] = Decoder.decodeString.emap(value => AirportCode.create(value).left.map(_.getMessage))
  given Encoder[CabinClass] = Encoder.encodeString.contramap(_.value)
  given Decoder[CabinClass] = Decoder.decodeString.emap(value => CabinClass.create(value).left.map(_.getMessage))
  given Encoder[SeatCount] = Encoder.encodeInt.contramap(_.value)
  given Decoder[SeatCount] = Decoder.decodeInt.emap(value => SeatCount.create(value).left.map(_.getMessage))
  given Encoder[FlightSchedule] = Encoder.forProduct2("departureAt", "arrivalAt")(schedule => (schedule.departureAt, schedule.arrivalAt))
  given Decoder[FlightSchedule] = Decoder.instance { cursor =>
    for
      departureAt <- cursor.downField("departureAt").as[OffsetDateTime]
      arrivalAt <- cursor.downField("arrivalAt").as[OffsetDateTime]
      schedule <- FlightSchedule.create(departureAt, arrivalAt).left.map(error => io.circe.DecodingFailure(error.getMessage, cursor.history))
    yield schedule
  }
