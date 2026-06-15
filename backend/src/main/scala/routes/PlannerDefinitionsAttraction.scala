// 本文件汇总 attraction 域的 Planner 路由定义。
package com.typesafe.travel.api.routes

import com.typesafe.travel.attraction.api.*

object PlannerDefinitionsAttraction:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(BookAttractionItemPlanner),
        WithConnection(AttractionSuggestionsPlanner),
        WithConnection(ListAttractionsPlanner),
        WithConnection(GetAttractionDetailsPlanner),
        WithConnection(ListManagedAttractionsPlanner),
        WithConnection(CreateAttractionPlanner),
        WithConnection(UploadAttractionImagePlanner),
        WithConnection(CreateAttractionTicketTypePlanner),
        WithConnection(CreateAttractionTicketSessionPlanner),
        WithConnection(CreateAttractionTicketRulePlanner)
      ).map(planner => planner.name -> planner).toMap
    )


