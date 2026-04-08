package com.typesafe.travel.api

import cats.MonadThrow
import cats.effect.kernel.Clock
import cats.syntax.all.*
import com.typesafe.travel.api.application.{AuthApplicationService, CurrentManagerSessionView}
import com.typesafe.travel.auth.domain.AuthError
import com.typesafe.travel.shared.kernel.{SessionId, UserId}
import org.http4s.{HttpDate, Request, Response, ResponseCookie, SameSite}

import java.time.{Instant, LocalDate}

trait ApiRouterSessionSupport[F[_]: MonadThrow: Clock]:
  protected val authApplicationService: AuthApplicationService[F]

  protected val userSessionCookieName = "travel_user_session"
  protected val managerSessionCookieName = "travel_manager_session"
  private val configuredSessionCookieDomain = sys.env.get("TRAVEL_SESSION_COOKIE_DOMAIN").map(_.trim).filter(_.nonEmpty)
  private val configuredSessionCookieSecure = sys.env.get("TRAVEL_SESSION_COOKIE_SECURE").exists(_.trim.equalsIgnoreCase("true"))
  private val configuredSessionCookieSameSite = sys.env
    .get("TRAVEL_SESSION_COOKIE_SAMESITE")
    .flatMap(parseSameSite)

  protected def currentInstantF: F[Instant] =
    Clock[F].realTimeInstant

  protected def currentLocalDateF: F[LocalDate] =
    currentInstantF.map(_.atZone(java.time.ZoneId.systemDefault()).toLocalDate)

  protected def currentUserSessionId(request: Request[F]): Option[SessionId] =
    request.cookies.find(_.name == userSessionCookieName).map(cookie => SessionId(cookie.content))

  protected def currentManagerSessionId(request: Request[F]): Option[SessionId] =
    request.cookies.find(_.name == managerSessionCookieName).map(cookie => SessionId(cookie.content))

  protected def requireCurrentUserId(request: Request[F]): F[UserId] =
    for
      sessionId <- currentUserSessionId(request).liftTo[F](AuthError.UserSessionWasRequired)
      now <- currentInstantF
      userSession <- authApplicationService.restoreCurrentUser(sessionId, now)
    yield userSession.user.userId

  protected def requireCurrentManager(request: Request[F]): F[CurrentManagerSessionView] =
    for
      sessionId <- currentManagerSessionId(request).liftTo[F](AuthError.ManagerSessionWasRequired)
      now <- currentInstantF
      managerSession <- authApplicationService.restoreCurrentManager(sessionId, now)
    yield managerSession

  protected def withUserSessionCookie(response: Response[F], sessionId: SessionId, expiresAt: Instant): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = userSessionCookieName,
        content = sessionId.value,
        httpOnly = true,
        secure = configuredSessionCookieSecure,
        path = Some("/"),
        domain = configuredSessionCookieDomain,
        sameSite = configuredSessionCookieSameSite.orElse(Some(SameSite.Lax)),
        expires = Some(HttpDate.unsafeFromInstant(expiresAt))
      )
    )

  protected def clearUserSessionCookie(response: Response[F]): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = userSessionCookieName,
        content = "",
        httpOnly = true,
        secure = configuredSessionCookieSecure,
        path = Some("/"),
        domain = configuredSessionCookieDomain,
        sameSite = configuredSessionCookieSameSite.orElse(Some(SameSite.Lax)),
        expires = Some(HttpDate.Epoch)
      )
    )

  protected def withManagerSessionCookie(response: Response[F], sessionId: SessionId, expiresAt: Instant): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = managerSessionCookieName,
        content = sessionId.value,
        httpOnly = true,
        secure = configuredSessionCookieSecure,
        path = Some("/"),
        domain = configuredSessionCookieDomain,
        sameSite = configuredSessionCookieSameSite.orElse(Some(SameSite.Lax)),
        expires = Some(HttpDate.unsafeFromInstant(expiresAt))
      )
    )

  protected def clearManagerSessionCookie(response: Response[F]): Response[F] =
    response.addCookie(
      ResponseCookie(
        name = managerSessionCookieName,
        content = "",
        httpOnly = true,
        secure = configuredSessionCookieSecure,
        path = Some("/"),
        domain = configuredSessionCookieDomain,
        sameSite = configuredSessionCookieSameSite.orElse(Some(SameSite.Lax)),
        expires = Some(HttpDate.Epoch)
      )
    )

  private def parseSameSite(value: String): Option[SameSite] =
    value.trim.toLowerCase match
      case "strict" => Some(SameSite.Strict)
      case "lax"    => Some(SameSite.Lax)
      case "none"   => Some(SameSite.None)
      case _        => None
