package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// ResourceMatcher is reserved for future integration with attractions, hotels, flights, and trains.
trait ResourceMatcher:
  def matchCandidate(
      request: PlannerRequest,
      candidate: PlannerCandidate
  ): Either[PlanningError, PlannerCandidate]

object ResourceMatcher:
  val noop: ResourceMatcher = new ResourceMatcher:
    override def matchCandidate(
        request: PlannerRequest,
        candidate: PlannerCandidate
    ): Either[PlanningError, PlannerCandidate] = Right(candidate)

