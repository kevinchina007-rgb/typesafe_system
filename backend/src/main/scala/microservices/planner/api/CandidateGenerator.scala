package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// CandidateGenerator produces candidate plans without dictating where they come from.
trait CandidateGenerator:
  def generateCandidates(request: PlannerRequest): Either[PlanningError, Vector[PlannerCandidate]]

