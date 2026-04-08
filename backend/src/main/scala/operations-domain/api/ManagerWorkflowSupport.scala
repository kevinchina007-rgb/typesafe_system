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


trait LiveManagerWorkflowSupport[F[_]: MonadThrow]:
  self: LiveManagerWorkflowApplicationService[F] =>
  protected def toSession(managerContext: ManagerContext): ManagerSession =
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

  protected def loadManagerContext(managerId: ManagerId, managerType: ManagerType): F[ManagerContext] =
    managerType match
      case ManagerType.Airline => managerService.loadAirlineManager(managerId).map(identity[ManagerContext])
      case ManagerType.Hotel   => managerService.loadHotelManager(managerId).map(identity[ManagerContext])
      case ManagerType.Attraction => managerService.loadAttractionManager(managerId).map(identity[ManagerContext])

  protected def buildTaskView(
      managerContext: ManagerContext,
      order: Order,
      orderLineItem: OrderLineItem,
      requestedSupplierReviewStatuses: Set[SupplierReviewStatus],
      requestedResourceTypes: Set[ManagerType]
  ): Option[ManagerBookingTaskView] =
    orderLineItem match
      case flightOrderItem: FlightOrderItem =>
        managerContext match
          case airlineManager: AirlineManager
              if airlineManager.airlineId == flightOrderItem.flightBookingSnapshot.airlineId &&
                requestedResourceTypes.contains(ManagerType.Airline) &&
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
                requestedAt = order.createdAt,
                reviewDecision = flightOrderItem.supplierReviewDecision,
                reviewedBy = flightOrderItem.supplierReviewDecision.map(_.managerId),
                reviewedAt = flightOrderItem.supplierReviewDecision.map(_.decidedAt),
                reviewNote = flightOrderItem.supplierReviewDecision.flatMap(_.reason)
              )
            )
          case _ => None
      case hotelOrderItem: HotelOrderItem =>
        managerContext match
          case hotelManager: HotelManager
              if hotelManager.hotelId == hotelOrderItem.hotelBookingSnapshot.hotelId &&
                requestedResourceTypes.contains(ManagerType.Hotel) &&
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
                requestedAt = order.createdAt,
                reviewDecision = hotelOrderItem.supplierReviewDecision,
                reviewedBy = hotelOrderItem.supplierReviewDecision.map(_.managerId),
                reviewedAt = hotelOrderItem.supplierReviewDecision.map(_.decidedAt),
                reviewNote = hotelOrderItem.supplierReviewDecision.flatMap(_.reason)
              )
            )
          case _ => None
      case attractionOrderItem: AttractionOrderItem =>
        managerContext match
          case attractionManager: AttractionManager
              if attractionOrderItem.attractionTicketSnapshot.managerId == attractionManager.managerId &&
                requestedResourceTypes.contains(ManagerType.Attraction) &&
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
                requestedAt = order.createdAt,
                reviewDecision = attractionOrderItem.supplierReviewDecision,
                reviewedBy = attractionOrderItem.supplierReviewDecision.map(_.managerId),
                reviewedAt = attractionOrderItem.supplierReviewDecision.map(_.decidedAt),
                reviewNote = attractionOrderItem.supplierReviewDecision.flatMap(_.reason)
              )
            )
          case _ => None
      case _: TrainOrderItem => None

  protected def buildRefundTaskView(managerContext: ManagerContext, order: Order): Option[ManagerRefundTaskView] =
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
        case _: TrainOrderItem =>
          None
      }.toSeq.headOption
    }

  protected def loadScopedOrder(managerContext: ManagerContext, orderItemId: OrderItemId): F[Order] =
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

  protected def loadScopedRefundOrder(managerContext: ManagerContext, orderId: OrderId): F[Order] =
    orderRepository
      .findOrderById(orderId)
      .flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
      .flatMap { order =>
        if buildRefundTaskView(managerContext, order).isDefined then order.pure[F]
        else MonadThrow[F].raiseError(ManagerError.ManagerScopeDidNotMatch(managerContext.managerType, managerContext.managerId, OrderItemId("refund-review")))
      }

  protected def loadRequestedRefund(order: Order): F[Refund] =
    order.orderRefunds.find(_.refundStatus == RefundStatus.Requested) match
      case Some(requestedRefund) => requestedRefund.pure[F]
      case None                  => MonadThrow[F].raiseError(OrderError.RefundWasNotAcceptedForOrderStatus(order.orderId, order.orderStatus))

  protected def stayDates(stayPeriod: StayPeriod): List[LocalDate] =
    Iterator.iterate(stayPeriod.checkIn)(_.plusDays(1)).takeWhile(_.isBefore(stayPeriod.checkOut)).toList

