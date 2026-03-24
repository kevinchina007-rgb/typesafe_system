package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

enum OrderStatus:
  case Draft, PendingPayment, Confirmed, PartiallyRefunded, Refunded, Cancelled

enum OrderItemStatus:
  case Reserved, Confirmed, Cancelled, Refunded

enum PaymentStatus:
  case Pending, Authorized, Captured, Failed, Refunded

enum RefundStatus:
  case Requested, Approved, Rejected, Settled

enum PaymentMethod:
  case Card, BankTransfer, Wallet, LoyaltyPoints

sealed trait OrderItem:
  def id: OrderItemId
  def totalPrice: Money
  def status: OrderItemStatus

final case class FlightBookingSnapshot(
    airlineId: AirlineId,
    flightId: FlightId,
    flightNumber: FlightNumber,
    schedule: FlightSchedule,
    departureAirport: AirportCode,
    arrivalAirport: AirportCode,
    cabinCode: CabinCode,
    travelerId: TravelerId
)

final case class HotelBookingSnapshot(
    hotelId: HotelId,
    hotelName: HotelName,
    roomTypeId: RoomTypeId,
    roomTypeName: RoomTypeName,
    stayPeriod: StayPeriod,
    guestCount: Capacity
)

final case class FlightOrderItem(
    id: OrderItemId,
    snapshot: FlightBookingSnapshot,
    totalPrice: Money,
    status: OrderItemStatus
) extends OrderItem

final case class HotelOrderItem(
    id: OrderItemId,
    snapshot: HotelBookingSnapshot,
    totalPrice: Money,
    status: OrderItemStatus
) extends OrderItem

final case class Payment(
    id: PaymentId,
    amount: Money,
    method: PaymentMethod,
    status: PaymentStatus,
    capturedAt: Option[Instant]
)

final case class Refund(
    id: RefundId,
    amount: Money,
    reason: String,
    status: RefundStatus,
    requestedAt: Instant
):
  require(reason.trim.nonEmpty, "Refund reason must be non-empty")

