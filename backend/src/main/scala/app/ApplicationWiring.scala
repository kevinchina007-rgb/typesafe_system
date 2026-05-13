package com.typesafe.travel.api

import cats.effect.IO
import cats.effect.kernel.Resource
import org.http4s.HttpApp

final case class ApplicationWiring(httpApp: HttpApp[cats.effect.IO])

object ApplicationWiring:
  def create: IO[ApplicationWiring] =
    resource.use(wiring => IO.pure(wiring))

  def resource: Resource[IO, ApplicationWiring] =
    PersistenceApplicationWiring.resource
