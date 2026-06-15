// 这个文件提供后端 planner 引擎的本地/测试用假候选生成器。
// 它的作用是让后端在没有接入完整算法或真实数据源时，仍能跑通规划链路；因此它也是后端专用实现，没有前端对应模块。
package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*
import com.typesafe.travel.shared.kernel.*

// FakeCandidateGenerator provides a deterministic stage-2 candidate for local iteration.
final class FakeCandidateGenerator extends CandidateGenerator:
  override def generateCandidates(request: PlannerRequest): Either[PlanningError, Vector[PlannerCandidate]] =
    Right(Vector(buildBalancedCandidate(request)))

  private def buildBalancedCandidate(request: PlannerRequest): PlannerCandidate =
    PlannerCandidate(
      candidateId = PlannerCandidateId(s"${request.requestId.value}-balanced"),
      strategy = PlannerStrategy.Balanced,
      score = PlannerScore(
        value = BigDecimal(78),
        explanation = "Stage-2 fake generator starts from a balanced baseline score."
      ),
      days = buildDayPlans(request),
      estimatedBudget = request.budget.targetBudget.orElse(Some(Money.unsafe(BigDecimal(1800), Currency.CNY))),
      warnings =
        if request.requiredAttractions.isEmpty then
          Vector(PlanningWarning(PlannerWarningId("warning-no-attractions"), "No required attractions were supplied."))
        else Vector.empty
    )

  private def buildDayPlans(request: PlannerRequest): Vector[TripPlanDay] =
    val totalDays = deriveDayCount(request.horizon)

    (1 to totalDays).toVector.map { dayNumber =>
      TripPlanDay(
        dayNumber = dayNumber,
        theme = s"${request.destination.city} Day $dayNumber",
        items = Vector(
          TripPlanItem.AttractionVisit(
            title = "Morning highlight",
            attractionName = request.requiredAttractions.lift(dayNumber - 1).map(_.label).getOrElse(s"${request.destination.city} landmark"),
            startTime = Some(PlannerTimeBlock(LocalDateTimeLabel("09:00"), Some(LocalDateTimeLabel("11:00")))),
            durationMinutes = 120,
            notes = Some("TODO(stage-4): replace stub attraction allocation with strategy-specific logic."),
            matchingStatus = MatchingStatus.NotRequested
          ),
          TripPlanItem.TransportSegment(
            title = "Midday transfer",
            mode = PlannedTransportMode.Metro,
            fromLabel = "City center",
            toLabel = "Next district",
            startTime = Some(PlannerTimeBlock(LocalDateTimeLabel("11:30"), Some(LocalDateTimeLabel("12:00")))),
            durationMinutes = 30,
            matchingStatus = MatchingStatus.NotRequested
          ),
          TripPlanItem.FreeBlock(
            title = "Flexible afternoon",
            label = "Lunch and free exploration",
            startTime = Some(PlannerTimeBlock(LocalDateTimeLabel("13:00"), Some(LocalDateTimeLabel("15:00")))),
            durationMinutes = 120,
            notes = Some("TODO(stage-9): allow users to edit or regenerate this free block."),
            matchingStatus = MatchingStatus.NotRequested
          )
        )
      )
    }

  private def deriveDayCount(horizon: PlanningHorizon): Int =
    horizon match
      case PlanningHorizon.FixedDates(period) => period.travelDayCount.toInt
      case PlanningHorizon.DayCount(days)     => days

