package com.typesafe.travel.persistence.identity

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.persistence.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite

import java.time.Instant

final class DoobieUserRepositorySpec extends FunSuite:
  test("user repository round-trips saved user with avatar and default traveler") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    val repository = DoobieUserRepository[cats.effect.IO](transactor)
    val savedUser =
      restorePersistedUser(
        userId = UserId("user-roundtrip"),
        primaryEmailAddress = EmailAddress.unsafe("roundtrip@example.com"),
        userDisplayName = PersonName.unsafe("Round Trip"),
        userPhoneNumber = ContactNumber.unsafe("+15550000001"),
        avatarUrl = Some(AvatarUrl.unsafe("/uploads/avatars/user-roundtrip.png")),
        userAccountStatus = UserAccountStatus.Active,
        membershipLevel = UserMembershipLevel.Gold,
        loyaltyPoints = Points.unsafe(23000),
        defaultTravelerProfileId = Some(TravelerId("traveler-default")),
        registeredAt = Instant.parse("2026-03-26T01:00:00Z")
      )

    repository.saveUser(savedUser).unsafeRunSync()
    val loadedUser = repository.findByUserId(savedUser.userId).unsafeRunSync()

    assertEquals(loadedUser, Some(savedUser))
  }

