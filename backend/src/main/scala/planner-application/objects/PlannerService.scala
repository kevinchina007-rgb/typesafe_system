package com.typesafe.travel.planner.application

import com.typesafe.travel.planner.domain.*

import java.time.Instant

// PlannerService orchestrates candidate generation, validation, scoring, and draft assembly.
final class PlannerService(
    candidateGenerator: CandidateGenerator,
    constraintChecker: ConstraintChecker,
    candidateScorer: CandidateScorer,
    plannerDraftRepository: PlannerDraftRepository,
    resourceMatcher: ResourceMatcher
):

  def buildDraft(request: PlannerRequest): Either[PlanningError, PlannerDraftResult] =
    for
      generatedCandidates <- candidateGenerator.generateCandidates(request)
      matchedCandidates <- generatedCandidates.foldLeft[Either[PlanningError, Vector[PlannerCandidate]]](Right(Vector.empty)) {
        case (accEither, candidate) =>
          for
            acc <- accEither
            matched <- resourceMatcher.matchCandidate(request, candidate)
          yield acc :+ matched
      }
      evaluatedCandidates = matchedCandidates.map(candidate => evaluateCandidate(request, candidate))
      recommended <- chooseRecommendedCandidate(evaluatedCandidates)
      alternatives = evaluatedCandidates.filterNot(_.candidate.candidateId == recommended.candidate.candidateId)
      draft = buildDraftAggregate(request, recommended, alternatives)
      savedDraft <- plannerDraftRepository.saveDraft(draft)
    yield PlannerDraftResult(
      draft = savedDraft,
      recommended = recommended.copy(isRecommended = true),
      alternatives = alternatives.map(_.copy(isRecommended = false))
    )

  private def evaluateCandidate(
      request: PlannerRequest,
      candidate: PlannerCandidate
  ): CandidateEvaluation =
    val validationResult = constraintChecker.validateCandidate(request, candidate)
    val rescoredCandidate = candidate.copy(score = candidateScorer.scoreCandidate(request, validationResult))
    CandidateEvaluation(
      candidate = rescoredCandidate,
      validation = validationResult.copy(candidate = rescoredCandidate),
      isRecommended = false
    )

  private def chooseRecommendedCandidate(
      evaluations: Vector[CandidateEvaluation]
  ): Either[PlanningError, CandidateEvaluation] =
    evaluations
      .filter(_.isValid)
      .sortBy(_.candidate.score.value)(Ordering[BigDecimal].reverse)
      .headOption
      .toRight(PlanningError.ConstraintViolationFound("No valid planner candidates were available in stage 2"))

  private def buildDraftAggregate(
      request: PlannerRequest,
      recommended: CandidateEvaluation,
      alternatives: Vector[CandidateEvaluation]
  ): TripPlan =
    TripPlan(
      tripPlanId = TripPlanId(s"trip-plan-${request.requestId.value}"),
      ownerUserId = request.ownerUserId,
      status = TripPlanStatus.CandidateGenerated,
      summary = TripPlanSummary(
        title = s"${request.destination.city} smart plan",
        description =
          s"Recommended ${recommended.candidate.strategy} candidate with ${alternatives.size} alternatives.",
        estimatedBudget = recommended.candidate.estimatedBudget,
        warningCount = recommended.validation.warnings.size
      ),
      selectedCandidateId = Some(recommended.candidate.candidateId),
      days = recommended.candidate.days,
      createdAt = Instant.now()
    )

