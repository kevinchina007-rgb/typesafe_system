package com.typesafe.travel.api.dto

import com.typesafe.travel.order.domain.*

final case class CreateOrderRequestDto(
    ownerUserId: String,
    travelerId: String,
    orderCurrency: String,
    itemKind: String,
    providerId: String,
    providerLabel: String,
    productId: String,
    referenceCode: String,
    variantLabel: String,
    originCode: String,
    destinationCode: String,
    periodStart: String,
    periodEnd: String,
    quantity: Int,
    bookedAmount: String
)

final case class AuthorizePaymentRequestDto(
    paymentAmount: String,
    paymentCurrency: String,
    paymentMethod: String
)

final case class RequestRefundRequestDto(
    refundAmount: String,
    refundCurrency: String,
    refundReason: String
)

final case class OrderLineItemResponseDto(
    orderItemId: String,
    orderItemKind: String,
    orderItemStatus: String,
    bookedAmount: String,
    bookedCurrency: String,
    summaryLabel: String
)

final case class PaymentResponseDto(
    paymentId: String,
    paymentAmount: String,
    paymentCurrency: String,
    paymentMethod: String,
    paymentStatus: String,
    authorizedAt: String,
    capturedAt: Option[String]
)

final case class RefundResponseDto(
    refundId: String,
    refundAmount: String,
    refundCurrency: String,
    refundReason: String,
    refundStatus: String,
    requestedAt: String,
    approvedAt: Option[String],
    settledAt: Option[String]
)

final case class OrderResponseDto(
    orderId: String,
    buyerUserId: String,
    orderType: String,
    status: String,
    orderCurrency: String,
    totalPrice: String,
    totalCapturedAmount: String,
    totalSettledRefundAmount: String,
    remainingRefundableAmount: String,
    createdAt: String,
    paidAt: Option[String],
    confirmedAt: Option[String],
    completedAt: Option[String],
    cancelledAt: Option[String],
    orderLineItems: List[OrderLineItemResponseDto],
    orderPayments: List[PaymentResponseDto],
    orderRefunds: List[RefundResponseDto]
)

object OrderResponseDto:
  def fromDomain(order: Order): OrderResponseDto =
    OrderResponseDto(
      orderId = order.orderId.value,
      buyerUserId = order.ownerUserId.value,
      orderType = order.orderType.toString,
      status = order.orderStatus.toString,
      orderCurrency = order.orderCurrency.toString,
      totalPrice = order.totalBookedMoney.amount.toString,
      totalCapturedAmount = order.totalCapturedMoney.amount.toString,
      totalSettledRefundAmount = order.totalSettledRefundMoney.amount.toString,
      remainingRefundableAmount = order.remainingRefundableMoney.amount.toString,
      createdAt = order.createdAt.toString,
      paidAt = order.paidAt.map(_.toString),
      confirmedAt = order.confirmedAt.map(_.toString),
      completedAt = order.completedAt.map(_.toString),
      cancelledAt = order.cancelledAt.map(_.toString),
      orderLineItems = order.orderLineItems.toList.map {
        case flightOrderItem: FlightOrderItem =>
          OrderLineItemResponseDto(
            orderItemId = flightOrderItem.orderItemId.value,
            orderItemKind = "flight",
            orderItemStatus = flightOrderItem.orderItemStatus.toString,
            bookedAmount = flightOrderItem.bookedMoney.amount.toString,
            bookedCurrency = flightOrderItem.bookedMoney.currency.toString,
            summaryLabel = s"${flightOrderItem.flightBookingSnapshot.flightNumber.value} ${flightOrderItem.flightBookingSnapshot.departureAirportCode.value}-${flightOrderItem.flightBookingSnapshot.arrivalAirportCode.value}"
          )
        case hotelOrderItem: HotelOrderItem =>
          OrderLineItemResponseDto(
            orderItemId = hotelOrderItem.orderItemId.value,
            orderItemKind = "hotel",
            orderItemStatus = hotelOrderItem.orderItemStatus.toString,
            bookedAmount = hotelOrderItem.bookedMoney.amount.toString,
            bookedCurrency = hotelOrderItem.bookedMoney.currency.toString,
            summaryLabel = s"${hotelOrderItem.hotelBookingSnapshot.hotelName.value} ${hotelOrderItem.hotelBookingSnapshot.roomTypeName.value}"
          )
      },
      orderPayments = order.orderPayments.toList.map(payment =>
        PaymentResponseDto(
          paymentId = payment.paymentId.value,
          paymentAmount = payment.paymentAmount.amount.toString,
          paymentCurrency = payment.paymentAmount.currency.toString,
          paymentMethod = payment.paymentMethod.toString,
          paymentStatus = payment.paymentStatus.toString,
          authorizedAt = payment.authorizedAt.toString,
          capturedAt = payment.capturedAt.map(_.toString)
        )
      ),
      orderRefunds = order.orderRefunds.toList.map(refund =>
        RefundResponseDto(
          refundId = refund.refundId.value,
          refundAmount = refund.refundAmount.amount.toString,
          refundCurrency = refund.refundAmount.currency.toString,
          refundReason = refund.refundReason,
          refundStatus = refund.refundStatus.toString,
          requestedAt = refund.requestedAt.toString,
          approvedAt = refund.approvedAt.map(_.toString),
          settledAt = refund.settledAt.map(_.toString)
        )
      )
    )

