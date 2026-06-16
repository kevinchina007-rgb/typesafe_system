// ManagerDomainFunctions 定义operations模块的领域辅助函数。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.persistence.operations.ManagerBookingTaskPlannerPlainSql
import com.typesafe.travel.persistence.operations.ManagerRefundTaskPlannerPlainSql
import com.typesafe.travel.shared.kernel.*
import java.sql.Connection
import java.time.Instant

def registerAirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): AirlineManager =
  AirlineManager(managerId, airlineId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)

def restorePersistedAirlineManager(
    managerId: ManagerId,
    airlineId: AirlineId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): AirlineManager =
  AirlineManager(managerId, airlineId, primaryEmailAddress, displayName, managerStatus, createdAt)

def registerHotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): HotelManager =
  HotelManager(managerId, hotelId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)

def restorePersistedHotelManager(
    managerId: ManagerId,
    hotelId: HotelId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): HotelManager =
  HotelManager(managerId, hotelId, primaryEmailAddress, displayName, managerStatus, createdAt)

def registerAttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    createdAt: Instant
): AttractionManager =
  AttractionManager(managerId, primaryEmailAddress, displayName, ManagerStatus.Active, createdAt)

def restorePersistedAttractionManager(
    managerId: ManagerId,
    primaryEmailAddress: EmailAddress,
    displayName: PersonName,
    managerStatus: ManagerStatus,
    createdAt: Instant
): AttractionManager =
  AttractionManager(managerId, primaryEmailAddress, displayName, managerStatus, createdAt)

def managerContextId(managerContext: ManagerContext): ManagerId =
  managerContext match
    case airlineManager: AirlineManager       => airlineManager.managerId
    case hotelManager: HotelManager           => hotelManager.managerId
    case attractionManager: AttractionManager => attractionManager.managerId

def managerContextStatus(managerContext: ManagerContext): ManagerStatus =
  managerContext match
    case airlineManager: AirlineManager       => airlineManager.managerStatus
    case hotelManager: HotelManager           => hotelManager.managerStatus
    case attractionManager: AttractionManager => attractionManager.managerStatus

def managerContextType(managerContext: ManagerContext): ManagerType =
  managerContext match
    case _: AirlineManager    => ManagerType.Airline
    case _: HotelManager      => ManagerType.Hotel
    case _: AttractionManager => ManagerType.Attraction

def updateRefundDecision(
    connection: Connection,
    orderId: String,
    action: String,
    refundStatus: String,
    approved: Boolean,
    now: Instant
): IO[ManagerBatchDecisionResponse] =
  ManagerRefundTaskPlannerPlainSql.updateRequestedRefundDecision(connection, orderId, refundStatus, approved, now)
    .as(ManagerBatchDecisionResponse(1, Nil, action))

def updateSupplierReviewDecisions(
    connection: Connection,
    managerId: String,
    orderItemIds: List[String],
    action: String,
    supplierReviewStatus: String,
    reviewDecision: String,
    reason: Option[String],
    now: Instant
): IO[ManagerBatchDecisionResponse] =
  orderItemIds.traverse_(orderItemId =>
    ManagerBookingTaskPlannerPlainSql.updateSupplierReviewDecision(
      connection,
      managerId,
      orderItemId,
      supplierReviewStatus,
      reviewDecision,
      reason,
      now
    )
  ).as(ManagerBatchDecisionResponse(orderItemIds.size, orderItemIds, action))
