package com.typesafe.travel.api

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{Host, Port, host}
import org.http4s.headers.Origin
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.syntax.header.*

import java.net.{BindException, HttpURLConnection, URI}
import java.net.InetAddress

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
        .withHttpApp(
          CORS.policy
            .withAllowOriginHeader(isAllowedFrontendOrigin)
            .withAllowCredentials(true)
            .apply(applicationWiring.httpApp)
        )
        .build
    }

  private def isAllowedFrontendOrigin(originHeader: Origin): Boolean =
    val originText = originHeader.renderString.stripPrefix("Origin: ").trim
    configuredAllowedOrigins.contains(originText) ||
    (allowPrivateNetworkOrigins && safeParseOrigin(originText).exists(parsedOrigin => Option(parsedOrigin.getHost).exists(isLocalNetworkHost)))

  private def configuredAllowedOrigins: Set[String] =
    sys.env
      .get("TRAVEL_ALLOWED_ORIGINS")
      .toList
      .flatMap(_.split(",").toList)
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSet

  private def allowPrivateNetworkOrigins: Boolean =
    sys.env
      .get("TRAVEL_ALLOW_PRIVATE_NETWORK_ORIGINS")
      .forall(_.trim.equalsIgnoreCase("true"))

  private def safeParseOrigin(originText: String): Option[URI] =
    try Some(URI.create(originText))
    catch case _: IllegalArgumentException => None

  private def isLocalNetworkHost(hostName: String): Boolean =
    hostName match
      case "localhost" => true
      case "127.0.0.1"  => true
      case "0.0.0.0"    => true
      case _ =>
        try
          val resolvedAddress = InetAddress.getByName(hostName)
          resolvedAddress.isLoopbackAddress ||
          resolvedAddress.isSiteLocalAddress ||
          resolvedAddress.isLinkLocalAddress
        catch
          case _: Exception => false

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
