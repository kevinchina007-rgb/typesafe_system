package com.typesafe.travel.train.domain

import com.typesafe.travel.shared.kernel.*

import java.time.LocalDate

final case class TrainSearchCriteria(
    fromStationCode: Option[TrainStationCode],
    toStationCode: Option[TrainStationCode],
    departureDate: Option[LocalDate]
)

trait TrainRepository[F[_]]:
  def nextManagerId: F[ManagerId]
  def nextTrainId: F[TrainId]
  def nextTrainStopId: F[TrainStopId]
  def nextTrainSeatInventoryId: F[TrainSeatInventoryId]
  def nextTrainSeatId: F[TrainSeatId]
  def listSeatAllocations(trainId: TrainId): F[Vector[TrainSegmentSeatAllocation]]
  def nextTrainSegmentPriceId: F[TrainSegmentPriceId]
  def nextTrainRefundPolicySegmentId: F[TrainRefundPolicySegmentId]
  def findRailwayManagerByEmail(emailAddress: EmailAddress): F[Option[RailwayManager]]
  def findRailwayManagerById(managerId: ManagerId): F[Option[RailwayManager]]
  def saveRailwayManager(railwayManager: RailwayManager): F[RailwayManager]
  def findTrainById(trainId: TrainId): F[Option[TrainJourney]]
  def findTrainBySeatInventoryId(trainSeatInventoryId: TrainSeatInventoryId): F[Option[TrainJourney]]
  def searchTrains(trainSearchCriteria: TrainSearchCriteria): F[List[TrainJourney]]
  def saveTrain(trainJourney: TrainJourney): F[TrainJourney]
