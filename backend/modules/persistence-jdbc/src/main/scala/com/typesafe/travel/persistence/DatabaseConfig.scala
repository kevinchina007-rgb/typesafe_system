package com.typesafe.travel.persistence

final case class DatabaseConfig(
    repositoryMode: RepositoryMode,
    jdbcUrl: String,
    jdbcUser: String,
    jdbcPassword: String,
    jdbcDriverClassName: String
)

enum RepositoryMode:
  case InMemory, Database

object DatabaseConfig:
  def loadFromEnvironment: DatabaseConfig =
    val repositoryMode =
      sys.env
        .get("TRAVEL_REPOSITORY_MODE")
        .map(_.trim.toLowerCase)
        .collect {
          case "in-memory" => RepositoryMode.InMemory
          case "database"  => RepositoryMode.Database
        }
        .getOrElse(RepositoryMode.Database)

    DatabaseConfig(
      repositoryMode = repositoryMode,
      jdbcUrl =
        sys.env.getOrElse(
          "TRAVEL_DB_URL",
          "jdbc:h2:file:./data/travel-platform;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
        ),
      jdbcUser = sys.env.getOrElse("TRAVEL_DB_USER", "sa"),
      jdbcPassword = sys.env.getOrElse("TRAVEL_DB_PASSWORD", ""),
      jdbcDriverClassName = sys.env.getOrElse("TRAVEL_DB_DRIVER", "org.h2.Driver")
    )
