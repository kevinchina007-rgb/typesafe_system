package com.typesafe.travel.persistence.attraction

import cats.effect.IO
import com.typesafe.travel.attraction.domain.*

import java.sql.Connection

object AttractionPlannerPlainSql:
  def suggestions(connection: Connection, input: AttractionSuggestionRequest): IO[AttractionSuggestionListPlannerResponse] =
    AttractionPlannerReadSql.suggestions(connection, input)

  def list(connection: Connection, input: ListAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    AttractionPlannerReadSql.list(connection, input)

  def details(connection: Connection, input: GetAttractionDetailsPlannerRequest): IO[Attraction] =
    AttractionPlannerReadSql.details(connection, input)
