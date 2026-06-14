// FlightBookingWindowStatus is a backend-computed status derived from booking-window rules and timestamps.
// It is intentionally backend-only because the frontend should consume the computed outcome, not reimplement the timing and surcharge rules.
package com.typesafe.travel.flight.objects

import io.circe.{Decoder, Encoder}

final case class FlightBookingWindowStatus(value: String):
  override def toString: String = value

object FlightBookingWindowStatus:
  val Available = FlightBookingWindowStatus("Available")
  val SurchargeRequired = FlightBookingWindowStatus("SurchargeRequired")
  val Expired = FlightBookingWindowStatus("Expired")

  given sourceEncoder: Encoder[FlightBookingWindowStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[FlightBookingWindowStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): FlightBookingWindowStatus =
    value.trim match
      case "Available" => Available
      case "SurchargeRequired" => SurchargeRequired
      case "Expired" => Expired
      case other => FlightBookingWindowStatus(other)

