package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*
import com.typesafe.travel.shared.kernel.*

import java.time.LocalDate

// PlannerApplicationExample is a minimal runnable example of the stage-2 orchestration flow.
object PlannerApplicationExample:
  def runExample(): Either[PlanningError, PlannerDraftResult] =
    val plannerService = new PlannerService(
      candidateGenerator = new FakeCandidateGenerator,
      constraintChecker = ConstraintChecker.permissive,
      candidateScorer = CandidateScorer.default,
      plannerDraftRepository = new InMemoryPlannerDraftRepository,
      resourceMatcher = ResourceMatcher.noop
    )

    plannerService.buildDraft(sampleRequest)

  val sampleRequest: PlannerRequest =
    PlannerRequest(
      requestId = PlannerRequestId("sample-request-1"),
      ownerUserId = UserId("user-001"),
      destination = PlannerDestination(
        city = "Shanghai",
        region = Some("Shanghai"),
        country = Some("China")
      ),
      horizon = PlanningHorizon.FixedDates(
        TravelPeriod.unsafe(
          startDate = LocalDate.of(2026, 5, 1),
          endDate = LocalDate.of(2026, 5, 3)
        )
      ),
      requiredAttractions = Vector(
        RequiredAttraction(
          label = "The Bund",
          attractionIdHint = None,
          notes = Some("Must fit the first day if possible.")
        )
      ),
      budget = BudgetPreference(
        targetBudget = Some(Money.unsafe(BigDecimal(2500), Currency.CNY)),
        isFlexible = true
      ),
      preferences = TravelPreference(
        budgetPriority = PreferenceLevel.Medium,
        comfortPriority = PreferenceLevel.Medium,
        attractionPriority = PreferenceLevel.High,
        transportPreference = TransportPreference.PublicTransitFirst,
        hotelPreference = HotelPreference.CentralLocation
      ),
      travelerCount = 2,
      pace = TripPace.Balanced,
      constraints = Vector(
        PlanningConstraint.BudgetCap(Money.unsafe(BigDecimal(2500), Currency.CNY)),
        PlanningConstraint.DailyTimeLimit(600)
      )
    )

  // TODO(stage-6): replace direct invocation with HTTP route wiring and DTO mapping.

