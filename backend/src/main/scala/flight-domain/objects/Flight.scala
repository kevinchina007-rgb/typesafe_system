package com.typesafe.travel.flight.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

// 航班域的核心类型主要分两层：
// 1. Flight：航班本体和时刻、状态
// 2. CabinInventory：各舱位的可售库存与价格
enum FlightStatus:
  case Scheduled, OpenForBooking, ClosedForBooking, Cancelled

enum InventoryStatus:
  case Open, SoldOut, Closed

enum FlightError(val message: String) extends DomainError:
  case AirlineWasNotFound(airlineId: AirlineId)
      extends FlightError(s"Airline '${airlineId.value}' was not found")
  case FlightWasNotFound(flightId: FlightId)
      extends FlightError(s"Flight '${flightId.value}' was not found")
  case FlightTravelerWasAlreadyBooked(flightId: FlightId, travelerId: TravelerId)
      extends FlightError(
        s"Traveler '${travelerId.value}' already holds a valid booking for flight '${flightId.value}'"
      )
  case FlightWasNotOpenForBooking(flightId: FlightId, flightStatus: FlightStatus)
      extends FlightError(s"Flight '${flightId.value}' is not open for booking while in status $flightStatus")
  case CabinInventoryWasNotFound(flightId: FlightId, cabinClass: CabinClass)
      extends FlightError(s"Flight '${flightId.value}' does not have cabin '${cabinClass.value}'")
  case CabinInventoryWasNotBookable(
      flightId: FlightId,
      cabinClass: CabinClass,
      inventoryStatus: InventoryStatus,
      availableSeats: SeatCount
  ) extends FlightError(
        s"Flight '${flightId.value}' cabin '${cabinClass.value}' is not bookable in status $inventoryStatus with ${availableSeats.value} seats left"
      )
  case DepartureAirportMatchedArrivalAirport(flightId: FlightId, airportCode: AirportCode)
      extends FlightError(s"Flight '${flightId.value}' cannot depart from and arrive at '${airportCode.value}'")

// CabinInventory 表示“某一舱位在该航班上的当前销售视图”。
// 真正锁票由 inventory / order 主链处理，这里只表达可售性与价格。
final case class CabinInventory private[domain] (
    cabinInventoryId: CabinInventoryId,
    flightId: FlightId,
    cabinClass: CabinClass,
    availableSeats: SeatCount,
    unitPrice: Money,
    inventoryStatus: InventoryStatus
):
  def isBookable: Boolean =
    inventoryStatus == InventoryStatus.Open && availableSeats.value > 0

  def ensureBookable: Either[FlightError, CabinInventory] =
    if isBookable then Right(this)
    else Left(FlightError.CabinInventoryWasNotBookable(flightId, cabinClass, inventoryStatus, availableSeats))

object CabinInventory:
  // create 只负责把领域值收拢进一个清晰的构造入口。
  def create(
      cabinInventoryId: CabinInventoryId,
      flightId: FlightId,
      cabinClass: CabinClass,
      availableSeats: SeatCount,
      unitPrice: Money,
      inventoryStatus: InventoryStatus
  ): CabinInventory =
    CabinInventory(cabinInventoryId, flightId, cabinClass, availableSeats, unitPrice, inventoryStatus)

// Flight 本体保存航班基础信息，以及挂在它下面的 cabin inventories。
final case class Flight private[domain] (
    flightId: FlightId,
    airlineId: AirlineId,
    flightNumber: FlightNumber,
    departureAirport: AirportCode,
    arrivalAirport: AirportCode,
    flightSchedule: FlightSchedule,
    flightStatus: FlightStatus,
    basePrice: Money,
    cabinInventories: Vector[CabinInventory],
    createdAt: Instant
):
  def openForBooking: Flight =
    copy(flightStatus = FlightStatus.OpenForBooking)

  def closeForBooking: Flight =
    copy(flightStatus = FlightStatus.ClosedForBooking)

  def cancelFlight: Flight =
    copy(flightStatus = FlightStatus.Cancelled)

  def findCabinInventoryByClass(cabinClass: CabinClass): Either[FlightError, CabinInventory] =
    cabinInventories.find(_.cabinClass == cabinClass).toRight(FlightError.CabinInventoryWasNotFound(flightId, cabinClass))

  def ensureBookableCabinInventory(cabinClass: CabinClass): Either[FlightError, CabinInventory] =
    for
      _ <- if flightStatus == FlightStatus.OpenForBooking then Right(()) else Left(FlightError.FlightWasNotOpenForBooking(flightId, flightStatus))
      cabinInventory <- findCabinInventoryByClass(cabinClass)
      bookableCabinInventory <- cabinInventory.ensureBookable
    yield bookableCabinInventory

  def matchesSearch(
      departureAirportFilter: Option[AirportCode],
      arrivalAirportFilter: Option[AirportCode],
      departureDateFilter: Option[java.time.LocalDate]
  ): Boolean =
    departureAirportFilter.forall(_ == departureAirport) &&
    arrivalAirportFilter.forall(_ == arrivalAirport) &&
    departureDateFilter.forall(_ == flightSchedule.departureAt.toLocalDate)

object Flight:
  // create 面向业务创建，带最小校验。
  def create(
      flightId: FlightId,
      airlineId: AirlineId,
      flightNumber: FlightNumber,
      departureAirport: AirportCode,
      arrivalAirport: AirportCode,
      flightSchedule: FlightSchedule,
      basePrice: Money,
      cabinInventories: Vector[CabinInventory],
      createdAt: Instant
  ): Either[FlightError, Flight] =
    if departureAirport == arrivalAirport then Left(FlightError.DepartureAirportMatchedArrivalAirport(flightId, departureAirport))
    else
      Right(
        Flight(
          flightId = flightId,
          airlineId = airlineId,
          flightNumber = flightNumber,
          departureAirport = departureAirport,
          arrivalAirport = arrivalAirport,
          flightSchedule = flightSchedule,
          flightStatus = FlightStatus.OpenForBooking,
          basePrice = basePrice,
          cabinInventories = cabinInventories,
          createdAt = createdAt
        )
      )

  // restore 面向仓储恢复，不重新应用 open-for-booking 的默认规则。
  def restore(
      flightId: FlightId,
      airlineId: AirlineId,
      flightNumber: FlightNumber,
      departureAirport: AirportCode,
      arrivalAirport: AirportCode,
      flightSchedule: FlightSchedule,
      flightStatus: FlightStatus,
      basePrice: Money,
      cabinInventories: Vector[CabinInventory],
      createdAt: Instant
  ): Flight =
    Flight(
      flightId = flightId,
      airlineId = airlineId,
      flightNumber = flightNumber,
      departureAirport = departureAirport,
      arrivalAirport = arrivalAirport,
      flightSchedule = flightSchedule,
      flightStatus = flightStatus,
      basePrice = basePrice,
      cabinInventories = cabinInventories,
      createdAt = createdAt
    )

