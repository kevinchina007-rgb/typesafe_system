package com.typesafe.travel.order.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.shared.kernel.*
import java.time.Instant

trait OrderService[F[_]]:
  def createDraftOrder(ownerUserId: UserId, orderCurrency: Currency, createdAt: Instant): F[Order]
  def listOrdersForUser(ownerUserId: UserId): F[List[Order]]
  def addFlightOrderItem(orderId: OrderId, flightBookingSnapshot: FlightBookingSnapshot): F[Order]
  def addHotelOrderItem(orderId: OrderId, hotelBookingSnapshot: HotelBookingSnapshot): F[Order]
  def addTrainOrderItem(orderId: OrderId, trainBookingSnapshot: TrainBookingSnapshot): F[Order]
  def addAttractionOrderItem(orderId: OrderId, attractionTicketSnapshot: AttractionTicketSnapshot): F[Order]
  def submitOrderForPayment(orderId: OrderId): F[Order]
  def payOrder(orderId: OrderId, paymentMethod: PaymentMethod, paidAt: Instant): F[Order]
  def authorizeOrderPayment(orderId: OrderId, paymentAmount: Money, paymentMethod: PaymentMethod, authorizedAt: Instant): F[Order]
  def captureAuthorizedPayment(orderId: OrderId, paymentId: PaymentId, capturedAt: Instant): F[Order]
  def requestCustomerRefund(orderId: OrderId, refundReason: String, requestedAt: Instant): F[Order]
  def requestOrderRefund(orderId: OrderId, refundAmount: Money, refundReason: String, requestedAt: Instant): F[Order]
  def approveRequestedRefund(orderId: OrderId, refundId: RefundId, approvedAt: Instant): F[Order]
  def rejectRequestedRefund(orderId: OrderId, refundId: RefundId): F[Order]
  def settleApprovedRefund(orderId: OrderId, refundId: RefundId, settledAt: Instant): F[Order]
  def confirmSupplierOrderItem(orderId: OrderId, orderItemId: OrderItemId, managerId: ManagerId, note: Option[String], decidedAt: Instant): F[Order]
  def rejectSupplierOrderItem(orderId: OrderId, orderItemId: OrderItemId, managerId: ManagerId, reason: String, decidedAt: Instant): F[Order]

