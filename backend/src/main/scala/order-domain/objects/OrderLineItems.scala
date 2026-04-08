package com.typesafe.travel.order.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

sealed trait OrderLineItem:
  def orderItemId: OrderItemId
  def bookedMoney: Money
  def orderItemStatus: OrderItemStatus
  def supplierReviewStatus: SupplierReviewStatus
  def supplierReviewDecision: Option[SupplierReviewDecision]

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
