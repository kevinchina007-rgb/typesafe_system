package com.typesafe.travel.attraction.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object AttractionSuggestionsPlanner extends ConnectionApiPlan[AttractionSuggestionRequest, AttractionSuggestionListPlannerResponse]:
  override val name: String = "AttractionSuggestionsPlanner"
  override def plan(input: AttractionSuggestionRequest, connection: Connection): IO[AttractionSuggestionListPlannerResponse] =
    AttractionPlannerPlainSql.suggestions(connection, input)

object ListAttractionsPlanner extends ConnectionApiPlan[ListAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListAttractionsPlanner"
  override def plan(input: ListAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.list(connection, input)

object GetAttractionDetailsPlanner extends ConnectionApiPlan[GetAttractionDetailsPlannerRequest, Attraction]:
  override val name: String = "GetAttractionDetailsPlanner"
  override def plan(input: GetAttractionDetailsPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.details(connection, input)

object ListManagedAttractionsPlanner extends ConnectionApiPlan[ListManagedAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListManagedAttractionsPlanner"
  override def plan(input: ListManagedAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.listManaged(connection, input)

object CreateAttractionPlanner extends ConnectionApiPlan[CreateAttractionPlannerRequest, Attraction]:
  override val name: String = "CreateAttractionPlanner"
  override def plan(input: CreateAttractionPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.create(connection, input, Instant.now())
