// ReferenceDataSeeder ???????????????????????
package com.typesafe.travel.persistence

import cats.effect.IO
import doobie.Transactor

object ReferenceDataSeeder:
  def seedIfNeeded(transactor: Transactor[IO]): IO[Unit] =
    ReferenceDataSeederSupport.seedIfNeeded(transactor)
