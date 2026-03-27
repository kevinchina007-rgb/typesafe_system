package com.typesafe.travel.persistence.operations

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.time.Instant
import java.util.UUID

final class DoobieManagerRepository[F[_]: Async](
    transactor: Transactor[F]
) extends ManagerRepository[F]:
  override def nextManagerId: F[ManagerId] =
    Async[F].delay(ManagerId(s"manager-${UUID.randomUUID().toString.take(12)}"))

  override def findAirlineManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[AirlineManager]] =
    sql"""
      select manager_id, airline_id, email, display_name, status, created_at
      from airline_managers
      where email = ${primaryEmailAddress.value}
    """
      .query[(String, String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildAirlineManager))

  override def findHotelManagerByEmail(primaryEmailAddress: EmailAddress): F[Option[HotelManager]] =
    sql"""
      select manager_id, hotel_id, email, display_name, status, created_at
      from hotel_managers
      where email = ${primaryEmailAddress.value}
    """
      .query[(String, String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildHotelManager))

  override def findAirlineManagerById(managerId: ManagerId): F[Option[AirlineManager]] =
    sql"""
      select manager_id, airline_id, email, display_name, status, created_at
      from airline_managers
      where manager_id = ${managerId.value}
    """
      .query[(String, String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildAirlineManager))

  override def findHotelManagerById(managerId: ManagerId): F[Option[HotelManager]] =
    sql"""
      select manager_id, hotel_id, email, display_name, status, created_at
      from hotel_managers
      where manager_id = ${managerId.value}
    """
      .query[(String, String, String, String, String, Instant)]
      .option
      .transact(transactor)
      .flatMap(_.traverse(buildHotelManager))

  override def saveAirlineManager(airlineManager: AirlineManager): F[AirlineManager] =
    sql"""
      insert into airline_managers (manager_id, airline_id, email, display_name, status, created_at)
      values (
        ${airlineManager.managerId.value},
        ${airlineManager.airlineId.value},
        ${airlineManager.primaryEmailAddress.value},
        ${airlineManager.displayName.value},
        ${airlineManager.managerStatus.toString},
        ${airlineManager.createdAt}
      )
    """.update.run.transact(transactor).as(airlineManager)

  override def saveHotelManager(hotelManager: HotelManager): F[HotelManager] =
    sql"""
      insert into hotel_managers (manager_id, hotel_id, email, display_name, status, created_at)
      values (
        ${hotelManager.managerId.value},
        ${hotelManager.hotelId.value},
        ${hotelManager.primaryEmailAddress.value},
        ${hotelManager.displayName.value},
        ${hotelManager.managerStatus.toString},
        ${hotelManager.createdAt}
      )
    """.update.run.transact(transactor).as(hotelManager)

  private def buildAirlineManager(row: (String, String, String, String, String, Instant)): F[AirlineManager] =
    val (managerIdValue, airlineIdValue, emailValue, displayNameValue, statusValue, createdAtValue) = row
    for
      primaryEmailAddress <- Async[F].fromEither(EmailAddress.create(emailValue))
      displayName <- Async[F].fromEither(PersonName.create(displayNameValue))
    yield AirlineManager.restorePersistedAirlineManager(
      managerId = ManagerId(managerIdValue),
      airlineId = AirlineId(airlineIdValue),
      primaryEmailAddress = primaryEmailAddress,
      displayName = displayName,
      managerStatus = ManagerStatus.valueOf(statusValue),
      createdAt = createdAtValue
    )

  private def buildHotelManager(row: (String, String, String, String, String, Instant)): F[HotelManager] =
    val (managerIdValue, hotelIdValue, emailValue, displayNameValue, statusValue, createdAtValue) = row
    for
      primaryEmailAddress <- Async[F].fromEither(EmailAddress.create(emailValue))
      displayName <- Async[F].fromEither(PersonName.create(displayNameValue))
    yield HotelManager.restorePersistedHotelManager(
      managerId = ManagerId(managerIdValue),
      hotelId = HotelId(hotelIdValue),
      primaryEmailAddress = primaryEmailAddress,
      displayName = displayName,
      managerStatus = ManagerStatus.valueOf(statusValue),
      createdAt = createdAtValue
    )
