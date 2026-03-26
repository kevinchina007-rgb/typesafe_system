package com.typesafe.travel.hotel.domain

import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate}
import munit.FunSuite

final class HotelSpec extends FunSuite:
  private val testHotelId = HotelId("hotel-1")
  private val testRoomTypeId = RoomTypeId("room-type-1")
  private val createdAt = Instant.parse("2026-03-26T00:00:00Z")

  private def buildHotel(roomCapacity: Capacity = Capacity.unsafe(2), availableRooms: Int = 3): Hotel =
    val roomType =
      RoomType.createRoomType(
        roomTypeId = testRoomTypeId,
        hotelId = testHotelId,
        roomTypeName = RoomTypeName.unsafe("Deluxe Twin"),
        roomCapacity = roomCapacity,
        bedType = BedType.unsafe("twin"),
        basePrice = Money.unsafe(BigDecimal(880), Currency.CNY),
        roomTypeStatus = RoomTypeStatus.OpenForBooking,
        roomInventories = Vector(
          RoomInventory.createRoomInventory(
            roomInventoryId = RoomInventoryId("inventory-1"),
            roomTypeId = testRoomTypeId,
            inventoryDate = LocalDate.parse("2026-04-08"),
            availableRooms = RoomCount.unsafe(availableRooms),
            unitPrice = Money.unsafe(BigDecimal(900), Currency.CNY),
            roomInventoryStatus = RoomInventoryStatus.Available
          ),
          RoomInventory.createRoomInventory(
            roomInventoryId = RoomInventoryId("inventory-2"),
            roomTypeId = testRoomTypeId,
            inventoryDate = LocalDate.parse("2026-04-09"),
            availableRooms = RoomCount.unsafe(availableRooms),
            unitPrice = Money.unsafe(BigDecimal(960), Currency.CNY),
            roomInventoryStatus = RoomInventoryStatus.Available
          )
        )
      )

    Hotel.createHotel(
      hotelId = testHotelId,
      hotelName = HotelName.unsafe("West Lake Retreat"),
      hotelLocation = HotelLocation.unsafe("Hangzhou"),
      roomTypes = Vector(roomType),
      createdAt = createdAt
    )

  test("hotel stay must have inventory for every day in the stay period") {
    val hotel = buildHotel()
    val invalidStayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-08"), LocalDate.parse("2026-04-11"))

    val result = hotel.ensureRoomTypeBookableForStay(testRoomTypeId, invalidStayPeriod, RoomCount.unsafe(1), guestCount = 2)

    assert(result.swap.exists(_.isInstanceOf[HotelError.RoomInventoryWasMissing]))
  }

  test("hotel room type enforces guest capacity against room count") {
    val hotel = buildHotel(roomCapacity = Capacity.unsafe(2))
    val stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-08"), LocalDate.parse("2026-04-10"))

    val result = hotel.ensureRoomTypeBookableForStay(testRoomTypeId, stayPeriod, RoomCount.unsafe(1), guestCount = 3)

    assert(result.swap.exists(_.isInstanceOf[HotelError.GuestCapacityWasExceeded]))
  }

  test("hotel room type stays bookable when each night has inventory") {
    val hotel = buildHotel()
    val stayPeriod = StayPeriod.unsafe(LocalDate.parse("2026-04-08"), LocalDate.parse("2026-04-10"))

    val result = hotel.ensureRoomTypeBookableForStay(testRoomTypeId, stayPeriod, RoomCount.unsafe(1), guestCount = 2)

    assertEquals(result.map(_.size), Right(2))
  }
