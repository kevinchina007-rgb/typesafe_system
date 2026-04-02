package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.train.domain.*
import java.time.Instant

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

sealed trait OrderLineItem:
  def orderItemId: OrderItemId
  def bookedMoney: Money
  def orderItemStatus: OrderItemStatus
  def supplierReviewStatus: SupplierReviewStatus
  def supplierReviewDecision: Option[SupplierReviewDecision]

final case class SupplierReviewDecision(
    decision: SupplierReviewDecisionType,
    reason: Option[String],
    decidedAt: Instant,
    managerId: ManagerId
)

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
    unitPriceSnapshot: Money
)

final case class TrainBookingSnapshot(
    trainId: TrainId,
    trainNumber: TrainNumber,
    fromStopId: TrainStopId,
    fromStationCode: TrainStationCode,
    fromStationName: TrainStationName,
    toStopId: TrainStopId,
    toStationCode: TrainStationCode,
    toStationName: TrainStationName,
    departureTime: Instant,
    arrivalTime: Instant,
    seatInventoryId: TrainSeatInventoryId,
    seatClass: TrainSeatClass,
    travelerIds: Vector[TravelerId],
    saleStartsAt: Instant,
    unitPriceSnapshot: Money
)

final case class AttractionTicketSnapshot(
    attractionId: AttractionId,
    managerId: ManagerId,
    attractionName: String,
    ticketTypeId: TicketTypeId,
    ticketTypeName: String,
    useDate: java.time.LocalDate,
    travelerIds: Vector[TravelerId],
    unitPriceSnapshot: Money,
    ruleSummaries: Vector[String],
    eligibilityValidatedAt: Instant
)

