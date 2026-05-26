package com.typesafe.travel.persistence

final case class DatabaseConfig(
    jdbcUrl: String,
    jdbcUser: String,
    jdbcPassword: String,
    jdbcDriverClassName: String
)

object DatabaseConfig:
  def loadFromEnvironment: DatabaseConfig =
    DatabaseConfig(
      jdbcUrl =
        sys.env.getOrElse(
          "TRAVEL_DB_URL",
          "jdbc:postgresql://127.0.0.1:5432/travel_platform"
        ),
      jdbcUser = sys.env.getOrElse("TRAVEL_DB_USER", "postgres"),
      jdbcPassword = sys.env.getOrElse("TRAVEL_DB_PASSWORD", "root"),
      jdbcDriverClassName = sys.env.getOrElse("TRAVEL_DB_DRIVER", "org.postgresql.Driver")
    )
