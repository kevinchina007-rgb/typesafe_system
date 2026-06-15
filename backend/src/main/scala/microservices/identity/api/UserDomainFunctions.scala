// 本文件定义 identity 用户域的纯辅助函数，只提供可复用的状态流转和对象构造逻辑。
package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

def userMembershipLevelFromPoints(loyaltyPoints: Points): UserMembershipLevel =
  if loyaltyPoints.value >= 50000 then UserMembershipLevel.Platinum
  else if loyaltyPoints.value >= 20000 then UserMembershipLevel.Gold
  else if loyaltyPoints.value >= 5000 then UserMembershipLevel.Silver
  else UserMembershipLevel.Standard

def registerUser(
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

def activateUserAccount(user: User): Either[UserError, User] =
  user.userAccountStatus match
    case currentStatus if currentStatus == UserAccountStatus.PendingActivation || currentStatus == UserAccountStatus.Suspended =>
      Right(user.copy(userAccountStatus = UserAccountStatus.Active))
    case _ =>
      Left(UserError.InvalidUserStateTransition(user.userId, user.userAccountStatus, UserAccountStatus.Active))

def suspendUserAccount(user: User): Either[UserError, User] =
  user.userAccountStatus match
    case currentStatus if currentStatus == UserAccountStatus.Active =>
      Right(user.copy(userAccountStatus = UserAccountStatus.Suspended))
    case _ =>
      Left(UserError.InvalidUserStateTransition(user.userId, user.userAccountStatus, UserAccountStatus.Suspended))

def closeUserAccount(user: User): Either[UserError, User] =
  user.userAccountStatus match
    case currentStatus if currentStatus == UserAccountStatus.Closed =>
      Left(UserError.InvalidUserStateTransition(user.userId, user.userAccountStatus, UserAccountStatus.Closed))
    case _ =>
      Right(user.copy(userAccountStatus = UserAccountStatus.Closed))

def accrueUserLoyaltyPoints(user: User, additionalPoints: Points): Either[UserError, User] =
  user.userAccountStatus match
    case currentStatus if currentStatus == UserAccountStatus.Closed =>
      Left(UserError.CannotAccruePointsForClosedUser(user.userId))
    case _ =>
      val updatedPoints = user.loyaltyPoints.add(additionalPoints)
      Right(user.copy(loyaltyPoints = updatedPoints, membershipLevel = userMembershipLevelFromPoints(updatedPoints)))

def redeemUserLoyaltyPoints(user: User, pointsToRedeem: Points): Either[UserError, User] =
  user.loyaltyPoints
    .subtract(pointsToRedeem)
    .left
    .map(_ => UserError.InsufficientLoyaltyPoints(user.userId, user.loyaltyPoints, pointsToRedeem))
    .map { updatedPoints =>
      user.copy(loyaltyPoints = updatedPoints, membershipLevel = userMembershipLevelFromPoints(updatedPoints))
    }

def assignDefaultTravelerProfile(user: User, travelerId: TravelerId): Either[UserError, User] =
  user.userAccountStatus match
    case currentStatus if currentStatus == UserAccountStatus.Closed =>
      Left(UserError.CannotAssignDefaultTravelerToClosedUser(user.userId))
    case _ =>
      Right(user.copy(defaultTravelerProfileId = Some(travelerId)))

def clearDefaultTravelerProfile(user: User, travelerId: TravelerId): Either[UserError, User] =
  user.defaultTravelerProfileId match
    case Some(existingTravelerId) if existingTravelerId == travelerId => Right(user.copy(defaultTravelerProfileId = None))
    case Some(existingTravelerId) => Left(UserError.DefaultTravelerDidNotMatch(user.userId, existingTravelerId, travelerId))
    case None => Right(user)

def updateAvatarUrl(user: User, nextAvatarUrl: AvatarUrl): Either[UserError, User] =
  user.userAccountStatus match
    case currentStatus if currentStatus == UserAccountStatus.Closed =>
      Left(UserError.CannotUpdateAvatarForClosedUser(user.userId))
    case _ =>
      Right(user.copy(avatarUrl = Some(nextAvatarUrl)))
