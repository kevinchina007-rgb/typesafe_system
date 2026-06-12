// AttractionSourceJsonCodecs 定义景点模块的源数据 JSON codec。

package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerDocumentType
import io.circe.{Decoder, Encoder}

import java.time.{DayOfWeek, Instant, LocalDate}
import scala.util.Try

private[domain] object AttractionSourceJsonCodecs:
  given Encoder[Instant] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Instant] = Decoder.decodeString.emap(value => Try(Instant.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[LocalDate] = Encoder.encodeString.contramap(_.toString)
  given Decoder[LocalDate] = Decoder.decodeString.emap(value => Try(LocalDate.parse(value)).toEither.left.map(_.getMessage))

  given Encoder[DayOfWeek] = Encoder.encodeString.contramap(_.toString)
  given Decoder[DayOfWeek] = Decoder.decodeString.emap(value => Try(DayOfWeek.valueOf(value)).toEither.left.map(_.getMessage))

  given Encoder[Currency] = Encoder.encodeString.contramap(_.toString)
  given Decoder[Currency] = Decoder.decodeString.emap(value => Try(Currency.fromText(value)).toEither.left.map(_.getMessage))

  given Encoder[Money] =
    Encoder.forProduct2("amount", "currency")(money => (money.amount, money.currency))

  given Decoder[Money] =
    Decoder.instance { cursor =>
      for
        amount <- cursor.downField("amount").as[BigDecimal]
        currency <- cursor.downField("currency").as[Currency]
        money <- Money.create(amount, currency).left.map(error => io.circe.DecodingFailure(error.getMessage, cursor.history))
      yield money
    }

  given Encoder[AttractionId] = Encoder.encodeString.contramap(_.value)
  given Decoder[AttractionId] = Decoder.decodeString.map(AttractionId.apply)
  given Encoder[TicketTypeId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TicketTypeId] = Decoder.decodeString.map(TicketTypeId.apply)
  given Encoder[TicketEligibilityRuleId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TicketEligibilityRuleId] = Decoder.decodeString.map(TicketEligibilityRuleId.apply)
  given Encoder[AttractionTicketSessionId] = Encoder.encodeString.contramap(_.value)
  given Decoder[AttractionTicketSessionId] = Decoder.decodeString.map(AttractionTicketSessionId.apply)
  given Encoder[ManagerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[ManagerId] = Decoder.decodeString.map(ManagerId.apply)
  given Encoder[TravelerId] = Encoder.encodeString.contramap(_.value)
  given Decoder[TravelerId] = Decoder.decodeString.map(TravelerId.apply)

  given Encoder[TravelerDocumentType] = Encoder.encodeString.contramap(_.toString)
  given Decoder[TravelerDocumentType] = Decoder.decodeString.map(TravelerDocumentType.fromText)
