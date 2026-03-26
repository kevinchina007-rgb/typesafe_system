package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

enum OrderStatus:
  case Draft, PendingPayment, Confirmed, PartiallyRefunded, Refunded, Cancelled

enum OrderItemStatus:
  case Reserved, Confirmed, Refunded, Cancelled

enum PaymentStatus:
  case Authorized, Captured, Failed

enum RefundStatus:
  case Requested, Approved, Rejected, Settled

enum PaymentMethod:
  case Card, BankTransfer, Wallet, LoyaltyPoints

enum OrderType:
  case FlightBooking, HotelBooking, MixedBooking, PendingSelection

sealed trait OrderLineItem:
  def orderItemId: OrderItemId
  def bookedMoney: Money
  def orderItemStatus: OrderItemStatus

final case class FlightBookingSnapshot(
    airlineId: AirlineId,
    airlineName: AirlineName,
    airlineCode: AirlineCode,
    flightId: FlightId,
    flightNumber: FlightNumber,
    flightSchedule: FlightSchedule,
    departureAirportCode: AirportCode,
    arrivalAirportCode: AirportCode,
    cabinClass: CabinClass,
    travelerIds: Vector[TravelerId],
    unitPriceSnapshot: Money
)

final case class HotelBookingSnapshot(
    hotelId: HotelId,
    hotelName: HotelName,
    hotelLocation: HotelLocation,
    roomTypeId: RoomTypeId,
    roomTypeName: RoomTypeName,
    stayPeriod: StayPeriod,
    guestTravelerIds: Vector[TravelerId],
    roomCount: RoomCount,
    unitPriceSnapshot: Money,
    totalPriceSnapshot: Money
)

final case class FlightOrderItem private (
    orderItemId: OrderItemId,
    flightBookingSnapshot: FlightBookingSnapshot,
    bookedMoney: Money,
    orderItemStatus: OrderItemStatus
) extends OrderLineItem:

  def markConfirmedOrderItem: FlightOrderItem =
    copy(orderItemStatus = OrderItemStatus.Confirmed)

  def markRefundedOrderItem: FlightOrderItem =
    copy(orderItemStatus = OrderItemStatus.Refunded)

  def markCancelledOrderItem: FlightOrderItem =
    copy(orderItemStatus = OrderItemStatus.Cancelled)

object FlightOrderItem:
  def createReservedFlightOrderItem(
      orderItemId: OrderItemId,
      flightBookingSnapshot: FlightBookingSnapshot,
      bookedMoney: Money
  ): FlightOrderItem =
    FlightOrderItem(orderItemId, flightBookingSnapshot, bookedMoney, OrderItemStatus.Reserved)

  def restorePersistedFlightOrderItem(
      orderItemId: OrderItemId,
      flightBookingSnapshot: FlightBookingSnapshot,
      bookedMoney: Money,
      orderItemStatus: OrderItemStatus
  ): FlightOrderItem =
    FlightOrderItem(orderItemId, flightBookingSnapshot, bookedMoney, orderItemStatus)

final case class HotelOrderItem private (
    orderItemId: OrderItemId,
    hotelBookingSnapshot: HotelBookingSnapshot,
    bookedMoney: Money,
    orderItemStatus: OrderItemStatus
) extends OrderLineItem:

  def markConfirmedOrderItem: HotelOrderItem =
    copy(orderItemStatus = OrderItemStatus.Confirmed)

  def markRefundedOrderItem: HotelOrderItem =
    copy(orderItemStatus = OrderItemStatus.Refunded)

  def markCancelledOrderItem: HotelOrderItem =
    copy(orderItemStatus = OrderItemStatus.Cancelled)

object HotelOrderItem:
  def createReservedHotelOrderItem(
      orderItemId: OrderItemId,
      hotelBookingSnapshot: HotelBookingSnapshot,
      bookedMoney: Money
  ): HotelOrderItem =
    HotelOrderItem(orderItemId, hotelBookingSnapshot, bookedMoney, OrderItemStatus.Reserved)

  def restorePersistedHotelOrderItem(
      orderItemId: OrderItemId,
      hotelBookingSnapshot: HotelBookingSnapshot,
      bookedMoney: Money,
      orderItemStatus: OrderItemStatus
  ): HotelOrderItem =
    HotelOrderItem(orderItemId, hotelBookingSnapshot, bookedMoney, orderItemStatus)

final case class Payment private (
    paymentId: PaymentId,
    paymentAmount: Money,
    paymentMethod: PaymentMethod,
    paymentStatus: PaymentStatus,
    authorizedAt: Instant,
    capturedAt: Option[Instant]
):

  def markCapturedPayment(capturedAt: Instant): Payment =
    copy(paymentStatus = PaymentStatus.Captured, capturedAt = Some(capturedAt))

  def markFailedPayment: Payment =
    copy(paymentStatus = PaymentStatus.Failed)

