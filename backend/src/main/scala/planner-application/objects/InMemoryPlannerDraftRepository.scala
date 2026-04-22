package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// InMemoryPlannerDraftRepository keeps stage-2 runnable without a database dependency.
final class InMemoryPlannerDraftRepository extends PlannerDraftRepository:
  private var drafts: Map[TripPlanId, TripPlan] = Map.empty

  override def saveDraft(draft: TripPlan): Either[PlanningError, TripPlan] =
    drafts = drafts.updated(draft.tripPlanId, draft)
    Right(draft)

  override def findDraftById(tripPlanId: TripPlanId): Either[PlanningError, Option[TripPlan]] =
    Right(drafts.get(tripPlanId))

