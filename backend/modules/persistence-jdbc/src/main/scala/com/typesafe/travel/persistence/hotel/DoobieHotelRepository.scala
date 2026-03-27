package com.typesafe.travel.persistence.hotel

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.time.{Instant, LocalDate}
import java.util.UUID

final class DoobieHotelRepository[F[_]: Async](
    transactor: Transactor[F]
) extends HotelRepository[F]:
  override def nextHotelId: F[HotelId] =
    Async[F].delay(HotelId(s"hotel-${UUID.randomUUID().toString.take(12)}"))

  override def nextRoomTypeId: F[RoomTypeId] =
    Async[F].delay(RoomTypeId(s"room-type-${UUID.randomUUID().toString.take(12)}"))

  override def nextRoomInventoryId: F[RoomInventoryId] =
    Async[F].delay(RoomInventoryId(s"room-inventory-${UUID.randomUUID().toString.take(12)}"))

  def saveHotel(hotel: Hotel): F[Hotel] =
    val upsertHotel =
      for
        updatedRowCount <- sql"""
          update hotels
          set
            name = ${hotel.hotelName.value},
            location = ${hotel.hotelLocation.value},
            status = ${hotel.hotelStatus.toString},
            created_at = ${hotel.createdAt}
          where hotel_id = ${hotel.hotelId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into hotels (hotel_id, name, location, status, created_at)
            values (
              ${hotel.hotelId.value},
              ${hotel.hotelName.value},
              ${hotel.hotelLocation.value},
              ${hotel.hotelStatus.toString},
              ${hotel.createdAt}
            )
          """.update.run.void
        _ <- sql"""
          delete from hotel_room_inventories
          where room_type_id in (
            select room_type_id from hotel_room_types where hotel_id = ${hotel.hotelId.value}
          )
        """.update.run
        _ <- sql"delete from hotel_room_types where hotel_id = ${hotel.hotelId.value}".update.run
        _ <- hotel.roomTypes.traverse_ { roomType =>
          for
            _ <- sql"""
              insert into hotel_room_types (
                room_type_id, hotel_id, name, capacity, bed_type, base_price_amount, base_price_currency, status
              ) values (
                ${roomType.roomTypeId.value},
                ${hotel.hotelId.value},
                ${roomType.roomTypeName.value},
                ${roomType.roomCapacity.value},
                ${roomType.bedType.value},
                ${roomType.basePrice.amount},
                ${roomType.basePrice.currency.toString},
                ${roomType.roomTypeStatus.toString}
              )
            """.update.run
            _ <- roomType.roomInventories.traverse_ { roomInventory =>
              sql"""
                insert into hotel_room_inventories (
                  inventory_id, room_type_id, inventory_date, available_rooms, unit_price_amount, unit_price_currency,
                  status, version_number, updated_at
                ) values (
                  ${roomInventory.roomInventoryId.value},
                  ${roomType.roomTypeId.value},
                  ${roomInventory.inventoryDate},
                  ${roomInventory.availableRooms.value},
                  ${roomInventory.unitPrice.amount},
                  ${roomInventory.unitPrice.currency.toString},
                  ${roomInventory.roomInventoryStatus.toString},
                  ${0},
                  ${Option.empty[Instant]}
                )
              """.update.run
            }
          yield ()
        }
      yield ()

    upsertHotel.transact(transactor).as(hotel)

  override def findHotelById(hotelId: HotelId): F[Option[Hotel]] =
    sql"""
      select hotel_id, name, location, status, created_at
      from hotels
      where hotel_id = ${hotelId.value}
    """
      .query[(String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildHotel))

  override def findHotelByRoomTypeId(roomTypeId: RoomTypeId): F[Option[Hotel]] =
    sql"""
      select h.hotel_id, h.name, h.location, h.status, h.created_at
      from hotels h
      inner join hotel_room_types rt on rt.hotel_id = h.hotel_id
      where rt.room_type_id = ${roomTypeId.value}
    """
      .query[(String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildHotel))

  override def searchHotels(hotelSearchCriteria: HotelSearchCriteria): F[List[Hotel]] =
    sql"""
      select hotel_id, name, location, status, created_at
      from hotels
      where (${hotelSearchCriteria.location.map(_.value)} is null or location = ${hotelSearchCriteria.location.map(_.value)})
      order by name, hotel_id
    """
      .query[(String, String, String, String, Instant)]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildHotel))
      .map { hotels =>
        hotelSearchCriteria.stayPeriod match
          case None             => hotels
          case Some(stayPeriod) => hotels.filter(_.hasBookableRoomTypeForStay(stayPeriod))
      }

  private def buildHotel(row: (String, String, String, String, Instant)): F[Hotel] =
    val (hotelIdValue, hotelNameValue, hotelLocationValue, hotelStatusValue, createdAtValue) = row
    for
      hotelName <- Async[F].fromEither(HotelName.create(hotelNameValue))
      hotelLocation <- Async[F].fromEither(HotelLocation.create(hotelLocationValue))
      roomTypes <- loadRoomTypes(HotelId(hotelIdValue))
    yield Hotel.restorePersistedHotel(
      hotelId = HotelId(hotelIdValue),
      hotelName = hotelName,
      hotelLocation = hotelLocation,
      hotelStatus = HotelStatus.valueOf(hotelStatusValue),
      roomTypes = roomTypes,
      createdAt = createdAtValue
    )

  private def loadRoomTypes(hotelId: HotelId): F[Vector[RoomType]] =
    sql"""
      select room_type_id, hotel_id, name, capacity, bed_type, base_price_amount, base_price_currency, status
      from hotel_room_types
      where hotel_id = ${hotelId.value}
      order by room_type_id
    """
      .query[(String, String, String, Int, String, BigDecimal, String, String)]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildRoomType).map(_.toVector))

  private def buildRoomType(row: (String, String, String, Int, String, BigDecimal, String, String)): F[RoomType] =
    val (roomTypeIdValue, hotelIdValue, roomTypeNameValue, capacityValue, bedTypeValue, basePriceAmountValue, basePriceCurrencyValue, statusValue) =
      row
    for
      roomTypeName <- Async[F].fromEither(RoomTypeName.create(roomTypeNameValue))
      roomCapacity <- Async[F].fromEither(Capacity.create(capacityValue))
      bedType <- Async[F].fromEither(BedType.create(bedTypeValue))
      basePriceCurrency <- Async[F].fromEither(Either.catchNonFatal(Currency.valueOf(basePriceCurrencyValue)))
      basePrice <- Async[F].fromEither(Money.create(basePriceAmountValue, basePriceCurrency))
      roomInventories <- loadRoomInventories(RoomTypeId(roomTypeIdValue))
    yield RoomType.restorePersistedRoomType(
      roomTypeId = RoomTypeId(roomTypeIdValue),
      hotelId = HotelId(hotelIdValue),
      roomTypeName = roomTypeName,
      roomCapacity = roomCapacity,
      bedType = bedType,
      basePrice = basePrice,
      roomTypeStatus = RoomTypeStatus.valueOf(statusValue),
      roomInventories = roomInventories
    )

  private def loadRoomInventories(roomTypeId: RoomTypeId): F[Vector[RoomInventory]] =
    sql"""
      select inventory_id, room_type_id, inventory_date, available_rooms, unit_price_amount, unit_price_currency, status
      from hotel_room_inventories
      where room_type_id = ${roomTypeId.value}
      order by inventory_date, inventory_id
    """
      .query[(String, String, LocalDate, Int, BigDecimal, String, String)]
      .to[List]
      .transact(transactor)
      .flatMap(_.traverse(buildRoomInventory).map(_.toVector))

  private def buildRoomInventory(row: (String, String, LocalDate, Int, BigDecimal, String, String)): F[RoomInventory] =
    val (inventoryIdValue, roomTypeIdValue, inventoryDateValue, availableRoomsValue, unitPriceAmountValue, unitPriceCurrencyValue, statusValue) = row
    for
      availableRooms <- Async[F].fromEither(RoomCount.create(availableRoomsValue))
      unitPriceCurrency <- Async[F].fromEither(Either.catchNonFatal(Currency.valueOf(unitPriceCurrencyValue)))
      unitPrice <- Async[F].fromEither(Money.create(unitPriceAmountValue, unitPriceCurrency))
    yield RoomInventory.createRoomInventory(
      roomInventoryId = RoomInventoryId(inventoryIdValue),
      roomTypeId = RoomTypeId(roomTypeIdValue),
      inventoryDate = inventoryDateValue,
      availableRooms = availableRooms,
      unitPrice = unitPrice,
      roomInventoryStatus = RoomInventoryStatus.valueOf(statusValue)
    )
