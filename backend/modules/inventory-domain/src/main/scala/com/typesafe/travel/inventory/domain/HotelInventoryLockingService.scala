package com.typesafe.travel.inventory.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Duration, Instant, LocalDate}

trait HotelInventoryLockingService[F[_]]:
  def acquireHotelRoomReservation(
      roomTypeId: RoomTypeId,
      orderId: OrderId,
      orderItemId: OrderItemId,
      roomCount: Int,
      dailyCapacities: Vector[(LocalDate, Int)],
      stayPeriod: StayPeriod,
      reservedAt: Instant
  ): F[InventoryReservation]

final class LiveHotelInventoryLockingService[F[_]: MonadThrow](
    inventoryReservationRepository: InventoryReservationRepository[F],
    reservationTtl: Duration,
    reservationLifecycle: ReservationLifecycle[F]
) extends HotelInventoryLockingService[F]:
  override def acquireHotelRoomReservation(
      roomTypeId: RoomTypeId,
      orderId: OrderId,
      orderItemId: OrderItemId,
      roomCount: Int,
      dailyCapacities: Vector[(LocalDate, Int)],
      stayPeriod: StayPeriod,
      reservedAt: Instant
  ): F[InventoryReservation] =
    for
      _ <- reservationLifecycle.expireReservationsForResource(
        ReservationResourceType.HotelRoomType,
        roomTypeId.value,
        reservedAt
      )
      reservations <- inventoryReservationRepository.findReservationsByResource(
        ReservationResourceType.HotelRoomType,
        roomTypeId.value
      )
      requiredDates = stayDates(stayPeriod)
      requestedQuantity = roomCount
      _ <- if requestedQuantity <= 0 then
        MonadThrow[F].raiseError(InventoryReservationError.ReservationQuantityWasInvalid(requestedQuantity))
      else MonadThrow[F].unit
      remainingByDate = requiredDates.map { inventoryDate =>
        val capacityQuantity = dailyCapacities.find(_._1 == inventoryDate).map(_._2).getOrElse(0)
        val reservedQuantity = reservedQuantityForDate(reservations, inventoryDate, reservedAt)
        inventoryDate -> (capacityQuantity - reservedQuantity)
      }
      minimumRemaining = remainingByDate.map(_._2).minOption.getOrElse(0)
      _ <- if minimumRemaining < requestedQuantity then
        MonadThrow[F].raiseError(
          InventoryReservationError.InventoryWasNotAvailable(roomTypeId.value, requestedQuantity, minimumRemaining.max(0))
        )
      else MonadThrow[F].unit
      reservationId <- inventoryReservationRepository.nextReservationId
      reservation <- InventoryReservation
        .createActiveReservation(
          reservationId = reservationId,
          resourceType = ReservationResourceType.HotelRoomType,
          resourceId = roomTypeId.value,
          orderId = orderId,
          orderItemId = orderItemId,
          quantity = requestedQuantity,
          reservedAt = reservedAt,
          expiresAt = reservedAt.plus(reservationTtl),
          checkInDate = Some(stayPeriod.checkIn),
          checkOutDate = Some(stayPeriod.checkOut)
        )
        .liftTo[F]
      savedReservation <- inventoryReservationRepository.saveReservation(reservation)
    yield savedReservation

  private def stayDates(stayPeriod: StayPeriod): Vector[LocalDate] =
    LazyList
      .iterate(stayPeriod.checkIn)(_.plusDays(1))
      .takeWhile(_.isBefore(stayPeriod.checkOut))
      .toVector

  private def overlaps(reservation: InventoryReservation, inventoryDate: LocalDate): Boolean =
    (reservation.checkInDate, reservation.checkOutDate) match
      case (Some(checkInDate), Some(checkOutDate)) =>
        !inventoryDate.isBefore(checkInDate) && inventoryDate.isBefore(checkOutDate)
      case _ =>
        false

  private def reservedQuantityForDate(
      reservations: List[InventoryReservation],
      inventoryDate: LocalDate,
      currentTime: Instant
  ): Int =
    reservations.collect {
      case reservation
          if overlaps(reservation, inventoryDate) &&
            (reservation.reservationStatus == ReservationStatus.Confirmed || reservation.isActiveAt(currentTime)) =>
        reservation.quantity
    }.sum
