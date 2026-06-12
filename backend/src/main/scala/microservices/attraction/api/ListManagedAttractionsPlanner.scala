// ListManagedAttractionsPlanner 是景点模块的列表查询入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.attraction.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection

object ListManagedAttractionsPlanner extends ConnectionApiPlan[ListManagedAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListManagedAttractionsPlanner"
  override def plan(input: ListManagedAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.listManaged(connection, input)
