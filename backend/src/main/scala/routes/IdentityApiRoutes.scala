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
import org.http4s.headers
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
        response <- Created(UserResponseDto.fromDomain(createdUser).asJson)
      yield response

    case request @ POST -> Root / "api" / "session" / "login" =>
      for
        loginUserRequestDto <- request.as[LoginUserRequestDto]
        primaryEmailAddress <- fromEither(EmailAddress.create(loginUserRequestDto.email))
        signedInUser <- userService.loginUserByEmail(primaryEmailAddress)
        response <- Ok(UserResponseDto.fromDomain(signedInUser).asJson)
      yield response

    case GET -> Root / "api" / "users" / userIdValue =>
      userRepository.findByUserId(UserId(userIdValue)).flatMap {
        case Some(foundUser) => Ok(UserResponseDto.fromDomain(foundUser).asJson)
        case None            => NotFound(ApiErrorResponseDto("user_not_found", s"User '$userIdValue' was not found").asJson)
      }

    case request @ POST -> Root / "api" / "users" / userIdValue / "avatar" =>
      for
        currentUserId <- requireCurrentUserId(request)
        _ <- if currentUserId == UserId(userIdValue) then Async[F].unit else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.SessionActorDidNotMatch)
        multipartPayload <- request.as[Multipart[F]]
        avatarPart <- multipartPayload.parts.find(_.name.contains("avatar")).liftTo[F](AvatarApplicationError.AvatarWasMissing)
        avatarFileName <- avatarPart.filename.liftTo[F](AvatarApplicationError.AvatarWasMissing)
        avatarContentType =
          avatarPart.headers
            .get[headers.`Content-Type`]
            .map(contentTypeHeader => s"${contentTypeHeader.mediaType.mainType}/${contentTypeHeader.mediaType.subType}")
            .getOrElse("")
        avatarBytes <- avatarPart.body.compile.to(Array)
        updatedUser <- avatarApplicationService.uploadUserAvatar(
          userId = UserId(userIdValue),
          originalFileName = avatarFileName,
          contentTypeValue = avatarContentType,
          fileBytes = avatarBytes
        )
        response <- Ok(UserResponseDto.fromDomain(updatedUser).asJson)
      yield response
  }
