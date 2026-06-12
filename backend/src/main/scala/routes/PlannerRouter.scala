// PlannerRouter 负责将 HTTP 请求分发到对应的 planner。

package com.typesafe.travel.api.routes

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.persistence.DatabaseConfig
import io.circe.Json
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.sql.{Connection, DriverManager}

final class PlannerRouter(
    plannerRegistry: PlannerRegistry
):

  private val logger = Slf4jLogger.getLogger[IO]

  private def decodeInput[Input](plannerName: String, payload: Json)(using decoder: io.circe.Decoder[Input]): IO[Input] =
    IO.fromEither(
      payload.as[Input].left.map(error =>
        new IllegalArgumentException(s"Invalid JSON for $plannerName: ${error.getMessage}")
      )
    )

  private def executePlanner(plannerName: String, payload: Json): IO[Json] =
    plannerRegistry.planners
      .get(plannerName)
      .liftTo[IO](new IllegalArgumentException(s"Unknown planner: $plannerName"))
      .flatMap {
        case registered: PlannerRegistry.RegisteredPlan.Plain[input, output] =>
          for
            input <- decodeInput(registered.name, payload)(using registered.inputDecoder)
            output <- registered.planner.plan(input)
          yield registered.outputEncoder(output)

        case registered: PlannerRegistry.RegisteredPlan.WithConnection[input, output] =>
          for
            input <- decodeInput(registered.name, payload)(using registered.inputDecoder)
            output <- withTransactionConnection(connection => registered.planner.plan(input, connection))
          yield registered.outputEncoder(output)
      }

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

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root / "api" / plannerName =>
      (
        for
          _ <- logger.info(s"PlannerRouter received POST /api/$plannerName")
          bodyJson <- request.as[Json]
          responseJson <- executePlanner(plannerName, bodyJson)
          response <- Ok(responseJson)
        yield response
      ).handleErrorWith { error =>
        for
          _ <- logger.error(error)(s"PlannerRouter failed: ${error.getMessage}")
          response <- BadRequest(Json.obj("error" -> Json.fromString(error.getMessage)))
        yield response
      }
  }
