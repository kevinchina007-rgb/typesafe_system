package com.typesafe.travel.order.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.Instant

trait OrderService[F[_]]:
  def createDraftOrder(ownerUserId: UserId, orderCurrency: Currency, createdAt: Instant): F[Order]
  def addFlightOrderItem(orderId: OrderId, flightBookingSnapshot: FlightBookingSnapshot, bookedMoney: Money): F[Order]
  def addHotelOrderItem(orderId: OrderId, hotelBookingSnapshot: HotelBookingSnapshot, bookedMoney: Money): F[Order]
  def submitOrderForPayment(orderId: OrderId): F[Order]
  def authorizeOrderPayment(orderId: OrderId, paymentAmount: Money, paymentMethod: PaymentMethod, authorizedAt: Instant): F[Order]
  def captureAuthorizedPayment(orderId: OrderId, paymentId: PaymentId, capturedAt: Instant): F[Order]
  def requestOrderRefund(orderId: OrderId, refundAmount: Money, refundReason: String, requestedAt: Instant): F[Order]
  def approveRequestedRefund(orderId: OrderId, refundId: RefundId, approvedAt: Instant): F[Order]
  def settleApprovedRefund(orderId: OrderId, refundId: RefundId, settledAt: Instant): F[Order]

final class LiveOrderService[F[_]: MonadThrow](
    orderRepository: OrderRepository[F]
) extends OrderService[F]:

  override def createDraftOrder(ownerUserId: UserId, orderCurrency: Currency, createdAt: Instant): F[Order] =
    orderRepository.nextOrderId.flatMap { generatedOrderId =>
      orderRepository.saveOrder(Order.createDraftOrder(generatedOrderId, ownerUserId, orderCurrency, createdAt))
    }

  override def addFlightOrderItem(
      orderId: OrderId,
      flightBookingSnapshot: FlightBookingSnapshot,
      bookedMoney: Money
  ): F[Order] =
    addOrderLineItem(
      orderId,
      generatedOrderItemId => _.addFlightOrderItem(generatedOrderItemId, flightBookingSnapshot, bookedMoney)
    )

  override def addHotelOrderItem(
      orderId: OrderId,
      hotelBookingSnapshot: HotelBookingSnapshot,
      bookedMoney: Money
  ): F[Order] =
    addOrderLineItem(
      orderId,
      generatedOrderItemId => _.addHotelOrderItem(generatedOrderItemId, hotelBookingSnapshot, bookedMoney)
    )

  override def submitOrderForPayment(orderId: OrderId): F[Order] =
    loadOrder(orderId)
      .flatMap(_.submitOrderForPayment.liftTo[F])
      .flatMap(orderRepository.saveOrder)

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

  override def approveRequestedRefund(orderId: OrderId, refundId: RefundId, approvedAt: Instant): F[Order] =
    loadOrder(orderId)
      .flatMap(_.approveRequestedRefund(refundId, approvedAt).liftTo[F])
      .flatMap(orderRepository.saveOrder)

  override def settleApprovedRefund(orderId: OrderId, refundId: RefundId, settledAt: Instant): F[Order] =
    loadOrder(orderId)
      .flatMap(_.settleApprovedRefund(refundId, settledAt).liftTo[F])
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
