// ListManagedAttractionsPlanner 只负责 attraction 管理端列表查询这一条后端链路，属于后端内部业务入口，不需要前端复制管理实现。
package com.typesafe.travel.operations.domain

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.operations.AttractionPlannerPlainSql

import java.sql.Connection

object ListManagedAttractionsPlanner extends ConnectionApiPlan[ListManagedAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListManagedAttractionsPlanner"
  override def plan(input: ListManagedAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.listManaged(connection, input)





