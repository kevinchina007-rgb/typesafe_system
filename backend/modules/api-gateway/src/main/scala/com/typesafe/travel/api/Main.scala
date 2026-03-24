package com.typesafe.travel.api

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    IO.println("travel-platform-backend api-gateway placeholder")
