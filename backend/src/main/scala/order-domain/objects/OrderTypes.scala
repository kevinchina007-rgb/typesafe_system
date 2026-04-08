package com.typesafe.travel.order.domain

enum OrderStatus:
  case Draft, PendingPayment, Confirmed, PartiallyRefunded, Refunded, Cancelled

enum OrderItemStatus:
  case Reserved, Confirmed, Refunded, Cancelled

enum SupplierReviewStatus:
  case NotSubmitted, PendingSupplierConfirmation, SupplierConfirmed, SupplierRejected

enum SupplierReviewDecisionType:
  case Confirm, Reject

enum PaymentStatus:
  case Authorized, Captured, Failed

enum RefundStatus:
  case Requested, Approved, Rejected, Settled

enum PaymentMethod:
  case Card, BankTransfer, Wallet, LoyaltyPoints

enum OrderType:
  case FlightBooking, HotelBooking, TrainBooking, AttractionBooking, MixedBooking, PendingSelection
