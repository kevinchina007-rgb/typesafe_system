package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*
import java.time.Instant

final case class UserAccountStatus(value: String):
  override def toString: String = value

object UserAccountStatus:
  val PendingActivation: UserAccountStatus = UserAccountStatus("PendingActivation")
  val Active: UserAccountStatus = UserAccountStatus("Active")
  val Suspended: UserAccountStatus = UserAccountStatus("Suspended")
  val Closed: UserAccountStatus = UserAccountStatus("Closed")

  def fromText(value: String): UserAccountStatus =
    value.trim.toLowerCase match
      case "pendingactivation" | "pending_activation" => PendingActivation
      case "active" => Active
      case "suspended" => Suspended
      case "closed" => Closed
      case _ => PendingActivation

final case class UserMembershipLevel(value: String):
  override def toString: String = value

object UserMembershipLevel:
  val Standard: UserMembershipLevel = UserMembershipLevel("Standard")
  val Silver: UserMembershipLevel = UserMembershipLevel("Silver")
  val Gold: UserMembershipLevel = UserMembershipLevel("Gold")
  val Platinum: UserMembershipLevel = UserMembershipLevel("Platinum")

  def fromText(value: String): UserMembershipLevel =
    value.trim.toLowerCase match
      case "silver" => Silver
      case "gold" => Gold
      case "platinum" => Platinum
      case _ => Standard

  def fromPoints(loyaltyPoints: Points): UserMembershipLevel =
    if loyaltyPoints.value >= 50000 then Platinum
    else if loyaltyPoints.value >= 20000 then Gold
    else if loyaltyPoints.value >= 5000 then Silver
    else Standard

final case class User(
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
      case currentStatus if currentStatus == UserAccountStatus.PendingActivation || currentStatus == UserAccountStatus.Suspended =>
        Right(copy(userAccountStatus = UserAccountStatus.Active))
      case _ =>
        Left(UserError.InvalidUserStateTransition(userId, userAccountStatus, UserAccountStatus.Active))

  def suspendUserAccount: Either[UserError, User] =
    userAccountStatus match
      case currentStatus if currentStatus == UserAccountStatus.Active =>
        Right(copy(userAccountStatus = UserAccountStatus.Suspended))
      case _ =>
        Left(UserError.InvalidUserStateTransition(userId, userAccountStatus, UserAccountStatus.Suspended))

  def closeUserAccount: Either[UserError, User] =
    userAccountStatus match
      case currentStatus if currentStatus == UserAccountStatus.Closed =>
        Left(UserError.InvalidUserStateTransition(userId, userAccountStatus, UserAccountStatus.Closed))
      case _ =>
        Right(copy(userAccountStatus = UserAccountStatus.Closed))

  def accrueUserLoyaltyPoints(additionalPoints: Points): Either[UserError, User] =
    userAccountStatus match
      case currentStatus if currentStatus == UserAccountStatus.Closed =>
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
      case currentStatus if currentStatus == UserAccountStatus.Closed =>
        Left(UserError.CannotAssignDefaultTravelerToClosedUser(userId))
      case _ =>
        Right(copy(defaultTravelerProfileId = Some(travelerId)))

  def clearDefaultTravelerProfile(travelerId: TravelerId): Either[UserError, User] =
    defaultTravelerProfileId match
      case Some(existingTravelerId) if existingTravelerId == travelerId => Right(copy(defaultTravelerProfileId = None))
      case Some(existingTravelerId) => Left(UserError.DefaultTravelerDidNotMatch(userId, existingTravelerId, travelerId))
      case None => Right(this)

  def updateAvatarUrl(nextAvatarUrl: AvatarUrl): Either[UserError, User] =
    userAccountStatus match
      case currentStatus if currentStatus == UserAccountStatus.Closed =>
        Left(UserError.CannotUpdateAvatarForClosedUser(userId))
      case _ =>
        Right(copy(avatarUrl = Some(nextAvatarUrl)))

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

sealed trait UserError extends DomainError:
  def message: String

object UserError:
  final case class UserWasNotFound(userId: UserId) extends UserError:
    override val message: String = s"User '${userId.value}' was not found"

  final case class UserWasNotFoundByEmail(primaryEmailAddress: EmailAddress) extends UserError:
    override val message: String = s"User email '${primaryEmailAddress.value}' was not found"

  final case class UserEmailAddressAlreadyExists(primaryEmailAddress: EmailAddress) extends UserError:
    override val message: String = s"User email '${primaryEmailAddress.value}' already exists"

  final case class InvalidUserStateTransition(userId: UserId, currentStatus: UserAccountStatus, targetStatus: UserAccountStatus) extends UserError:
    override val message: String = s"User '${userId.value}' cannot transition from $currentStatus to $targetStatus"

  final case class CannotAccruePointsForClosedUser(userId: UserId) extends UserError:
    override val message: String = s"Closed user '${userId.value}' cannot accrue loyalty points"

  final case class InsufficientLoyaltyPoints(userId: UserId, currentPoints: Points, requestedPoints: Points) extends UserError:
    override val message: String =
      s"User '${userId.value}' has only ${currentPoints.value} points and cannot redeem ${requestedPoints.value}"

  final case class CannotAssignDefaultTravelerToClosedUser(userId: UserId) extends UserError:
    override val message: String = s"Closed user '${userId.value}' cannot assign a default traveler"

  final case class DefaultTravelerDidNotMatch(userId: UserId, currentTravelerId: TravelerId, requestedTravelerId: TravelerId) extends UserError:
    override val message: String =
      s"User '${userId.value}' default traveler '${currentTravelerId.value}' did not match '${requestedTravelerId.value}'"

  final case class CannotUpdateAvatarForClosedUser(userId: UserId) extends UserError:
    override val message: String = s"Closed user '${userId.value}' cannot update avatar"