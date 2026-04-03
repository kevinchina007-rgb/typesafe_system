package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

import java.nio.file.Path
import java.util.UUID

object PersistenceTestSupport:
  def createTestTransactor(): Transactor[IO] =
    DatabaseTransactor.create[IO](
      DatabaseConfig(
        repositoryMode = RepositoryMode.Database,
        jdbcUrl =
          s"jdbc:h2:mem:travel-${UUID.randomUUID().toString};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        jdbcUser = "sa",
        jdbcPassword = "",
        jdbcDriverClassName = "org.h2.Driver"
      )
    )

  def createFileTransactor(databaseRootPath: Path): Transactor[IO] =
    DatabaseTransactor.create[IO](
      DatabaseConfig(
        repositoryMode = RepositoryMode.Database,
        jdbcUrl =
          s"jdbc:h2:file:${databaseRootPath.toAbsolutePath.toString.replace("\\", "/")};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        jdbcUser = "sa",
        jdbcPassword = "",
        jdbcDriverClassName = "org.h2.Driver"
      )
    )