object Payment:
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

final case class Refund private (
    refundId: RefundId,
    refundAmount: Money,
    refundReason: String,
    refundStatus: RefundStatus,
    requestedAt: Instant,
    approvedAt: Option[Instant],
    settledAt: Option[Instant]
):

  def markApprovedRefund(approvedAt: Instant): Refund =
    copy(refundStatus = RefundStatus.Approved, approvedAt = Some(approvedAt))

  def markRejectedRefund: Refund =
    copy(refundStatus = RefundStatus.Rejected)

  def markSettledRefund(settledAt: Instant): Refund =
    copy(refundStatus = RefundStatus.Settled, settledAt = Some(settledAt))

object Refund:
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

final case class Order private (
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
):
  val orderType: OrderType =
    if orderLineItems.isEmpty then OrderType.PendingSelection
    else if orderLineItems.forall(_.isInstanceOf[FlightOrderItem]) then OrderType.FlightBooking
    else if orderLineItems.forall(_.isInstanceOf[HotelOrderItem]) then OrderType.HotelBooking
    else OrderType.MixedBooking

  val totalBookedMoney: Money =
    orderLineItems.foldLeft(Money.zero(orderCurrency)) { (currentTotalMoney, orderLineItem) =>
      currentTotalMoney.add(orderLineItem.bookedMoney).fold(throw _, identity)
    }

  val totalCapturedMoney: Money =
    orderPayments
      .filter(_.paymentStatus == PaymentStatus.Captured)
      .foldLeft(Money.zero(orderCurrency)) { (currentCapturedMoney, payment) =>
        currentCapturedMoney.add(payment.paymentAmount).fold(throw _, identity)
      }

  val totalSettledRefundMoney: Money =
    orderRefunds
      .filter(_.refundStatus == RefundStatus.Settled)
      .foldLeft(Money.zero(orderCurrency)) { (currentRefundMoney, refund) =>
        currentRefundMoney.add(refund.refundAmount).fold(throw _, identity)
      }

  val remainingRefundableMoney: Money =
    totalCapturedMoney.subtract(totalSettledRefundMoney).fold(throw _, identity)

  def addFlightOrderItem(
      orderItemId: OrderItemId,
      flightBookingSnapshot: FlightBookingSnapshot,
      bookedMoney: Money
  ): Either[OrderError, Order] =
    for
      _ <- validateFlightBookingSnapshot(flightBookingSnapshot)
      orderWithLineItem <- addOrderLineItem(
        FlightOrderItem.createReservedFlightOrderItem(orderItemId, flightBookingSnapshot, bookedMoney)
      )
    yield orderWithLineItem

  def addHotelOrderItem(
      orderItemId: OrderItemId,
      hotelBookingSnapshot: HotelBookingSnapshot,
      bookedMoney: Money
  ): Either[OrderError, Order] =
    for
      _ <- validateHotelBookingSnapshot(hotelBookingSnapshot)
      updatedOrder <- addOrderLineItem(
        HotelOrderItem.createReservedHotelOrderItem(orderItemId, hotelBookingSnapshot, bookedMoney)
      )
    yield updatedOrder

  def submitOrderForPayment: Either[OrderError, Order] =
    orderStatus match
      case OrderStatus.Draft if orderLineItems.isEmpty =>
        Left(OrderError.OrderCannotBeSubmittedWithoutItems(orderId))
      case OrderStatus.Draft =>
        Right(copy(orderStatus = OrderStatus.PendingPayment))
      case _ =>
        Left(OrderError.InvalidOrderStatusTransition(orderId, orderStatus, OrderStatus.PendingPayment))

  def authorizeOrderPayment(
      paymentId: PaymentId,
      paymentAmount: Money,
      paymentMethod: PaymentMethod,
      authorizedAt: Instant
  ): Either[OrderError, Order] =
    orderStatus match
      case OrderStatus.PendingPayment | OrderStatus.Confirmed =>
        ensureMatchingCurrency(paymentAmount).map { _ =>
          copy(orderPayments = orderPayments :+ Payment.authorizePayment(paymentId, paymentAmount, paymentMethod, authorizedAt))
        }
      case _ =>
        Left(OrderError.PaymentWasNotAcceptedForOrderStatus(orderId, orderStatus))

  def captureAuthorizedPayment(
      paymentId: PaymentId,
      capturedAt: Instant
  ): Either[OrderError, Order] =
    updatePayment(paymentId) { payment =>
      payment.paymentStatus match
        case PaymentStatus.Authorized =>
          Right(payment.markCapturedPayment(capturedAt))
        case _ =>
          Left(OrderError.PaymentCouldNotBeCaptured(paymentId, payment.paymentStatus))
    }.map(_.refreshOrderStatusAfterFinancialChange)

  def failAuthorizedPayment(paymentId: PaymentId): Either[OrderError, Order] =
    updatePayment(paymentId) { payment =>
      payment.paymentStatus match
        case PaymentStatus.Authorized =>
          Right(payment.markFailedPayment)
        case _ =>
          Left(OrderError.PaymentCouldNotBeFailed(paymentId, payment.paymentStatus))
    }

  def requestOrderRefund(
      refundId: RefundId,
      refundAmount: Money,
      refundReason: String,
      requestedAt: Instant
  ): Either[OrderError, Order] =
    for
      _ <- orderStatus match
        case OrderStatus.Confirmed | OrderStatus.PartiallyRefunded => Right(())
        case _ => Left(OrderError.RefundWasNotAcceptedForOrderStatus(orderId, orderStatus))
      _ <- ensureMatchingCurrency(refundAmount)
      _ <- if refundAmount.amount <= remainingRefundableMoney.amount then Right(())
      else Left(OrderError.RefundExceededRemainingBalance(orderId, remainingRefundableMoney, refundAmount))
      refund <- Refund.requestRefund(refundId, refundAmount, refundReason, requestedAt)
    yield copy(orderRefunds = orderRefunds :+ refund)

  def approveRequestedRefund(
      refundId: RefundId,
      approvedAt: Instant
  ): Either[OrderError, Order] =
    updateRefund(refundId) { refund =>
      refund.refundStatus match
        case RefundStatus.Requested =>
          Right(refund.markApprovedRefund(approvedAt))
        case _ =>
          Left(OrderError.RefundCouldNotBeApproved(refundId, refund.refundStatus))
    }

  def rejectRequestedRefund(refundId: RefundId): Either[OrderError, Order] =
    updateRefund(refundId) { refund =>
      refund.refundStatus match
        case RefundStatus.Requested =>
          Right(refund.markRejectedRefund)
        case _ =>
          Left(OrderError.RefundCouldNotBeRejected(refundId, refund.refundStatus))
    }

  def settleApprovedRefund(
      refundId: RefundId,
      settledAt: Instant
  ): Either[OrderError, Order] =
    updateRefund(refundId) { refund =>
      refund.refundStatus match
        case RefundStatus.Approved =>
          Right(refund.markSettledRefund(settledAt))
        case _ =>
          Left(OrderError.RefundCouldNotBeSettled(refundId, refund.refundStatus))
    }.map(_.refreshOrderStatusAfterFinancialChange)

  def cancelDraftOrder(cancelledAt: Instant): Either[OrderError, Order] =
    orderStatus match
      case OrderStatus.Draft | OrderStatus.PendingPayment if totalCapturedMoney.amount == 0 =>
        Right(
          copy(
            orderStatus = OrderStatus.Cancelled,
            cancelledAt = Some(cancelledAt),
            orderLineItems = orderLineItems.map {
              case flightOrderItem: FlightOrderItem => flightOrderItem.markCancelledOrderItem
              case hotelOrderItem: HotelOrderItem   => hotelOrderItem.markCancelledOrderItem
            }
          )
        )
      case _ =>
        Left(OrderError.OrderCouldNotBeCancelled(orderId, orderStatus))

  private def addOrderLineItem(orderLineItem: OrderLineItem): Either[OrderError, Order] =
    orderStatus match
      case OrderStatus.Draft =>
        ensureMatchingCurrency(orderLineItem.bookedMoney).map { _ =>
          copy(orderLineItems = orderLineItems :+ orderLineItem)
        }
      case _ =>
        Left(OrderError.OrderItemsCouldOnlyBeAddedInDraft(orderId, orderStatus))

  private def validateFlightBookingSnapshot(
      flightBookingSnapshot: FlightBookingSnapshot
  ): Either[OrderError, Unit] =
    if flightBookingSnapshot.travelerIds.isEmpty then
      Left(OrderError.FlightBookingTravelerSelectionWasEmpty(orderId, flightBookingSnapshot.flightId))
    else if flightBookingSnapshot.travelerIds.distinct.size != flightBookingSnapshot.travelerIds.size then
      Left(OrderError.FlightBookingTravelerSelectionContainedDuplicates(orderId, flightBookingSnapshot.flightId))
    else if flightBookingSnapshot.unitPriceSnapshot.currency != orderCurrency then
      Left(OrderError.OrderCurrencyDidNotMatch(orderId, orderCurrency, flightBookingSnapshot.unitPriceSnapshot.currency))
    else Right(())

  private def ensureMatchingCurrency(moneyToCheck: Money): Either[OrderError, Unit] =
    if moneyToCheck.currency == orderCurrency then Right(())
    else Left(OrderError.OrderCurrencyDidNotMatch(orderId, orderCurrency, moneyToCheck.currency))

  private def validateHotelBookingSnapshot(
      hotelBookingSnapshot: HotelBookingSnapshot
  ): Either[OrderError, Unit] =
    if hotelBookingSnapshot.guestTravelerIds.isEmpty then
      Left(OrderError.HotelBookingGuestSelectionWasEmpty(orderId, hotelBookingSnapshot.roomTypeId))
    else if hotelBookingSnapshot.guestTravelerIds.distinct.size != hotelBookingSnapshot.guestTravelerIds.size then
      Left(OrderError.HotelBookingGuestSelectionContainedDuplicates(orderId, hotelBookingSnapshot.roomTypeId))
    else if hotelBookingSnapshot.roomCount.value <= 0 then
      Left(OrderError.HotelBookingRoomCountWasInvalid(orderId, hotelBookingSnapshot.roomTypeId, hotelBookingSnapshot.roomCount))
    else if hotelBookingSnapshot.unitPriceSnapshot.currency != orderCurrency then
      Left(OrderError.OrderCurrencyDidNotMatch(orderId, orderCurrency, hotelBookingSnapshot.unitPriceSnapshot.currency))
    else if hotelBookingSnapshot.totalPriceSnapshot.currency != orderCurrency then
      Left(OrderError.OrderCurrencyDidNotMatch(orderId, orderCurrency, hotelBookingSnapshot.totalPriceSnapshot.currency))
    else Right(())

  private def updatePayment(
      paymentId: PaymentId
  )(paymentUpdater: Payment => Either[OrderError, Payment]): Either[OrderError, Order] =
    orderPayments.indexWhere(_.paymentId == paymentId) match
      case -1 =>
        Left(OrderError.PaymentWasNotFound(orderId, paymentId))
      case paymentIndex =>
        paymentUpdater(orderPayments(paymentIndex)).map { updatedPayment =>
          copy(orderPayments = orderPayments.updated(paymentIndex, updatedPayment))
        }

  private def updateRefund(
      refundId: RefundId
  )(refundUpdater: Refund => Either[OrderError, Refund]): Either[OrderError, Order] =
    orderRefunds.indexWhere(_.refundId == refundId) match
      case -1 =>
        Left(OrderError.RefundWasNotFound(orderId, refundId))
      case refundIndex =>
        refundUpdater(orderRefunds(refundIndex)).map { updatedRefund =>
          copy(orderRefunds = orderRefunds.updated(refundIndex, updatedRefund))
        }

  private def refreshOrderStatusAfterFinancialChange: Order =
    if totalSettledRefundMoney.amount == totalCapturedMoney.amount && totalCapturedMoney.amount >= totalBookedMoney.amount && totalCapturedMoney.amount > 0 then
      copy(
        orderStatus = OrderStatus.Refunded,
        orderLineItems = orderLineItems.map {
          case flightOrderItem: FlightOrderItem => flightOrderItem.markRefundedOrderItem
          case hotelOrderItem: HotelOrderItem   => hotelOrderItem.markRefundedOrderItem
        }
      )
    else if totalSettledRefundMoney.amount > 0 then
      copy(orderStatus = OrderStatus.PartiallyRefunded)
    else if totalCapturedMoney.amount >= totalBookedMoney.amount && totalBookedMoney.amount > 0 then
      val latestCapturedAt = orderPayments.filter(_.paymentStatus == PaymentStatus.Captured).flatMap(_.capturedAt).lastOption
      copy(
        orderStatus = OrderStatus.Confirmed,
        paidAt = paidAt.orElse(latestCapturedAt),
        confirmedAt = confirmedAt.orElse(latestCapturedAt),
        orderLineItems = orderLineItems.map {
          case flightOrderItem: FlightOrderItem => flightOrderItem.markConfirmedOrderItem
          case hotelOrderItem: HotelOrderItem   => hotelOrderItem.markConfirmedOrderItem
        }
      )
    else
      this

