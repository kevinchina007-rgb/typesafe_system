package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.time.Instant

final case class AirlineStatus(value: String):
  override def toString: String = value

object AirlineStatus:
  val Active = AirlineStatus("Active")
  val Suspended = AirlineStatus("Suspended")
  val Retired = AirlineStatus("Retired")
  given sourceEncoder: Encoder[AirlineStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AirlineStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AirlineStatus =
    value.trim match
      case "Suspended" => Suspended
      case "Retired"   => Retired
      case _           => Active

final case class Airline(
    airlineId: AirlineId,
    airlineName: AirlineName,
    airlineCode: AirlineCode,
    airlineStatus: AirlineStatus,
    createdAt: Instant
)

object Airline:
  import FlightSourceJsonCodecs.given
  given sourceEncoder: Encoder[Airline] = deriveEncoder
  given sourceDecoder: Decoder[Airline] = deriveDecoder

