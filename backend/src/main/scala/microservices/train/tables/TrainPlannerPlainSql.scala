package com.typesafe.travel.train.domain

import cats.effect.IO

import java.sql.Connection
import java.time.Instant

object TrainPlannerPlainSql:
  import com.typesafe.travel.train.domain.TrainPlannerPlainSqlSupport.*

  def suggestions(connection: Connection, input: TrainSuggestionsPlannerRequest): IO[TrainSuggestionListPlannerResponse] =
    TrainPlannerPlainSqlSupport.suggestions(connection, input)

  def search(connection: Connection, input: SearchTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSqlSupport.search(connection, input, now)

  def get(connection: Connection, input: GetTrainDetailsPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    TrainPlannerPlainSqlSupport.get(connection, input, now)

  def bookItem(connection: Connection, input: BookTrainItemPlannerRequest, now: Instant): IO[BookTrainItemPlannerResponse] =
    TrainPlannerPlainSqlSupport.bookItem(connection, input, now)
