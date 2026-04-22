package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.auth.domain.AuthManagerType
import com.typesafe.travel.content.domain.{FeedbackSenderRole, FeedbackThreadKind}
import com.typesafe.travel.shared.kernel.{ReviewId, SharedValidationError, SupportTicketId}
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

trait FeedbackApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  private def ensureSiteAdmin(managerType: AuthManagerType): F[Unit] =
    if managerType == AuthManagerType.SiteAdmin then Async[F].unit
    else Async[F].raiseError(com.typesafe.travel.auth.domain.AuthError.ManagerSessionWasRequired)

  protected final def feedbackRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "api" / "feedback" / "threads" / "mine" =>
      for
        currentUserId <- requireCurrentUserId(request)
        threads <- feedbackApplicationService.listThreadsForUser(currentUserId)
        response <- Ok(FeedbackThreadListResponseDto(threads.map(FeedbackThreadResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "feedback" / "threads" / "manager" =>
      for
        currentManagerSession <- requireCurrentManager(request)
        threads <- feedbackApplicationService.listThreadsForManager(com.typesafe.travel.content.domain.FeedbackManagerType.fromText(currentManagerSession.managerType.toString))
        response <- Ok(FeedbackThreadListResponseDto(threads.map(FeedbackThreadResponseDto.fromView)).asJson)
      yield response

    case request @ GET -> Root / "api" / "feedback" / "threads" / "site-admin" =>
      for
        currentManagerSession <- requireCurrentManager(request)
        _ <- ensureSiteAdmin(currentManagerSession.managerType)
        channelValue <- fromEither(request.params.get("channel").filter(_.trim.nonEmpty).toRight(SharedValidationError.RequiredFieldWasEmpty("channel")))
        channel = if channelValue.trim.equalsIgnoreCase("manager") then FeedbackThreadKind.ManagerEscalation else FeedbackThreadKind.ServiceReview
        threads <- feedbackApplicationService.listThreadsForSiteAdmin(channel)
        response <- Ok(FeedbackThreadListResponseDto(threads.map(FeedbackThreadResponseDto.fromView)).asJson)
      yield response

    case request @ POST -> Root / "api" / "feedback" / "review-threads" =>
      for
        currentUserId <- requireCurrentUserId(request)
        createRequest <- request.as[CreateReviewFeedbackThreadRequestDto]
        now <- currentInstantF
        thread <- feedbackApplicationService.ensureThreadForReview(ReviewId(createRequest.reviewId), currentUserId, now)
        response <- Ok(FeedbackThreadResponseDto.fromView(thread).asJson)
      yield response

    case request @ POST -> Root / "api" / "feedback" / "threads" / threadIdValue / "messages" =>
      for
        sendRequest <- request.as[SendFeedbackMessageRequestDto]
        now <- currentInstantF
        thread <- currentUserSessionId(request) match
          case Some(_) =>
            requireCurrentUserId(request).flatMap(currentUserId =>
              feedbackApplicationService.sendUserMessage(currentUserId, SupportTicketId(threadIdValue), sendRequest.body, now)
            )
          case None =>
            requireCurrentManager(request).flatMap(currentManagerSession =>
              val senderRole =
                if currentManagerSession.managerType == AuthManagerType.SiteAdmin then FeedbackSenderRole.SiteAdmin
                else FeedbackSenderRole.Manager
              feedbackApplicationService.sendManagerMessage(
                SupportTicketId(threadIdValue),
                currentManagerSession.displayName.value,
                senderRole,
                sendRequest.body,
                now
              )
            )
        response <- Ok(FeedbackThreadResponseDto.fromView(thread).asJson)
      yield response

    case request @ POST -> Root / "api" / "feedback" / "threads" / threadIdValue / "read" =>
      for
        _ <- request.as[MarkFeedbackThreadReadRequestDto]
        audience <- currentUserSessionId(request) match
          case Some(_) => Async[F].pure(FeedbackSenderRole.User)
          case None =>
            requireCurrentManager(request).map(currentManagerSession =>
              if currentManagerSession.managerType == AuthManagerType.SiteAdmin then FeedbackSenderRole.SiteAdmin
              else FeedbackSenderRole.Manager
            )
        thread <- feedbackApplicationService.markRead(
          SupportTicketId(threadIdValue),
          audience
        )
        response <- Ok(FeedbackThreadResponseDto.fromView(thread).asJson)
      yield response

    case request @ POST -> Root / "api" / "feedback" / "threads" / threadIdValue / "escalate" =>
      for
        _ <- requireCurrentManager(request)
        escalateRequest <- request.as[EscalateFeedbackThreadRequestDto]
        now <- currentInstantF
        thread <- feedbackApplicationService.escalateThread(
          SupportTicketId(threadIdValue),
          escalateRequest.senderDisplayName,
          escalateRequest.body,
          now
        )
        response <- Ok(FeedbackThreadResponseDto.fromView(thread).asJson)
      yield response
  }
