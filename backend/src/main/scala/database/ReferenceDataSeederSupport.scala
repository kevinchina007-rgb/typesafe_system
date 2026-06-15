// 本文件提供参考数据种子的公共支撑逻辑。
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.*
import doobie.implicits.*

object ReferenceDataSeederSupport:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    for
      airlineCount <- sql"select count(*) from airlines".query[Long].unique.transact(transactor)
      _ <- if airlineCount == 0 then ReferenceDataSeederFlights.seedFlights(transactor) else IO.unit
      _ <- ReferenceDataSeederHotels.seedHotels(transactor)
      _ <- TrainReferenceDataSeeder.seedIfNeeded(transactor)
      _ <- AttractionReferenceDataSeeder.seedIfNeeded(transactor)
      _ <- TourGroupReferenceDataSeeder.seedIfNeeded(transactor)
      _ <- ReferenceDataSeederAirlines.seedAirlineManagers(transactor)
    yield ()
