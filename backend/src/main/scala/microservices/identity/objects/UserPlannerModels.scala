// UserPlannerModels 定义身份模块的请求和响应模型。

package com.typesafe.travel.identity.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateUserPlannerRequest(email: String, nickname: String, phone: String)
object CreateUserPlannerRequest:
  given sourceEncoder: Encoder[CreateUserPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateUserPlannerRequest] = deriveDecoder

final case class LoginUserPlannerRequest(email: String)
object LoginUserPlannerRequest:
  given sourceEncoder: Encoder[LoginUserPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LoginUserPlannerRequest] = deriveDecoder

final case class GetUserPlannerRequest(userId: String)
object GetUserPlannerRequest:
  given sourceEncoder: Encoder[GetUserPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetUserPlannerRequest] = deriveDecoder

final case class UploadUserAvatarPlannerRequest(userId: String, publicUrl: String)
object UploadUserAvatarPlannerRequest:
  given sourceEncoder: Encoder[UploadUserAvatarPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadUserAvatarPlannerRequest] = deriveDecoder

final case class UpdateUserProfilePlannerRequest(userId: String, nickname: String, phone: String)
object UpdateUserProfilePlannerRequest:
  given sourceEncoder: Encoder[UpdateUserProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateUserProfilePlannerRequest] = deriveDecoder

final case class UserPlannerResponse(
    userId: String,
    email: String,
    nickname: String,
    phone: String,
    avatarUrl: Option[String],
    status: String,
    membershipLevel: String,
    points: Long,
    defaultTravelerProfileId: Option[String],
    createdAt: String
)
object UserPlannerResponse:
  given sourceEncoder: Encoder[UserPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[UserPlannerResponse] = deriveDecoder
