// TrainSeatModels 定义火车模块的数据模型。

package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import TrainSourceJsonCodecs.given

final case class TrainSeatLayoutColumn(
    code: String,
    sortOrder: Int,
    positionType: TrainSeatPositionType
)

object TrainSeatLayoutColumn:
  given sourceEncoder: Encoder[TrainSeatLayoutColumn] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeatLayoutColumn] = deriveDecoder

final case class TrainSeat(
    seatId: TrainSeatId,
    trainId: TrainId,
    inventoryId: TrainSeatInventoryId,
    seatClass: TrainSeatClass,
    carriageNo: Int,
    rowNo: TrainSeatRowNo,
    seatCode: String,
    seatNo: String,
    seatLabel: String,
    seatPositionType: TrainSeatPositionType,
    seatStatus: TrainSeatStatus
)

object TrainSeat:
  given sourceEncoder: Encoder[TrainSeat] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeat] = deriveDecoder

final case class TrainTravelerSeatAssignment(
    travelerId: TravelerId,
    seatId: TrainSeatId,
    carriageNo: Int,
    seatNo: String,
    seatLabel: String,
    seatPositionType: TrainSeatPositionType
)

object TrainTravelerSeatAssignment:
  given sourceEncoder: Encoder[TrainTravelerSeatAssignment] = deriveEncoder
  given sourceDecoder: Decoder[TrainTravelerSeatAssignment] = deriveDecoder

final case class TrainSegmentSeatAllocation(
    seatId: TrainSeatId,
    orderId: OrderId,
    orderItemId: OrderItemId,
    fromStopSequenceNo: Int,
    toStopSequenceNo: Int
)

object TrainSegmentSeatAllocation:
  given sourceEncoder: Encoder[TrainSegmentSeatAllocation] = deriveEncoder
  given sourceDecoder: Decoder[TrainSegmentSeatAllocation] = deriveDecoder

final case class TrainSeatInventory(
    inventoryId: TrainSeatInventoryId,
    trainId: TrainId,
    seatClass: TrainSeatClass,
    totalSeats: SeatCount,
    saleableSeats: SeatCount,
    seatInventoryStatus: TrainSeatInventoryStatus
)

object TrainSeatInventory:
  given sourceEncoder: Encoder[TrainSeatInventory] = deriveEncoder
  given sourceDecoder: Decoder[TrainSeatInventory] = deriveDecoder