final class LiveOrderService[F[_]: MonadThrow](
    orderRepository: OrderRepository[F]
) extends OrderService[F]:

  override def createDraftOrder(ownerUserId: UserId, orderCurrency: Currency, createdAt: Instant): F[Order] =
    orderRepository.nextOrderId.flatMap { generatedOrderId =>
      orderRepository.saveOrder(Order.createDraftOrder(generatedOrderId, ownerUserId, orderCurrency, createdAt))
    }

  override def listOrdersForUser(ownerUserId: UserId): F[List[Order]] =
    orderRepository.findAllOrders.map(_.filter(_.ownerUserId == ownerUserId).sortBy(_.createdAt.toEpochMilli).reverse)

  override def addFlightOrderItem(
      orderId: OrderId,
      flightBookingSnapshot: FlightBookingSnapshot
  ): F[Order] =
    addOrderLineItem(
      orderId,
      generatedOrderItemId => _.addFlightOrderItem(generatedOrderItemId, flightBookingSnapshot)
    )

  override def addHotelOrderItem(
      orderId: OrderId,
      hotelBookingSnapshot: HotelBookingSnapshot
  ): F[Order] =
    addOrderLineItem(
      orderId,
      generatedOrderItemId => _.addHotelOrderItem(generatedOrderItemId, hotelBookingSnapshot)
    )

  override def addTrainOrderItem(
      orderId: OrderId,
      trainBookingSnapshot: TrainBookingSnapshot
  ): F[Order] =
    addOrderLineItem(
      orderId,
      generatedOrderItemId => _.addTrainOrderItem(generatedOrderItemId, trainBookingSnapshot)
    )

  override def addAttractionOrderItem(
      orderId: OrderId,
      attractionTicketSnapshot: AttractionTicketSnapshot
  ): F[Order] =
    addOrderLineItem(
      orderId,
      generatedOrderItemId => _.addAttractionOrderItem(generatedOrderItemId, attractionTicketSnapshot)
    )

  override def submitOrderForPayment(orderId: OrderId): F[Order] =
    loadOrder(orderId)
      .flatMap(_.submitOrderForPayment.liftTo[F])
      .flatMap(orderRepository.saveOrder)

  override def payOrder(orderId: OrderId, paymentMethod: PaymentMethod, paidAt: Instant): F[Order] =
    for
      order <- loadOrder(orderId)
      submittedOrder <- order.orderStatus match
        case OrderStatus.Draft => order.submitOrderForPayment.liftTo[F]
        case _                 => order.pure[F]
      paidAmount = submittedOrder.totalBookedMoney
      paymentId <- orderRepository.nextPaymentId
      authorizedOrder <- submittedOrder.authorizeOrderPayment(paymentId, paidAmount, paymentMethod, paidAt).liftTo[F]
      capturedOrder <- authorizedOrder.captureAuthorizedPayment(paymentId, paidAt).liftTo[F]
      savedOrder <- orderRepository.saveOrder(capturedOrder)
    yield savedOrder

  override def authorizeOrderPayment(
      orderId: OrderId,
      paymentAmount: Money,
      paymentMethod: PaymentMethod,
      authorizedAt: Instant
  ): F[Order] =
    for
      order <- loadOrder(orderId)
      paymentId <- orderRepository.nextPaymentId
      updatedOrder <- order.authorizeOrderPayment(paymentId, paymentAmount, paymentMethod, authorizedAt).liftTo[F]
      savedOrder <- orderRepository.saveOrder(updatedOrder)
    yield savedOrder

  override def captureAuthorizedPayment(orderId: OrderId, paymentId: PaymentId, capturedAt: Instant): F[Order] =
    loadOrder(orderId)
      .flatMap(_.captureAuthorizedPayment(paymentId, capturedAt).liftTo[F])
      .flatMap(orderRepository.saveOrder)

  override def requestOrderRefund(
      orderId: OrderId,
      refundAmount: Money,
      refundReason: String,
      requestedAt: Instant
  ): F[Order] =
    for
      order <- loadOrder(orderId)
      refundId <- orderRepository.nextRefundId
      updatedOrder <- order.requestOrderRefund(refundId, refundAmount, refundReason, requestedAt).liftTo[F]
      savedOrder <- orderRepository.saveOrder(updatedOrder)
    yield savedOrder

  override def requestCustomerRefund(orderId: OrderId, refundReason: String, requestedAt: Instant): F[Order] =
    for
      order <- loadOrder(orderId)
      refundId <- orderRepository.nextRefundId
      requestedOrder <- order
        .requestOrderRefund(
          refundId = refundId,
          refundAmount = order.remainingRefundableMoney,
          refundReason = refundReason,
          requestedAt = requestedAt
        )
        .liftTo[F]
      resultingOrder <- if order.allSupplierReviewDecisionsConfirmed then
        orderRepository.saveOrder(requestedOrder)
      else
        requestedOrder
          .approveRequestedRefund(refundId, requestedAt)
          .liftTo[F]
          .flatMap(_.settleApprovedRefund(refundId, requestedAt).liftTo[F])
          .flatMap(orderRepository.saveOrder)
    yield resultingOrder

  override def approveRequestedRefund(orderId: OrderId, refundId: RefundId, approvedAt: Instant): F[Order] =
    loadOrder(orderId)
      .flatMap(_.approveRequestedRefund(refundId, approvedAt).liftTo[F])
      .flatMap(orderRepository.saveOrder)

  override def rejectRequestedRefund(orderId: OrderId, refundId: RefundId): F[Order] =
    loadOrder(orderId)
      .flatMap(_.rejectRequestedRefund(refundId).liftTo[F])
      .flatMap(orderRepository.saveOrder)

  override def settleApprovedRefund(orderId: OrderId, refundId: RefundId, settledAt: Instant): F[Order] =
    loadOrder(orderId)
      .flatMap(_.settleApprovedRefund(refundId, settledAt).liftTo[F])
      .flatMap(orderRepository.saveOrder)

  override def confirmSupplierOrderItem(
      orderId: OrderId,
      orderItemId: OrderItemId,
      managerId: ManagerId,
      note: Option[String],
      decidedAt: Instant
  ): F[Order] =
    loadOrder(orderId)
      .flatMap(_.confirmSupplierOrderItem(orderItemId, managerId, note, decidedAt).liftTo[F])
      .flatMap(orderRepository.saveOrder)

  override def rejectSupplierOrderItem(
      orderId: OrderId,
      orderItemId: OrderItemId,
      managerId: ManagerId,
      reason: String,
      decidedAt: Instant
  ): F[Order] =
    loadOrder(orderId)
      .flatMap(_.rejectSupplierOrderItem(orderItemId, managerId, reason, decidedAt).liftTo[F])
      .flatMap(orderRepository.saveOrder)

  private def addOrderLineItem(
      orderId: OrderId,
      addLineItem: OrderItemId => Order => Either[OrderError, Order]
  ): F[Order] =
    for
      order <- loadOrder(orderId)
      orderItemId <- orderRepository.nextOrderItemId
      updatedOrder <- addLineItem(orderItemId)(order).liftTo[F]
      savedOrder <- orderRepository.saveOrder(updatedOrder)
    yield savedOrder

  private def loadOrder(orderId: OrderId): F[Order] =
    orderRepository
      .findOrderById(orderId)
      .flatMap(_.liftTo[F](OrderError.OrderWasNotFound(orderId)))
