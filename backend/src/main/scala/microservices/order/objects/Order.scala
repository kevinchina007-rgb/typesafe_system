package com.typesafe.travel.order.domain

import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

// Order 浠嶇劧鏄氦鏄撲富鑱氬悎鏍广�?
// Flight / Hotel / Train / Attraction 閮藉厛鎶婅嚜宸辩殑涓氬姟蹇収浜ょ粰 Order�?
// 鐒跺悗鐢?Order 缁熶竴绠＄悊鏀粯銆侀€€娆俱€佺‘璁ゅ拰鍙栨秷绛変氦鏄撶姸鎬併€?
final case class Order(
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
)

object Order:
  import OrderSourceJsonCodecs.given

  given sourceEncoder: Encoder[Order] = deriveEncoder[Order]
  given sourceDecoder: Decoder[Order] = deriveDecoder[Order]

enum OrderError(val message: String) extends DomainError:
  // OrderError 鏄氦鏄撴牴鍙В閲婃€х殑鏍稿績锛氭墍鏈夊澶栧け璐ユ渶缁堥兘鍙互杩藉埌杩欓噷銆?
  case OrderWasNotFound(orderId: OrderId) extends OrderError(s"Order '${orderId.value}' was not found")
  case OrderCannotBeSubmittedWithoutItems(orderId: OrderId) extends OrderError(s"Order '${orderId.value}' cannot be submitted without any order items")
  case OrderItemsCouldOnlyBeAddedInDraft(orderId: OrderId, currentOrderStatus: OrderStatus) extends OrderError(s"Order '${orderId.value}' cannot add items while in status $currentOrderStatus")
  case InvalidOrderStatusTransition(orderId: OrderId, currentOrderStatus: OrderStatus, targetOrderStatus: OrderStatus) extends OrderError(s"Order '${orderId.value}' cannot transition from $currentOrderStatus to $targetOrderStatus")
  case OrderCurrencyDidNotMatch(orderId: OrderId, expectedCurrency: Currency, actualCurrency: Currency) extends OrderError(s"Order '${orderId.value}' expected currency $expectedCurrency but received $actualCurrency")
  case PaymentWasNotAcceptedForOrderStatus(orderId: OrderId, currentOrderStatus: OrderStatus) extends OrderError(s"Order '${orderId.value}' does not accept payments while in status $currentOrderStatus")
  case PaymentWasNotFound(orderId: OrderId, paymentId: PaymentId) extends OrderError(s"Payment '${paymentId.value}' was not found in order '${orderId.value}'")
  case PaymentWasAlreadyCompleted(orderId: OrderId) extends OrderError(s"Order '${orderId.value}' has already been paid successfully")
  case PaymentCouldNotBeCaptured(paymentId: PaymentId, currentPaymentStatus: PaymentStatus) extends OrderError(s"Payment '${paymentId.value}' cannot be captured from status $currentPaymentStatus")
  case PaymentCouldNotBeFailed(paymentId: PaymentId, currentPaymentStatus: PaymentStatus) extends OrderError(s"Payment '${paymentId.value}' cannot be failed from status $currentPaymentStatus")
  case RefundWasNotAcceptedForOrderStatus(orderId: OrderId, currentOrderStatus: OrderStatus) extends OrderError(s"Order '${orderId.value}' does not accept refunds while in status $currentOrderStatus")
  case RefundExceededRemainingBalance(orderId: OrderId, remainingRefundableMoney: Money, requestedRefundMoney: Money) extends OrderError(s"Order '${orderId.value}' can refund only ${remainingRefundableMoney.amount} but requested ${requestedRefundMoney.amount}")
  case RefundWasAlreadyRequested(orderId: OrderId) extends OrderError(s"Order '${orderId.value}' already has a pending refund request")
  case RefundReasonWasEmpty(refundId: RefundId) extends OrderError(s"Refund '${refundId.value}' must have a reason")
  case RefundWasNotFound(orderId: OrderId, refundId: RefundId) extends OrderError(s"Refund '${refundId.value}' was not found in order '${orderId.value}'")
  case RefundCouldNotBeApproved(refundId: RefundId, currentRefundStatus: RefundStatus) extends OrderError(s"Refund '${refundId.value}' cannot be approved from status $currentRefundStatus")
  case RefundCouldNotBeRejected(refundId: RefundId, currentRefundStatus: RefundStatus) extends OrderError(s"Refund '${refundId.value}' cannot be rejected from status $currentRefundStatus")
  case RefundCouldNotBeSettled(refundId: RefundId, currentRefundStatus: RefundStatus) extends OrderError(s"Refund '${refundId.value}' cannot be settled from status $currentRefundStatus")
  case OrderCouldNotBeCancelled(orderId: OrderId, currentOrderStatus: OrderStatus) extends OrderError(s"Order '${orderId.value}' cannot be cancelled from status $currentOrderStatus")
  case FlightBookingTravelerSelectionWasEmpty(orderId: OrderId, flightId: FlightId) extends OrderError(s"Order '${orderId.value}' cannot add flight '${flightId.value}' without any travelers")
  case FlightBookingTravelerSelectionContainedDuplicates(orderId: OrderId, flightId: FlightId) extends OrderError(s"Order '${orderId.value}' cannot add flight '${flightId.value}' with duplicate travelers")
  case HotelBookingGuestSelectionWasEmpty(orderId: OrderId, roomTypeId: RoomTypeId) extends OrderError(s"Order '${orderId.value}' cannot add room type '${roomTypeId.value}' without any guests")
  case HotelBookingGuestSelectionContainedDuplicates(orderId: OrderId, roomTypeId: RoomTypeId) extends OrderError(s"Order '${orderId.value}' cannot add room type '${roomTypeId.value}' with duplicate guests")
  case HotelBookingRoomCountWasInvalid(orderId: OrderId, roomTypeId: RoomTypeId, roomCount: RoomCount) extends OrderError(s"Order '${orderId.value}' cannot add room type '${roomTypeId.value}' with room count ${roomCount.value}")
  case TrainBookingTravelerSelectionWasEmpty(orderId: OrderId, trainId: TrainId) extends OrderError(s"Order '${orderId.value}' cannot add train '${trainId.value}' without any travelers")
  case TrainBookingTravelerSelectionContainedDuplicates(orderId: OrderId, trainId: TrainId) extends OrderError(s"Order '${orderId.value}' cannot add train '${trainId.value}' with duplicate travelers")
  case AttractionBookingTravelerSelectionWasEmpty(orderId: OrderId, ticketTypeId: TicketTypeId) extends OrderError(s"Order '${orderId.value}' cannot add attraction ticket '${ticketTypeId.value}' without any travelers")
  case AttractionBookingTravelerSelectionContainedDuplicates(orderId: OrderId, ticketTypeId: TicketTypeId) extends OrderError(s"Order '${orderId.value}' cannot add attraction ticket '${ticketTypeId.value}' with duplicate travelers")
  case OrderItemWasNotFound(orderId: OrderId, orderItemId: OrderItemId) extends OrderError(s"Order item '${orderItemId.value}' was not found in order '${orderId.value}'")
  case OrderItemDidNotSupportSupplierReview(orderItemId: OrderItemId) extends OrderError(s"Order item '${orderItemId.value}' does not support supplier review")
  case OrderItemWasNotAwaitingSupplierDecision(orderItemId: OrderItemId, supplierReviewStatus: SupplierReviewStatus) extends OrderError(s"Order item '${orderItemId.value}' cannot be decided from supplier review status $supplierReviewStatus")
  case SupplierRejectReasonWasEmpty(orderItemId: OrderItemId) extends OrderError(s"Order item '${orderItemId.value}' requires a non-empty reject reason")
