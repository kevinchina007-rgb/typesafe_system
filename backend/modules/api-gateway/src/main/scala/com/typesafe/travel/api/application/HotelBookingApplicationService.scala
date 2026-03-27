package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.hotel.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerProfileRepository
import java.time.LocalDate

enum HotelBookingApplicationError(val message: String) extends DomainError:
  case OrderWasNotOwnedByUser(orderId: OrderId, actingUserId: UserId)
      extends HotelBookingApplicationError(
        s"Order '${orderId.value}' does not belong to user '${actingUserId.value}'"
      )
  case TravelerSelectionWasInvalid(reason: String)
      extends HotelBookingApplicationError(reason)
  case StayPeriodWasInvalid(checkInDate: LocalDate, checkOutDate: LocalDate)
      extends HotelBookingApplicationError(
        s"Stay period '$checkInDate' to '$checkOutDate' is not valid"
      )
  case RoomCountWasInvalid(roomCount: Int)
      extends HotelBookingApplicationError(
        s"Room count '$roomCount' is not valid"
      )
  case RoomTypeWasNotFound(roomTypeId: RoomTypeId)
      extends HotelBookingApplicationError(
        s"Room type '${roomTypeId.value}' was not found"
      )
  case RoomInventoryWasNotBookable(roomTypeId: RoomTypeId, checkInDate: LocalDate, checkOutDate: LocalDate)
      extends HotelBookingApplicationError(
        s"Room type '${roomTypeId.value}' is not available for '$checkInDate' to '$checkOutDate'"
      )
  case RoomCapacityWasExceeded(roomTypeId: RoomTypeId, allowedGuestCount: Int, actualGuestCount: Int)
      extends HotelBookingApplicationError(
        s"Room type '${roomTypeId.value}' allows only $allowedGuestCount guests but received $actualGuestCount"
      )

trait HotelBookingApplicationService[F[_]]:
  def browseHotels(
      locationQuery: Option[String],
      stayPeriod: Option[StayPeriod]
  ): F[List[Hotel]]
  def getHotelDetails(hotelId: HotelId): F[Hotel]
  def createHotelOrder(
      actingUserId: UserId,
      roomTypeId: RoomTypeId,
      guestTravelerIds: List[TravelerId],
      checkInDate: LocalDate,
      checkOutDate: LocalDate,
      roomCount: RoomCount
  ): F[Order]

