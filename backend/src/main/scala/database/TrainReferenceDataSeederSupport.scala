// 本文件提供 train 参考数据种子的公共支撑逻辑。
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TrainReferenceDataSeederSupport:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TrainReferenceDataSeederCore.seedIfNeeded(transactor)
