package com.typesafe.travel.api.routes

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.{AuthError, CurrentUserPlannerResponse}
import com.typesafe.travel.persistence.DatabaseConfig
import com.typesafe.travel.persistence.auth.AuthPlannerPlainSql
import com.typesafe.travel.tourgroup.domain.*
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.sql.{Connection, DriverManager}
import java.time.Instant

final class TourGroupChatRouter:

  private val logger = Slf4jLogger.getLogger[IO]

  private def withTransactionConnection[A](useConnection: Connection => IO[A]): IO[A] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment

    IO.blocking {
      Class.forName(databaseConfig.jdbcDriverClassName)
      val connection = DriverManager.getConnection(databaseConfig.jdbcUrl, databaseConfig.jdbcUser, databaseConfig.jdbcPassword)
      connection.setAutoCommit(false)
      connection
    }.bracket { connection =>
      useConnection(connection).attempt.flatMap {
        case Right(value) =>
          IO.blocking(connection.commit()).as(value)
        case Left(error) =>
          IO.blocking(connection.rollback()) *> IO.raiseError(error)
      }
    } { connection =>
      IO.blocking(connection.close()).handleErrorWith(_ => IO.unit)
    }

  private def requireSessionId(request: Request[IO]): IO[String] =
    IO.fromOption(request.params.get("sessionId").map(_.trim).filter(_.nonEmpty))(AuthError.UserSessionWasRequired)

  private def respondJson[A: Encoder](value: A): IO[Response[IO]] =
    Ok(value.asJson)

  private def respondJsonList[A: Encoder](value: List[A]): IO[Response[IO]] =
    Ok(Json.fromValues(value.map(_.asJson)))

  private def withCurrentUser(request: Request[IO])(use: (Connection, CurrentUserPlannerResponse) => IO[Response[IO]]): IO[Response[IO]] =
    withTransactionConnection { connection =>
      for
        sessionId <- requireSessionId(request)
        currentUser <- AuthPlannerPlainSql.currentUser(connection, sessionId, Instant.now())
        response <- use(connection, currentUser)
      yield response
    }

  private def handleRouteError(error: Throwable): IO[Response[IO]] =
    error match
      case AuthError.UserSessionWasRequired =>
        Response[IO](status = Status.Unauthorized).withEntity(error.getMessage).pure[IO]
      case other =>
        logger.error(other)(s"TourGroupChatRouter failed: ${other.getMessage}") *> BadRequest(Json.obj("error" -> Json.fromString(other.getMessage)))

  private def listDirectConversations(groupId: String, connection: Connection, currentUser: CurrentUserPlannerResponse): IO[TourGroupConversationListPlannerResponse] =
    TourGroupChatPlainSql
      .listConversations(connection, groupId, currentUser.userId, Instant.now())
      .map(response => response.copy(conversations = response.conversations.filter(_.conversationType == TourGroupConversationType.Direct.toString)))

  private def toMessageSearchResponse(results: List[TourGroupMessageSearchResultPlannerResponse]): TourGroupMessageSearchPlannerResponse =
    TourGroupMessageSearchPlannerResponse(results)

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat-settings" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupPlannerPlainSql.get(connection, TourGroupByIdPlannerRequest(groupId)).flatMap { group =>
          val isOrganizer = group.group.organizerUserId == currentUser.userId
          TourGroupChatPlainSql.loadChatSettings(connection, groupId, currentUser.userId, isOrganizer, Instant.now()).flatMap(respondJson)
        }
      }.handleErrorWith(handleRouteError)

    case request @ PATCH -> Root / "api" / "tour-groups" / groupId / "chat-settings" =>
      (
        for
          payload <- request.as[UpdateTourGroupChatSettingsPlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupPlannerPlainSql.get(connection, TourGroupByIdPlannerRequest(groupId)).flatMap { group =>
              TourGroupChatPlainSql.updateChatSettings(connection, groupId, currentUser.userId, payload.allowMemberDirectChat, group.group.organizerUserId == currentUser.userId, Instant.now()).flatMap(respondJson)
            }
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "conversations" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.listConversations(connection, groupId, currentUser.userId, Instant.now()).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "direct-conversations" =>
      withCurrentUser(request) { (connection, currentUser) =>
        listDirectConversations(groupId, connection, currentUser).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat" / "conversations" / "search" =>
      val query = request.params.get("q").getOrElse("")
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.searchConversations(connection, groupId, currentUser.userId, query, Instant.now()).flatMap(respondJsonList)
      }.handleErrorWith(handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat" / "search" =>
      val query = request.params.get("q").getOrElse("")
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.searchMessages(connection, groupId, currentUser.userId, query, Instant.now())
          .map(toMessageSearchResponse)
          .flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "tour-groups" / groupId / "direct-conversations" =>
      (
        for
          payload <- request.as[GetOrCreateTourGroupDirectConversationPlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.getOrCreateDirectConversation(connection, groupId, currentUser.userId, payload.targetUserId, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ GET -> Root / "api" / "tour-groups" / groupId / "chat" / "messages" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.listGroupChatMessages(connection, groupId, currentUser.userId, Instant.now()).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "tour-groups" / groupId / "chat" / "messages" =>
      (
        for
          payload <- request.as[SendTourGroupMessagePlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.sendGroupChatMessage(connection, groupId, currentUser.userId, payload, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ GET -> Root / "api" / "conversations" / conversationId / "messages" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.listMessages(connection, conversationId, currentUser.userId, Instant.now()).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ GET -> Root / "api" / "direct-conversations" / conversationId / "messages" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.listMessages(connection, conversationId, currentUser.userId, Instant.now()).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "conversations" / conversationId / "messages" =>
      (
        for
          payload <- request.as[SendTourGroupMessagePlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.sendMessage(connection, conversationId, currentUser.userId, payload, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "direct-conversations" / conversationId / "messages" =>
      (
        for
          payload <- request.as[SendTourGroupMessagePlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.sendMessage(connection, conversationId, currentUser.userId, payload, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "conversations" / conversationId / "read" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.markConversationRead(connection, conversationId, currentUser.userId, Instant.now()).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "conversations" / conversationId / "attachments" =>
      (
        for
          payload <- request.as[UploadConversationAttachmentPlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            for
              _ <- TourGroupChatPlainSql.ensureConversationAccess(connection, conversationId, currentUser.userId, Instant.now())
              attachment <- TourGroupChatPlainSql.uploadAttachment(connection, currentUser.userId, payload, Instant.now())
              result <- respondJson(attachment)
            yield result
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ PATCH -> Root / "api" / "messages" / messageId =>
      (
        for
          payload <- request.as[EditTourGroupMessagePlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.editMessage(connection, messageId, currentUser.userId, payload.content, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "messages" / messageId / "delete" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.deleteMessage(connection, messageId, currentUser.userId, Instant.now()).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "messages" / messageId / "recall" =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.recallMessage(connection, messageId, currentUser.userId, Instant.now()).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ POST -> Root / "api" / "messages" / messageId / "reactions" =>
      (
        for
          payload <- request.as[AddConversationReactionPlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.addReaction(connection, messageId, currentUser.userId, payload.reactionType, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ DELETE -> Root / "api" / "messages" / messageId / "reactions" / reactionType =>
      withCurrentUser(request) { (connection, currentUser) =>
        TourGroupChatPlainSql.removeReaction(connection, messageId, currentUser.userId, reactionType).flatMap(respondJson)
      }.handleErrorWith(handleRouteError)

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationId / "mute" =>
      (
        for
          payload <- request.as[UpdateDirectConversationMuteStatePlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.updateMuteState(connection, conversationId, currentUser.userId, payload.muted, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)

    case request @ PATCH -> Root / "api" / "direct-conversations" / conversationId / "archive" =>
      (
        for
          payload <- request.as[UpdateDirectConversationArchiveStatePlannerRequest]
          response <- withCurrentUser(request) { (connection, currentUser) =>
            TourGroupChatPlainSql.updateArchiveState(connection, conversationId, currentUser.userId, payload.archived, Instant.now()).flatMap(respondJson)
          }
        yield response
      ).handleErrorWith(handleRouteError)
  }

object TourGroupChatRouter:
  def apply(): TourGroupChatRouter =
    new TourGroupChatRouter
