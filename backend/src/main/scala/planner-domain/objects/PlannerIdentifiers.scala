package com.typesafe.travel.planner.domain

// Strongly typed IDs keep planner models explicit and prevent accidental String mixing.
final case class PlannerRequestId(value: String) extends AnyVal
final case class TripPlanId(value: String) extends AnyVal
final case class PlannerCandidateId(value: String) extends AnyVal
final case class PlannerWarningId(value: String) extends AnyVal
final case class ConstraintViolationId(value: String) extends AnyVal

