package com.typesafe.travel.api.dto

import com.typesafe.travel.flight.domain.*

final case class CabinInventoryResponseDto(
    inventoryId: String,
    cabinClass: String,
    availableSeats: Int,
    unitPrice: String,
    currency: String,
    status: String,
    isBookable: Boolean
)

final case class FlightResponseDto(
    flightId: String,
    airlineId: String,
    airlineName: String,
    airlineCode: String,
    flightNumber: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    status: String,
    basePrice: String,
    currency: String,
    createdAt: String,
    cabinInventories: List[CabinInventoryResponseDto]
)

final case class FlightListResponseDto(
    flights: List[FlightResponseDto]
)

final case class BookFlightRequestDto(
    buyerUserId: String,
    flightId: String,
    travelerIds: List[String],
    cabinClass: String
)

object FlightResponseDto:
  def fromDomain(
      airline: Airline,
      flight: Flight,
      remainingAvailableSeatsByInventoryId: Map[String, Int] = Map.empty
  ): FlightResponseDto =
    FlightResponseDto(
      flightId = flight.flightId.value,
      airlineId = airline.airlineId.value,
      airlineName = airline.airlineName.value,
      airlineCode = airline.airlineCode.value,
      flightNumber = flight.flightNumber.value,
      departureAirport = flight.departureAirport.value,
      arrivalAirport = flight.arrivalAirport.value,
      departureTime = flight.flightSchedule.departureAt.toString,
      arrivalTime = flight.flightSchedule.arrivalAt.toString,
      status = flight.flightStatus.toString,
      basePrice = flight.basePrice.amount.toString,
      currency = flight.basePrice.currency.toString,
      createdAt = flight.createdAt.toString,
      cabinInventories = flight.cabinInventories.toList.map(cabinInventory =>
        val remainingSeats = remainingAvailableSeatsByInventoryId.getOrElse(cabinInventory.cabinInventoryId.value, cabinInventory.availableSeats.value)
        CabinInventoryResponseDto(
          inventoryId = cabinInventory.cabinInventoryId.value,
          cabinClass = cabinInventory.cabinClass.value,
          availableSeats = remainingSeats,
          unitPrice = cabinInventory.unitPrice.amount.toString,
          currency = cabinInventory.unitPrice.currency.toString,
          status = cabinInventory.inventoryStatus.toString,
          isBookable = cabinInventory.inventoryStatus == InventoryStatus.Open && remainingSeats > 0
        )
      )
    )
