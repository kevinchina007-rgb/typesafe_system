package com.typesafe.travel.attraction.domain

import cats.effect.IO
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object GetAttractionPlanner:
  def details(connection: Connection, input: GetAttractionDetailsPlannerRequest): IO[Attraction] =
    AttractionPlannerPlainSql.details(connection, input)

  def listManaged(connection: Connection, input: ListManagedAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.listManaged(connection, input)
