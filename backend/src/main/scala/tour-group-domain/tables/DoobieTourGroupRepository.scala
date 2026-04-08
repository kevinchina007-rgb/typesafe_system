package com.typesafe.travel.persistence.tourgroup

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import cats.data.NonEmptyList
import doobie.*
import doobie.implicits.*

import java.time.{Instant, LocalDate}

final class DoobieTourGroupRepository[F[_]: Async](protected val transactor: Transactor[F])
    extends TourGroupRepository[F]
    with DoobieTourGroupRepositoryCoreSupport[F]
    with DoobieTourGroupRepositoryChatSupport[F]
    with DoobieTourGroupRepositoryPersistenceSupport[F]

object DoobieTourGroupRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieTourGroupRepository[F] =
    new DoobieTourGroupRepository[F](transactor)
