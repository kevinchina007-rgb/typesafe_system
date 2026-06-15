// 这个文件只保存 planner 引擎内部使用的强类型 ID，避免 String 在请求、候选、行程与错误对象之间混用。
// 这些标识符是后端实现细节，不是前端可见的镜像对象，因此不会单独生成前端同名文件。
package com.typesafe.travel.planner.domain

// Strongly typed IDs keep planner models explicit and prevent accidental String mixing.
final case class PlannerRequestId(value: String) extends AnyVal
final case class TripPlanId(value: String) extends AnyVal
final case class PlannerCandidateId(value: String) extends AnyVal
final case class PlannerWarningId(value: String) extends AnyVal
final case class ConstraintViolationId(value: String) extends AnyVal

