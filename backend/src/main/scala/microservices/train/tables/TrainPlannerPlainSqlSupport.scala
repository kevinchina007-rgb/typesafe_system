package com.typesafe.travel.train.domain

import cats.effect.IO

import java.sql.Connection
import java.time.Instant

object TrainPlannerPlainSqlSupport:
  def suggestions(connection: Connection, input: TrainSuggestionsPlannerRequest): IO[TrainSuggestionListPlannerResponse] =
    TrainPlannerPlainSqlSearch.suggestions(connection, input)

  def search(connection: Connection, input: SearchTrainsPlannerRequest, now: Instant): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSqlSearch.search(connection, input, now)

  def get(connection: Connection, input: GetTrainDetailsPlannerRequest, now: Instant): IO[TrainPlannerResponse] =
    IO.blocking(TrainPlannerPlainSqlShared.readTrain(connection, input.trainId, now))

  def bookItem(connection: Connection, input: BookTrainItemPlannerRequest, now: Instant): IO[BookTrainItemPlannerResponse] =
    TrainPlannerPlainSqlBooking.bookItem(connection, input, now)