object OrderDtoMappers:
  def toCurrency(currencyValue: String) =
    currencyValue.trim.toUpperCase match
      case "USD" => com.typesafe.travel.shared.kernel.Currency.USD
      case "EUR" => com.typesafe.travel.shared.kernel.Currency.EUR
      case _     => com.typesafe.travel.shared.kernel.Currency.CNY

  def toPaymentMethod(paymentMethodValue: String): PaymentMethod =
    paymentMethodValue.trim.toLowerCase match
      case "card"          => PaymentMethod.Card
      case "bank-transfer" => PaymentMethod.BankTransfer
      case "wallet"        => PaymentMethod.Wallet
      case _               => PaymentMethod.LoyaltyPoints

object TravelerDtoMappers:
  def toTravelerDocumentType(documentTypeValue: String): com.typesafe.travel.traveler.domain.TravelerDocumentType =
    documentTypeValue.trim.toLowerCase match
      case "identity-card"  => com.typesafe.travel.traveler.domain.TravelerDocumentType.NationalIdentityCard
      case "residence-permit" => com.typesafe.travel.traveler.domain.TravelerDocumentType.ResidencePermit
      case "other" => com.typesafe.travel.traveler.domain.TravelerDocumentType.OtherGovernmentDocument
      case _ => com.typesafe.travel.traveler.domain.TravelerDocumentType.Passport

  def toSeatPreference(seatPreferenceValue: String): com.typesafe.travel.traveler.domain.SeatPreference =
    seatPreferenceValue.trim.toLowerCase match
      case "window" => com.typesafe.travel.traveler.domain.SeatPreference.Window
      case "aisle"  => com.typesafe.travel.traveler.domain.SeatPreference.Aisle
      case "middle" => com.typesafe.travel.traveler.domain.SeatPreference.Middle
      case _        => com.typesafe.travel.traveler.domain.SeatPreference.NoPreference

  def toMealPreference(mealPreferenceValue: String): com.typesafe.travel.traveler.domain.MealPreference =
    mealPreferenceValue.trim.toLowerCase match
      case "vegetarian" => com.typesafe.travel.traveler.domain.MealPreference.Vegetarian
      case "vegan"      => com.typesafe.travel.traveler.domain.MealPreference.Vegan
      case "halal"      => com.typesafe.travel.traveler.domain.MealPreference.Halal
      case "kosher"     => com.typesafe.travel.traveler.domain.MealPreference.Kosher
      case "childmeal"  => com.typesafe.travel.traveler.domain.MealPreference.ChildMeal
      case "standard"   => com.typesafe.travel.traveler.domain.MealPreference.Standard
      case _            => com.typesafe.travel.traveler.domain.MealPreference.NoPreference
