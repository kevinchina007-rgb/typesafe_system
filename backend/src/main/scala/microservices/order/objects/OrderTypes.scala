package com.typesafe.travel.order.domain

import io.circe.{Decoder, Encoder}

enum OrderStatus:
  case Draft, PendingPayment, Confirmed, PartiallyRefunded, Refunded, Cancelled

object OrderStatus:
  given sourceEncoder: Encoder[OrderStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[OrderStatus] = Decoder.decodeString.emap(decodeEnum(OrderStatus.valueOf))

enum OrderItemStatus:
  case Reserved, Confirmed, Refunded, Cancelled

object OrderItemStatus:
  given sourceEncoder: Encoder[OrderItemStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[OrderItemStatus] = Decoder.decodeString.emap(decodeEnum(OrderItemStatus.valueOf))

enum SupplierReviewStatus:
  case NotSubmitted, PendingSupplierConfirmation, SupplierConfirmed, SupplierRejected

object SupplierReviewStatus:
  given sourceEncoder: Encoder[SupplierReviewStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[SupplierReviewStatus] = Decoder.decodeString.emap(decodeEnum(SupplierReviewStatus.valueOf))

enum SupplierReviewDecisionType:
  case Confirm, Reject

object SupplierReviewDecisionType:
  given sourceEncoder: Encoder[SupplierReviewDecisionType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[SupplierReviewDecisionType] = Decoder.decodeString.emap(decodeEnum(SupplierReviewDecisionType.valueOf))

enum PaymentStatus:
  case Authorized, Captured, Failed

object PaymentStatus:
  given sourceEncoder: Encoder[PaymentStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[PaymentStatus] = Decoder.decodeString.emap(decodeEnum(PaymentStatus.valueOf))

enum RefundStatus:
  case Requested, Approved, Rejected, Settled

object RefundStatus:
  given sourceEncoder: Encoder[RefundStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[RefundStatus] = Decoder.decodeString.emap(decodeEnum(RefundStatus.valueOf))

enum PaymentMethod:
  case Card, BankTransfer, Wallet, LoyaltyPoints

object PaymentMethod:
  given sourceEncoder: Encoder[PaymentMethod] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[PaymentMethod] = Decoder.decodeString.emap(decodeEnum(PaymentMethod.valueOf))

enum OrderType:
  case FlightBooking, HotelBooking, TrainBooking, AttractionBooking, MixedBooking, PendingSelection

object OrderType:
  given sourceEncoder: Encoder[OrderType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[OrderType] = Decoder.decodeString.emap(decodeEnum(OrderType.valueOf))

private def decodeEnum[A](decode: String => A)(value: String): Either[String, A] =
  try Right(decode(value))
  catch case error: IllegalArgumentException => Left(error.getMessage)
