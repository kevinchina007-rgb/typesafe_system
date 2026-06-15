// 这个文件定义 planner 后端引擎的核心数据结构：请求、行程计划、候选结果、约束与时间块。
// 它属于后端基础能力层，没有独立的前端微服务目录；前端只会通过其他业务域的入口间接使用这些规划结果。
package com.typesafe.travel.planner.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

// PlannerRequest is the structured input collected from the Smart Planner page.
final case class PlannerRequest(
    requestId: PlannerRequestId,
    ownerUserId: UserId,
    destination: PlannerDestination,
    horizon: PlanningHorizon,
    requiredAttractions: Vector[RequiredAttraction],
    budget: BudgetPreference,
    preferences: TravelPreference,
    travelerCount: Int,
    pace: TripPace,
    constraints: Vector[PlanningConstraint]
)
// TODO(stage-2): add smart constructors and validation helpers for request invariants.

final case class PlannerDestination(
    city: String,
    region: Option[String],
    country: Option[String]
)

sealed trait PlanningHorizon

object PlanningHorizon:
  final case class FixedDates(period: TravelPeriod) extends PlanningHorizon
  final case class DayCount(days: Int) extends PlanningHorizon

final case class RequiredAttraction(
    label: String,
    attractionIdHint: Option[AttractionId],
    notes: Option[String]
)

final case class BudgetPreference(
    targetBudget: Option[Money],
    isFlexible: Boolean
)

final case class TravelPreference(
    budgetPriority: PreferenceLevel,
    comfortPriority: PreferenceLevel,
    attractionPriority: PreferenceLevel,
    transportPreference: TransportPreference,
    hotelPreference: HotelPreference
)
// TODO(stage-2): derive weighting helpers for candidate scoring.

final case class TripPlan(
    tripPlanId: TripPlanId,
    ownerUserId: UserId,
    status: TripPlanStatus,
    summary: TripPlanSummary,
    selectedCandidateId: Option[PlannerCandidateId],
    days: Vector[TripPlanDay],
    createdAt: Instant
)
// TODO(stage-5): add editable draft operations once the planner becomes user-editable.

final case class TripPlanSummary(
    title: String,
    description: String,
    estimatedBudget: Option[Money],
    warningCount: Int
)

final case class TripPlanDay(
    dayNumber: Int,
    theme: String,
    items: Vector[TripPlanItem]
)

sealed trait TripPlanItem:
  def title: String
  def startTime: Option[PlannerTimeBlock]
  def matchingStatus: MatchingStatus

object TripPlanItem:
  final case class AttractionVisit(
      title: String,
      attractionName: String,
      startTime: Option[PlannerTimeBlock],
      durationMinutes: Int,
      notes: Option[String],
      matchingStatus: MatchingStatus
  ) extends TripPlanItem

  final case class TransportSegment(
      title: String,
      mode: PlannedTransportMode,
      fromLabel: String,
      toLabel: String,
      startTime: Option[PlannerTimeBlock],
      durationMinutes: Int,
      matchingStatus: MatchingStatus
  ) extends TripPlanItem

  final case class HotelStay(
      title: String,
      hotelLabel: String,
      stayPeriod: StayPeriod,
      matchingStatus: MatchingStatus
  ) extends TripPlanItem:
    override def startTime: Option[PlannerTimeBlock] = None

  final case class FreeBlock(
      title: String,
      label: String,
      startTime: Option[PlannerTimeBlock],
      durationMinutes: Int,
      notes: Option[String],
      matchingStatus: MatchingStatus
  ) extends TripPlanItem

final case class PlannerTimeBlock(
    startAt: LocalDateTimeLabel,
    endAt: Option[LocalDateTimeLabel]
)

final case class LocalDateTimeLabel(value: String) extends AnyVal

final case class PlannerCandidate(
    candidateId: PlannerCandidateId,
    strategy: PlannerStrategy,
    score: PlannerScore,
    days: Vector[TripPlanDay],
    estimatedBudget: Option[Money],
    warnings: Vector[PlanningWarning]
)

final case class PlannerScore(
    value: BigDecimal,
    explanation: String
)

final case class PlanningWarning(
    warningId: PlannerWarningId,
    message: String
)

sealed trait PlanningConstraint

object PlanningConstraint:
  final case class BudgetCap(limit: Money) extends PlanningConstraint
  final case class DailyTimeLimit(maxMinutesPerDay: Int) extends PlanningConstraint
  final case class MustIncludeAttraction(attraction: RequiredAttraction) extends PlanningConstraint
  final case class PreferredTransport(mode: TransportPreference) extends PlanningConstraint
  case object AvoidLateNightTransfer extends PlanningConstraint

  // TODO(stage-3): expand with open-hours, transfer-feasibility, and existing-order conflict constraints.

// TODO(stage-2): add application-facing domain service contracts once orchestration begins.
