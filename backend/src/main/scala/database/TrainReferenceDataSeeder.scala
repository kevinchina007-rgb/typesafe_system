// 本文件提供 train 参考数据种子入口。
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TrainReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TrainReferenceDataSeederSupport.seedIfNeeded(transactor)
