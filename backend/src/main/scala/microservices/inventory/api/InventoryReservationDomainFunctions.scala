// InventoryReservationDomainFunctions 是 inventory 域的纯领域辅助函数集合。
// 这里负责库存预留的状态流转、过期、确认和创建，不对应任何前端页面入口。
// 这个域是后端内部支撑层，服务于订单、航班、酒店和火车等业务。
package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate}

// 判断某条库存预留在某个时刻是否仍然有效。
def inventoryReservationIsActiveAt(inventoryReservation: InventoryReservation, currentTime: Instant): Boolean =
  inventoryReservation.reservationStatus == ReservationStatus.Active &&
    inventoryReservation.expiresAt.isAfter(currentTime)

// 将已过期的库存预留标记为 Expired。
def markInventoryReservationExpired(
    inventoryReservation: InventoryReservation,
    expiredAt: Instant
): Either[InventoryReservationError, InventoryReservation] =
  inventoryReservation.reservationStatus match
    case currentStatus if currentStatus == ReservationStatus.Active && !inventoryReservation.expiresAt.isAfter(expiredAt) =>
      Right(inventoryReservation.copy(reservationStatus = ReservationStatus.Expired, releasedAt = Some(expiredAt)))
    case _ =>
      Left(InventoryReservationError.ReservationWasNotActive(inventoryReservation.reservationId, inventoryReservation.reservationStatus))

// 将活动中的库存预留标记为 Confirmed。
def confirmInventoryReservation(
    inventoryReservation: InventoryReservation,
    confirmedAt: Instant
): Either[InventoryReservationError, InventoryReservation] =
  inventoryReservation.reservationStatus match
    case currentStatus if currentStatus == ReservationStatus.Active =>
      Right(inventoryReservation.copy(reservationStatus = ReservationStatus.Confirmed, confirmedAt = Some(confirmedAt)))
    case _ =>
      Left(InventoryReservationError.ReservationWasNotActive(inventoryReservation.reservationId, inventoryReservation.reservationStatus))

// 将活动中的库存预留标记为 Released。
def releaseInventoryReservation(
    inventoryReservation: InventoryReservation,
    releasedAt: Instant
): Either[InventoryReservationError, InventoryReservation] =
  inventoryReservation.reservationStatus match
    case currentStatus if currentStatus == ReservationStatus.Active =>
      Right(inventoryReservation.copy(reservationStatus = ReservationStatus.Released, releasedAt = Some(releasedAt)))
    case _ =>
      Left(InventoryReservationError.ReservationWasNotActive(inventoryReservation.reservationId, inventoryReservation.reservationStatus))

// 创建一条新的活动库存预留。
def createActiveInventoryReservation(
    reservationId: ReservationId,
    resourceType: ReservationResourceType,
    resourceId: String,
    orderId: OrderId,
    orderItemId: OrderItemId,
    quantity: Int,
    reservedAt: Instant,
    expiresAt: Instant,
    checkInDate: Option[LocalDate] = None,
    checkOutDate: Option[LocalDate] = None
): Either[InventoryReservationError, InventoryReservation] =
  if quantity <= 0 then Left(InventoryReservationError.ReservationQuantityWasInvalid(quantity))
  else
    Right(
      InventoryReservation(
        reservationId = reservationId,
        resourceType = resourceType,
        resourceId = resourceId,
        orderId = orderId,
        orderItemId = orderItemId,
        quantity = quantity,
        reservationStatus = ReservationStatus.Active,
        reservedAt = reservedAt,
        expiresAt = expiresAt,
        confirmedAt = None,
        releasedAt = None,
        checkInDate = checkInDate,
        checkOutDate = checkOutDate
      )
    )

// 从持久化记录中恢复库存预留对象。
def restorePersistedInventoryReservation(
    reservationId: ReservationId,
    resourceType: ReservationResourceType,
    resourceId: String,
    orderId: OrderId,
    orderItemId: OrderItemId,
    quantity: Int,
    reservationStatus: ReservationStatus,
    reservedAt: Instant,
    expiresAt: Instant,
    confirmedAt: Option[Instant],
    releasedAt: Option[Instant],
    checkInDate: Option[LocalDate],
    checkOutDate: Option[LocalDate]
): InventoryReservation =
  InventoryReservation(
    reservationId = reservationId,
    resourceType = resourceType,
    resourceId = resourceId,
    orderId = orderId,
    orderItemId = orderItemId,
    quantity = quantity,
    reservationStatus = reservationStatus,
    reservedAt = reservedAt,
    expiresAt = expiresAt,
    confirmedAt = confirmedAt,
    releasedAt = releasedAt,
    checkInDate = checkInDate,
    checkOutDate = checkOutDate
  )