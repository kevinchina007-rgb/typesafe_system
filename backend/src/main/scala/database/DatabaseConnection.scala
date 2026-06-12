// DatabaseConnection 负责创建和关闭 JDBC 数据库连接。

package com.typesafe.travel.persistence

import cats.effect.IO

import java.sql.{Connection, DriverManager}
import org.postgresql.ds.PGSimpleDataSource

object DatabaseConnection:
  private val loginTimeoutSeconds = 10

  def open(databaseConfig: DatabaseConfig): IO[Connection] =
    IO.blocking {
      Class.forName(databaseConfig.jdbcDriverClassName)

      val dataSource = new PGSimpleDataSource()
      dataSource.setUrl(databaseConfig.jdbcUrl)
      dataSource.setUser(databaseConfig.jdbcUser)
      dataSource.setPassword(databaseConfig.jdbcPassword)
      dataSource.setLoginTimeout(loginTimeoutSeconds)

      dataSource.getConnection
    }
