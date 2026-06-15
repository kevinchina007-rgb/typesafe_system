// PlannerErrors 定义planner模块的错误模型。

// 这个文件定义 planner 后端引擎的错误模型，用来描述请求不合法、候选生成失败、约束冲突和对象缺失等情况。
// 这些错误只在后端规划引擎内部流转，用于把算法/验证失败转换成可读的业务异常，不对应前端独立模块。
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
