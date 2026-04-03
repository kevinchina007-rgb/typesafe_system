package com.typesafe.travel.api.dto

import com.typesafe.travel.order.domain.*
import com.typesafe.travel.inventory.domain.*
import com.typesafe.travel.shared.kernel.CabinClass
import java.time.LocalDate

final case class CreateOrderRequestDto(
    ownerUserId: String,
    orderCurrency: String
)

final case class AuthorizePaymentRequestDto(
    paymentAmount: String,
    paymentCurrency: String,
    paymentMethod: String
)

final case class RequestRefundRequestDto(
    refundReason: String
)

final case class PayOrderRequestDto(
    paymentMethod: String,
    paymentSucceeded: Boolean
)

final case class FlightItemDetailsResponseDto(
    airlineName: String,
    airlineCode: String,
    flightId: String,
    flightNumber: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    cabinClass: String,
    travelerIds: List[String],
    reservationStatus: Option[String],
    reservationExpiresAt: Option[String],
    unitPrice: String,
    currency: String
)

final case class HotelItemDetailsResponseDto(
    hotelId: String,
    hotelName: String,
    location: String,
    roomTypeId: String,
    roomTypeName: String,
    checkInDate: String,
    checkOutDate: String,
    guestTravelerIds: List[String],
    roomCount: Int,
    reservationStatus: Option[String],
    reservationExpiresAt: Option[String],
    unitPrice: String,
    totalPrice: String,
    currency: String
)

final case class TrainItemDetailsResponseDto(
    trainId: String,
    trainNumber: String,
    fromStationCode: String,
    fromStationName: String,
    toStationCode: String,
    toStationName: String,
    departureTime: String,
    arrivalTime: String,
    seatClass: String,
    requestedSeatPreference: Option[String],
    seatAssignments: List[TrainSeatAssignmentResponseDto],
    travelerIds: List[String],
    reservationStatus: Option[String],
    reservationExpiresAt: Option[String],
    unitPrice: String,
    totalPrice: String,
    currency: String
)

final case class AttractionItemDetailsResponseDto(
    attractionId: String,
    attractionName: String,
    ticketTypeId: String,
    ticketTypeName: String,
    sessionId: Option[String],
    sessionName: Option[String],
    sessionStartsAt: Option[String],
    sessionEndsAt: Option[String],
    useDate: String,
    travelerIds: List[String],
    unitPrice: String,
    totalPrice: String,
    currency: String,
    ruleSummaries: List[String]
)

final case class TrainSeatAssignmentResponseDto(
    travelerId: String,
    seatId: String,
    carriageNo: Int,
    seatNo: String,
    seatLabel: String,
    seatPositionType: String
)

