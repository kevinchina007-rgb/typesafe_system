package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object SearchAttractionsPlanner:
  def suggestions(connection: Connection, input: AttractionSuggestionRequest): IO[AttractionSuggestionListPlannerResponse] =
    AttractionPlannerPlainSql.suggestions(connection, input)

  def list(connection: Connection, input: ListAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.list(connection, input)



