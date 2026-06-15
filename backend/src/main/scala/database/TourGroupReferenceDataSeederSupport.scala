// 本文件提供 tour-group 参考数据种子的公共支撑逻辑。
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TourGroupReferenceDataSeederSupport:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TourGroupReferenceDataSeederSetup.seedIfNeeded(transactor)
