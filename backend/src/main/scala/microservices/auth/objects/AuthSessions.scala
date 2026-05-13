package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*

import java.time.Instant

final case class AuthActorType(value: String):
  override def toString: String = value

object AuthActorType:
  val User: AuthActorType = AuthActorType("User")
  val Manager: AuthActorType = AuthActorType("Manager")
  given sourceEncoder: Encoder[AuthActorType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AuthActorType] = Decoder.decodeString.map(fromText)

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
  given sourceEncoder: Encoder[AuthSessionStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AuthSessionStatus] = Decoder.decodeString.map(fromText)

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
object AuthSession:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[AuthSession] = deriveEncoder
  given sourceDecoder: Decoder[AuthSession] = deriveDecoder

sealed trait CurrentPrincipal:
  def actorType: AuthActorType

final case class CurrentUserPrincipal(userId: UserId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.User
object CurrentUserPrincipal:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[CurrentUserPrincipal] = deriveEncoder
  given sourceDecoder: Decoder[CurrentUserPrincipal] = deriveDecoder

final case class CurrentManagerPrincipal(managerType: AuthManagerType, managerId: ManagerId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.Manager
object CurrentManagerPrincipal:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[CurrentManagerPrincipal] = deriveEncoder
  given sourceDecoder: Decoder[CurrentManagerPrincipal] = deriveDecoder

object CurrentPrincipal:
  given sourceEncoder: Encoder[CurrentPrincipal] =
    Encoder.instance {
      case principal: CurrentUserPrincipal =>
        principal.asJson.deepMerge(Json.obj("principalType" -> Json.fromString("User")))
      case principal: CurrentManagerPrincipal =>
        principal.asJson.deepMerge(Json.obj("principalType" -> Json.fromString("Manager")))
    }

  given sourceDecoder: Decoder[CurrentPrincipal] =
    Decoder.instance { cursor =>
      cursor.downField("principalType").as[String].flatMap {
        case "User"    => cursor.as[CurrentUserPrincipal]
        case "Manager" => cursor.as[CurrentManagerPrincipal]
        case other     => Left(io.circe.DecodingFailure(s"Unknown current principal type: $other", cursor.history))
      }
    }