final class LiveHotelBookingApplicationService[F[_]: MonadThrow](
    hotelService: HotelService[F],
    hotelRepository: HotelRepository[F],
    orderService: OrderService[F],
    orderRepository: OrderRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F]
) extends HotelBookingApplicationService[F]:
  override def browseHotels(
      locationQuery: Option[String],
      stayPeriod: Option[StayPeriod]
  ): F[List[Hotel]] =
    hotelService
      .browseHotels(None, stayPeriod)
      .map(_.filter(hotelMatchesSearch(_, locationQuery)))

  override def getHotelDetails(hotelId: HotelId): F[Hotel] =
    hotelService.getHotelDetails(hotelId)

  override def createHotelOrder(
      actingUserId: UserId,
      roomTypeId: RoomTypeId,
      guestTravelerIds: List[TravelerId],
      checkInDate: LocalDate,
      checkOutDate: LocalDate,
      roomCount: RoomCount
  ): F[Order] =
    for
      stayPeriod <- StayPeriod.create(checkInDate, checkOutDate).leftMap(_ => HotelBookingApplicationError.StayPeriodWasInvalid(checkInDate, checkOutDate)).liftTo[F]
      _ <- ensureRoomCount(roomCount)
      validatedTravelerIds <- validateTravelerSelection(guestTravelerIds)
      travelerProfiles <- validatedTravelerIds.traverse(loadOwnedTravelerProfile(actingUserId, _))
      hotel <- hotelRepository.findHotelByRoomTypeId(roomTypeId).flatMap(_.liftTo[F](HotelError.RoomTypeWasNotFound(roomTypeId)))
      roomType <- hotel.findRoomTypeById(roomTypeId).leftMap {
        case HotelError.RoomTypeWasNotFound(missingRoomTypeId) => HotelBookingApplicationError.RoomTypeWasNotFound(missingRoomTypeId)
        case otherHotelError                                   => otherHotelError
      }.liftTo[F]
      roomInventories <- hotel.ensureRoomTypeBookableForStay(roomTypeId, stayPeriod, roomCount, travelerProfiles.size).leftMap {
        case HotelError.RoomTypeWasNotFound(missingRoomTypeId) =>
          HotelBookingApplicationError.RoomTypeWasNotFound(missingRoomTypeId)
        case HotelError.RoomInventoryWasMissing(_, _) | HotelError.RoomInventoryWasNotBookable(_, _) =>
          HotelBookingApplicationError.RoomInventoryWasNotBookable(roomTypeId, checkInDate, checkOutDate)
        case HotelError.GuestCapacityWasExceeded(_, allowedGuestCount, actualGuestCount) =>
          HotelBookingApplicationError.RoomCapacityWasExceeded(roomTypeId, allowedGuestCount, actualGuestCount)
        case otherHotelError =>
          otherHotelError
      }.liftTo[F]
      totalPriceSnapshot <- calculateTotalStayPrice(roomType.roomTypeId, roomInventories, roomCount, checkInDate, checkOutDate)
      createdOrder <- orderService.createDraftOrder(
        ownerUserId = actingUserId,
        orderCurrency = totalPriceSnapshot.currency,
        createdAt = java.time.Instant.now()
      )
      updatedOrder <- orderService.addHotelOrderItem(
        orderId = createdOrder.orderId,
        hotelBookingSnapshot = HotelBookingSnapshot(
          hotelId = hotel.hotelId,
          hotelName = hotel.hotelName,
          hotelLocation = hotel.hotelLocation,
          roomTypeId = roomType.roomTypeId,
          roomTypeName = roomType.roomTypeName,
          stayPeriod = stayPeriod,
          guestTravelerIds = travelerProfiles.map(_.travelerId).toVector,
          roomCount = roomCount,
          unitPriceSnapshot = roomInventories.headOption.map(_.unitPrice).getOrElse(roomType.basePrice),
          totalPriceSnapshot = totalPriceSnapshot
        ),
        bookedMoney = totalPriceSnapshot
      )
    yield updatedOrder

  private def validateTravelerSelection(guestTravelerIds: List[TravelerId]): F[List[TravelerId]] =
    if guestTravelerIds.isEmpty then
      MonadThrow[F].raiseError(HotelBookingApplicationError.TravelerSelectionWasInvalid("At least one guest must be selected"))
    else if guestTravelerIds.distinct.size != guestTravelerIds.size then
      MonadThrow[F].raiseError(HotelBookingApplicationError.TravelerSelectionWasInvalid("Guest selection contains duplicates"))
    else MonadThrow[F].pure(guestTravelerIds)

  private def ensureRoomCount(roomCount: RoomCount): F[Unit] =
    if roomCount.value > 0 then MonadThrow[F].unit
    else MonadThrow[F].raiseError(HotelBookingApplicationError.RoomCountWasInvalid(roomCount.value))

  private def loadOwnedTravelerProfile(
      actingUserId: UserId,
      travelerId: TravelerId
  ): F[com.typesafe.travel.traveler.domain.TravelerProfile] =
    travelerProfileRepository.findTravelerProfileById(travelerId).flatMap {
      case Some(travelerProfile) if travelerProfile.ownerUserId == actingUserId =>
        MonadThrow[F].pure(travelerProfile)
      case _ =>
        MonadThrow[F].raiseError(
          HotelBookingApplicationError.TravelerSelectionWasInvalid(
            s"Traveler '${travelerId.value}' is not available for user '${actingUserId.value}'"
          )
        )
    }

  private def calculateTotalStayPrice(
      roomTypeId: RoomTypeId,
      roomInventories: Vector[RoomInventory],
      roomCount: RoomCount,
      checkInDate: LocalDate,
      checkOutDate: LocalDate
  ): F[Money] =
    roomInventories.toList
      .traverse(_.unitPrice.multiply(roomCount.value).liftTo[F])
      .flatMap {
        case Nil => MonadThrow[F].raiseError(HotelBookingApplicationError.RoomInventoryWasNotBookable(roomTypeId, checkInDate, checkOutDate))
        case firstNightPrice :: remainingNightPrices =>
          remainingNightPrices.foldLeft(MonadThrow[F].pure(firstNightPrice)) { (accumulatedMoneyF, nextNightMoney) =>
            accumulatedMoneyF.flatMap(_.add(nextNightMoney).liftTo[F])
          }
      }

  private def hotelMatchesSearch(hotel: Hotel, locationQuery: Option[String]): Boolean =
    locationQuery.forall(queryText => TravelSearchAliases.hasUsableKeyword(queryText) && TravelSearchAliases.matchesHotelLocationQuery(hotel.hotelLocation, queryText))
