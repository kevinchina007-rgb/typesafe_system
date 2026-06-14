package com.typesafe.travel.train.domain

import cats.effect.IO

import java.sql.Connection
import java.time.Instant

object TrainPlannerPlainSqlSupport:
  def suggestions(connection: Connection, input: TrainSuggestionPlannerRequest): IO[TrainSuggestionListPlannerResponse] =
    TrainPlannerPlainSqlSearch.suggestions(connection, input)

  def registerManager(connection: Connection, input: RegisterRailwayManagerPlannerRequest, passwordHash: String, now: Instant): IO[TrainAdminSessionPlannerResponse] =
    TrainPlannerPlainSqlManager.registerManager(connection, input, passwordHash, now)

  def listManaged(connection: Connection, input: ListManagedTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSqlManager.listManaged(connection, input, now)

  def search(connection: Connection, input: SearchTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSqlSearch.search(connection, input, now)

  def get(connection: Connection, input: TrainByIdPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    TrainPlannerPlainSqlShared.readTrain(connection, input.trainId, now)

  def create(connection: Connection, input: CreateTrainJourneyPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    TrainPlannerPlainSqlManager.create(connection, input, now)

  def bookItem(connection: Connection, input: BookTrainItemPlannerRequest, now: Instant): IO[BookTrainItemPlannerResponse] =
    TrainPlannerPlainSqlBooking.bookItem(connection, input, now)