object Order:
  def createDraftOrder(
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

  def restorePersistedOrder(
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

enum OrderError(val message: String) extends DomainError:
  case OrderWasNotFound(orderId: OrderId)
      extends OrderError(s"Order '${orderId.value}' was not found")
  case OrderCannotBeSubmittedWithoutItems(orderId: OrderId)
      extends OrderError(s"Order '${orderId.value}' cannot be submitted without any order items")
  case OrderItemsCouldOnlyBeAddedInDraft(orderId: OrderId, currentOrderStatus: OrderStatus)
      extends OrderError(s"Order '${orderId.value}' cannot add items while in status $currentOrderStatus")
  case InvalidOrderStatusTransition(orderId: OrderId, currentOrderStatus: OrderStatus, targetOrderStatus: OrderStatus)
      extends OrderError(s"Order '${orderId.value}' cannot transition from $currentOrderStatus to $targetOrderStatus")
  case OrderCurrencyDidNotMatch(orderId: OrderId, expectedCurrency: Currency, actualCurrency: Currency)
      extends OrderError(
        s"Order '${orderId.value}' expected currency $expectedCurrency but received $actualCurrency"
      )
  case PaymentWasNotAcceptedForOrderStatus(orderId: OrderId, currentOrderStatus: OrderStatus)
      extends OrderError(s"Order '${orderId.value}' does not accept payments while in status $currentOrderStatus")
  case PaymentWasNotFound(orderId: OrderId, paymentId: PaymentId)
      extends OrderError(s"Payment '${paymentId.value}' was not found in order '${orderId.value}'")
  case PaymentCouldNotBeCaptured(paymentId: PaymentId, currentPaymentStatus: PaymentStatus)
      extends OrderError(s"Payment '${paymentId.value}' cannot be captured from status $currentPaymentStatus")
  case PaymentCouldNotBeFailed(paymentId: PaymentId, currentPaymentStatus: PaymentStatus)
      extends OrderError(s"Payment '${paymentId.value}' cannot be failed from status $currentPaymentStatus")
  case RefundWasNotAcceptedForOrderStatus(orderId: OrderId, currentOrderStatus: OrderStatus)
      extends OrderError(s"Order '${orderId.value}' does not accept refunds while in status $currentOrderStatus")
  case RefundExceededRemainingBalance(orderId: OrderId, remainingRefundableMoney: Money, requestedRefundMoney: Money)
      extends OrderError(
        s"Order '${orderId.value}' can refund only ${remainingRefundableMoney.amount} but requested ${requestedRefundMoney.amount}"
      )
  case RefundReasonWasEmpty(refundId: RefundId)
      extends OrderError(s"Refund '${refundId.value}' must have a reason")
  case RefundWasNotFound(orderId: OrderId, refundId: RefundId)
      extends OrderError(s"Refund '${refundId.value}' was not found in order '${orderId.value}'")
  case RefundCouldNotBeApproved(refundId: RefundId, currentRefundStatus: RefundStatus)
      extends OrderError(s"Refund '${refundId.value}' cannot be approved from status $currentRefundStatus")
  case RefundCouldNotBeRejected(refundId: RefundId, currentRefundStatus: RefundStatus)
      extends OrderError(s"Refund '${refundId.value}' cannot be rejected from status $currentRefundStatus")
  case RefundCouldNotBeSettled(refundId: RefundId, currentRefundStatus: RefundStatus)
      extends OrderError(s"Refund '${refundId.value}' cannot be settled from status $currentRefundStatus")
  case OrderCouldNotBeCancelled(orderId: OrderId, currentOrderStatus: OrderStatus)
      extends OrderError(s"Order '${orderId.value}' cannot be cancelled from status $currentOrderStatus")
  case FlightBookingTravelerSelectionWasEmpty(orderId: OrderId, flightId: FlightId)
      extends OrderError(s"Order '${orderId.value}' cannot add flight '${flightId.value}' without any travelers")
  case FlightBookingTravelerSelectionContainedDuplicates(orderId: OrderId, flightId: FlightId)
      extends OrderError(s"Order '${orderId.value}' cannot add flight '${flightId.value}' with duplicate travelers")
  case HotelBookingGuestSelectionWasEmpty(orderId: OrderId, roomTypeId: RoomTypeId)
      extends OrderError(s"Order '${orderId.value}' cannot add room type '${roomTypeId.value}' without any guests")
  case HotelBookingGuestSelectionContainedDuplicates(orderId: OrderId, roomTypeId: RoomTypeId)
      extends OrderError(s"Order '${orderId.value}' cannot add room type '${roomTypeId.value}' with duplicate guests")
  case HotelBookingRoomCountWasInvalid(orderId: OrderId, roomTypeId: RoomTypeId, roomCount: RoomCount)
      extends OrderError(s"Order '${orderId.value}' cannot add room type '${roomTypeId.value}' with room count ${roomCount.value}")
