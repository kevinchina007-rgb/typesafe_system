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
          "jdbc:h2:file:./data/travel-platform;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ),
      jdbcUser = sys.env.getOrElse("TRAVEL_DB_USER", "sa"),
      jdbcPassword = sys.env.getOrElse("TRAVEL_DB_PASSWORD", ""),
      jdbcDriverClassName = sys.env.getOrElse("TRAVEL_DB_DRIVER", "org.h2.Driver")
    )
