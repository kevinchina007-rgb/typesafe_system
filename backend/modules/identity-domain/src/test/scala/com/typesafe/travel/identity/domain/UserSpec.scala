package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*
import munit.FunSuite
import java.time.Instant

final class UserSpec extends FunSuite:

  private val registeredAtInstant = Instant.parse("2026-03-25T00:00:00Z")

  test("new user starts pending activation with standard membership") {
    val newUser =
      User.registerNewUser(
        userId = UserId("user-1"),
        primaryEmailAddress = EmailAddress.unsafe("ada@example.com"),
        userDisplayName = PersonName.unsafe("Ada Lovelace"),
        userPhoneNumber = ContactNumber.unsafe("+15550000001"),
        registeredAt = registeredAtInstant
      )

    assertEquals(newUser.userAccountStatus, UserAccountStatus.PendingActivation)
    assertEquals(newUser.membershipLevel, UserMembershipLevel.Standard)
    assertEquals(newUser.loyaltyPoints, Points.zero)
  }

  test("accruing points upgrades membership level") {
    val activeUser =
      User
        .registerNewUser(
          userId = UserId("user-2"),
          primaryEmailAddress = EmailAddress.unsafe("grace@example.com"),
          userDisplayName = PersonName.unsafe("Grace Hopper"),
          userPhoneNumber = ContactNumber.unsafe("+15550000002"),
          registeredAt = registeredAtInstant
        )
        .activateUserAccount
        .toOption
        .get

    val upgradedUser = activeUser.accrueUserLoyaltyPoints(Points.unsafe(25000))

    assertEquals(upgradedUser.map(_.membershipLevel), Right(UserMembershipLevel.Gold))
  }

  test("closed user cannot assign default traveler") {
    val closedUser =
      User
        .registerNewUser(
          userId = UserId("user-3"),
          primaryEmailAddress = EmailAddress.unsafe("linus@example.com"),
          userDisplayName = PersonName.unsafe("Linus Torvalds"),
          userPhoneNumber = ContactNumber.unsafe("+15550000003"),
          registeredAt = registeredAtInstant
        )
        .closeUserAccount
        .toOption
        .get

    val result = closedUser.assignDefaultTravelerProfile(TravelerId("traveler-1"))

    assert(result.swap.exists(_.isInstanceOf[UserError.CannotAssignDefaultTravelerToClosedUser]))
  }

  test("active user can update avatar url") {
    val activeUser =
      User
        .registerNewUser(
          userId = UserId("user-4"),
          primaryEmailAddress = EmailAddress.unsafe("margaret@example.com"),
          userDisplayName = PersonName.unsafe("Margaret Hamilton"),
          userPhoneNumber = ContactNumber.unsafe("+15550000004"),
          registeredAt = registeredAtInstant
        )
        .activateUserAccount
        .toOption
        .get

    val updatedUser = activeUser.updateAvatarUrl(AvatarUrl.unsafe("/uploads/avatars/user-4-avatar.png"))

    assertEquals(updatedUser.map(_.avatarUrl.map(_.value)), Right(Some("/uploads/avatars/user-4-avatar.png")))
  }
