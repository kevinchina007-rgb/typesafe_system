package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.inventory.domain.ReservationLifecycle
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*


import java.time.{Instant, LocalDate, OffsetDateTime}

final class LiveManagerWorkflowApplicationService[F[_]: MonadThrow](
    protected val managerService: ManagerService[F],
    protected val managerRepository: ManagerRepository[F],
    protected val orderRepository: OrderRepository[F],
    protected val orderService: OrderService[F],
    protected val reservationLifecycle: ReservationLifecycle[F],
    protected val flightRepository: FlightRepository[F],
    protected val hotelRepository: HotelRepository[F],
    protected val attractionRepository: AttractionRepository[F]
) extends ManagerWorkflowApplicationService[F]
    with LiveManagerWorkflowSupport[F]:
  override def registerAirlineManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      airlineName: AirlineName,
      airlineCode: AirlineCode,
      createdAt: Instant
  ): F[ManagerSession] =
    for
      airlineId <- flightRepository.nextAirlineId
      airline <- flightRepository.saveAirline(createAirline(airlineId, airlineName, airlineCode, createdAt))
      airlineManager <- managerService.registerAirlineManager(airline.airlineId, primaryEmailAddress, displayName, createdAt)
    yield toSession(airlineManager)

  override def registerHotelManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      hotelName: HotelName,
      hotelLocation: HotelLocation,
      createdAt: Instant
  ): F[ManagerSession] =
    for
      hotelId <- hotelRepository.nextHotelId
      hotel <- hotelRepository.saveHotel(
        createHotel(
          hotelId = hotelId,
          hotelName = hotelName,
          hotelLocation = hotelLocation,
          roomTypes = Vector.empty,
          createdAt = createdAt
        )
      )
      hotelManager <- managerService.registerHotelManager(hotel.hotelId, primaryEmailAddress, displayName, createdAt)
    yield toSession(hotelManager)

  override def registerAttractionManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[ManagerSession] =
    managerService.registerAttractionManager(primaryEmailAddress, displayName, createdAt).map(toSession)

  override def loginManager(managerType: ManagerType, primaryEmailAddress: EmailAddress): F[ManagerSession] =
    managerType match
      case ManagerType.Airline => managerService.loginAirlineManager(primaryEmailAddress).map(toSession)
      case ManagerType.Hotel   => managerService.loginHotelManager(primaryEmailAddress).map(toSession)
      case ManagerType.Attraction => managerService.loginAttractionManager(primaryEmailAddress).map(toSession)

  override def listManagerTasks(
      managerId: ManagerId,
      managerType: ManagerType,
      requestedSupplierReviewStatuses: Set[SupplierReviewStatus],
      requestedResourceTypes: Set[ManagerType]
  ): F[List[ManagerBookingTaskView]] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      allOrders <- orderRepository.findAllOrders
    yield allOrders
      .flatMap(order =>
        order.orderLineItems.flatMap(orderLineItem =>
          buildTaskView(managerContext, order, orderLineItem, requestedSupplierReviewStatuses, requestedResourceTypes)
        )
      )
      .sortBy(task => (task.requestedAt.toEpochMilli, task.orderId.value, task.orderItemId.value))
      .reverse

  override def listManagerRefundTasks(managerId: ManagerId, managerType: ManagerType): F[List[ManagerRefundTaskView]] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      allOrders <- orderRepository.findAllOrders
    yield allOrders
      .flatMap(order => buildRefundTaskView(managerContext, order).toList)
      .sortBy(task => (task.requestedAt.toEpochMilli, task.orderId.value))
      .reverse

  override def listFlightsForAirlineManager(managerId: ManagerId): F[List[(Airline, Flight)]] =
    for
      airlineManager <- managerService.loadAirlineManager(managerId)
      airline <- flightRepository.findAirlineById(airlineManager.airlineId).flatMap(_.liftTo[F](FlightError.AirlineWasNotFound(airlineManager.airlineId)))
      flights <- flightRepository.searchFlights(FlightSearchCriteria(None, None, None))
    yield flights
      .filter(_.airlineId == airline.airlineId)
      .sortBy(flight => (flight.flightSchedule.departureAt.toInstant.toEpochMilli, flight.flightId.value))
      .map(airline -> _)

  override def createFlightForAirlineManager(
      managerId: ManagerId,
      flightNumber: FlightNumber,
      departureAirport: AirportCode,
      arrivalAirport: AirportCode,
      departureAt: OffsetDateTime,
      arrivalAt: OffsetDateTime,
      economySeatCount: SeatCount,
      economyPrice: Money,
      businessSeatCount: SeatCount,
      businessPrice: Money,
      createdAt: Instant
  ): F[(Airline, Flight)] =
    for
      airlineManager <- managerService.loadAirlineManager(managerId)
      airline <- flightRepository.findAirlineById(airlineManager.airlineId).flatMap(_.liftTo[F](FlightError.AirlineWasNotFound(airlineManager.airlineId)))
      flightId <- flightRepository.nextFlightId
      economyInventoryId <- flightRepository.nextCabinInventoryId
      businessInventoryId <- flightRepository.nextCabinInventoryId
      flightSchedule <- FlightSchedule.create(departureAt, arrivalAt).liftTo[F]
      flight <- createFlight(
        flightId = flightId,
        airlineId = airline.airlineId,
        flightNumber = flightNumber,
        departureAirport = departureAirport,
        arrivalAirport = arrivalAirport,
        flightSchedule = flightSchedule,
        basePrice = economyPrice,
        cabinInventories = Vector(
          createCabinInventory(economyInventoryId, flightId, CabinClass.unsafe("economy"), economySeatCount, economyPrice, InventoryStatus.Open),
          createCabinInventory(businessInventoryId, flightId, CabinClass.unsafe("business"), businessSeatCount, businessPrice, InventoryStatus.Open)
        ),
        createdAt = createdAt
      )
        .liftTo[F]
      savedFlight <- flightRepository.saveFlight(flight)
    yield airline -> savedFlight

  override def createRoomTypeForHotelManager(
      managerId: ManagerId,
      roomTypeName: RoomTypeName,
      roomCapacity: Capacity,
      bedType: BedType,
      nightlyPrice: Money,
      availableRooms: RoomCount,
      inventoryStartDate: LocalDate,
      inventoryEndDate: LocalDate
  ): F[Hotel] =
    for
      hotelManager <- managerService.loadHotelManager(managerId)
      hotel <- hotelRepository.findHotelById(hotelManager.hotelId).flatMap(_.liftTo[F](HotelError.HotelWasNotFound(hotelManager.hotelId)))
      stayPeriod <- StayPeriod.create(inventoryStartDate, inventoryEndDate).liftTo[F]
      roomTypeId <- hotelRepository.nextRoomTypeId
      roomInventories <- stayDates(stayPeriod).traverse(inventoryDate =>
        hotelRepository.nextRoomInventoryId.map(roomInventoryId =>
          createRoomInventory(
            roomInventoryId = roomInventoryId,
            roomTypeId = roomTypeId,
            inventoryDate = inventoryDate,
            availableRooms = availableRooms,
            unitPrice = nightlyPrice,
            roomInventoryStatus = RoomInventoryStatus.Available
          )
        )
      )
      updatedHotel <- hotelRepository.saveHotel(
        hotel.addRoomType(
          createRoomType(
            roomTypeId = roomTypeId,
            hotelId = hotel.hotelId,
            roomTypeName = roomTypeName,
            roomCapacity = roomCapacity,
            bedType = bedType,
            basePrice = nightlyPrice,
            roomTypeStatus = RoomTypeStatus.OpenForBooking,
            roomInventories = roomInventories.toVector
          )
        )
      )
    yield updatedHotel

  override def confirmBookingItem(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemId: OrderItemId,
      note: Option[String],
      decidedAt: Instant
  ): F[Order] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      order <- loadScopedOrder(managerContext, orderItemId)
      updatedOrder <- orderService.confirmSupplierOrderItem(order.orderId, orderItemId, managerId, note, decidedAt)
    yield updatedOrder

  override def batchConfirmBookingItems(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemIds: List[OrderItemId],
      note: Option[String],
      decidedAt: Instant
  ): F[List[Order]] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      _ <- orderItemIds match
        case Nil => MonadThrow[F].raiseError(SharedValidationError.RequiredFieldWasEmpty("orderItemIds"))
        case _   => MonadThrow[F].unit
      _ <- orderItemIds.traverse(loadScopedOrder(managerContext, _))
      updatedOrders <- orderItemIds.traverse(orderItemId =>
        orderRepository
          .findOrderByOrderItemId(orderItemId)
          .flatMap(_.liftTo[F](OrderError.OrderItemWasNotFound(OrderId("unknown-order"), orderItemId)))
          .flatMap(order => orderService.confirmSupplierOrderItem(order.orderId, orderItemId, managerId, note, decidedAt))
      )
    yield updatedOrders

  override def rejectBookingItem(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemId: OrderItemId,
      reason: String,
      decidedAt: Instant
  ): F[Order] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      order <- loadScopedOrder(managerContext, orderItemId)
      rejectedOrder <- orderService.rejectSupplierOrderItem(order.orderId, orderItemId, managerId, reason, decidedAt)
      _ <- reservationLifecycle.releaseActiveReservationsForOrderItem(orderItemId, decidedAt)
    yield rejectedOrder

  override def batchRejectBookingItems(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemIds: List[OrderItemId],
      reason: String,
      decidedAt: Instant
  ): F[List[Order]] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      _ <- orderItemIds match
        case Nil => MonadThrow[F].raiseError(SharedValidationError.RequiredFieldWasEmpty("orderItemIds"))
        case _   => MonadThrow[F].unit
      _ <- orderItemIds.traverse(loadScopedOrder(managerContext, _))
      updatedOrders <- orderItemIds.traverse(orderItemId =>
        orderRepository
          .findOrderByOrderItemId(orderItemId)
          .flatMap(_.liftTo[F](OrderError.OrderItemWasNotFound(OrderId("unknown-order"), orderItemId)))
          .flatMap(order => orderService.rejectSupplierOrderItem(order.orderId, orderItemId, managerId, reason, decidedAt))
          .flatTap(_ => reservationLifecycle.releaseActiveReservationsForOrderItem(orderItemId, decidedAt))
      )
    yield updatedOrders

  override def approveRefund(
      managerId: ManagerId,
      managerType: ManagerType,
      orderId: OrderId,
      decidedAt: Instant
  ): F[Order] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      order <- loadScopedRefundOrder(managerContext, orderId)
      requestedRefund <- loadRequestedRefund(order)
      approvedOrder <- orderService.approveRequestedRefund(order.orderId, requestedRefund.refundId, decidedAt)
      settledOrder <- orderService.settleApprovedRefund(approvedOrder.orderId, requestedRefund.refundId, decidedAt)
    yield settledOrder

  override def rejectRefund(
      managerId: ManagerId,
      managerType: ManagerType,
      orderId: OrderId
  ): F[Order] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      order <- loadScopedRefundOrder(managerContext, orderId)
      requestedRefund <- loadRequestedRefund(order)
      updatedOrder <- orderService.rejectRequestedRefund(order.orderId, requestedRefund.refundId)
    yield updatedOrder

