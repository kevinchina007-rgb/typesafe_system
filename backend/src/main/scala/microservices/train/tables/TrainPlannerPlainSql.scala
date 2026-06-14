// TrainPlannerPlainSql is the thin forwarding entry for train plain SQL operations.
package com.typesafe.travel.train.domain

import cats.effect.IO

import java.sql.Connection
import java.time.Instant

object TrainPlannerPlainSql:
  import com.typesafe.travel.train.domain.TrainPlannerPlainSqlSupport.*

  def suggestions(connection: Connection, input: TrainSuggestionPlannerRequest): IO[TrainSuggestionListPlannerResponse] =
    TrainPlannerPlainSqlSupport.suggestions(connection, input)

  def registerManager(connection: Connection, input: RegisterRailwayManagerPlannerRequest, passwordHash: String, now: Instant): IO[TrainAdminSessionPlannerResponse] =
    TrainPlannerPlainSqlSupport.registerManager(connection, input, passwordHash, now)

  def listManaged(connection: Connection, input: ListManagedTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSqlSupport.listManaged(connection, input, now)

  def search(connection: Connection, input: SearchTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSqlSupport.search(connection, input, now)

  def get(connection: Connection, input: TrainByIdPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    TrainPlannerPlainSqlSupport.get(connection, input, now)

  def create(connection: Connection, input: CreateTrainJourneyPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    TrainPlannerPlainSqlSupport.create(connection, input, now)

  def bookItem(connection: Connection, input: BookTrainItemPlannerRequest, now: Instant): IO[BookTrainItemPlannerResponse] =
    TrainPlannerPlainSqlSupport.bookItem(connection, input, now)
