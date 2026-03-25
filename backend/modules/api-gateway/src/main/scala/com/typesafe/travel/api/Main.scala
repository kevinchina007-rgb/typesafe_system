package com.typesafe.travel.api

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{host, port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    val applicationWiring = ApplicationWiring.create[IO]

    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(CORS.policy.withAllowOriginAll(applicationWiring.httpApp))
      .build
      .useForever
