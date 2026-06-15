// 这个文件声明后端 planner 引擎的“约束校验”契约，用于判断候选行程是否满足输入要求和系统限制。
// 它是纯后端逻辑，不需要在前端建立同名微服务目录；前端只消费校验后的结果。
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

