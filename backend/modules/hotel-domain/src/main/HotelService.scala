package com.typesafe.travel.hotel.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

trait HotelService[F[_]]:
  def browseHotels(location: Option[HotelLocation], stayPeriod: Option[StayPeriod]): F[List[Hotel]]
  def getHotelDetails(hotelId: HotelId): F[Hotel]

final class LiveHotelService[F[_]: MonadThrow](hotelRepository: HotelRepository[F]) extends HotelService[F]:
  override def browseHotels(location: Option[HotelLocation], stayPeriod: Option[StayPeriod]): F[List[Hotel]] =
    hotelRepository.searchHotels(HotelSearchCriteria(location, stayPeriod))

  override def getHotelDetails(hotelId: HotelId): F[Hotel] =
    hotelRepository.findHotelById(hotelId).flatMap(_.liftTo[F](HotelError.HotelWasNotFound(hotelId)))
