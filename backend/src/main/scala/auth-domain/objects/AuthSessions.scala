package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

final case class AuthActorType(value: String):
  override def toString: String = value

object AuthActorType:
  val User: AuthActorType = AuthActorType("User")
  val Manager: AuthActorType = AuthActorType("Manager")

  def fromText(value: String): AuthActorType =
    value.trim.toLowerCase match
      case "manager" => Manager
      case _ => User

final case class AuthSessionStatus(value: String):
  override def toString: String = value

object AuthSessionStatus:
  val Active: AuthSessionStatus = AuthSessionStatus("Active")
  val Expired: AuthSessionStatus = AuthSessionStatus("Expired")
  val Revoked: AuthSessionStatus = AuthSessionStatus("Revoked")

  def fromText(value: String): AuthSessionStatus =
    value.trim.toLowerCase match
      case "expired" => Expired
      case "revoked" => Revoked
      case _ => Active

final case class AuthSession(
    sessionId: SessionId,
    actorType: AuthActorType,
    actorId: String,
    managerType: Option[AuthManagerType],
    createdAt: Instant,
    lastSeenAt: Instant,
    expiresAt: Instant,
    status: AuthSessionStatus
)

sealed trait CurrentPrincipal:
  def actorType: AuthActorType

final case class CurrentUserPrincipal(userId: UserId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.User

final case class CurrentManagerPrincipal(managerType: AuthManagerType, managerId: ManagerId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.Manager
