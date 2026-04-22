package com.typesafe.travel.order.domain

enum OrderStatus:
  case Draft, PendingPayment, Confirmed, PartiallyRefunded, Refunded, Cancelled

object OrderStatus:
  def fromText(value: String): OrderStatus =
    value.trim.toLowerCase match
      case "draft" => OrderStatus.Draft
      case "pendingpayment" | "pending_payment" => OrderStatus.PendingPayment
      case "confirmed" => OrderStatus.Confirmed
      case "partiallyrefunded" | "partially_refunded" => OrderStatus.PartiallyRefunded
      case "refunded" => OrderStatus.Refunded
      case "cancelled" | "canceled" => OrderStatus.Cancelled
      case _ => OrderStatus.Draft

enum OrderItemStatus:
  case Reserved, Confirmed, Refunded, Cancelled

object OrderItemStatus:
  def fromText(value: String): OrderItemStatus =
    value.trim.toLowerCase match
      case "confirmed" => OrderItemStatus.Confirmed
      case "refunded" => OrderItemStatus.Refunded
      case "cancelled" | "canceled" => OrderItemStatus.Cancelled
      case _ => OrderItemStatus.Reserved

enum SupplierReviewStatus:
  case NotSubmitted, PendingSupplierConfirmation, SupplierConfirmed, SupplierRejected

object SupplierReviewStatus:
  def fromText(value: String): SupplierReviewStatus =
    value.trim.toLowerCase match
      case "pendingsupplierconfirmation" | "pending_supplier_confirmation" =>
        SupplierReviewStatus.PendingSupplierConfirmation
      case "supplierconfirmed" | "supplier_confirmed" => SupplierReviewStatus.SupplierConfirmed
      case "supplierrejected" | "supplier_rejected" => SupplierReviewStatus.SupplierRejected
      case _ => SupplierReviewStatus.NotSubmitted

enum SupplierReviewDecisionType:
  case Confirm, Reject

object SupplierReviewDecisionType:
  def fromText(value: String): SupplierReviewDecisionType =
    value.trim.toLowerCase match
      case "reject" => SupplierReviewDecisionType.Reject
      case _ => SupplierReviewDecisionType.Confirm

enum PaymentStatus:
  case Authorized, Captured, Failed

object PaymentStatus:
  def fromText(value: String): PaymentStatus =
    value.trim.toLowerCase match
      case "captured" => PaymentStatus.Captured
      case "failed" => PaymentStatus.Failed
      case _ => PaymentStatus.Authorized

enum RefundStatus:
  case Requested, Approved, Rejected, Settled

object RefundStatus:
  def fromText(value: String): RefundStatus =
    value.trim.toLowerCase match
      case "approved" => RefundStatus.Approved
      case "rejected" => RefundStatus.Rejected
      case "settled" => RefundStatus.Settled
      case _ => RefundStatus.Requested

enum PaymentMethod:
  case Card, BankTransfer, Wallet, LoyaltyPoints

object PaymentMethod:
  def fromText(value: String): PaymentMethod =
    value.trim.toLowerCase match
      case "card" => PaymentMethod.Card
      case "banktransfer" | "bank_transfer" | "bank-transfer" => PaymentMethod.BankTransfer
      case "wallet" | "alipay" | "wechatpay" | "wechat-pay" | "nailongpay" | "nailong-pay" =>
        PaymentMethod.Wallet
      case _ => PaymentMethod.LoyaltyPoints

enum OrderType:
  case FlightBooking, HotelBooking, TrainBooking, AttractionBooking, MixedBooking, PendingSelection

object OrderType:
  def fromText(value: String): OrderType =
    value.trim.toLowerCase match
      case "flightbooking" | "flight_booking" => OrderType.FlightBooking
      case "hotelbooking" | "hotel_booking" => OrderType.HotelBooking
      case "trainbooking" | "train_booking" => OrderType.TrainBooking
      case "attractionbooking" | "attraction_booking" => OrderType.AttractionBooking
      case "mixedbooking" | "mixed_booking" => OrderType.MixedBooking
      case _ => OrderType.PendingSelection
