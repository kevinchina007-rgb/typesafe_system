// 这个文件声明后端 planner 引擎的“候选评分”契约，以及默认评分实现。
// 评分属于后端内部决策逻辑，前端不会直接镜像这套接口，而是只接收最终生成的行程结果。
package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// CandidateScorer recalculates scores after validation/matching so recommendation stays explicit.
trait CandidateScorer:
  def scoreCandidate(
      request: PlannerRequest,
      validationResult: CandidateValidationResult
  ): PlannerScore

object CandidateScorer:
  val default: CandidateScorer = new CandidateScorer:
    override def scoreCandidate(
        request: PlannerRequest,
        validationResult: CandidateValidationResult
    ): PlannerScore =
      val baseScore = validationResult.candidate.score.value
      val warningPenalty = BigDecimal(validationResult.warnings.size) * BigDecimal(2)
      val blockingPenalty =
        if validationResult.blockingErrors.isEmpty then BigDecimal(0)
        else BigDecimal(100)

      PlannerScore(
        value = baseScore - warningPenalty - blockingPenalty,
        explanation =
          if validationResult.blockingErrors.isEmpty then
            "Stage-2 default scorer kept the candidate valid and applied only warning penalties."
          else
            "Stage-2 default scorer heavily penalised blocking validation errors."
      )

