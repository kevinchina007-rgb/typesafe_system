// AttractionInventoryPlanner handles attraction-related inventory consumption and restoration.
// It is an internal helper used by backend flows such as payment and cancellation, not a frontend-facing business entry point.
// The frontend should only see the final planner request / response objects and should not mirror this inventory helper layer.

package com.typesafe.travel.operations.domain

import com.typesafe.travel.attraction.domain.*

import com.typesafe.travel.persistence.operations.AttractionPlannerPlainSql

import java.sql.Connection

object AttractionInventoryPlanner:
  def consumeInventoryForPaidOrder(connection: Connection, orderId: String): Unit =
    AttractionPlannerPlainSql.consumeInventoryForPaidOrder(connection, orderId)

  def restoreInventoryForCancelledOrder(connection: Connection, orderId: String): Unit =
    AttractionPlannerPlainSql.restoreInventoryForCancelledOrder(connection, orderId)




