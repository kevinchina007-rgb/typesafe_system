// FlightStatusSupport is backend-only normalization logic for persisted flight and inventory status strings.
// It converts raw text from database rows and source feeds into typed domain statuses used by planners.
// The frontend should never mirror this parser, because it consumes the already-shaped API contract instead of raw status text.
package com.typesafe.travel.flight.objects

object FlightStatusSupport:
  def parseFlightStatus(value: String): FlightStatus =
    value.trim match
      case "Scheduled" => FlightStatus.Scheduled
      case "OpenForBooking" => FlightStatus.OpenForBooking
      case "ClosedForBooking" => FlightStatus.ClosedForBooking
      case "Cancelled" => FlightStatus.Cancelled
      case other => FlightStatus(other)

  def parseInventoryStatus(value: String): InventoryStatus =
    value.trim match
      case "Open" => InventoryStatus.Open
      case "SoldOut" => InventoryStatus.SoldOut
      case "Closed" => InventoryStatus.Closed
      case other => InventoryStatus(other)

