package com.typesafe.travel.persistence

import cats.effect.kernel.Async
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*
import scala.io.Source

object SchemaInitializer:
  def initialize[F[_]: Async](transactor: Transactor[F]): F[Unit] =
    for
      schemaStatements <- Async[F].fromEither(loadSchemaStatements)
      _ <- schemaStatements.traverse_(schemaStatement => Update0(schemaStatement, None).run.void).transact(transactor)
      _ <- ReferenceDataSeeder.seedIfNeeded(transactor)
    yield ()

  private def loadSchemaStatements: Either[Throwable, List[String]] =
    Option(getClass.getClassLoader.getResourceAsStream("schema.sql"))
      .toRight(new IllegalStateException("schema.sql resource was not found"))
      .flatMap { schemaInputStream =>
        Either.catchNonFatal {
          val schemaSource = Source.fromInputStream(schemaInputStream, "UTF-8")
          try schemaSource.mkString
          finally schemaSource.close()
        }
      }
      .map(
        _.split(";").toList.map(_.trim).filter(_.nonEmpty)
      )
