package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryHotelRepository[F[_]: Sync] private (
    hotelState: TrieMap[HotelId, Hotel],
    hotelSequence: AtomicLong,
    roomTypeSequence: AtomicLong,
    roomInventorySequence: AtomicLong
) extends HotelRepository[F]:
  override def nextHotelId: F[HotelId] =
    Sync[F].delay(HotelId(s"hotel-generated-${hotelSequence.incrementAndGet()}"))

  override def nextRoomTypeId: F[RoomTypeId] =
    Sync[F].delay(RoomTypeId(s"room-type-generated-${roomTypeSequence.incrementAndGet()}"))

  override def nextRoomInventoryId: F[RoomInventoryId] =
    Sync[F].delay(RoomInventoryId(s"room-inventory-generated-${roomInventorySequence.incrementAndGet()}"))

  override def findHotelById(hotelId: HotelId): F[Option[Hotel]] =
    Sync[F].delay(hotelState.get(hotelId))

  override def findHotelByRoomTypeId(roomTypeId: RoomTypeId): F[Option[Hotel]] =
    Sync[F].delay(hotelState.values.find(_.roomTypes.exists(_.roomTypeId == roomTypeId)))

  override def searchHotels(hotelSearchCriteria: HotelSearchCriteria): F[List[Hotel]] =
    Sync[F].delay(
      hotelState.values.toList
        .filter(_.isSearchMatch(hotelSearchCriteria.location, hotelSearchCriteria.stayPeriod))
        .sortBy(_.hotelName.value)
    )

  override def saveHotel(hotel: Hotel): F[Hotel] =
    Sync[F].delay {
      hotelState.put(hotel.hotelId, hotel)
      hotel
    }

object InMemoryHotelRepository:
  def create[F[_]: Sync]: InMemoryHotelRepository[F] =
    val createdAtInstant = Instant.parse("2026-03-26T00:00:00Z")

    val hangzhouHotelId = HotelId("hotel-hz-westlake")
    val shanghaiHotelId = HotelId("hotel-sh-bund")

    new InMemoryHotelRepository[F](
      hotelState = TrieMap(
        hangzhouHotelId -> createHotel(
          hotelId = hangzhouHotelId,
          hotelName = HotelName.unsafe("West Lake Retreat"),
          hotelLocation = HotelLocation.unsafe("Hangzhou"),
          roomTypes = Vector(
            buildRoomType(
              roomTypeId = RoomTypeId("roomtype-westlake-deluxe"),
              hotelId = hangzhouHotelId,
              roomTypeName = RoomTypeName.unsafe("Deluxe Twin"),
              roomCapacity = Capacity.unsafe(2),
              bedType = BedType.unsafe("twin"),
              basePriceAmount = BigDecimal(860),
              inventoryPrefix = "westlake-deluxe",
              inventoryDates = List(
                LocalDate.parse("2026-04-05") -> (3, BigDecimal(880)),
                LocalDate.parse("2026-04-06") -> (3, BigDecimal(920)),
                LocalDate.parse("2026-04-07") -> (2, BigDecimal(950)),
                LocalDate.parse("2026-04-08") -> (2, BigDecimal(980))
              )
            ),
            buildRoomType(
              roomTypeId = RoomTypeId("roomtype-westlake-family"),
              hotelId = hangzhouHotelId,
              roomTypeName = RoomTypeName.unsafe("Family Suite"),
              roomCapacity = Capacity.unsafe(4),
              bedType = BedType.unsafe("family"),
              basePriceAmount = BigDecimal(1280),
              inventoryPrefix = "westlake-family",
              inventoryDates = List(
                LocalDate.parse("2026-04-05") -> (2, BigDecimal(1320)),
                LocalDate.parse("2026-04-06") -> (2, BigDecimal(1360)),
                LocalDate.parse("2026-04-07") -> (1, BigDecimal(1420)),
                LocalDate.parse("2026-04-08") -> (1, BigDecimal(1450))
              )
            )
          ),
          createdAt = createdAtInstant
        ),
        shanghaiHotelId -> createHotel(
          hotelId = shanghaiHotelId,
          hotelName = HotelName.unsafe("Bund Skyline Hotel"),
          hotelLocation = HotelLocation.unsafe("Shanghai"),
          roomTypes = Vector(
            buildRoomType(
              roomTypeId = RoomTypeId("roomtype-bund-queen"),
              hotelId = shanghaiHotelId,
              roomTypeName = RoomTypeName.unsafe("City Queen"),
              roomCapacity = Capacity.unsafe(2),
              bedType = BedType.unsafe("queen"),
              basePriceAmount = BigDecimal(980),
              inventoryPrefix = "bund-queen",
              inventoryDates = List(
                LocalDate.parse("2026-04-05") -> (4, BigDecimal(990)),
                LocalDate.parse("2026-04-06") -> (4, BigDecimal(1030)),
                LocalDate.parse("2026-04-07") -> (3, BigDecimal(1070)),
                LocalDate.parse("2026-04-08") -> (3, BigDecimal(1090))
              )
            )
          ),
          createdAt = createdAtInstant
        )
      ),
      hotelSequence = AtomicLong(100),
      roomTypeSequence = AtomicLong(100),
      roomInventorySequence = AtomicLong(1000)
    )

  private def buildRoomType(
      roomTypeId: RoomTypeId,
      hotelId: HotelId,
      roomTypeName: RoomTypeName,
      roomCapacity: Capacity,
      bedType: BedType,
      basePriceAmount: BigDecimal,
      inventoryPrefix: String,
      inventoryDates: List[(LocalDate, (Int, BigDecimal))]
  ): RoomType =
    createRoomType(
      roomTypeId = roomTypeId,
      hotelId = hotelId,
      roomTypeName = roomTypeName,
      roomCapacity = roomCapacity,
      bedType = bedType,
      basePrice = Money.unsafe(basePriceAmount, Currency.CNY),
      roomTypeStatus = RoomTypeStatus.OpenForBooking,
      roomInventories = inventoryDates.zipWithIndex.map { case ((inventoryDate, (availableRooms, unitPriceAmount)), inventoryIndex) =>
        createRoomInventory(
          roomInventoryId = RoomInventoryId(s"$inventoryPrefix-$inventoryIndex"),
          roomTypeId = roomTypeId,
          inventoryDate = inventoryDate,
          availableRooms = RoomCount.unsafe(availableRooms),
          unitPrice = Money.unsafe(unitPriceAmount, Currency.CNY),
          roomInventoryStatus = if availableRooms > 0 then RoomInventoryStatus.Available else RoomInventoryStatus.SoldOut
        )
      }.toVector
    )

