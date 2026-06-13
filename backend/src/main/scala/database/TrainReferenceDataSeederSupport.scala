// TrainReferenceDataSeeder ???????????????????????
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TrainReferenceDataSeederSupport:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TrainReferenceDataSeederCore.seedIfNeeded(transactor)
