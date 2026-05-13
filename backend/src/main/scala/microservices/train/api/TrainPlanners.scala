package com.typesafe.travel.train.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.hashPasswordForLoginEmail
import com.typesafe.travel.shared.kernel.EmailAddress

import java.sql.Connection

object TrainSuggestionsPlanner extends ConnectionApiPlan[TrainSuggestionPlannerRequest, TrainSuggestionListPlannerResponse]:
  override val name: String = "TrainSuggestionsPlanner"
  override def plan(input: TrainSuggestionPlannerRequest, connection: Connection): IO[TrainSuggestionListPlannerResponse] =
    TrainPlannerPlainSql.suggestions(connection, input)

object RegisterRailwayManagerPlanner extends ConnectionApiPlan[RegisterRailwayManagerPlannerRequest, TrainAdminSessionPlannerResponse]:
  override val name: String = "RegisterRailwayManagerPlanner"
  override def plan(input: RegisterRailwayManagerPlannerRequest, connection: Connection): IO[TrainAdminSessionPlannerResponse] =
    for
      email <- IO.fromEither(EmailAddress.create(input.email))
      passwordHash <- hashPasswordForLoginEmail(input.password, email)
      response <- TrainPlannerPlainSql.registerManager(connection, input, passwordHash, java.time.Instant.now())
    yield response

object ListManagedTrainsPlanner extends ConnectionApiPlan[ListManagedTrainsPlannerRequest, TrainListPlannerResponse]:
  override val name: String = "ListManagedTrainsPlanner"
  override def plan(input: ListManagedTrainsPlannerRequest, connection: Connection): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSql.listManaged(connection, input, java.time.Instant.now())

object CreateTrainJourneyPlanner extends ConnectionApiPlan[CreateTrainJourneyPlannerRequest, TrainPlannerResponse]:
  override val name: String = "CreateTrainJourneyPlanner"
  override def plan(input: CreateTrainJourneyPlannerRequest, connection: Connection): IO[TrainPlannerResponse] =
    TrainPlannerPlainSql.create(connection, input, java.time.Instant.now())

object SearchTrainsPlanner extends ConnectionApiPlan[SearchTrainsPlannerRequest, TrainListPlannerResponse]:
  override val name: String = "SearchTrainsPlanner"
  override def plan(input: SearchTrainsPlannerRequest, connection: Connection): IO[TrainListPlannerResponse] =
    TrainPlannerPlainSql.search(connection, input, java.time.Instant.now())

object GetTrainDetailsPlanner extends ConnectionApiPlan[TrainByIdPlannerRequest, TrainPlannerResponse]:
  override val name: String = "GetTrainDetailsPlanner"
  override def plan(input: TrainByIdPlannerRequest, connection: Connection): IO[TrainPlannerResponse] =
    TrainPlannerPlainSql.get(connection, input, java.time.Instant.now())

object BookTrainItemPlanner extends ConnectionApiPlan[BookTrainItemPlannerRequest, BookTrainItemPlannerResponse]:
  override val name: String = "BookTrainItemPlanner"
  override def plan(input: BookTrainItemPlannerRequest, connection: Connection): IO[BookTrainItemPlannerResponse] =
    TrainPlannerPlainSql.bookItem(connection, input, java.time.Instant.now())
