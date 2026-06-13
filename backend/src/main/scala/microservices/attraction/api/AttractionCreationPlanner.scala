package com.typesafe.travel.attraction.domain

import cats.effect.IO
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant

object AttractionCreationPlanner:
  def create(connection: Connection, input: CreateAttractionPlannerRequest): IO[Attraction] =
    AttractionPlannerPlainSql.create(connection, input, Instant.now())

  def createTicketType(connection: Connection, input: CreateAttractionTicketTypePlannerRequest): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketType(connection, input, Instant.now())

  def createTicketSession(connection: Connection, input: CreateAttractionTicketSessionPlannerRequest): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketSession(connection, input, Instant.now())

  def createTicketRule(connection: Connection, input: CreateAttractionTicketRulePlannerRequest): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketRule(connection, input, Instant.now())
