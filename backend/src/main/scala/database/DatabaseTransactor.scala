// DatabaseTransactor 提供数据库事务执行支持。

package com.typesafe.travel.persistence

import cats.effect.IO

import cats.effect.kernel.Resource
import doobie.Transactor

object DatabaseTransactor:
  def create(databaseConfig: DatabaseConfig): Transactor[IO] =
    Transactor.fromDatabaseConfig[IO](databaseConfig)

  def resource(databaseConfig: DatabaseConfig): Resource[IO, Transactor[IO]] =
    Transactor.resource[IO](databaseConfig)
