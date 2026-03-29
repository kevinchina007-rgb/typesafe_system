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

final case class ManagerSession(
    managerId: ManagerId,
    managerType: ManagerType,
    emailAddress: EmailAddress,
    displayName: PersonName,
    status: ManagerStatus,
    scopeId: String,
    createdAt: Instant
)

final case class ManagerBookingTaskView(
    orderId: OrderId,
    orderItemId: OrderItemId,
    buyerUserId: UserId,
    taskType: ManagerType,
    supplierReviewStatus: SupplierReviewStatus,
    summaryLabel: String,
    detailLabel: String,
    reviewDecision: Option[SupplierReviewDecision]
)

final case class ManagerRefundTaskView(
    orderId: OrderId,
    buyerUserId: UserId,
    taskType: ManagerType,
    summaryLabel: String,
    refundId: RefundId,
    refundReason: String,
    refundAmount: Money,
    requestedAt: Instant
)

trait ManagerWorkflowApplicationService[F[_]]:
  def registerAirlineManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      airlineName: AirlineName,
      airlineCode: AirlineCode,
      createdAt: Instant
  ): F[ManagerSession]
  def registerHotelManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      hotelName: HotelName,
      hotelLocation: HotelLocation,
      createdAt: Instant
  ): F[ManagerSession]
  def registerAttractionManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[ManagerSession]
  def loginManager(managerType: ManagerType, primaryEmailAddress: EmailAddress): F[ManagerSession]
  def listManagerTasks(
      managerId: ManagerId,
      managerType: ManagerType,
      requestedSupplierReviewStatuses: Set[SupplierReviewStatus]
  ): F[List[ManagerBookingTaskView]]
  def listManagerRefundTasks(managerId: ManagerId, managerType: ManagerType): F[List[ManagerRefundTaskView]]
  def createFlightForAirlineManager(
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
  ): F[(Airline, Flight)]
  def createRoomTypeForHotelManager(
      managerId: ManagerId,
      roomTypeName: RoomTypeName,
      roomCapacity: Capacity,
      bedType: BedType,
      nightlyPrice: Money,
      availableRooms: RoomCount,
      inventoryStartDate: LocalDate,
      inventoryEndDate: LocalDate
  ): F[Hotel]
  def confirmBookingItem(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemId: OrderItemId,
      note: Option[String],
      decidedAt: Instant
  ): F[Order]
  def rejectBookingItem(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemId: OrderItemId,
      reason: String,
      decidedAt: Instant
  ): F[Order]
  def approveRefund(
      managerId: ManagerId,
      managerType: ManagerType,
      orderId: OrderId,
      decidedAt: Instant
  ): F[Order]
  def rejectRefund(
      managerId: ManagerId,
      managerType: ManagerType,
      orderId: OrderId
  ): F[Order]

