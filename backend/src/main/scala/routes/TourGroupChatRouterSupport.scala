// TourGroupChatRouterSupport 负责请求路由分发。

package com.typesafe.travel.api.routes

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.persistence.{DatabaseConfig, DatabaseConnection}
import org.http4s.*

import java.sql.Connection

object TourGroupChatRouterSupport:
  def withTransactionConnection[A](useConnection: Connection => IO[A]): IO[A] =
    val databaseConfig = DatabaseConfig.loadFromEnvironment

    DatabaseConnection.open(databaseConfig).bracket { connection =>
      connection.setAutoCommit(false)
      useConnection(connection).attempt.flatMap {
        case Right(value) =>
          IO.blocking(connection.commit()).as(value)
        case Left(error) =>
          IO.blocking(connection.rollback()) *> IO.raiseError(error)
      }
    } { connection =>
      IO.blocking(connection.close()).handleErrorWith(_ => IO.unit)
    }

  def requireSessionId(request: Request[IO]): IO[String] =
    IO.fromOption(request.params.get("sessionId").map(_.trim).filter(_.nonEmpty))(com.typesafe.travel.auth.domain.AuthError.UserSessionWasRequired)
