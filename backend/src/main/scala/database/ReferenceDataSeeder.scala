// 本文件提供参考数据种子入口。
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object ReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    ReferenceDataSeederSupport.seedIfNeeded(transactor)
