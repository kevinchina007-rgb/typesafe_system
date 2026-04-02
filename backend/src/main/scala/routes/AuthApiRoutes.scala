package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.auth.domain.AuthError
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

trait AuthApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def authRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "auth" / "signup" =>
      for
        signupRequest <- request.as[SignupRequestDto]
        loginEmail <- fromEither(EmailAddress.create(signupRequest.email))
        nickname <- fromEither(PersonName.create(signupRequest.nickname))
        phone <- fromEither(ContactNumber.create(signupRequest.phone))
        now <- currentInstantF
        userSession <- authApplicationService.signupUser(loginEmail, nickname, phone, signupRequest.password, now)
        response <- Created(CurrentUserSessionResponseDto.fromView(userSession).asJson)
      yield withUserSessionCookie(response, userSession.sessionId, userSession.expiresAt)

    case request @ POST -> Root / "api" / "auth" / "login" =>
      for
        loginRequest <- request.as[PasswordLoginRequestDto]
        loginEmail <- fromEither(EmailAddress.create(loginRequest.email))
        now <- currentInstantF
        userSession <- authApplicationService.loginUser(loginEmail, loginRequest.password, now)
        response <- Ok(CurrentUserSessionResponseDto.fromView(userSession).asJson)
      yield withUserSessionCookie(response, userSession.sessionId, userSession.expiresAt)

    case request @ POST -> Root / "api" / "auth" / "logout" =>
      currentUserSessionId(request) match
        case Some(sessionId) =>
          authApplicationService.logoutUser(sessionId) *> Ok(Map("status" -> "logged_out").asJson).map(clearUserSessionCookie)
        case None =>
          Ok(Map("status" -> "logged_out").asJson).map(clearUserSessionCookie)

    case request @ GET -> Root / "api" / "auth" / "me" =>
      for
        sessionId <- currentUserSessionId(request).liftTo[F](AuthError.UserSessionWasRequired)
        now <- currentInstantF
        userSession <- authApplicationService.restoreCurrentUser(sessionId, now)
        response <- Ok(CurrentUserSessionResponseDto.fromView(userSession).asJson)
      yield withUserSessionCookie(response, userSession.sessionId, userSession.expiresAt)
  }
