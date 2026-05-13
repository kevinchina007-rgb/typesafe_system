package com.typesafe.travel.api.application

import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*

def bookedMoney(orderLineItem: OrderLineItem): Money =
  orderLineItem match
    case flightOrderItem: FlightOrderItem =>
      multiplyMoney(
        flightOrderItem.flightBookingSnapshot.unitPriceSnapshot,
        flightOrderItem.flightBookingSnapshot.travelerIds.size.toLong
      )
    case hotelOrderItem: HotelOrderItem =>
      multiplyMoney(
        hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot,
        hotelOrderItem.hotelBookingSnapshot.roomCount.value.toLong *
          hotelOrderItem.hotelBookingSnapshot.stayPeriod.stayNightCount
      )
    case trainOrderItem: TrainOrderItem =>
      multiplyMoney(
        trainOrderItem.trainBookingSnapshot.unitPriceSnapshot,
        trainOrderItem.trainBookingSnapshot.travelerIds.size.toLong
      )
    case attractionOrderItem: AttractionOrderItem =>
      multiplyMoney(
        attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot,
        attractionOrderItem.attractionTicketSnapshot.travelerIds.size.toLong
      )

def totalBookedMoney(order: Order): Money =
  order.orderLineItems.foldLeft(Money.zero(order.orderCurrency)) { (totalMoney, orderLineItem) =>
    totalMoney.add(bookedMoney(orderLineItem)).fold(throw _, identity)
  }

def totalCapturedMoney(order: Order): Money =
  order.orderPayments
    .filter(_.paymentStatus == PaymentStatus.Captured)
    .foldLeft(Money.zero(order.orderCurrency)) { (totalMoney, payment) =>
      totalMoney.add(payment.paymentAmount).fold(throw _, identity)
    }

def totalSettledRefundMoney(order: Order): Money =
  order.orderRefunds
    .filter(_.refundStatus == RefundStatus.Settled)
    .foldLeft(Money.zero(order.orderCurrency)) { (totalMoney, refund) =>
      totalMoney.add(refund.refundAmount).fold(throw _, identity)
    }

def remainingRefundableMoney(order: Order): Money =
  totalCapturedMoney(order).subtract(totalSettledRefundMoney(order)).fold(throw _, identity)

def hasCapturedPayment(order: Order): Boolean =
  order.orderPayments.exists(_.paymentStatus == PaymentStatus.Captured)

def allSupplierReviewDecisionsConfirmed(order: Order): Boolean =
  val reviewableItems = order.orderLineItems.collect {
    case flightOrderItem: FlightOrderItem         => flightOrderItem.supplierReviewStatus
    case hotelOrderItem: HotelOrderItem           => hotelOrderItem.supplierReviewStatus
    case attractionOrderItem: AttractionOrderItem => attractionOrderItem.supplierReviewStatus
  }
  reviewableItems.isEmpty || reviewableItems.forall(_ == SupplierReviewStatus.SupplierConfirmed)

def orderType(order: Order): OrderType =
  val itemKinds = order.orderLineItems.map {
    case _: FlightOrderItem     => OrderType.FlightBooking
    case _: HotelOrderItem      => OrderType.HotelBooking
    case _: TrainOrderItem      => OrderType.TrainBooking
    case _: AttractionOrderItem => OrderType.AttractionBooking
  }.distinct

  itemKinds.toList match
    case Nil        => OrderType.PendingSelection
    case one :: Nil => one
    case _          => OrderType.MixedBooking

def paymentMethodFromText(paymentMethodValue: String): PaymentMethod =
  paymentMethodValue.trim.toLowerCase.replace("_", "-").replace(" ", "-") match
    case "card" | "credit-card" | "debit-card" => PaymentMethod.Card
    case "bank-transfer" | "bank"              => PaymentMethod.BankTransfer
    case "wallet"                              => PaymentMethod.Wallet
    case "loyalty-points" | "points"           => PaymentMethod.LoyaltyPoints
    case _                                     => PaymentMethod.Card

private def multiplyMoney(money: Money, multiplier: Long): Money =
  Money.unsafe(money.amount * BigDecimal(multiplier.max(0L)), money.currency)
