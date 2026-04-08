package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

def registerNewUser(
    userId: UserId,
    primaryEmailAddress: EmailAddress,
    userDisplayName: PersonName,
    userPhoneNumber: ContactNumber,
    registeredAt: Instant
): User =
  User.register(userId, primaryEmailAddress, userDisplayName, userPhoneNumber, registeredAt)


def restorePersistedUser(
    userId: UserId,
    primaryEmailAddress: EmailAddress,
    userDisplayName: PersonName,
    userPhoneNumber: ContactNumber,
    avatarUrl: Option[AvatarUrl],
    userAccountStatus: UserAccountStatus,
    membershipLevel: UserMembershipLevel,
    loyaltyPoints: Points,
    defaultTravelerProfileId: Option[TravelerId],
    registeredAt: Instant
): User =
  User.restore(
    userId,
    primaryEmailAddress,
    userDisplayName,
    userPhoneNumber,
    avatarUrl,
    userAccountStatus,
    membershipLevel,
    loyaltyPoints,
    defaultTravelerProfileId,
    registeredAt
  )
