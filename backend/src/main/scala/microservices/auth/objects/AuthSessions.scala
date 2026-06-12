package com.typesafe.travel.auth.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*

import java.time.Instant

// 单个登录会话的领域模型。
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
// 会话领域模型的 JSON codec。
object AuthSession:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[AuthSession] = deriveEncoder
  given sourceDecoder: Decoder[AuthSession] = deriveDecoder

// 当前登录主体的抽象父类型。
sealed trait CurrentPrincipal:
  def actorType: AuthActorType

// 当前用户登录主体。
final case class CurrentUserPrincipal(userId: UserId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.User
// 当前用户主体的 JSON codec。
object CurrentUserPrincipal:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[CurrentUserPrincipal] = deriveEncoder
  given sourceDecoder: Decoder[CurrentUserPrincipal] = deriveDecoder

// 当前管理员登录主体。
final case class CurrentManagerPrincipal(managerType: AuthManagerType, managerId: ManagerId) extends CurrentPrincipal:
  val actorType: AuthActorType = AuthActorType.Manager
// 当前管理员主体的 JSON codec。
object CurrentManagerPrincipal:
  import AuthSourceJsonCodecs.given
  given sourceEncoder: Encoder[CurrentManagerPrincipal] = deriveEncoder
  given sourceDecoder: Decoder[CurrentManagerPrincipal] = deriveDecoder

// 登录主体父类型的 JSON codec。
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
