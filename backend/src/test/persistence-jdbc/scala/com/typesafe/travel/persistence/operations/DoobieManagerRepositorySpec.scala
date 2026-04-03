package com.typesafe.travel.persistence.operations

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.operations.domain.{ManagerStatus, ManagerType}
import com.typesafe.travel.persistence.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

final class DoobieManagerRepositorySpec extends FunSuite:
  test("manager repository loads seeded airline and hotel managers") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    val managerRepository = DoobieManagerRepository[cats.effect.IO](transactor)

    val airlineManager =
      managerRepository.findAirlineManagerByEmail(EmailAddress.unsafe("ops@mu.example")).unsafeRunSync()
    val hotelManager =
      managerRepository.findHotelManagerByEmail(EmailAddress.unsafe("ops@westlake.example")).unsafeRunSync()

    assertEquals(airlineManager.map(_.managerId), Some(ManagerId("manager-airline-mu")))
    assertEquals(airlineManager.map(_.managerType), Some(ManagerType.Airline))
    assertEquals(airlineManager.map(_.managerStatus), Some(ManagerStatus.Active))
    assertEquals(airlineManager.map(_.airlineId), Some(AirlineId("airline-mu")))

    assertEquals(hotelManager.map(_.managerId), Some(ManagerId("manager-hotel-westlake")))
    assertEquals(hotelManager.map(_.managerType), Some(ManagerType.Hotel))
    assertEquals(hotelManager.map(_.managerStatus), Some(ManagerStatus.Active))
    assertEquals(hotelManager.map(_.hotelId), Some(HotelId("hotel-hz-westlake")))
  }
