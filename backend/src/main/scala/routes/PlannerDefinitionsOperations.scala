// PlannerDefinitionsOperations 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.operations.domain.*

object PlannerDefinitionsOperations:
  val registry: PlannerRegistry =
    PlannerRegistry.combine(
      PlannerDefinitionsOperationsOverall.registry,
      PlannerDefinitionsOperationsAirline.registry,
      PlannerDefinitionsOperationsHotel.registry,
      PlannerDefinitionsOperationsTrain.registry,
      PlannerDefinitionsOperationsSiteAdmin.registry,
      PlannerDefinitionsOperationsAttraction.registry
    )
