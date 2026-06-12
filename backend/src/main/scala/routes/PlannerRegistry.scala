// PlannerRegistry 负责请求路由分发。

package com.typesafe.travel.api.routes

import io.circe.{Decoder, Encoder}

final case class PlannerRegistry(
    planners: Map[String, PlannerRegistry.RegisteredPlan]
)

object PlannerRegistry:
  def combine(registries: PlannerRegistry*): PlannerRegistry =
    PlannerRegistry(
      registries.iterator.flatMap(_.planners).toMap
    )

  sealed trait RegisteredPlan:
    def name: String

  object RegisteredPlan:
    final case class Plain[Input, Output](
        planner: PlainApiPlan[Input, Output]
    )(using val inputDecoder: Decoder[Input], val outputEncoder: Encoder[Output]) extends RegisteredPlan:
      override val name: String = planner.name

    final case class WithConnection[Input, Output](
        planner: ConnectionApiPlan[Input, Output]
    )(using val inputDecoder: Decoder[Input], val outputEncoder: Encoder[Output]) extends RegisteredPlan:
      override val name: String = planner.name
