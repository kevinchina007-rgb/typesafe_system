package com.typesafe.travel.operations.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.Instant

trait ManagerRepository[F[_]]:
  def nextManagerId: F[ManagerId]
  def findAirlineManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[AirlineManager]]
  def findHotelManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[HotelManager]]
  def findAttractionManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[AttractionManager]]
  def findAirlineManagerById(managerId: ManagerId): F[Option[AirlineManager]]
  def findHotelManagerById(managerId: ManagerId): F[Option[HotelManager]]
  def findAttractionManagerById(managerId: ManagerId): F[Option[AttractionManager]]
  def saveAirlineManager(airlineManager: AirlineManager): F[AirlineManager]
  def saveHotelManager(hotelManager: HotelManager): F[HotelManager]
  def saveAttractionManager(attractionManager: AttractionManager): F[AttractionManager]

trait ManagerService[F[_]]:
  def registerAirlineManager(airlineId: AirlineId, primaryEmailAddress: EmailAddress, displayName: PersonName, createdAt: Instant): F[AirlineManager]
  def registerHotelManager(hotelId: HotelId, primaryEmailAddress: EmailAddress, displayName: PersonName, createdAt: Instant): F[HotelManager]
  def registerAttractionManager(primaryEmailAddress: EmailAddress, displayName: PersonName, createdAt: Instant): F[AttractionManager]
  def loginAirlineManager(primaryEmailAddress: EmailAddress): F[AirlineManager]
  def loginHotelManager(primaryEmailAddress: EmailAddress): F[HotelManager]
  def loginAttractionManager(primaryEmailAddress: EmailAddress): F[AttractionManager]
  def loadAirlineManager(managerId: ManagerId): F[AirlineManager]
  def loadHotelManager(managerId: ManagerId): F[HotelManager]
  def loadAttractionManager(managerId: ManagerId): F[AttractionManager]

final class LiveManagerService[F[_]: MonadThrow](
    managerRepository: ManagerRepository[F]
) extends ManagerService[F]:
  override def registerAirlineManager(
      airlineId: AirlineId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[AirlineManager] =
    ensureManagerEmailAvailable(primaryEmailAddress) *>
      managerRepository.nextManagerId.flatMap { managerId =>
        managerRepository.saveAirlineManager(
          AirlineManager.register(managerId, airlineId, primaryEmailAddress, displayName, createdAt)
        )
      }

  override def registerHotelManager(
      hotelId: HotelId,
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[HotelManager] =
    ensureManagerEmailAvailable(primaryEmailAddress) *>
      managerRepository.nextManagerId.flatMap { managerId =>
        managerRepository.saveHotelManager(
          HotelManager.register(managerId, hotelId, primaryEmailAddress, displayName, createdAt)
        )
      }

  override def registerAttractionManager(
      primaryEmailAddress: EmailAddress,
      displayName: PersonName,
      createdAt: Instant
  ): F[AttractionManager] =
    ensureManagerEmailAvailable(primaryEmailAddress) *>
      managerRepository.nextManagerId.flatMap { managerId =>
        managerRepository.saveAttractionManager(
          AttractionManager.register(managerId, primaryEmailAddress, displayName, createdAt)
        )
      }

  override def loginAirlineManager(primaryEmailAddress: EmailAddress): F[AirlineManager] =
    managerRepository
      .findAirlineManagerByEmail(primaryEmailAddress)
      .flatMap(_.liftTo[F](ManagerError.ManagerWasNotFoundByEmail(ManagerType.Airline, primaryEmailAddress)))
      .flatMap(validateActiveManager)

  override def loginHotelManager(primaryEmailAddress: EmailAddress): F[HotelManager] =
    managerRepository
      .findHotelManagerByEmail(primaryEmailAddress)
      .flatMap(_.liftTo[F](ManagerError.ManagerWasNotFoundByEmail(ManagerType.Hotel, primaryEmailAddress)))
      .flatMap(validateActiveManager)

  override def loginAttractionManager(primaryEmailAddress: EmailAddress): F[AttractionManager] =
    managerRepository
      .findAttractionManagerByEmail(primaryEmailAddress)
      .flatMap(_.liftTo[F](ManagerError.ManagerWasNotFoundByEmail(ManagerType.Attraction, primaryEmailAddress)))
      .flatMap(validateActiveManager)

  override def loadAirlineManager(managerId: ManagerId): F[AirlineManager] =
    managerRepository
      .findAirlineManagerById(managerId)
      .flatMap(_.liftTo[F](ManagerError.ManagerWasNotFoundById(ManagerType.Airline, managerId)))
      .flatMap(validateActiveManager)

  override def loadHotelManager(managerId: ManagerId): F[HotelManager] =
    managerRepository
      .findHotelManagerById(managerId)
      .flatMap(_.liftTo[F](ManagerError.ManagerWasNotFoundById(ManagerType.Hotel, managerId)))
      .flatMap(validateActiveManager)

  override def loadAttractionManager(managerId: ManagerId): F[AttractionManager] =
    managerRepository
      .findAttractionManagerById(managerId)
      .flatMap(_.liftTo[F](ManagerError.ManagerWasNotFoundById(ManagerType.Attraction, managerId)))
      .flatMap(validateActiveManager)

  private def validateActiveManager[A <: ManagerContext](managerContext: A): F[A] =
    managerContext.managerStatus match
      case ManagerStatus.Active   => managerContext.pure[F]
      case ManagerStatus.Inactive => MonadThrow[F].raiseError(ManagerError.ManagerWasInactive(managerContext.managerType, managerContext.managerId))

  private def ensureManagerEmailAvailable(primaryEmailAddress: EmailAddress): F[Unit] =
    (
      managerRepository.findAirlineManagerByEmail(primaryEmailAddress),
      managerRepository.findHotelManagerByEmail(primaryEmailAddress),
      managerRepository.findAttractionManagerByEmail(primaryEmailAddress)
    ).mapN { (existingAirlineManager, existingHotelManager, existingAttractionManager) =>
      if existingAirlineManager.isDefined || existingHotelManager.isDefined || existingAttractionManager.isDefined then
        Left(ManagerError.ManagerEmailAlreadyExists(primaryEmailAddress))
      else Right(())
    }.flatMap(_.liftTo[F])
