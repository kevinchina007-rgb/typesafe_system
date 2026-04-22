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
    bookingWindowStatus: String,
    canBookOnline: Boolean,
    bookingNotice: Option[String],
    lateBookingSurchargeAmount: Option[String],
    lateBookingSurchargeCurrency: Option[String],
    basePrice: String,
    currency: String,
    createdAt: String,
    cabinInventories: List[CabinInventoryResponseDto]
)

final case class FlightListResponseDto(
    flights: List[FlightResponseDto]
)

final case class BookFlightRequestDto(
    flightId: String,
    travelerIds: List[String],
    cabinClass: String
)

private val flightLateBookingThresholdHours = 48L
private val flightBookingWindowAvailable = "Available"
private val flightBookingWindowSurchargeRequired = "SurchargeRequired"
private val flightBookingWindowExpired = "Expired"

def flightResponseDto(
    airline: Airline,
    flight: Flight,
    currentTime: java.time.Instant,
    remainingAvailableSeatsByInventoryId: Map[String, Int] = Map.empty
): FlightResponseDto =
  val departureInstant = flight.flightSchedule.departureAt.toInstant
  val bookingWindowStatus =
    if !departureInstant.isAfter(currentTime) then flightBookingWindowExpired
    else if departureInstant.isBefore(currentTime.plusSeconds(flightLateBookingThresholdHours * 3600)) then flightBookingWindowSurchargeRequired
    else flightBookingWindowAvailable

  val lateBookingSurcharge =
    Option.when(bookingWindowStatus == flightBookingWindowSurchargeRequired)(
      (flight.basePrice.amount * BigDecimal("0.15")).setScale(2, BigDecimal.RoundingMode.HALF_UP)
    )

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
    status = flight.flightStatus.value,
    bookingWindowStatus = bookingWindowStatus,
    canBookOnline = bookingWindowStatus == flightBookingWindowAvailable,
    bookingNotice =
      bookingWindowStatus match
        case `flightBookingWindowAvailable` => None
        case `flightBookingWindowSurchargeRequired` =>
          lateBookingSurcharge.map(amount => s"Departure is within 48 hours. Online booking is paused until a late-booking surcharge of ${amount.toString} ${flight.basePrice.currency.toString} is confirmed.")
        case `flightBookingWindowExpired` =>
          Some("This flight has already departed and is no longer searchable."),
    lateBookingSurchargeAmount = lateBookingSurcharge.map(_.toString),
    lateBookingSurchargeCurrency = lateBookingSurcharge.map(_ => flight.basePrice.currency.toString),
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
        status = cabinInventory.inventoryStatus.value,
        isBookable = cabinInventory.inventoryStatus == InventoryStatus.Open && remainingSeats > 0
      )
    )
  )
