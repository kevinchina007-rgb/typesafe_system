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
  User(
    userId = userId,
    primaryEmailAddress = primaryEmailAddress,
    userDisplayName = userDisplayName,
    userPhoneNumber = userPhoneNumber,
    avatarUrl = None,
    userAccountStatus = UserAccountStatus.PendingActivation,
    membershipLevel = UserMembershipLevel.Standard,
    loyaltyPoints = Points.zero,
    defaultTravelerProfileId = None,
    registeredAt = registeredAt
  )


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
  User(
    userId = userId,
    primaryEmailAddress = primaryEmailAddress,
    userDisplayName = userDisplayName,
    userPhoneNumber = userPhoneNumber,
    avatarUrl = avatarUrl,
    userAccountStatus = userAccountStatus,
    membershipLevel = membershipLevel,
    loyaltyPoints = loyaltyPoints,
    defaultTravelerProfileId = defaultTravelerProfileId,
    registeredAt = registeredAt
  )
