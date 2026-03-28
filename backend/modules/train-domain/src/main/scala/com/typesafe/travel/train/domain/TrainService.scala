package com.typesafe.travel.train.domain

import cats.MonadThrow
import cats.syntax.all.*

final class TrainService[F[_]: MonadThrow](
    trainRepository: TrainRepository[F]
):
  def registerRailwayManager(
      operatorCode: String,
      primaryEmailAddress: com.typesafe.travel.shared.kernel.EmailAddress,
      displayName: com.typesafe.travel.shared.kernel.PersonName,
      createdAt: java.time.Instant
  ): F[RailwayManager] =
    trainRepository.nextManagerId.flatMap { managerId =>
      trainRepository.saveRailwayManager(
        RailwayManager.registerNewRailwayManager(managerId, operatorCode, primaryEmailAddress, displayName, createdAt)
      )
    }

  def loginRailwayManager(primaryEmailAddress: com.typesafe.travel.shared.kernel.EmailAddress): F[RailwayManager] =
    trainRepository
      .findRailwayManagerByEmail(primaryEmailAddress)
      .flatMap(_.liftTo[F](TrainError.RailwayManagerWasNotFoundByEmail(primaryEmailAddress)))

  def browseTrains(trainSearchCriteria: TrainSearchCriteria): F[List[TrainJourney]] =
    trainRepository.searchTrains(trainSearchCriteria)

  def getTrainDetails(trainId: com.typesafe.travel.shared.kernel.TrainId): F[TrainJourney] =
    trainRepository.findTrainById(trainId).flatMap(_.liftTo[F](TrainError.TrainWasNotFound(trainId)))

  def saveTrain(trainJourney: TrainJourney): F[TrainJourney] =
    trainRepository.saveTrain(trainJourney)
