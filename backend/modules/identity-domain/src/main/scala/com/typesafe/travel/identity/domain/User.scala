package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

enum UserAccountStatus:
  case PendingActivation, Active, Suspended, Closed

enum UserMembershipLevel:
  case Standard, Silver, Gold, Platinum

object UserMembershipLevel:
  def fromPoints(loyaltyPoints: Points): UserMembershipLevel =
    if loyaltyPoints.value >= 50000 then UserMembershipLevel.Platinum
    else if loyaltyPoints.value >= 20000 then UserMembershipLevel.Gold
    else if loyaltyPoints.value >= 5000 then UserMembershipLevel.Silver
    else UserMembershipLevel.Standard

final case class User private (
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
):
  def activateUserAccount: Either[UserError, User] =
    userAccountStatus match
      case UserAccountStatus.PendingActivation | UserAccountStatus.Suspended =>
        Right(copy(userAccountStatus = UserAccountStatus.Active))
      case _ =>
        Left(UserError.InvalidUserStateTransition(userId, userAccountStatus, UserAccountStatus.Active))

  def suspendUserAccount: Either[UserError, User] =
    userAccountStatus match
      case UserAccountStatus.Active =>
        Right(copy(userAccountStatus = UserAccountStatus.Suspended))
      case _ =>
        Left(UserError.InvalidUserStateTransition(userId, userAccountStatus, UserAccountStatus.Suspended))

  def closeUserAccount: Either[UserError, User] =
    userAccountStatus match
      case UserAccountStatus.Closed =>
        Left(UserError.InvalidUserStateTransition(userId, userAccountStatus, UserAccountStatus.Closed))
      case _ =>
        Right(copy(userAccountStatus = UserAccountStatus.Closed))

  def accrueUserLoyaltyPoints(additionalPoints: Points): Either[UserError, User] =
    userAccountStatus match
      case UserAccountStatus.Closed =>
        Left(UserError.CannotAccruePointsForClosedUser(userId))
      case _ =>
        val updatedPoints = loyaltyPoints.add(additionalPoints)
        Right(copy(loyaltyPoints = updatedPoints, membershipLevel = UserMembershipLevel.fromPoints(updatedPoints)))

  def redeemUserLoyaltyPoints(pointsToRedeem: Points): Either[UserError, User] =
    loyaltyPoints
      .subtract(pointsToRedeem)
      .left
      .map(_ => UserError.InsufficientLoyaltyPoints(userId, loyaltyPoints, pointsToRedeem))
      .map { updatedPoints =>
        copy(loyaltyPoints = updatedPoints, membershipLevel = UserMembershipLevel.fromPoints(updatedPoints))
      }

  def assignDefaultTravelerProfile(travelerId: TravelerId): Either[UserError, User] =
    userAccountStatus match
      case UserAccountStatus.Closed =>
        Left(UserError.CannotAssignDefaultTravelerToClosedUser(userId))
      case _ =>
        Right(copy(defaultTravelerProfileId = Some(travelerId)))

  def clearDefaultTravelerProfile(travelerId: TravelerId): Either[UserError, User] =
    defaultTravelerProfileId match
      case Some(existingTravelerId) if existingTravelerId == travelerId =>
        Right(copy(defaultTravelerProfileId = None))
      case Some(existingTravelerId) =>
        Left(UserError.DefaultTravelerDidNotMatch(userId, existingTravelerId, travelerId))
      case None =>
        Right(this)

  def updateAvatarUrl(nextAvatarUrl: AvatarUrl): Either[UserError, User] =
    userAccountStatus match
      case UserAccountStatus.Closed =>
        Left(UserError.CannotUpdateAvatarForClosedUser(userId))
      case _ =>
        Right(copy(avatarUrl = Some(nextAvatarUrl)))

object User:
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

enum UserError(val message: String) extends DomainError:
  case UserWasNotFound(userId: UserId)
      extends UserError(s"User '${userId.value}' was not found")
  case UserWasNotFoundByEmail(primaryEmailAddress: EmailAddress)
      extends UserError(s"User email '${primaryEmailAddress.value}' was not found")
  case UserEmailAddressAlreadyExists(primaryEmailAddress: EmailAddress)
      extends UserError(s"User email '${primaryEmailAddress.value}' already exists")
  case InvalidUserStateTransition(
      userId: UserId,
      currentStatus: UserAccountStatus,
      targetStatus: UserAccountStatus
  ) extends UserError(s"User '${userId.value}' cannot transition from $currentStatus to $targetStatus")
  case CannotAccruePointsForClosedUser(userId: UserId)
      extends UserError(s"Closed user '${userId.value}' cannot accrue loyalty points")
  case InsufficientLoyaltyPoints(userId: UserId, currentPoints: Points, requestedPoints: Points)
      extends UserError(
        s"User '${userId.value}' has only ${currentPoints.value} points and cannot redeem ${requestedPoints.value}"
      )
  case CannotAssignDefaultTravelerToClosedUser(userId: UserId)
      extends UserError(s"Closed user '${userId.value}' cannot assign a default traveler")
  case DefaultTravelerDidNotMatch(userId: UserId, currentTravelerId: TravelerId, requestedTravelerId: TravelerId)
      extends UserError(
        s"User '${userId.value}' default traveler '${currentTravelerId.value}' did not match '${requestedTravelerId.value}'"
      )
  case CannotUpdateAvatarForClosedUser(userId: UserId)
      extends UserError(s"Closed user '${userId.value}' cannot update avatar")
