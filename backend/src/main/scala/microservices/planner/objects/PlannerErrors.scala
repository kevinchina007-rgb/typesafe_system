// PlannerErrors 定义planner模块的错误模型。

package com.typesafe.travel.planner.domain

import com.typesafe.travel.shared.kernel.*

sealed trait PlanningError extends DomainError:
  def message: String

object PlanningError:
  final case class PlannerRequestWasInvalid(reason: String) extends PlanningError:
    val message: String = s"Planner request was invalid: $reason"

  final case class CandidateGenerationFailed(strategy: PlannerStrategy, reason: String) extends PlanningError:
    val message: String = s"Planner candidate generation failed for '$strategy': $reason"

  final case class ConstraintViolationFound(reason: String) extends PlanningError:
    val message: String = s"Planner constraint violation: $reason"

  final case class TripPlanWasNotFound(tripPlanId: TripPlanId) extends PlanningError:
    val message: String = s"Trip plan '${tripPlanId.value}' was not found"

  final case class CandidateWasNotFound(candidateId: PlannerCandidateId) extends PlanningError:
    val message: String = s"Planner candidate '${candidateId.value}' was not found"
