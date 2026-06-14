// AttractionCreationPlanner orchestrates the backend creation flow for attraction.
// It chains attraction creation, ticket types, sessions, and rules, then delegates the actual persistence work to plain SQL.
// This file is an internal backend entry point; it does not require one-to-one frontend mirroring and should not carry database details directly.

package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

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


