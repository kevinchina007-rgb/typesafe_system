package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// PlannerDraftRepository persists trip plan drafts once infrastructure is introduced.
trait PlannerDraftRepository:
  def saveDraft(draft: TripPlan): Either[PlanningError, TripPlan]
  def findDraftById(tripPlanId: TripPlanId): Either[PlanningError, Option[TripPlan]]

