package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// ConstraintChecker validates whether a generated candidate is acceptable for the request.
trait ConstraintChecker:
  def validateCandidate(
      request: PlannerRequest,
      candidate: PlannerCandidate
  ): CandidateValidationResult

object ConstraintChecker:
  val permissive: ConstraintChecker = new ConstraintChecker:
    override def validateCandidate(
        request: PlannerRequest,
        candidate: PlannerCandidate
    ): CandidateValidationResult =
      CandidateValidationResult(
        candidate = candidate,
        blockingErrors = Vector.empty,
        warnings = candidate.warnings
      )