final case class FlightOrderItem private[domain] (
    orderItemId: OrderItemId,
    flightBookingSnapshot: FlightBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem:
  override def bookedMoney: Money =
    flightBookingSnapshot.unitPriceSnapshot
      .multiply(flightBookingSnapshot.travelerIds.size)
      .fold(throw _, identity)

  def markConfirmedOrderItem: FlightOrderItem =
    copy(
      orderItemStatus = OrderItemStatus.Confirmed,
      supplierReviewStatus =
        supplierReviewStatus match
          case SupplierReviewStatus.NotSubmitted => SupplierReviewStatus.PendingSupplierConfirmation
          case otherSupplierReviewStatus         => otherSupplierReviewStatus
    )

  def markRefundedOrderItem: FlightOrderItem =
    copy(orderItemStatus = OrderItemStatus.Refunded)

  def markCancelledOrderItem: FlightOrderItem =
    copy(orderItemStatus = OrderItemStatus.Cancelled)

  def markSupplierConfirmed(managerId: ManagerId, note: Option[String], decidedAt: Instant): Either[OrderError, FlightOrderItem] =
    supplierReviewStatus match
      case SupplierReviewStatus.PendingSupplierConfirmation =>
        Right(
          copy(
            supplierReviewStatus = SupplierReviewStatus.SupplierConfirmed,
            supplierReviewDecision = Some(
              SupplierReviewDecision(
                decision = SupplierReviewDecisionType.Confirm,
                reason = note.map(_.trim).filter(_.nonEmpty),
                decidedAt = decidedAt,
                managerId = managerId
              )
            )
          )
        )
      case _ =>
        Left(OrderError.OrderItemWasNotAwaitingSupplierDecision(orderItemId, supplierReviewStatus))

  def markSupplierRejected(managerId: ManagerId, reason: String, decidedAt: Instant): Either[OrderError, FlightOrderItem] =
    val normalizedReason = reason.trim
    if normalizedReason.isEmpty then Left(OrderError.SupplierRejectReasonWasEmpty(orderItemId))
    else
      supplierReviewStatus match
        case SupplierReviewStatus.PendingSupplierConfirmation =>
          Right(
            copy(
              supplierReviewStatus = SupplierReviewStatus.SupplierRejected,
              supplierReviewDecision = Some(
                SupplierReviewDecision(
                  decision = SupplierReviewDecisionType.Reject,
                  reason = Some(normalizedReason),
                  decidedAt = decidedAt,
                  managerId = managerId
                )
              )
            )
          )
        case _ =>
          Left(OrderError.OrderItemWasNotAwaitingSupplierDecision(orderItemId, supplierReviewStatus))

final case class HotelOrderItem private[domain] (
    orderItemId: OrderItemId,
    hotelBookingSnapshot: HotelBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem:
  override def bookedMoney: Money =
    hotelBookingSnapshot.unitPriceSnapshot
      .multiply(hotelBookingSnapshot.roomCount.value)
      .fold(throw _, identity)

  def markConfirmedOrderItem: HotelOrderItem =
    copy(
      orderItemStatus = OrderItemStatus.Confirmed,
      supplierReviewStatus =
        supplierReviewStatus match
          case SupplierReviewStatus.NotSubmitted => SupplierReviewStatus.PendingSupplierConfirmation
          case otherSupplierReviewStatus         => otherSupplierReviewStatus
    )

  def markRefundedOrderItem: HotelOrderItem =
    copy(orderItemStatus = OrderItemStatus.Refunded)

  def markCancelledOrderItem: HotelOrderItem =
    copy(orderItemStatus = OrderItemStatus.Cancelled)

  def markSupplierConfirmed(managerId: ManagerId, note: Option[String], decidedAt: Instant): Either[OrderError, HotelOrderItem] =
    supplierReviewStatus match
      case SupplierReviewStatus.PendingSupplierConfirmation =>
        Right(
          copy(
            supplierReviewStatus = SupplierReviewStatus.SupplierConfirmed,
            supplierReviewDecision = Some(
              SupplierReviewDecision(
                decision = SupplierReviewDecisionType.Confirm,
                reason = note.map(_.trim).filter(_.nonEmpty),
                decidedAt = decidedAt,
                managerId = managerId
              )
            )
          )
        )
      case _ =>
        Left(OrderError.OrderItemWasNotAwaitingSupplierDecision(orderItemId, supplierReviewStatus))

  def markSupplierRejected(managerId: ManagerId, reason: String, decidedAt: Instant): Either[OrderError, HotelOrderItem] =
    val normalizedReason = reason.trim
    if normalizedReason.isEmpty then Left(OrderError.SupplierRejectReasonWasEmpty(orderItemId))
    else
      supplierReviewStatus match
        case SupplierReviewStatus.PendingSupplierConfirmation =>
          Right(
            copy(
              supplierReviewStatus = SupplierReviewStatus.SupplierRejected,
              supplierReviewDecision = Some(
                SupplierReviewDecision(
                  decision = SupplierReviewDecisionType.Reject,
                  reason = Some(normalizedReason),
                  decidedAt = decidedAt,
                  managerId = managerId
                )
              )
            )
          )
        case _ =>
          Left(OrderError.OrderItemWasNotAwaitingSupplierDecision(orderItemId, supplierReviewStatus))

final case class TrainOrderItem private[domain] (
    orderItemId: OrderItemId,
    trainBookingSnapshot: TrainBookingSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem:
  override def bookedMoney: Money =
    trainBookingSnapshot.unitPriceSnapshot
      .multiply(trainBookingSnapshot.travelerIds.size)
      .fold(throw _, identity)

  def markConfirmedOrderItem: TrainOrderItem =
    copy(orderItemStatus = OrderItemStatus.Confirmed)

  def markRefundedOrderItem: TrainOrderItem =
    copy(orderItemStatus = OrderItemStatus.Refunded)

  def markCancelledOrderItem: TrainOrderItem =
    copy(orderItemStatus = OrderItemStatus.Cancelled)

final case class AttractionOrderItem private[domain] (
    orderItemId: OrderItemId,
    attractionTicketSnapshot: AttractionTicketSnapshot,
    orderItemStatus: OrderItemStatus,
    supplierReviewStatus: SupplierReviewStatus,
    supplierReviewDecision: Option[SupplierReviewDecision]
) extends OrderLineItem:
  override def bookedMoney: Money =
    attractionTicketSnapshot.unitPriceSnapshot
      .multiply(attractionTicketSnapshot.travelerIds.size)
      .fold(throw _, identity)

  def markConfirmedOrderItem: AttractionOrderItem =
    copy(
      orderItemStatus = OrderItemStatus.Confirmed,
      supplierReviewStatus =
        supplierReviewStatus match
          case SupplierReviewStatus.NotSubmitted => SupplierReviewStatus.PendingSupplierConfirmation
          case otherSupplierReviewStatus         => otherSupplierReviewStatus
    )

  def markRefundedOrderItem: AttractionOrderItem =
    copy(orderItemStatus = OrderItemStatus.Refunded)

  def markCancelledOrderItem: AttractionOrderItem =
    copy(orderItemStatus = OrderItemStatus.Cancelled)

  def markSupplierConfirmed(managerId: ManagerId, note: Option[String], decidedAt: Instant): Either[OrderError, AttractionOrderItem] =
    supplierReviewStatus match
      case SupplierReviewStatus.PendingSupplierConfirmation =>
        Right(
          copy(
            supplierReviewStatus = SupplierReviewStatus.SupplierConfirmed,
            supplierReviewDecision = Some(
              SupplierReviewDecision(
                decision = SupplierReviewDecisionType.Confirm,
                reason = note.map(_.trim).filter(_.nonEmpty),
                decidedAt = decidedAt,
                managerId = managerId
              )
            )
          )
        )
      case _ =>
        Left(OrderError.OrderItemWasNotAwaitingSupplierDecision(orderItemId, supplierReviewStatus))

  def markSupplierRejected(managerId: ManagerId, reason: String, decidedAt: Instant): Either[OrderError, AttractionOrderItem] =
    val normalizedReason = reason.trim
    if normalizedReason.isEmpty then Left(OrderError.SupplierRejectReasonWasEmpty(orderItemId))
    else
      supplierReviewStatus match
        case SupplierReviewStatus.PendingSupplierConfirmation =>
          Right(
            copy(
              supplierReviewStatus = SupplierReviewStatus.SupplierRejected,
              supplierReviewDecision = Some(
                SupplierReviewDecision(
                  decision = SupplierReviewDecisionType.Reject,
                  reason = Some(normalizedReason),
                  decidedAt = decidedAt,
                  managerId = managerId
                )
              )
            )
          )
        case _ =>
          Left(OrderError.OrderItemWasNotAwaitingSupplierDecision(orderItemId, supplierReviewStatus))

final case class Payment private[domain] (
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

final case class Refund private[domain] (
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

final case class Order private[domain] (
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
  def hasCapturedPayment: Boolean =
    orderPayments.exists(_.paymentStatus == PaymentStatus.Captured)

  def hasPendingRefundRequest: Boolean =
    orderRefunds.exists(_.refundStatus == RefundStatus.Requested)

  def allSupplierReviewDecisionsConfirmed: Boolean =
    val orderItemsRequiringSupplierReview = orderLineItems.collect {
      case flightOrderItem: FlightOrderItem => flightOrderItem
      case hotelOrderItem: HotelOrderItem   => hotelOrderItem
      case attractionOrderItem: AttractionOrderItem => attractionOrderItem
    }
    orderItemsRequiringSupplierReview.isEmpty || orderItemsRequiringSupplierReview.forall(_.supplierReviewStatus == SupplierReviewStatus.SupplierConfirmed)

  def hasAnySupplierRejected: Boolean =
    orderLineItems.exists {
      case flightOrderItem: FlightOrderItem => flightOrderItem.supplierReviewStatus == SupplierReviewStatus.SupplierRejected
      case hotelOrderItem: HotelOrderItem   => hotelOrderItem.supplierReviewStatus == SupplierReviewStatus.SupplierRejected
      case attractionOrderItem: AttractionOrderItem => attractionOrderItem.supplierReviewStatus == SupplierReviewStatus.SupplierRejected
      case _: TrainOrderItem                => false
    }

  def orderType: OrderType =
    if orderLineItems.isEmpty then OrderType.PendingSelection
    else if orderLineItems.forall(_.isInstanceOf[FlightOrderItem]) then OrderType.FlightBooking
    else if orderLineItems.forall(_.isInstanceOf[HotelOrderItem]) then OrderType.HotelBooking
    else if orderLineItems.forall(_.isInstanceOf[TrainOrderItem]) then OrderType.TrainBooking
    else if orderLineItems.forall(_.isInstanceOf[AttractionOrderItem]) then OrderType.AttractionBooking
    else OrderType.MixedBooking

  def totalBookedMoney: Money =
    orderLineItems.foldLeft(Money.zero(orderCurrency)) { (currentTotalMoney, orderLineItem) =>
      currentTotalMoney.add(orderLineItem.bookedMoney).fold(throw _, identity)
    }

  def totalCapturedMoney: Money =
    orderPayments
      .filter(_.paymentStatus == PaymentStatus.Captured)
      .foldLeft(Money.zero(orderCurrency)) { (currentCapturedMoney, payment) =>
        currentCapturedMoney.add(payment.paymentAmount).fold(throw _, identity)
      }

  def totalSettledRefundMoney: Money =
    orderRefunds
      .filter(_.refundStatus == RefundStatus.Settled)
      .foldLeft(Money.zero(orderCurrency)) { (currentRefundMoney, refund) =>
        currentRefundMoney.add(refund.refundAmount).fold(throw _, identity)
      }

  def remainingRefundableMoney: Money =
    totalCapturedMoney.subtract(totalSettledRefundMoney).fold(throw _, identity)

  def addFlightOrderItem(
      orderItemId: OrderItemId,
      flightBookingSnapshot: FlightBookingSnapshot
  ): Either[OrderError, Order] =
    for
      _ <- validateFlightBookingSnapshot(flightBookingSnapshot)
      orderWithLineItem <- addOrderLineItem(
        createReservedFlightOrderItem(orderItemId, flightBookingSnapshot)
      )
    yield orderWithLineItem

  def addHotelOrderItem(
      orderItemId: OrderItemId,
      hotelBookingSnapshot: HotelBookingSnapshot
  ): Either[OrderError, Order] =
    for
      _ <- validateHotelBookingSnapshot(hotelBookingSnapshot)
      updatedOrder <- addOrderLineItem(
        createReservedHotelOrderItem(orderItemId, hotelBookingSnapshot)
      )
    yield updatedOrder

  def addTrainOrderItem(
      orderItemId: OrderItemId,
      trainBookingSnapshot: TrainBookingSnapshot
  ): Either[OrderError, Order] =
    for
      _ <- validateTrainBookingSnapshot(trainBookingSnapshot)
      updatedOrder <- addOrderLineItem(
        createReservedTrainOrderItem(orderItemId, trainBookingSnapshot)
      )
    yield updatedOrder

  def addAttractionOrderItem(
      orderItemId: OrderItemId,
      attractionTicketSnapshot: AttractionTicketSnapshot
  ): Either[OrderError, Order] =
    for
      _ <- validateAttractionTicketSnapshot(attractionTicketSnapshot)
      updatedOrder <- addOrderLineItem(
        createReservedAttractionOrderItem(orderItemId, attractionTicketSnapshot)
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
    if hasCapturedPayment then Left(OrderError.PaymentWasAlreadyCompleted(orderId))
    else
      orderStatus match
        case OrderStatus.Draft | OrderStatus.PendingPayment =>
          ensureMatchingCurrency(paymentAmount).map { _ =>
            copy(orderPayments = orderPayments :+ authorizePayment(paymentId, paymentAmount, paymentMethod, authorizedAt))
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
        case OrderStatus.Confirmed => Right(())
        case _ => Left(OrderError.RefundWasNotAcceptedForOrderStatus(orderId, orderStatus))
      _ <- if hasPendingRefundRequest then Left(OrderError.RefundWasAlreadyRequested(orderId)) else Right(())
      _ <- ensureMatchingCurrency(refundAmount)
      _ <- if refundAmount.amount <= remainingRefundableMoney.amount then Right(())
      else Left(OrderError.RefundExceededRemainingBalance(orderId, remainingRefundableMoney, refundAmount))
      refund <- requestRefund(refundId, refundAmount, refundReason, requestedAt)
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
              case trainOrderItem: TrainOrderItem   => trainOrderItem.markCancelledOrderItem
              case attractionOrderItem: AttractionOrderItem => attractionOrderItem.markCancelledOrderItem
            }
          )
        )
      case _ =>
        Left(OrderError.OrderCouldNotBeCancelled(orderId, orderStatus))

  def confirmSupplierOrderItem(
      orderItemId: OrderItemId,
      managerId: ManagerId,
      note: Option[String],
      decidedAt: Instant
  ): Either[OrderError, Order] =
    updateOrderLineItem(orderItemId) {
      case flightOrderItem: FlightOrderItem => flightOrderItem.markSupplierConfirmed(managerId, note, decidedAt)
      case hotelOrderItem: HotelOrderItem   => hotelOrderItem.markSupplierConfirmed(managerId, note, decidedAt)
      case attractionOrderItem: AttractionOrderItem => attractionOrderItem.markSupplierConfirmed(managerId, note, decidedAt)
      case _: TrainOrderItem                => Left(OrderError.OrderItemDidNotSupportSupplierReview(orderItemId))
    }.map(_.refreshOrderStatusAfterSupplierDecision(decidedAt))

  def rejectSupplierOrderItem(
      orderItemId: OrderItemId,
      managerId: ManagerId,
      reason: String,
      decidedAt: Instant
  ): Either[OrderError, Order] =
    updateOrderLineItem(orderItemId) {
      case flightOrderItem: FlightOrderItem => flightOrderItem.markSupplierRejected(managerId, reason, decidedAt)
      case hotelOrderItem: HotelOrderItem   => hotelOrderItem.markSupplierRejected(managerId, reason, decidedAt)
      case attractionOrderItem: AttractionOrderItem => attractionOrderItem.markSupplierRejected(managerId, reason, decidedAt)
      case _: TrainOrderItem                => Left(OrderError.OrderItemDidNotSupportSupplierReview(orderItemId))
    }

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
    else Right(())

  private def validateTrainBookingSnapshot(
      trainBookingSnapshot: TrainBookingSnapshot
  ): Either[OrderError, Unit] =
    if trainBookingSnapshot.travelerIds.isEmpty then
      Left(OrderError.TrainBookingTravelerSelectionWasEmpty(orderId, trainBookingSnapshot.trainId))
    else if trainBookingSnapshot.travelerIds.distinct.size != trainBookingSnapshot.travelerIds.size then
      Left(OrderError.TrainBookingTravelerSelectionContainedDuplicates(orderId, trainBookingSnapshot.trainId))
    else if trainBookingSnapshot.unitPriceSnapshot.currency != orderCurrency then
      Left(OrderError.OrderCurrencyDidNotMatch(orderId, orderCurrency, trainBookingSnapshot.unitPriceSnapshot.currency))
    else Right(())

  private def validateAttractionTicketSnapshot(
      attractionTicketSnapshot: AttractionTicketSnapshot
  ): Either[OrderError, Unit] =
    if attractionTicketSnapshot.travelerIds.isEmpty then
      Left(OrderError.AttractionBookingTravelerSelectionWasEmpty(orderId, attractionTicketSnapshot.ticketTypeId))
    else if attractionTicketSnapshot.travelerIds.distinct.size != attractionTicketSnapshot.travelerIds.size then
      Left(OrderError.AttractionBookingTravelerSelectionContainedDuplicates(orderId, attractionTicketSnapshot.ticketTypeId))
    else if attractionTicketSnapshot.unitPriceSnapshot.currency != orderCurrency then
      Left(OrderError.OrderCurrencyDidNotMatch(orderId, orderCurrency, attractionTicketSnapshot.unitPriceSnapshot.currency))
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

  private def updateOrderLineItem(
      orderItemId: OrderItemId
  )(orderLineItemUpdater: OrderLineItem => Either[OrderError, OrderLineItem]): Either[OrderError, Order] =
    orderLineItems.indexWhere(_.orderItemId == orderItemId) match
      case -1 =>
        Left(OrderError.OrderItemWasNotFound(orderId, orderItemId))
      case orderLineItemIndex =>
        orderLineItemUpdater(orderLineItems(orderLineItemIndex)).map { updatedOrderLineItem =>
          copy(orderLineItems = orderLineItems.updated(orderLineItemIndex, updatedOrderLineItem))
        }

  private def refreshOrderStatusAfterFinancialChange: Order =
    if totalSettledRefundMoney.amount == totalCapturedMoney.amount && totalCapturedMoney.amount >= totalBookedMoney.amount && totalCapturedMoney.amount > 0 then
      copy(
        orderStatus = OrderStatus.Refunded,
        orderLineItems = orderLineItems.map {
          case flightOrderItem: FlightOrderItem => flightOrderItem.markRefundedOrderItem
          case hotelOrderItem: HotelOrderItem   => hotelOrderItem.markRefundedOrderItem
          case trainOrderItem: TrainOrderItem   => trainOrderItem.markRefundedOrderItem
          case attractionOrderItem: AttractionOrderItem => attractionOrderItem.markRefundedOrderItem
        }
      )
    else if totalSettledRefundMoney.amount > 0 then
      copy(orderStatus = OrderStatus.PartiallyRefunded)
    else if totalCapturedMoney.amount >= totalBookedMoney.amount && totalBookedMoney.amount > 0 then
      val latestCapturedAt = orderPayments.filter(_.paymentStatus == PaymentStatus.Captured).flatMap(_.capturedAt).lastOption
      copy(
        orderStatus = OrderStatus.Confirmed,
        paidAt = paidAt.orElse(latestCapturedAt),
        confirmedAt = if allSupplierReviewDecisionsConfirmed then confirmedAt.orElse(latestCapturedAt) else confirmedAt,
        orderLineItems = orderLineItems.map {
          case flightOrderItem: FlightOrderItem => flightOrderItem.markConfirmedOrderItem
          case hotelOrderItem: HotelOrderItem   => hotelOrderItem.markConfirmedOrderItem
          case trainOrderItem: TrainOrderItem   => trainOrderItem.markConfirmedOrderItem
          case attractionOrderItem: AttractionOrderItem => attractionOrderItem.markConfirmedOrderItem
        }
      )
    else
      this

  private def refreshOrderStatusAfterSupplierDecision(decidedAt: Instant): Order =
    if orderStatus == OrderStatus.Confirmed && allSupplierReviewDecisionsConfirmed then
      copy(confirmedAt = confirmedAt.orElse(Some(decidedAt)))
    else
      this

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
  case PaymentWasAlreadyCompleted(orderId: OrderId)
      extends OrderError(s"Order '${orderId.value}' has already been paid successfully")
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
  case RefundWasAlreadyRequested(orderId: OrderId)
      extends OrderError(s"Order '${orderId.value}' already has a pending refund request")
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
  case TrainBookingTravelerSelectionWasEmpty(orderId: OrderId, trainId: TrainId)
      extends OrderError(s"Order '${orderId.value}' cannot add train '${trainId.value}' without any travelers")
  case TrainBookingTravelerSelectionContainedDuplicates(orderId: OrderId, trainId: TrainId)
      extends OrderError(s"Order '${orderId.value}' cannot add train '${trainId.value}' with duplicate travelers")
  case AttractionBookingTravelerSelectionWasEmpty(orderId: OrderId, ticketTypeId: TicketTypeId)
      extends OrderError(s"Order '${orderId.value}' cannot add attraction ticket '${ticketTypeId.value}' without any travelers")
  case AttractionBookingTravelerSelectionContainedDuplicates(orderId: OrderId, ticketTypeId: TicketTypeId)
      extends OrderError(s"Order '${orderId.value}' cannot add attraction ticket '${ticketTypeId.value}' with duplicate travelers")
  case OrderItemWasNotFound(orderId: OrderId, orderItemId: OrderItemId)
      extends OrderError(s"Order item '${orderItemId.value}' was not found in order '${orderId.value}'")
  case OrderItemDidNotSupportSupplierReview(orderItemId: OrderItemId)
      extends OrderError(s"Order item '${orderItemId.value}' does not support supplier review")
  case OrderItemWasNotAwaitingSupplierDecision(orderItemId: OrderItemId, supplierReviewStatus: SupplierReviewStatus)
      extends OrderError(s"Order item '${orderItemId.value}' cannot be decided from supplier review status $supplierReviewStatus")
  case SupplierRejectReasonWasEmpty(orderItemId: OrderItemId)
      extends OrderError(s"Order item '${orderItemId.value}' requires a non-empty reject reason")

