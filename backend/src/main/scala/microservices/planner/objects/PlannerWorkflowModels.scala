// 这个文件只定义后端 planner 引擎内部的流程模型，供生成、匹配、评分、验证等步骤在同一条链路里传递。
// 它不是前端镜像对象，也不对应一个独立的前端 microservice；这里的模型仅服务于后端智能行程规划引擎。
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

