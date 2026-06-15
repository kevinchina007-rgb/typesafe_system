// 这个文件声明后端 planner 引擎的“候选生成”契约。
// 它只存在于后端，因为候选行程的生成依赖后端内部的规则、数据和算法，不存在对应的前端微服务目录。
package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

// CandidateGenerator produces candidate plans without dictating where they come from.
trait CandidateGenerator:
  def generateCandidates(request: PlannerRequest): Either[PlanningError, Vector[PlannerCandidate]]

