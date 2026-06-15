// InventoryReservation 是 inventory 域的核心数据模型。
// 这个域只存在于后端，用来描述库存预留、确认、过期和释放。
// 前端不会直接镜像这一层，因为它只是航班/酒店/火车等供应域背后的内部支撑结构。
package com.typesafe.travel.inventory.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.{Instant, LocalDate}

// 库存预留所对应的资源类型：航班舱位、酒店房型、火车座位。
final case class ReservationResourceType(value: String):
  override def toString: String = value

object ReservationResourceType:
  val FlightCabinInventory: ReservationResourceType = ReservationResourceType("FlightCabinInventory")
  val HotelRoomType: ReservationResourceType = ReservationResourceType("HotelRoomType")
  val TrainSeatInventory: ReservationResourceType = ReservationResourceType("TrainSeatInventory")
  given sourceEncoder: Encoder[ReservationResourceType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ReservationResourceType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ReservationResourceType =
    value.trim.toLowerCase match
      case "hotelroomtype" | "hotel_room_type" => HotelRoomType
      case "trainseatinventory" | "train_seat_inventory" => TrainSeatInventory
      case _ => FlightCabinInventory

// 库存预留的生命周期状态。
final case class ReservationStatus(value: String):
  override def toString: String = value

object ReservationStatus:
  val Active: ReservationStatus = ReservationStatus("Active")
  val Expired: ReservationStatus = ReservationStatus("Expired")
  val Confirmed: ReservationStatus = ReservationStatus("Confirmed")
  val Released: ReservationStatus = ReservationStatus("Released")
  given sourceEncoder: Encoder[ReservationStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[ReservationStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): ReservationStatus =
    value.trim.toLowerCase match
      case "expired" => Expired
      case "confirmed" => Confirmed
      case "released" => Released
      case _ => Active

// 库存预留域错误。这里只做领域表达，不承载前端页面错误文案。
sealed trait InventoryReservationError extends DomainError:
  def message: String

object InventoryReservationError:
  final case class ReservationWasNotFound(reservationId: ReservationId) extends InventoryReservationError:
    override val message: String = s"Reservation '${reservationId.value}' was not found"

  final case class ReservationWasNotActive(reservationId: ReservationId, status: ReservationStatus) extends InventoryReservationError:
    override val message: String = s"Reservation '${reservationId.value}' is not active while in status $status"

  final case class ReservationQuantityWasInvalid(quantity: Int) extends InventoryReservationError:
    override val message: String = s"Reservation quantity '$quantity' must be greater than zero"

  final case class InventoryWasNotAvailable(resourceId: String, requestedQuantity: Int, remainingQuantity: Int) extends InventoryReservationError:
    override val message: String =
      s"Inventory '$resourceId' does not have enough remaining quantity for request '$requestedQuantity'; remaining '$remainingQuantity'"

// 单条库存预留记录。
// 它不是前端 UI 对象，而是后端用来驱动订单、库存和资源锁定的内部对象。
final case class InventoryReservation(
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
)

object InventoryReservation:
  // 这里使用后端内部 codec，把 shared-kernel 值对象与 JSON 互转。
  import InventoryReservationSourceJsonCodecs.given
  given sourceEncoder: Encoder[InventoryReservation] = deriveEncoder
  given sourceDecoder: Decoder[InventoryReservation] = deriveDecoder