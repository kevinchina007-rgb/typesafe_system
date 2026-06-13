// TourGroupReferenceDataSeeder ???????????????????????
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TourGroupReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TourGroupReferenceDataSeederSupport.seedIfNeeded(transactor)
