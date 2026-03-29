package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryManagerRepository[F[_]: Sync] private (
    airlineManagerState: TrieMap[ManagerId, AirlineManager],
    hotelManagerState: TrieMap[ManagerId, HotelManager],
    attractionManagerState: TrieMap[ManagerId, AttractionManager],
    managerSequence: AtomicLong
) extends ManagerRepository[F]:
  override def nextManagerId: F[ManagerId] =
    Sync[F].delay(ManagerId(s"manager-${managerSequence.incrementAndGet()}"))

  override def findAirlineManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[AirlineManager]] =
    Sync[F].delay(airlineManagerState.values.find(_.primaryEmailAddress == primaryEmailAddress))

  override def findHotelManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[HotelManager]] =
    Sync[F].delay(hotelManagerState.values.find(_.primaryEmailAddress == primaryEmailAddress))

  override def findAttractionManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[AttractionManager]] =
    Sync[F].delay(attractionManagerState.values.find(_.primaryEmailAddress == primaryEmailAddress))

  override def findAirlineManagerById(managerId: ManagerId): F[Option[AirlineManager]] =
    Sync[F].delay(airlineManagerState.get(managerId))

  override def findHotelManagerById(managerId: ManagerId): F[Option[HotelManager]] =
    Sync[F].delay(hotelManagerState.get(managerId))

  override def findAttractionManagerById(managerId: ManagerId): F[Option[AttractionManager]] =
    Sync[F].delay(attractionManagerState.get(managerId))

  override def saveAirlineManager(airlineManager: AirlineManager): F[AirlineManager] =
    Sync[F].delay {
      airlineManagerState.put(airlineManager.managerId, airlineManager)
      airlineManager
    }

  override def saveHotelManager(hotelManager: HotelManager): F[HotelManager] =
    Sync[F].delay {
      hotelManagerState.put(hotelManager.managerId, hotelManager)
      hotelManager
    }

  override def saveAttractionManager(attractionManager: AttractionManager): F[AttractionManager] =
    Sync[F].delay {
      attractionManagerState.put(attractionManager.managerId, attractionManager)
      attractionManager
    }

object InMemoryManagerRepository:
  def create[F[_]: Sync]: InMemoryManagerRepository[F] =
    val createdAtInstant = Instant.parse("2026-03-27T00:00:00Z")
    new InMemoryManagerRepository[F](
      airlineManagerState = TrieMap(
        ManagerId("manager-airline-mu") ->
          AirlineManager.restorePersistedAirlineManager(
            managerId = ManagerId("manager-airline-mu"),
            airlineId = AirlineId("airline-mu"),
            primaryEmailAddress = EmailAddress.unsafe("ops@mu.example"),
            displayName = PersonName.unsafe("China Eastern Ops"),
            managerStatus = ManagerStatus.Active,
            createdAt = createdAtInstant
          ),
        ManagerId("manager-airline-9c") ->
          AirlineManager.restorePersistedAirlineManager(
            managerId = ManagerId("manager-airline-9c"),
            airlineId = AirlineId("airline-9c"),
            primaryEmailAddress = EmailAddress.unsafe("ops@9c.example"),
            displayName = PersonName.unsafe("Spring Airlines Ops"),
            managerStatus = ManagerStatus.Active,
            createdAt = createdAtInstant
          )
      ),
      hotelManagerState = TrieMap(
        ManagerId("manager-hotel-westlake") ->
          HotelManager.restorePersistedHotelManager(
            managerId = ManagerId("manager-hotel-westlake"),
            hotelId = HotelId("hotel-hz-westlake"),
            primaryEmailAddress = EmailAddress.unsafe("ops@westlake.example"),
            displayName = PersonName.unsafe("West Lake Ops"),
            managerStatus = ManagerStatus.Active,
            createdAt = createdAtInstant
          ),
        ManagerId("manager-hotel-bund") ->
          HotelManager.restorePersistedHotelManager(
            managerId = ManagerId("manager-hotel-bund"),
            hotelId = HotelId("hotel-sh-bund"),
            primaryEmailAddress = EmailAddress.unsafe("ops@bund.example"),
            displayName = PersonName.unsafe("Bund Hotel Ops"),
            managerStatus = ManagerStatus.Active,
            createdAt = createdAtInstant
          )
      ),
      attractionManagerState = TrieMap.empty,
      managerSequence = AtomicLong(100)
    )
