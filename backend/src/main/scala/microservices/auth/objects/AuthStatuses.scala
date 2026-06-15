// 本文件定义 auth 域的状态值对象，只供后端内部判定与序列化使用，不对应前端镜像文件。
package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}

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
