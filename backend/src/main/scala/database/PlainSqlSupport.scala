package com.typesafe.travel.persistence

import cats.effect.IO

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

object PlainSqlSupport:
  def withConnection[A](databaseConfig: DatabaseConfig)(use: Connection => A): IO[A] =
    IO.blocking {
      Class.forName(databaseConfig.jdbcDriverClassName)
      val connection = DriverManager.getConnection(databaseConfig.jdbcUrl, databaseConfig.jdbcUser, databaseConfig.jdbcPassword)
      try use(connection)
      finally connection.close()
    }

  def withStatement[A](connection: Connection, sql: String)(use: PreparedStatement => A): A =
    val statement = connection.prepareStatement(sql)
    try use(statement)
    finally statement.close()

  def queryOptional[A](statement: PreparedStatement)(read: ResultSet => A): Option[A] =
    val resultSet = statement.executeQuery()
    try Option.when(resultSet.next())(read(resultSet))
    finally resultSet.close()

  def queryList[A](statement: PreparedStatement)(read: ResultSet => A): List[A] =
    val resultSet = statement.executeQuery()
    try
      val rows = List.newBuilder[A]
      while resultSet.next() do rows += read(resultSet)
      rows.result()
    finally resultSet.close()
