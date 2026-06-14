// GetAttractionPlanner 是 attraction 的兼容汇总入口，只保留 details / listManaged 这类过渡方法，不应作为前端新镜像对象的目标。
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object GetAttractionPlanner:
  def details(connection: Connection, input: GetAttractionDetailsPlannerRequest): IO[Attraction] =
    AttractionPlannerPlainSql.details(connection, input)

  def listManaged(connection: Connection, input: ListManagedAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.listManaged(connection, input)


