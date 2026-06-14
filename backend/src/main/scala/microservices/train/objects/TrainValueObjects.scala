// TrainValueObjects 汇总 train 模块内部的站点、车次、席别、行号和退款率等值对象。

package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

final case class TrainStationCode private (value: String) extends AnyVal
object TrainStationCode:
  def create(value: String): Either[SharedValidationError, TrainStationCode] =
    val normalized = value.trim.toUpperCase
    if normalized.matches("^[A-Z0-9]{2,10}$") then Right(TrainStationCode(normalized))
    else Left(SharedValidationError.RequiredFieldWasEmpty("train-station-code"))

  def unsafe(value: String): TrainStationCode =
    create(value).fold(throw _, identity)

  given sourceEncoder: Encoder[TrainStationCode] = Encoder.encodeString.contramap(_.value)
  given sourceDecoder: Decoder[TrainStationCode] = Decoder.decodeString.emap(create(_).left.map(_.message))

final case class TrainStationName private (value: String) extends AnyVal
object TrainStationName:
  def create(value: String): Either[SharedValidationError, TrainStationName] =
    val normalized = value.trim
    if normalized.nonEmpty && normalized.length <= 120 then Right(TrainStationName(normalized))
    else if normalized.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("train-station-name"))
    else Left(SharedValidationError.StringWasTooLong("train-station-name", 120, normalized.length))

  def unsafe(value: String): TrainStationName =
    create(value).fold(throw _, identity)

  given sourceEncoder: Encoder[TrainStationName] = Encoder.encodeString.contramap(_.value)
  given sourceDecoder: Decoder[TrainStationName] = Decoder.decodeString.emap(create(_).left.map(_.message))

final case class TrainNumber private (value: String) extends AnyVal
object TrainNumber:
  def create(value: String): Either[SharedValidationError, TrainNumber] =
    val normalized = value.trim.toUpperCase
    if normalized.matches("^[A-Z0-9]{2,20}$") then Right(TrainNumber(normalized))
    else if normalized.isEmpty then Left(SharedValidationError.RequiredFieldWasEmpty("train-number"))
    else Left(SharedValidationError.StringWasTooLong("train-number", 20, normalized.length))

  def unsafe(value: String): TrainNumber =
    create(value).fold(throw _, identity)

  given sourceEncoder: Encoder[TrainNumber] = Encoder.encodeString.contramap(_.value)
  given sourceDecoder: Decoder[TrainNumber] = Decoder.decodeString.emap(create(_).left.map(_.message))

final case class TrainSeatClass private (value: String) extends AnyVal
object TrainSeatClass:
  private val supported = Set("SECOND_CLASS", "FIRST_CLASS", "BUSINESS_CLASS", "SLEEPER")

  def create(value: String): Either[SharedValidationError, TrainSeatClass] =
    val normalized = value.trim.toUpperCase.replace('-', '_').replace(' ', '_')
    if supported.contains(normalized) then Right(TrainSeatClass(normalized))
    else Left(SharedValidationError.CabinClassWasInvalid(normalized))

  def unsafe(value: String): TrainSeatClass =
    create(value).fold(throw _, identity)

  given sourceEncoder: Encoder[TrainSeatClass] = Encoder.encodeString.contramap(_.value)
  given sourceDecoder: Decoder[TrainSeatClass] = Decoder.decodeString.emap(create(_).left.map(_.message))

final case class TrainSeatRowNo private (value: Int) extends AnyVal
object TrainSeatRowNo:
  def create(value: Int): Either[SharedValidationError, TrainSeatRowNo] =
    if value > 0 then Right(TrainSeatRowNo(value))
    else Left(SharedValidationError.NumberWasOutOfRange("train-seat-row-no", BigDecimal(1), BigDecimal(Int.MaxValue), BigDecimal(value)))

  given sourceEncoder: Encoder[TrainSeatRowNo] = Encoder.encodeInt.contramap(_.value)
  given sourceDecoder: Decoder[TrainSeatRowNo] = Decoder.decodeInt.emap(create(_).left.map(_.message))

final case class RefundRate private (value: BigDecimal) extends AnyVal
object RefundRate:
  def create(value: BigDecimal): Either[SharedValidationError, RefundRate] =
    if value >= 0 && value <= 1 then Right(RefundRate(value))
    else Left(SharedValidationError.NumberWasOutOfRange("refund-rate", BigDecimal(0), BigDecimal(1), value))

  def unsafe(value: BigDecimal): RefundRate =
    create(value).fold(throw _, identity)

  given sourceEncoder: Encoder[RefundRate] = Encoder.encodeBigDecimal.contramap(_.value)
  given sourceDecoder: Decoder[RefundRate] = Decoder.decodeBigDecimal.emap(create(_).left.map(_.message))
