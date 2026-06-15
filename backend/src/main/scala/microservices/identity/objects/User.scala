// 本文件定义 identity 用户域核心模型，包括用户状态、会员等级、用户主体和领域错误。
package com.typesafe.travel.identity.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.time.Instant

final case class UserAccountStatus(value: String):
  override def toString: String = value

object UserAccountStatus:
  val PendingActivation: UserAccountStatus = UserAccountStatus("PendingActivation")
  val Active: UserAccountStatus = UserAccountStatus("Active")
  val Suspended: UserAccountStatus = UserAccountStatus("Suspended")
  val Closed: UserAccountStatus = UserAccountStatus("Closed")
  given sourceEncoder: Encoder[UserAccountStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[UserAccountStatus] = Decoder.decodeString.map(fromText)

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
  given sourceEncoder: Encoder[UserMembershipLevel] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[UserMembershipLevel] = Decoder.decodeString.map(fromText)

  def fromText(value: String): UserMembershipLevel =
    value.trim.toLowerCase match
      case "silver" => Silver
      case "gold" => Gold
      case "platinum" => Platinum
      case _ => Standard

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
)

object User:
  import UserSourceJsonCodecs.given
  given sourceEncoder: Encoder[User] = deriveEncoder
  given sourceDecoder: Decoder[User] = deriveDecoder

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
