// TrainReferenceDataSeeder ???????????????????????
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object TrainReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    TrainReferenceDataSeederSupport.seedIfNeeded(transactor)