final case class Order(
    id: OrderId,
    userId: UserId,
    status: OrderStatus,
    items: Vector[OrderItem],
    payments: Vector[Payment],
    refunds: Vector[Refund],
    createdAt: Instant
):
  def currency: Option[Currency] =
    items.headOption.map(_.totalPrice.currency).orElse(payments.headOption.map(_.amount.currency))

  def totalAmount: Option[Money] =
    items.headOption.map { first =>
      items.foldLeft(Money.zero(first.totalPrice.currency))(_ + _.totalPrice)
    }

  def paidAmount: Option[Money] =
    capturedPayments.headOption.map { first =>
      capturedPayments.foldLeft(Money.zero(first.amount.currency))(_ + _.amount)
    }

  def refundedAmount: Option[Money] =
    settledRefunds.headOption.map { first =>
      settledRefunds.foldLeft(Money.zero(first.amount.currency))(_ + _.amount)
    }

  def addItem(item: OrderItem): Either[OrderDomainError, Order] =
    status match
      case OrderStatus.Draft =>
        currency match
          case Some(existingCurrency) if existingCurrency != item.totalPrice.currency =>
            Left(OrderDomainError.CurrencyMismatch(id))
          case _ =>
            Right(copy(items = items :+ item))
      case _ =>
        Left(OrderDomainError.InvalidOrderTransition(id, status, OrderStatus.Draft))

  def submit: Either[OrderDomainError, Order] =
    status match
      case OrderStatus.Draft if items.isEmpty =>
        Left(OrderDomainError.EmptyOrder(id))
      case OrderStatus.Draft =>
        Right(copy(status = OrderStatus.PendingPayment))
      case _ =>
        Left(OrderDomainError.InvalidOrderTransition(id, status, OrderStatus.PendingPayment))

  def recordPayment(payment: Payment): Either[OrderDomainError, Order] =
    status match
      case OrderStatus.PendingPayment | OrderStatus.Confirmed =>
        val orderCurrency = totalAmount.map(_.currency).getOrElse(payment.amount.currency)
        if orderCurrency != payment.amount.currency then
          Left(OrderDomainError.CurrencyMismatch(id))
        else
          val updated = copy(payments = payments :+ payment)
          Right(updated.refreshFinancialStatus)
      case _ =>
        Left(OrderDomainError.InvalidPaymentState(id, status))

  def recordRefund(refund: Refund): Either[OrderDomainError, Order] =
    status match
      case OrderStatus.Confirmed | OrderStatus.PartiallyRefunded =>
        if refund.amount.currency != totalAmount.map(_.currency).getOrElse(refund.amount.currency) then
          Left(OrderDomainError.CurrencyMismatch(id))
        else if refund.amount.amount > refundableBalance.amount then
          Left(OrderDomainError.RefundExceedsPaidAmount(id))
        else
          val updated = copy(refunds = refunds :+ refund)
          Right(updated.refreshFinancialStatus)
      case _ =>
        Left(OrderDomainError.InvalidRefundState(id, status))

  def cancel: Either[OrderDomainError, Order] =
    status match
      case OrderStatus.Draft | OrderStatus.PendingPayment if capturedPayments.isEmpty =>
        Right(copy(status = OrderStatus.Cancelled))
      case _ =>
        Left(OrderDomainError.InvalidOrderTransition(id, status, OrderStatus.Cancelled))

  def refundableBalance: Money =
    val orderCurrency = totalAmount.map(_.currency).orElse(paidAmount.map(_.currency)).getOrElse(Currency.USD)
    paidAmount.getOrElse(Money.zero(orderCurrency)) - refundedAmount.getOrElse(Money.zero(orderCurrency))

  private def capturedPayments: Vector[Payment] =
    payments.filter(_.status == PaymentStatus.Captured)

  private def settledRefunds: Vector[Refund] =
    refunds.filter(_.status == RefundStatus.Settled)

  private def refreshFinancialStatus: Order =
    val total = totalAmount
    val paid = paidAmount
    val refunded = refundedAmount

    val nextStatus =
      (total, paid, refunded) match
        case (Some(orderTotal), Some(paidTotal), Some(refundedTotal)) if refundedTotal.amount == paidTotal.amount && paidTotal.amount >= orderTotal.amount =>
          OrderStatus.Refunded
        case (Some(_), Some(_), Some(refundedTotal)) if refundedTotal.amount > 0 =>
          OrderStatus.PartiallyRefunded
        case (Some(orderTotal), Some(paidTotal), _) if paidTotal.amount >= orderTotal.amount =>
          OrderStatus.Confirmed
        case _ =>
          status

    copy(status = nextStatus)

object Order:
  def draft(
      id: OrderId,
      userId: UserId,
      createdAt: Instant
  ): Order =
    Order(
      id = id,
      userId = userId,
      status = OrderStatus.Draft,
      items = Vector.empty,
      payments = Vector.empty,
      refunds = Vector.empty,
      createdAt = createdAt
    )

enum OrderDomainError(val message: String) extends DomainError:
  case OrderNotFound(id: OrderId)
      extends OrderDomainError(s"Order ${id.value} was not found")
  case EmptyOrder(id: OrderId)
      extends OrderDomainError(s"Order ${id.value} cannot be submitted without any order items")
  case CurrencyMismatch(id: OrderId)
      extends OrderDomainError(s"Order ${id.value} requires all financial records to use the same currency")
  case InvalidOrderTransition(id: OrderId, from: OrderStatus, to: OrderStatus)
      extends OrderDomainError(s"Order ${id.value} cannot transition from $from to $to")
  case InvalidPaymentState(id: OrderId, status: OrderStatus)
      extends OrderDomainError(s"Order ${id.value} does not accept payments while in status $status")
  case InvalidRefundState(id: OrderId, status: OrderStatus)
      extends OrderDomainError(s"Order ${id.value} does not accept refunds while in status $status")
  case RefundExceedsPaidAmount(id: OrderId)
      extends OrderDomainError(s"Order ${id.value} refund exceeds the paid and unsettled amount")
