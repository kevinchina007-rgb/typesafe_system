package com.typesafe.travel.persistence

import cats.effect.kernel.Async
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import java.util.Properties

object DatabaseTransactor:
  def create[F[_]: Async](databaseConfig: DatabaseConfig): Aux[F, Unit] =
    val connectionProperties = Properties()
    connectionProperties.setProperty("user", databaseConfig.jdbcUser)
    connectionProperties.setProperty("password", databaseConfig.jdbcPassword)
    Transactor.fromDriverManager[F](
      driver = databaseConfig.jdbcDriverClassName,
      url = databaseConfig.jdbcUrl,
      info = connectionProperties,
      logHandler = None
    )