final case class OrderLineItemResponseDto(
    orderItemId: String,
    orderItemKind: String,
    orderItemStatus: String,
    supplierReviewStatus: String,
    supplierReviewDecision: Option[SupplierReviewDecisionResponseDto],
    bookedAmount: String,
    bookedCurrency: String,
    summaryLabel: String,
    flightDetails: Option[FlightItemDetailsResponseDto],
    hotelDetails: Option[HotelItemDetailsResponseDto],
    trainDetails: Option[TrainItemDetailsResponseDto],
    attractionDetails: Option[AttractionItemDetailsResponseDto]
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

final case class OrderListResponseDto(
    orders: List[OrderResponseDto]
)

object OrderResponseDto:
  def fromDomain(order: Order, inventoryReservations: List[InventoryReservation] = Nil): OrderResponseDto =
    OrderResponseDto(
      orderId = order.orderId.value,
      buyerUserId = order.ownerUserId.value,
      orderType = order.orderType.toString,
      status = deriveCustomerFacingOrderStatus(order),
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
            supplierReviewStatus = flightOrderItem.supplierReviewStatus.toString,
            supplierReviewDecision = flightOrderItem.supplierReviewDecision.map(SupplierReviewDecisionResponseDto.fromDomain),
            bookedAmount = flightOrderItem.bookedMoney.amount.toString,
            bookedCurrency = flightOrderItem.bookedMoney.currency.toString,
            summaryLabel =
              s"${flightOrderItem.flightBookingSnapshot.airlineName.value} ${flightOrderItem.flightBookingSnapshot.flightNumber.value} ${flightOrderItem.flightBookingSnapshot.departureAirportCode.value}-${flightOrderItem.flightBookingSnapshot.arrivalAirportCode.value}",
            flightDetails = Some(
              FlightItemDetailsResponseDto(
                airlineName = flightOrderItem.flightBookingSnapshot.airlineName.value,
                airlineCode = flightOrderItem.flightBookingSnapshot.airlineCode.value,
                flightId = flightOrderItem.flightBookingSnapshot.flightId.value,
                flightNumber = flightOrderItem.flightBookingSnapshot.flightNumber.value,
                departureAirport = flightOrderItem.flightBookingSnapshot.departureAirportCode.value,
                arrivalAirport = flightOrderItem.flightBookingSnapshot.arrivalAirportCode.value,
                departureTime = flightOrderItem.flightBookingSnapshot.flightSchedule.departureAt.toString,
                arrivalTime = flightOrderItem.flightBookingSnapshot.flightSchedule.arrivalAt.toString,
                cabinClass = flightOrderItem.flightBookingSnapshot.cabinClass.value,
                travelerIds = flightOrderItem.flightBookingSnapshot.travelerIds.map(_.value).toList,
                reservationStatus =
                  inventoryReservations.find(_.orderItemId == flightOrderItem.orderItemId).map(_.reservationStatus.toString),
                reservationExpiresAt =
                  inventoryReservations.find(_.orderItemId == flightOrderItem.orderItemId).map(_.expiresAt.toString),
                unitPrice = flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.amount.toString,
                currency = flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.currency.toString
              )
            ),
            hotelDetails = None,
            trainDetails = None,
            attractionDetails = None
          )
        case hotelOrderItem: HotelOrderItem =>
          OrderLineItemResponseDto(
            orderItemId = hotelOrderItem.orderItemId.value,
            orderItemKind = "hotel",
            orderItemStatus = hotelOrderItem.orderItemStatus.toString,
            supplierReviewStatus = hotelOrderItem.supplierReviewStatus.toString,
            supplierReviewDecision = hotelOrderItem.supplierReviewDecision.map(SupplierReviewDecisionResponseDto.fromDomain),
            bookedAmount = hotelOrderItem.bookedMoney.amount.toString,
            bookedCurrency = hotelOrderItem.bookedMoney.currency.toString,
            summaryLabel =
              s"${hotelOrderItem.hotelBookingSnapshot.hotelName.value} ${hotelOrderItem.hotelBookingSnapshot.roomTypeName.value}",
            flightDetails = None,
            hotelDetails = Some(
              HotelItemDetailsResponseDto(
                hotelId = hotelOrderItem.hotelBookingSnapshot.hotelId.value,
                hotelName = hotelOrderItem.hotelBookingSnapshot.hotelName.value,
                location = hotelOrderItem.hotelBookingSnapshot.hotelLocation.value,
                roomTypeId = hotelOrderItem.hotelBookingSnapshot.roomTypeId.value,
                roomTypeName = hotelOrderItem.hotelBookingSnapshot.roomTypeName.value,
                checkInDate = hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkIn.toString,
                checkOutDate = hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkOut.toString,
                guestTravelerIds = hotelOrderItem.hotelBookingSnapshot.guestTravelerIds.map(_.value).toList,
                roomCount = hotelOrderItem.hotelBookingSnapshot.roomCount.value,
                reservationStatus =
                  inventoryReservations.find(_.orderItemId == hotelOrderItem.orderItemId).map(_.reservationStatus.toString),
                reservationExpiresAt =
                  inventoryReservations.find(_.orderItemId == hotelOrderItem.orderItemId).map(_.expiresAt.toString),
                unitPrice = hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.amount.toString,
                totalPrice = hotelOrderItem.bookedMoney.amount.toString,
                currency = hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.currency.toString
              )
            ),
            trainDetails = None,
            attractionDetails = None
          )
        case trainOrderItem: TrainOrderItem =>
          OrderLineItemResponseDto(
            orderItemId = trainOrderItem.orderItemId.value,
            orderItemKind = "train",
            orderItemStatus = trainOrderItem.orderItemStatus.toString,
            supplierReviewStatus = trainOrderItem.supplierReviewStatus.toString,
            supplierReviewDecision = None,
            bookedAmount = trainOrderItem.bookedMoney.amount.toString,
            bookedCurrency = trainOrderItem.bookedMoney.currency.toString,
            summaryLabel =
              s"${trainOrderItem.trainBookingSnapshot.trainNumber.value} ${trainOrderItem.trainBookingSnapshot.fromStationName.value}-${trainOrderItem.trainBookingSnapshot.toStationName.value}",
            flightDetails = None,
            hotelDetails = None,
            trainDetails = Some(
              TrainItemDetailsResponseDto(
                trainId = trainOrderItem.trainBookingSnapshot.trainId.value,
                trainNumber = trainOrderItem.trainBookingSnapshot.trainNumber.value,
                fromStationCode = trainOrderItem.trainBookingSnapshot.fromStationCode.value,
                fromStationName = trainOrderItem.trainBookingSnapshot.fromStationName.value,
                toStationCode = trainOrderItem.trainBookingSnapshot.toStationCode.value,
                toStationName = trainOrderItem.trainBookingSnapshot.toStationName.value,
                departureTime = trainOrderItem.trainBookingSnapshot.departureTime.toString,
                arrivalTime = trainOrderItem.trainBookingSnapshot.arrivalTime.toString,
                seatClass = trainOrderItem.trainBookingSnapshot.seatClass.value,
                requestedSeatPreference = trainOrderItem.trainBookingSnapshot.requestedSeatPreference.map(_.toString),
                seatAssignments = trainOrderItem.trainBookingSnapshot.seatAssignments.map(assignment =>
                  TrainSeatAssignmentResponseDto(
                    travelerId = assignment.travelerId.value,
                    seatId = assignment.seatId.value,
                    carriageNo = assignment.carriageNo,
                    seatNo = assignment.seatNo,
                    seatLabel = assignment.seatLabel,
                    seatPositionType = assignment.seatPositionType.toString
                  )
                ).toList,
                travelerIds = trainOrderItem.trainBookingSnapshot.travelerIds.map(_.value).toList,
                reservationStatus = inventoryReservations.find(_.orderItemId == trainOrderItem.orderItemId).map(_.reservationStatus.toString),
                reservationExpiresAt = inventoryReservations.find(_.orderItemId == trainOrderItem.orderItemId).map(_.expiresAt.toString),
                unitPrice = trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.amount.toString,
                totalPrice = trainOrderItem.bookedMoney.amount.toString,
                currency = trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.currency.toString
              )
            ),
            attractionDetails = None
          )
        case attractionOrderItem: AttractionOrderItem =>
          OrderLineItemResponseDto(
            orderItemId = attractionOrderItem.orderItemId.value,
            orderItemKind = "attraction",
            orderItemStatus = attractionOrderItem.orderItemStatus.toString,
            supplierReviewStatus = attractionOrderItem.supplierReviewStatus.toString,
            supplierReviewDecision = attractionOrderItem.supplierReviewDecision.map(SupplierReviewDecisionResponseDto.fromDomain),
            bookedAmount = attractionOrderItem.bookedMoney.amount.toString,
            bookedCurrency = attractionOrderItem.bookedMoney.currency.toString,
            summaryLabel = s"${attractionOrderItem.attractionTicketSnapshot.attractionName} ${attractionOrderItem.attractionTicketSnapshot.ticketTypeName}",
            flightDetails = None,
            hotelDetails = None,
            trainDetails = None,
            attractionDetails = Some(
              AttractionItemDetailsResponseDto(
                attractionId = attractionOrderItem.attractionTicketSnapshot.attractionId.value,
                attractionName = attractionOrderItem.attractionTicketSnapshot.attractionName,
                ticketTypeId = attractionOrderItem.attractionTicketSnapshot.ticketTypeId.value,
                ticketTypeName = attractionOrderItem.attractionTicketSnapshot.ticketTypeName,
                sessionId = attractionOrderItem.attractionTicketSnapshot.sessionId.map(_.value),
                sessionName = attractionOrderItem.attractionTicketSnapshot.sessionName,
                sessionStartsAt = attractionOrderItem.attractionTicketSnapshot.sessionStartsAt.map(_.toString),
                sessionEndsAt = attractionOrderItem.attractionTicketSnapshot.sessionEndsAt.map(_.toString),
                useDate = attractionOrderItem.attractionTicketSnapshot.useDate.toString,
                travelerIds = attractionOrderItem.attractionTicketSnapshot.travelerIds.map(_.value).toList,
                unitPrice = attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.amount.toString,
                totalPrice = attractionOrderItem.bookedMoney.amount.toString,
                currency = attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.currency.toString,
                ruleSummaries = attractionOrderItem.attractionTicketSnapshot.ruleSummaries.toList
              )
            )
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

  private def deriveCustomerFacingOrderStatus(order: Order): String =
    if order.orderStatus == OrderStatus.Cancelled then "Cancelled"
    else if order.orderStatus == OrderStatus.Refunded || order.totalSettledRefundMoney.amount >= order.totalBookedMoney.amount && order.totalBookedMoney.amount > 0 then "Refunded"
    else if order.orderRefunds.exists(_.refundStatus == RefundStatus.Requested) && order.allSupplierReviewDecisionsConfirmed then "PendingRefund"
    else if order.hasCapturedPayment && order.allSupplierReviewDecisionsConfirmed then "Booked"
    else if order.hasCapturedPayment then "Paid"
    else "PendingPayment"

object OrderDtoMappers:
  def toCurrency(currencyValue: String) =
    currencyValue.trim.toUpperCase match
      case "USD" => com.typesafe.travel.shared.kernel.Currency.USD
      case "EUR" => com.typesafe.travel.shared.kernel.Currency.EUR
      case _     => com.typesafe.travel.shared.kernel.Currency.CNY

  def toPaymentMethod(paymentMethodValue: String): PaymentMethod =
    paymentMethodValue.trim.toLowerCase match
      case "alipay"       => PaymentMethod.Wallet
      case "wechat-pay"   => PaymentMethod.Wallet
      case "nailong-pay"  => PaymentMethod.Wallet
      case "card"          => PaymentMethod.Card
      case "bank-transfer" => PaymentMethod.BankTransfer
      case "wallet"        => PaymentMethod.Wallet
      case _               => PaymentMethod.LoyaltyPoints

  def toCabinClass(cabinClassValue: String) =
    CabinClass.create(cabinClassValue)

object TravelerDtoMappers:
  def toTravelerDocumentType(documentTypeValue: String): com.typesafe.travel.traveler.domain.TravelerDocumentType =
    documentTypeValue.trim.toLowerCase match
      case "identity-card"    => com.typesafe.travel.traveler.domain.TravelerDocumentType.NationalIdentityCard
      case "residence-permit" => com.typesafe.travel.traveler.domain.TravelerDocumentType.ResidencePermit
      case "other"            => com.typesafe.travel.traveler.domain.TravelerDocumentType.OtherGovernmentDocument
      case _                  => com.typesafe.travel.traveler.domain.TravelerDocumentType.Passport

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
