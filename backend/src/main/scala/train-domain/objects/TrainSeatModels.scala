package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

final case class TrainSeatLayoutColumn(
    code: String,
    sortOrder: Int,
    positionType: TrainSeatPositionType
)

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
):
  def isBookable: Boolean = seatStatus == TrainSeatStatus.Available

final case class TrainTravelerSeatAssignment(
    travelerId: TravelerId,
    seatId: TrainSeatId,
    carriageNo: Int,
    seatNo: String,
    seatLabel: String,
    seatPositionType: TrainSeatPositionType
)

final case class TrainSegmentSeatAllocation(
    seatId: TrainSeatId,
    orderId: OrderId,
    orderItemId: OrderItemId,
    fromStopSequenceNo: Int,
    toStopSequenceNo: Int
):
  def overlaps(fromSequenceNo: Int, toSequenceNo: Int): Boolean =
    fromStopSequenceNo < toSequenceNo && toStopSequenceNo > fromSequenceNo

final case class TrainSeatInventory(
    inventoryId: TrainSeatInventoryId,
    trainId: TrainId,
    seatClass: TrainSeatClass,
    totalSeats: SeatCount,
    saleableSeats: SeatCount,
    seatInventoryStatus: TrainSeatInventoryStatus
):
  def ensureBookable: Either[TrainError, TrainSeatInventory] =
    if seatInventoryStatus == TrainSeatInventoryStatus.OpenForSale && saleableSeats.value > 0 then Right(this)
    else Left(TrainError.TrainSeatInventoryWasNotBookable(trainId, seatClass, seatInventoryStatus))

