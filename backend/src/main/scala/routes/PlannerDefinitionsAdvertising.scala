// PlannerDefinitionsAdvertising 负责请求路由分发。

package com.typesafe.travel.api.routes

import com.typesafe.travel.advertising.domain.*

object PlannerDefinitionsAdvertising:
  import PlannerRegistry.RegisteredPlan.WithConnection

  val registry: PlannerRegistry =
    PlannerRegistry(
      List(
        WithConnection(ListAdvertisementsPlanner),
        WithConnection(CreateAdvertisementPlanner),
        WithConnection(UpdateAdvertisementPlanner),
        WithConnection(SubmitAdvertisementForReviewPlanner),
        WithConnection(PauseAdvertisementPlanner),
        WithConnection(ApproveAdvertisementPlanner),
        WithConnection(RejectAdvertisementPlanner),
        WithConnection(AssignAdvertisementSlotPlanner),
        WithConnection(PauseAdvertisementDisplayPlanner),
        WithConnection(GetAdvertisementDeliverySettingsPlanner),
        WithConnection(SaveAdvertisementDeliverySettingsPlanner),
        WithConnection(UploadAdvertisementImagePlanner),
        WithConnection(GenerateAdvertisementImageCandidatesPlanner),
        WithConnection(GenerateAdvertisementTextCandidatesPlanner)
      ).map(planner => planner.name -> planner).toMap
    )

