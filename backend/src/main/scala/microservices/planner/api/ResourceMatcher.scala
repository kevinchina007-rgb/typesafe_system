// 这个文件声明后端 planner 引擎的“资源匹配”契约，用于把候选结果和酒店、航班、火车、景点等资源对接起来。
// 匹配过程依赖后端可用资源与业务规则，因此它是后端内部能力，不对应前端镜像文件。
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

