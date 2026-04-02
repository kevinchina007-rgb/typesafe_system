package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.train.domain.*
import java.time.Instant

def createReservedFlightOrderItem(
    orderItemId: OrderItemId,
    flightBookingSnapshot: FlightBookingSnapshot
): FlightOrderItem =
  FlightOrderItem(
    orderItemId,
    flightBookingSnapshot,
    OrderItemStatus.Reserved,
    SupplierReviewStatus.NotSubmitted,
    None
  )


def restorePersistedFlightOrderItem(
    orderItemId: OrderItemId,
    flightBookingSnapshot: FlightBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
): FlightOrderItem =
  FlightOrderItem(orderItemId, flightBookingSnapshot, orderItemStatus, supplierReviewStatus, supplierReviewDecision)


def createReservedHotelOrderItem(
    orderItemId: OrderItemId,
    hotelBookingSnapshot: HotelBookingSnapshot
): HotelOrderItem =
  HotelOrderItem(
    orderItemId,
    hotelBookingSnapshot,
    OrderItemStatus.Reserved,
    SupplierReviewStatus.NotSubmitted,
    None
  )


def restorePersistedHotelOrderItem(
    orderItemId: OrderItemId,
    hotelBookingSnapshot: HotelBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
): HotelOrderItem =
  HotelOrderItem(orderItemId, hotelBookingSnapshot, orderItemStatus, supplierReviewStatus, supplierReviewDecision)


def createReservedTrainOrderItem(
    orderItemId: OrderItemId,
    trainBookingSnapshot: TrainBookingSnapshot
): TrainOrderItem =
  TrainOrderItem(
    orderItemId = orderItemId,
    trainBookingSnapshot = trainBookingSnapshot,
    orderItemStatus = OrderItemStatus.Reserved,
    supplierReviewStatus = SupplierReviewStatus.NotSubmitted,
    supplierReviewDecision = None
  )


def restorePersistedTrainOrderItem(
    orderItemId: OrderItemId,
    trainBookingSnapshot: TrainBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
): TrainOrderItem =
  TrainOrderItem(orderItemId, trainBookingSnapshot, orderItemStatus, supplierReviewStatus, supplierReviewDecision)


def createReservedAttractionOrderItem(
    orderItemId: OrderItemId,
    attractionTicketSnapshot: AttractionTicketSnapshot
): AttractionOrderItem =
  AttractionOrderItem(
    orderItemId,
    attractionTicketSnapshot,
    OrderItemStatus.Reserved,
    SupplierReviewStatus.NotSubmitted,
    None
  )


def restorePersistedAttractionOrderItem(
    orderItemId: OrderItemId,
    attractionTicketSnapshot: AttractionTicketSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
): AttractionOrderItem =
  AttractionOrderItem(orderItemId, attractionTicketSnapshot, orderItemStatus, supplierReviewStatus, supplierReviewDecision)


def authorizePayment(
    paymentId: PaymentId,
    paymentAmount: Money,
    paymentMethod: PaymentMethod,
    authorizedAt: Instant
): Payment =
  Payment(paymentId, paymentAmount, paymentMethod, PaymentStatus.Authorized, authorizedAt, None)


def restorePersistedPayment(
    paymentId: PaymentId,
    paymentAmount: Money,
    paymentMethod: PaymentMethod,
    paymentStatus: PaymentStatus,
    authorizedAt: Instant,
    capturedAt: Option[Instant]
): Payment =
  Payment(paymentId, paymentAmount, paymentMethod, paymentStatus, authorizedAt, capturedAt)


def requestRefund(
    refundId: RefundId,
    refundAmount: Money,
    refundReason: String,
    requestedAt: Instant
): Either[OrderError, Refund] =
  val normalizedRefundReason = refundReason.trim
  if normalizedRefundReason.nonEmpty then
    Right(Refund(refundId, refundAmount, normalizedRefundReason, RefundStatus.Requested, requestedAt, None, None))
  else
    Left(OrderError.RefundReasonWasEmpty(refundId))


def restorePersistedRefund(
    refundId: RefundId,
    refundAmount: Money,
    refundReason: String,
    refundStatus: RefundStatus,
    requestedAt: Instant,
    approvedAt: Option[Instant],
    settledAt: Option[Instant]
): Refund =
  Refund(refundId, refundAmount, refundReason, refundStatus, requestedAt, approvedAt, settledAt)


def newDraftOrder(
    orderId: OrderId,
    ownerUserId: UserId,
    orderCurrency: Currency,
    createdAt: Instant
): Order =
  Order(
    orderId = orderId,
    ownerUserId = ownerUserId,
    orderStatus = OrderStatus.Draft,
    orderCurrency = orderCurrency,
    orderLineItems = Vector.empty,
    orderPayments = Vector.empty,
    orderRefunds = Vector.empty,
    createdAt = createdAt,
    paidAt = None,
    confirmedAt = None,
    completedAt = None,
    cancelledAt = None
  )


def restoreOrder(
    orderId: OrderId,
    ownerUserId: UserId,
    orderStatus: OrderStatus,
    orderCurrency: Currency,
    orderLineItems: Vector[OrderLineItem],
    orderPayments: Vector[Payment],
    orderRefunds: Vector[Refund],
    createdAt: Instant,
    paidAt: Option[Instant],
    confirmedAt: Option[Instant],
    completedAt: Option[Instant],
    cancelledAt: Option[Instant]
): Order =
  Order(
    orderId = orderId,
    ownerUserId = ownerUserId,
    orderStatus = orderStatus,
    orderCurrency = orderCurrency,
    orderLineItems = orderLineItems,
    orderPayments = orderPayments,
    orderRefunds = orderRefunds,
    createdAt = createdAt,
    paidAt = paidAt,
    confirmedAt = confirmedAt,
    completedAt = completedAt,
    cancelledAt = cancelledAt
  )
