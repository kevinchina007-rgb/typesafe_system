// TourGroupReferenceDataSeeder ???????????????????????
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TourGroupReferenceDataSeederSupport:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TourGroupReferenceDataSeederSetup.seedIfNeeded(transactor)