final class LiveManagerWorkflowApplicationService[F[_]: MonadThrow](
    managerService: ManagerService[F],
    managerRepository: ManagerRepository[F],
    orderRepository: OrderRepository[F],
    orderService: OrderService[F],
    reservationLifecycle: ReservationLifecycle[F],
    flightRepository: FlightRepository[F],
    hotelRepository: HotelRepository[F],
    attractionRepository: AttractionRepository[F]
) extends ManagerWorkflowApplicationService[F]:
  override def registerAirlineManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      airlineName: AirlineName,
      airlineCode: AirlineCode,
      createdAt: Instant
  ): F[ManagerSession] =
    for
      airlineId <- flightRepository.nextAirlineId
      airline <- flightRepository.saveAirline(Airline.createAirline(airlineId, airlineName, airlineCode, createdAt))
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
        Hotel.createHotel(
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

  override def listManagerTasks(
      managerId: ManagerId,
      managerType: ManagerType,
      requestedSupplierReviewStatuses: Set[SupplierReviewStatus]
  ): F[List[ManagerBookingTaskView]] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      allOrders <- orderRepository.findAllOrders
    yield allOrders
      .flatMap(order => order.orderLineItems.flatMap(orderLineItem => buildTaskView(managerContext, order, orderLineItem, requestedSupplierReviewStatuses)))
      .sortBy(task => (task.orderId.value, task.orderItemId.value))

  override def listManagerRefundTasks(managerId: ManagerId, managerType: ManagerType): F[List[ManagerRefundTaskView]] =
    for
      managerContext <- loadManagerContext(managerId, managerType)
      allOrders <- orderRepository.findAllOrders
    yield allOrders
      .flatMap(order => buildRefundTaskView(managerContext, order).toList)
      .sortBy(task => (task.requestedAt.toEpochMilli, task.orderId.value))
      .reverse

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
      flight <- Flight
        .createFlight(
          flightId = flightId,
          airlineId = airline.airlineId,
          flightNumber = flightNumber,
          departureAirport = departureAirport,
          arrivalAirport = arrivalAirport,
          flightSchedule = flightSchedule,
          basePrice = economyPrice,
          cabinInventories = Vector(
            CabinInventory.createCabinInventory(economyInventoryId, flightId, CabinClass.unsafe("economy"), economySeatCount, economyPrice, InventoryStatus.Open),
            CabinInventory.createCabinInventory(businessInventoryId, flightId, CabinClass.unsafe("business"), businessSeatCount, businessPrice, InventoryStatus.Open)
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
          RoomInventory.createRoomInventory(
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
          RoomType.createRoomType(
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

  private def toSession(managerContext: ManagerContext): ManagerSession =
    managerContext match
      case airlineManager: AirlineManager =>
        ManagerSession(
          managerId = airlineManager.managerId,
          managerType = ManagerType.Airline,
          emailAddress = airlineManager.primaryEmailAddress,
          displayName = airlineManager.displayName,
          status = airlineManager.managerStatus,
          scopeId = airlineManager.airlineId.value,
          createdAt = airlineManager.createdAt
        )
      case hotelManager: HotelManager =>
        ManagerSession(
          managerId = hotelManager.managerId,
          managerType = ManagerType.Hotel,
          emailAddress = hotelManager.primaryEmailAddress,
          displayName = hotelManager.displayName,
          status = hotelManager.managerStatus,
          scopeId = hotelManager.hotelId.value,
          createdAt = hotelManager.createdAt
        )
      case attractionManager: AttractionManager =>
        ManagerSession(
          managerId = attractionManager.managerId,
          managerType = ManagerType.Attraction,
          emailAddress = attractionManager.primaryEmailAddress,
          displayName = attractionManager.displayName,
          status = attractionManager.managerStatus,
          scopeId = attractionManager.managerId.value,
          createdAt = attractionManager.createdAt
        )

  private def loadManagerContext(managerId: ManagerId, managerType: ManagerType): F[ManagerContext] =
    managerType match
      case ManagerType.Airline => managerService.loadAirlineManager(managerId).map(identity[ManagerContext])
      case ManagerType.Hotel   => managerService.loadHotelManager(managerId).map(identity[ManagerContext])
      case ManagerType.Attraction => managerService.loadAttractionManager(managerId).map(identity[ManagerContext])

  private def buildTaskView(
      managerContext: ManagerContext,
      order: Order,
      orderLineItem: OrderLineItem,
      requestedSupplierReviewStatuses: Set[SupplierReviewStatus]
  ): Option[ManagerBookingTaskView] =
    orderLineItem match
      case flightOrderItem: FlightOrderItem =>
        managerContext match
          case airlineManager: AirlineManager
              if airlineManager.airlineId == flightOrderItem.flightBookingSnapshot.airlineId &&
                requestedSupplierReviewStatuses.contains(flightOrderItem.supplierReviewStatus) =>
            Some(
              ManagerBookingTaskView(
                orderId = order.orderId,
                orderItemId = flightOrderItem.orderItemId,
                buyerUserId = order.ownerUserId,
                taskType = ManagerType.Airline,
                supplierReviewStatus = flightOrderItem.supplierReviewStatus,
                summaryLabel = s"${flightOrderItem.flightBookingSnapshot.airlineName.value} ${flightOrderItem.flightBookingSnapshot.flightNumber.value}",
                detailLabel = s"${flightOrderItem.flightBookingSnapshot.departureAirportCode.value}-${flightOrderItem.flightBookingSnapshot.arrivalAirportCode.value} ${flightOrderItem.flightBookingSnapshot.cabinClass.value}",
                reviewDecision = flightOrderItem.supplierReviewDecision
              )
            )
          case _ => None
      case hotelOrderItem: HotelOrderItem =>
        managerContext match
          case hotelManager: HotelManager
              if hotelManager.hotelId == hotelOrderItem.hotelBookingSnapshot.hotelId &&
                requestedSupplierReviewStatuses.contains(hotelOrderItem.supplierReviewStatus) =>
            Some(
              ManagerBookingTaskView(
                orderId = order.orderId,
                orderItemId = hotelOrderItem.orderItemId,
                buyerUserId = order.ownerUserId,
                taskType = ManagerType.Hotel,
                supplierReviewStatus = hotelOrderItem.supplierReviewStatus,
                summaryLabel = s"${hotelOrderItem.hotelBookingSnapshot.hotelName.value} ${hotelOrderItem.hotelBookingSnapshot.roomTypeName.value}",
                detailLabel = s"${hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkIn} - ${hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkOut}",
                reviewDecision = hotelOrderItem.supplierReviewDecision
              )
            )
          case _ => None
      case attractionOrderItem: AttractionOrderItem =>
        managerContext match
          case attractionManager: AttractionManager
              if attractionOrderItem.attractionTicketSnapshot.managerId == attractionManager.managerId &&
                requestedSupplierReviewStatuses.contains(attractionOrderItem.supplierReviewStatus) =>
            Some(
              ManagerBookingTaskView(
                orderId = order.orderId,
                orderItemId = attractionOrderItem.orderItemId,
                buyerUserId = order.ownerUserId,
                taskType = ManagerType.Attraction,
                supplierReviewStatus = attractionOrderItem.supplierReviewStatus,
                summaryLabel = s"${attractionOrderItem.attractionTicketSnapshot.attractionName} ${attractionOrderItem.attractionTicketSnapshot.ticketTypeName}",
                detailLabel = s"Use on ${attractionOrderItem.attractionTicketSnapshot.useDate}",
                reviewDecision = attractionOrderItem.supplierReviewDecision
              )
            )
          case _ => None

  private def buildRefundTaskView(managerContext: ManagerContext, order: Order): Option[ManagerRefundTaskView] =
    order.orderRefunds.find(_.refundStatus == RefundStatus.Requested).flatMap { requestedRefund =>
      order.orderLineItems.iterator.flatMap {
        case flightOrderItem: FlightOrderItem =>
          managerContext match
            case airlineManager: AirlineManager if airlineManager.airlineId == flightOrderItem.flightBookingSnapshot.airlineId =>
              Some(
                ManagerRefundTaskView(
                  orderId = order.orderId,
                  buyerUserId = order.ownerUserId,
                  taskType = ManagerType.Airline,
                  summaryLabel = s"${flightOrderItem.flightBookingSnapshot.airlineName.value} ${flightOrderItem.flightBookingSnapshot.flightNumber.value}",
                  refundId = requestedRefund.refundId,
                  refundReason = requestedRefund.refundReason,
                  refundAmount = requestedRefund.refundAmount,
                  requestedAt = requestedRefund.requestedAt
                )
              )
            case _ => None
        case hotelOrderItem: HotelOrderItem =>
          managerContext match
            case hotelManager: HotelManager if hotelManager.hotelId == hotelOrderItem.hotelBookingSnapshot.hotelId =>
              Some(
                ManagerRefundTaskView(
                  orderId = order.orderId,
                  buyerUserId = order.ownerUserId,
                  taskType = ManagerType.Hotel,
                  summaryLabel = s"${hotelOrderItem.hotelBookingSnapshot.hotelName.value} ${hotelOrderItem.hotelBookingSnapshot.roomTypeName.value}",
                  refundId = requestedRefund.refundId,
                  refundReason = requestedRefund.refundReason,
                  refundAmount = requestedRefund.refundAmount,
                  requestedAt = requestedRefund.requestedAt
                )
              )
            case _ => None
        case attractionOrderItem: AttractionOrderItem =>
          managerContext match
            case attractionManager: AttractionManager if attractionOrderItem.attractionTicketSnapshot.managerId == attractionManager.managerId =>
              Some(
                ManagerRefundTaskView(
                  orderId = order.orderId,
                  buyerUserId = order.ownerUserId,
                  taskType = ManagerType.Attraction,
                  summaryLabel = s"${attractionOrderItem.attractionTicketSnapshot.attractionName} ${attractionOrderItem.attractionTicketSnapshot.ticketTypeName}",
                  refundId = requestedRefund.refundId,
                  refundReason = requestedRefund.refundReason,
                  refundAmount = requestedRefund.refundAmount,
                  requestedAt = requestedRefund.requestedAt
                )
              )
            case _ => None
      }.toSeq.headOption
    }

  private def loadScopedOrder(managerContext: ManagerContext, orderItemId: OrderItemId): F[Order] =
    orderRepository
      .findOrderByOrderItemId(orderItemId)
      .flatMap(_.liftTo[F](OrderError.OrderItemWasNotFound(OrderId("unknown-order"), orderItemId)))
      .flatMap { order =>
        order.orderLineItems.find(_.orderItemId == orderItemId) match
          case Some(flightOrderItem: FlightOrderItem) =>
            managerContext match
              case airlineManager: AirlineManager if airlineManager.airlineId == flightOrderItem.flightBookingSnapshot.airlineId =>
                order.pure[F]
              case _ =>
                MonadThrow[F].raiseError(ManagerError.ManagerScopeDidNotMatch(managerContext.managerType, managerContext.managerId, orderItemId))
          case Some(hotelOrderItem: HotelOrderItem) =>
            managerContext match
              case hotelManager: HotelManager if hotelManager.hotelId == hotelOrderItem.hotelBookingSnapshot.hotelId =>
                order.pure[F]
              case _ =>
                MonadThrow[F].raiseError(ManagerError.ManagerScopeDidNotMatch(managerContext.managerType, managerContext.managerId, orderItemId))
          case Some(attractionOrderItem: AttractionOrderItem) =>
            managerContext match
              case attractionManager: AttractionManager if attractionOrderItem.attractionTicketSnapshot.managerId == attractionManager.managerId =>
                order.pure[F]
              case _ =>
                MonadThrow[F].raiseError(ManagerError.ManagerScopeDidNotMatch(managerContext.managerType, managerContext.managerId, orderItemId))
          case _ =>
            MonadThrow[F].raiseError(OrderError.OrderItemWasNotFound(order.orderId, orderItemId))
      }

  private def loadScopedRefundOrder(managerContext: ManagerContext, orderId: OrderId): F[Order] =
    orderRepository
      .findOrderById(orderId)
      .flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
      .flatMap { order =>
        if buildRefundTaskView(managerContext, order).isDefined then order.pure[F]
        else MonadThrow[F].raiseError(ManagerError.ManagerScopeDidNotMatch(managerContext.managerType, managerContext.managerId, OrderItemId("refund-review")))
      }

  private def loadRequestedRefund(order: Order): F[Refund] =
    order.orderRefunds.find(_.refundStatus == RefundStatus.Requested) match
      case Some(requestedRefund) => requestedRefund.pure[F]
      case None                  => MonadThrow[F].raiseError(OrderError.RefundWasNotAcceptedForOrderStatus(order.orderId, order.orderStatus))

  private def stayDates(stayPeriod: StayPeriod): List[LocalDate] =
    Iterator.iterate(stayPeriod.checkIn)(_.plusDays(1)).takeWhile(_.isBefore(stayPeriod.checkOut)).toList
