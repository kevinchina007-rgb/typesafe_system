// 本文件提供 tour-group 参考数据种子入口。
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TourGroupReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TourGroupReferenceDataSeederSupport.seedIfNeeded(transactor)
