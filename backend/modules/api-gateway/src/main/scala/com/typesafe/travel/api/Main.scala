package com.typesafe.travel.api

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{Host, Port, host}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS

import java.net.{BindException, HttpURLConnection, URI}

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    val backendPort = configuredBackendPort
    serverResource(backendPort).useForever.handleErrorWith {
      case _: BindException =>
        isBackendAlreadyHealthy(backendPort).flatMap { backendAlreadyHealthy =>
          if backendAlreadyHealthy then
            IO.println(s"travel-backend is already running on port ${backendPort.value}; startup skipped.")
          else
            IO.println(s"Port ${backendPort.value} is already in use. Stop the existing process or set TRAVEL_BACKEND_PORT to another port.")
        }
      case throwable =>
        IO.raiseError(throwable)
    }

  private def serverResource(backendPort: Port) =
    ApplicationWiring.resource[IO].flatMap { applicationWiring =>
      EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(backendPort)
        .withHttpApp(CORS.policy.withAllowOriginAll(applicationWiring.httpApp))
        .build
    }

  private def configuredBackendPort: Port =
    sys.env
      .get("TRAVEL_BACKEND_PORT")
      .flatMap(_.trim.toIntOption)
      .flatMap(Port.fromInt)
      .getOrElse(Port.fromInt(19095).get)

  private def isBackendAlreadyHealthy(backendPort: Port): IO[Boolean] =
    IO.blocking {
      val healthCheckUri = URI.create(s"http://${Host.fromString("127.0.0.1").get}:${backendPort.value}/api/health")
      val connection = healthCheckUri.toURL.openConnection().asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(1000)
      connection.setReadTimeout(1000)
      connection.setRequestMethod("GET")
      connection.getResponseCode == 200
    }.handleError(_ => false)
