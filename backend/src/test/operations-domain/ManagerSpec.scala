package com.typesafe.travel.operations.domain

import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.Instant

final class ManagerSpec extends FunSuite:
  private type TestResult[A] = Either[Throwable, A]

  test("inactive airline manager cannot enter manager context") {
    val inactiveManager =
      restorePersistedAirlineManager(
        managerId = ManagerId("manager-airline-1"),
        airlineId = AirlineId("airline-mu"),
        primaryEmailAddress = EmailAddress.unsafe("ops@mu.example"),
        displayName = PersonName.unsafe("MU Ops"),
        managerStatus = ManagerStatus.Inactive,
        createdAt = Instant.parse("2026-03-27T00:00:00Z")
      )

    val managerService = LiveManagerService[TestResult](new StubManagerRepository(Some(inactiveManager), None))

    interceptMessage[ManagerError.ManagerWasInactive]("Airline manager 'manager-airline-1' is inactive") {
      managerService.loginAirlineManager(EmailAddress.unsafe("ops@mu.example")).fold(throw _, identity)
    }
  }

  test("active hotel manager can enter manager context") {
    val activeManager =
      restorePersistedHotelManager(
        managerId = ManagerId("manager-hotel-1"),
        hotelId = HotelId("hotel-hz-westlake"),
        primaryEmailAddress = EmailAddress.unsafe("ops@westlake.example"),
        displayName = PersonName.unsafe("West Lake Ops"),
        managerStatus = ManagerStatus.Active,
        createdAt = Instant.parse("2026-03-27T00:00:00Z")
      )

    val managerService = LiveManagerService[TestResult](new StubManagerRepository(None, Some(activeManager)))

    val loadedManager = managerService.loginHotelManager(EmailAddress.unsafe("ops@westlake.example")).fold(throw _, identity)

    assertEquals(loadedManager.managerId, activeManager.managerId)
  }

final class StubManagerRepository(
    airlineManagerOption: Option[AirlineManager],
    hotelManagerOption: Option[HotelManager],
    attractionManagerOption: Option[AttractionManager] = None
) extends ManagerRepository[[A] =>> Either[Throwable, A]]:
  override def nextManagerId: Either[Throwable, ManagerId] =
    Right(ManagerId("manager-generated"))

  override def findAirlineManagerByEmail(primaryEmailAddress: EmailAddress): Either[Throwable, Option[AirlineManager]] =
    Right(airlineManagerOption.filter(_.primaryEmailAddress == primaryEmailAddress))

  override def findHotelManagerByEmail(primaryEmailAddress: EmailAddress): Either[Throwable, Option[HotelManager]] =
    Right(hotelManagerOption.filter(_.primaryEmailAddress == primaryEmailAddress))

  override def findAirlineManagerById(managerId: ManagerId): Either[Throwable, Option[AirlineManager]] =
    Right(airlineManagerOption.filter(_.managerId == managerId))

  override def findHotelManagerById(managerId: ManagerId): Either[Throwable, Option[HotelManager]] =
    Right(hotelManagerOption.filter(_.managerId == managerId))

  override def findAttractionManagerByEmail(primaryEmailAddress: EmailAddress): Either[Throwable, Option[AttractionManager]] =
    Right(attractionManagerOption.filter(_.primaryEmailAddress == primaryEmailAddress))

  override def findAttractionManagerById(managerId: ManagerId): Either[Throwable, Option[AttractionManager]] =
    Right(attractionManagerOption.filter(_.managerId == managerId))

  override def saveAirlineManager(airlineManager: AirlineManager): Either[Throwable, AirlineManager] =
    Right(airlineManager)

  override def saveHotelManager(hotelManager: HotelManager): Either[Throwable, HotelManager] =
    Right(hotelManager)

  override def saveAttractionManager(attractionManager: AttractionManager): Either[Throwable, AttractionManager] =
    Right(attractionManager)

