package com.typesafe.travel.persistence

import cats.effect.IO

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import com.zaxxer.hikari.HikariConfig
import doobie.Transactor
import doobie.hikari.HikariTransactor
import java.util.Properties

object DatabaseTransactor:
  def create(databaseConfig: DatabaseConfig): Transactor[IO] =
    val connectionProperties = Properties()
    connectionProperties.setProperty("user", databaseConfig.jdbcUser)
    connectionProperties.setProperty("password", databaseConfig.jdbcPassword)
    Transactor.fromDriverManager[IO](
      driver = databaseConfig.jdbcDriverClassName,
      url = databaseConfig.jdbcUrl,
      info = connectionProperties,
      logHandler = None
    )

  def resource(databaseConfig: DatabaseConfig): Resource[IO, Transactor[IO]] =
    val hikariConfig = HikariConfig()
    hikariConfig.setJdbcUrl(databaseConfig.jdbcUrl)
    hikariConfig.setUsername(databaseConfig.jdbcUser)
    hikariConfig.setPassword(databaseConfig.jdbcPassword)
    hikariConfig.setMaximumPoolSize(8)
    hikariConfig.setMinimumIdle(1)
    hikariConfig.setPoolName("travel-platform-backend")
    hikariConfig.setInitializationFailTimeout(-1)

    HikariTransactor.fromHikariConfig[IO](hikariConfig).map(xa => xa: Transactor[IO])
