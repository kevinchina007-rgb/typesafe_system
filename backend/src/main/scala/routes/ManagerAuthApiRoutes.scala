package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.auth.domain.AuthManagerType
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

trait ManagerAuthApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def managerAuthRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "manager-auth" / "login" =>
      for
        loginRequest <- request.as[ManagerPasswordLoginRequestDto]
        loginEmail <- fromEither(EmailAddress.create(loginRequest.email))
        managerType <- fromEither(parseManagerType(loginRequest.managerType))
        now <- currentInstantF
        managerSession <- authApplicationService.loginManager(managerType, loginEmail, loginRequest.password, now)
        response <- Ok(CurrentManagerSessionResponseDto.fromView(managerSession).asJson)
      yield withManagerSessionCookie(response, managerSession.sessionId, managerSession.expiresAt)

    case request @ POST -> Root / "api" / "manager-auth" / "logout" =>
      currentManagerSessionId(request) match
        case Some(sessionId) =>
          authApplicationService.logoutManager(sessionId) *> Ok(Map("status" -> "logged_out").asJson).map(clearManagerSessionCookie)
        case None =>
          Ok(Map("status" -> "logged_out").asJson).map(clearManagerSessionCookie)

    case request @ POST -> Root / "api" / "manager-auth" / "logout-current" =>
      currentManagerSessionId(request) match
        case Some(sessionId) =>
          authApplicationService.logoutManager(sessionId) *> Ok(Map("status" -> "logged_out").asJson).map(clearManagerSessionCookie)
        case None =>
          Ok(Map("status" -> "logged_out").asJson).map(clearManagerSessionCookie)

    case request @ POST -> Root / "api" / "manager-auth" / "logout-others" =>
      for
        sessionId <- currentManagerSessionId(request).liftTo[F](com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        now <- currentInstantF
        revokedCount <- authApplicationService.logoutOtherManagerSessions(sessionId, now)
        response <- Ok(LogoutOtherSessionsResponseDto(revokedCount).asJson)
      yield response

    case request @ POST -> Root / "api" / "manager-auth" / "change-password" =>
      for
        sessionId <- currentManagerSessionId(request).liftTo[F](com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        changePasswordRequest <- request.as[ChangePasswordRequestDto]
        now <- currentInstantF
        _ <- authApplicationService.changeManagerPassword(sessionId, changePasswordRequest.currentPassword, changePasswordRequest.newPassword, now)
        response <- Ok(Map("status" -> "password_changed").asJson)
      yield response

    case request @ GET -> Root / "api" / "manager-auth" / "sessions" =>
      for
        sessionId <- currentManagerSessionId(request).liftTo[F](com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        now <- currentInstantF
        sessions <- authApplicationService.listManagerSessions(sessionId, now)
        response <- Ok(AuthSessionListResponseDto(sessions.map(AuthSessionResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "manager-auth" / "me" =>
      for
        sessionId <- currentManagerSessionId(request).liftTo[F](com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)
        now <- currentInstantF
        managerSession <- authApplicationService.restoreCurrentManager(sessionId, now)
        response <- Ok(CurrentManagerSessionResponseDto.fromView(managerSession).asJson)
      yield withManagerSessionCookie(response, managerSession.sessionId, managerSession.expiresAt)
  }

  private def parseManagerType(value: String): Either[SharedValidationError, AuthManagerType] =
    value.trim.toLowerCase match
      case "airline"    => Right(AuthManagerType.Airline)
      case "hotel"      => Right(AuthManagerType.Hotel)
      case "train"      => Right(AuthManagerType.Train)
      case "attraction" => Right(AuthManagerType.Attraction)
      case "siteadmin" | "site-admin" => Right(AuthManagerType.SiteAdmin)
      case _            => Left(SharedValidationError.RequiredFieldWasEmpty("managerType"))
