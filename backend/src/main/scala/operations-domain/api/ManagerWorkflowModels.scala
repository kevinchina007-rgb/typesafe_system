package com.typesafe.travel.api.application

import com.typesafe.travel.flight.domain.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate, OffsetDateTime}

final case class ManagerSession(
    managerId: ManagerId,
    managerType: ManagerType,
    emailAddress: EmailAddress,
    displayName: PersonName,
    status: ManagerStatus,
    scopeId: String,
    createdAt: Instant
)

final case class ManagerBookingTaskView(
    orderId: OrderId,
    orderItemId: OrderItemId,
    buyerUserId: UserId,
    taskType: ManagerType,
    supplierReviewStatus: SupplierReviewStatus,
    summaryLabel: String,
    detailLabel: String,
    requestedAt: Instant,
    reviewDecision: Option[SupplierReviewDecision],
    reviewedBy: Option[ManagerId],
    reviewedAt: Option[Instant],
    reviewNote: Option[String]
)

final case class ManagerRefundTaskView(
    orderId: OrderId,
    buyerUserId: UserId,
    taskType: ManagerType,
    summaryLabel: String,
    refundId: RefundId,
    refundReason: String,
    refundAmount: Money,
    requestedAt: Instant
)

trait ManagerWorkflowApplicationService[F[_]]:
  def registerAirlineManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      airlineName: AirlineName,
      airlineCode: AirlineCode,
      createdAt: Instant
  ): F[ManagerSession]
  def registerHotelManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      hotelName: HotelName,
      hotelLocation: HotelLocation,
      createdAt: Instant
  ): F[ManagerSession]
  def registerAttractionManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[ManagerSession]
  def loginManager(managerType: ManagerType, primaryEmailAddress: EmailAddress): F[ManagerSession]
  def listManagerTasks(
      managerId: ManagerId,
      managerType: ManagerType,
      requestedSupplierReviewStatuses: Set[SupplierReviewStatus],
      requestedResourceTypes: Set[ManagerType]
  ): F[List[ManagerBookingTaskView]]
  def listManagerRefundTasks(managerId: ManagerId, managerType: ManagerType): F[List[ManagerRefundTaskView]]
  def listFlightsForAirlineManager(managerId: ManagerId): F[List[(Airline, Flight)]]
  def listHotelsForHotelManager(managerId: ManagerId): F[List[Hotel]]
  def createFlightForAirlineManager(
      managerId: ManagerId,
      flightNumber: FlightNumber,
      departureAirport: AirportCode,
      arrivalAirport: AirportCode,
      departureAt: OffsetDateTime,
      arrivalAt: OffsetDateTime,
      economySeatCount: SeatCount,
      economyPrice: Money,
      businessSeatCount: SeatCount,
      businessPrice: Money,
      createdAt: Instant
  ): F[(Airline, Flight)]
  def createRoomTypeForHotelManager(
      managerId: ManagerId,
      roomTypeName: RoomTypeName,
      roomCapacity: Capacity,
      bedType: BedType,
      nightlyPrice: Money,
      availableRooms: RoomCount,
      inventoryStartDate: LocalDate,
      inventoryEndDate: LocalDate
  ): F[Hotel]
  def confirmBookingItem(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemId: OrderItemId,
      note: Option[String],
      decidedAt: Instant
  ): F[Order]
  def batchConfirmBookingItems(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemIds: List[OrderItemId],
      note: Option[String],
      decidedAt: Instant
  ): F[List[Order]]
  def rejectBookingItem(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemId: OrderItemId,
      reason: String,
      decidedAt: Instant
  ): F[Order]
  def batchRejectBookingItems(
      managerId: ManagerId,
      managerType: ManagerType,
      orderItemIds: List[OrderItemId],
      reason: String,
      decidedAt: Instant
  ): F[List[Order]]
  def approveRefund(managerId: ManagerId, managerType: ManagerType, orderId: OrderId, decidedAt: Instant): F[Order]
  def rejectRefund(managerId: ManagerId, managerType: ManagerType, orderId: OrderId): F[Order]
