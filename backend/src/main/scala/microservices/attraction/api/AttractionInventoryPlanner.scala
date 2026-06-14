package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object AttractionInventoryPlanner:
  def consumeInventoryForPaidOrder(connection: Connection, orderId: String): Unit =
    AttractionPlannerPlainSql.consumeInventoryForPaidOrder(connection, orderId)

  def restoreInventoryForCancelledOrder(connection: Connection, orderId: String): Unit =
    AttractionPlannerPlainSql.restoreInventoryForCancelledOrder(connection, orderId)



