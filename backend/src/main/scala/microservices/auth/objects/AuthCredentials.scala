package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant

final case class CredentialStatus(value: String):
  override def toString: String = value

object CredentialStatus:
  val Active: CredentialStatus = CredentialStatus("Active")
  val Disabled: CredentialStatus = CredentialStatus("Disabled")
  given sourceEncoder: Encoder[CredentialStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[CredentialStatus] = Decoder.decodeString.map(fromText)

  def fromText(value: String): CredentialStatus =
    value.trim.toLowerCase match
      case "disabled" => Disabled
      case _ => Active

final case class AuthManagerType(value: String):
  override def toString: String = value

object AuthManagerType:
  val Airline: AuthManagerType = AuthManagerType("Airline")
  val Hotel: AuthManagerType = AuthManagerType("Hotel")
  val Train: AuthManagerType = AuthManagerType("Train")
  val Attraction: AuthManagerType = AuthManagerType("Attraction")
  val SiteAdmin: AuthManagerType = AuthManagerType("SiteAdmin")
  given sourceEncoder: Encoder[AuthManagerType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AuthManagerType] = Decoder.decodeString.map(fromText)

  def fromText(value: String): AuthManagerType =
    value.trim.toLowerCase match
      case "hotel" => Hotel
      case "train" => Train
      case "attraction" => Attraction
      case "siteadmin" | "site-admin" => SiteAdmin
      case _ => Airline

final case class UserCredential(
    credentialId: CredentialId,
    userId: UserId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant,
    passwordUpdatedAt: Instant
)
object UserCredential:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[UserCredential] = deriveEncoder
  given sourceDecoder: Decoder[UserCredential] = deriveDecoder

final case class ManagerCredential(
    credentialId: CredentialId,
    managerType: AuthManagerType,
    managerId: ManagerId,
    loginEmail: EmailAddress,
    passwordHash: String,
    status: CredentialStatus,
    createdAt: Instant,
    updatedAt: Instant,
    passwordUpdatedAt: Instant
)
object ManagerCredential:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[ManagerCredential] = deriveEncoder
  given sourceDecoder: Decoder[ManagerCredential] = deriveDecoder
