package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// CandidateValidationResult keeps candidate validation output structured for later stages.
final case class CandidateValidationResult(
    candidate: PlannerCandidate,
    blockingErrors: Vector[PlanningError],
    warnings: Vector[PlanningWarning]
)

// CandidateEvaluation represents a candidate after matching, validation, and scoring.
final case class CandidateEvaluation(
    candidate: PlannerCandidate,
    validation: CandidateValidationResult,
    isRecommended: Boolean
)

// PlannerDraftResult is the stage-2 service result for the Smart Planner flow.
final case class PlannerDraftResult(
    draft: TripPlan,
    recommended: CandidateEvaluation,
    alternatives: Vector[CandidateEvaluation]
)

