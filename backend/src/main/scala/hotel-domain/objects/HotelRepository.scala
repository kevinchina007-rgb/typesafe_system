package com.typesafe.travel.hotel.domain

import com.typesafe.travel.shared.kernel.*

final case class HotelSearchCriteria(
    location: Option[HotelLocation],
    stayPeriod: Option[StayPeriod]
)

trait HotelRepository[F[_]]:
  def nextHotelId: F[HotelId]
  def nextRoomTypeId: F[RoomTypeId]
  def nextRoomInventoryId: F[RoomInventoryId]
  def findHotelById(hotelId: HotelId): F[Option[Hotel]]
  def findHotelByRoomTypeId(roomTypeId: RoomTypeId): F[Option[Hotel]]
  def searchHotels(hotelSearchCriteria: HotelSearchCriteria): F[List[Hotel]]
  def saveHotel(hotel: Hotel): F[Hotel]
