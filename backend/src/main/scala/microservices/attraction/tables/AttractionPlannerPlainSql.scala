package com.typesafe.travel.persistence.attraction

import cats.effect.IO
import com.typesafe.travel.attraction.domain.*

import java.sql.Connection
import java.time.Instant

object AttractionPlannerPlainSql:
  def suggestions(connection: Connection, input: AttractionSuggestionRequest): IO[AttractionSuggestionListPlannerResponse] =
    AttractionPlannerReadSql.suggestions(connection, input)

  def list(connection: Connection, input: ListAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    AttractionPlannerReadSql.list(connection, input)

  def details(connection: Connection, input: GetAttractionDetailsPlannerRequest): IO[Attraction] =
    AttractionPlannerReadSql.details(connection, input)

  def listManaged(connection: Connection, input: ListManagedAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    AttractionPlannerReadSql.listManaged(connection, input)

  def create(connection: Connection, input: CreateAttractionPlannerRequest, now: Instant): IO[Attraction] =
    AttractionPlannerWriteSql.create(connection, input, now)

  def createTicketType(connection: Connection, input: CreateAttractionTicketTypePlannerRequest, now: Instant): IO[Attraction] =
    AttractionPlannerWriteSql.createTicketType(connection, input, now)

  def createTicketSession(connection: Connection, input: CreateAttractionTicketSessionPlannerRequest, now: Instant): IO[Attraction] =
    AttractionPlannerWriteSql.createTicketSession(connection, input, now)

  def createTicketRule(connection: Connection, input: CreateAttractionTicketRulePlannerRequest, now: Instant): IO[Attraction] =
    AttractionPlannerWriteSql.createTicketRule(connection, input, now)

  def consumeInventoryForPaidOrder(connection: Connection, orderId: String): Unit =
    AttractionPlannerInventorySql.consumeInventoryForPaidOrder(connection, orderId)

  def restoreInventoryForCancelledOrder(connection: Connection, orderId: String): Unit =
    AttractionPlannerInventorySql.restoreInventoryForCancelledOrder(connection, orderId)
