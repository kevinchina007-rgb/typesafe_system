package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.application.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.Multipart

trait IdentityApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def identityRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "users" =>
      for
        createUserRequestDto <- request.as[CreateUserRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(createUserRequestDto.email))
        userDisplayName <- fromEither(PersonName.create(createUserRequestDto.nickname))
        userPhoneNumber <- fromEither(ContactNumber.create(createUserRequestDto.phone))
        createdAt <- currentInstantF
        createdUser <- userService.registerUser(primaryEmailAddress, userDisplayName, userPhoneNumber, createdAt)
        response <- createdJson(UserResponseDto.fromDomain(createdUser))
      yield response

    case request @ POST -> Root / "api" / "session" / "login" =>
      for
        loginUserRequestDto <- request.as[LoginUserRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(loginUserRequestDto.email))
        signedInUser <- userService.loginUserByEmail(primaryEmailAddress)
        response <- okJson(UserResponseDto.fromDomain(signedInUser))
      yield response

    case GET -> Root / "api" / "users" / userIdValue =>
      userRepository.findByUserId(UserId(userIdValue)).flatMap {
        case Some(foundUser) => okJson(UserResponseDto.fromDomain(foundUser))
        case None            => NotFound(ApiErrorResponseDto("user_not_found", s"User '$userIdValue' was not found").asJson)
      }

    case request @ POST -> Root / "api" / "users" / userIdValue / "avatar" =>
      for
        currentUserId <- requireCurrentUserId(request)
        _ <- if currentUserId == UserId(userIdValue) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.SessionActorDidNotMatch)
        multipartPayload <- request.as[Multipart[F]]
        avatarPart <- requireMultipartPart(multipartPayload, "avatar", AvatarApplicationError.AvatarWasMissing)
        uploadedAvatar <- readUploadedBinary(avatarPart, AvatarApplicationError.AvatarWasMissing)
        updatedUser <- avatarApplicationService.uploadUserAvatar(
          userId = UserId(userIdValue),
          originalFileName = uploadedAvatar.originalFileName,
          contentTypeValue = uploadedAvatar.contentTypeValue,
          fileBytes = uploadedAvatar.fileBytes
        )
        response <- okJson(UserResponseDto.fromDomain(updatedUser))
      yield response
  }
