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

final case class PreferenceLevel(value: String):
  override def toString: String = value

object PreferenceLevel:
  val Low: PreferenceLevel = PreferenceLevel("Low")
  val Medium: PreferenceLevel = PreferenceLevel("Medium")
  val High: PreferenceLevel = PreferenceLevel("High")

  def fromText(value: String): PreferenceLevel =
    value.trim.toLowerCase match
      case "low" => Low
      case "high" => High
      case _ => Medium

final case class TransportPreference(value: String):
  override def toString: String = value

object TransportPreference:
  val Any: TransportPreference = TransportPreference("Any")
  val WalkFirst: TransportPreference = TransportPreference("WalkFirst")
  val PublicTransitFirst: TransportPreference = TransportPreference("PublicTransitFirst")
  val TaxiFirst: TransportPreference = TransportPreference("TaxiFirst")
  val FlightAllowed: TransportPreference = TransportPreference("FlightAllowed")
  val TrainPreferred: TransportPreference = TransportPreference("TrainPreferred")

  def fromText(value: String): TransportPreference =
    value.trim.toLowerCase match
      case "walkfirst" | "walk_first" => WalkFirst
      case "publictransitfirst" | "public_transit_first" => PublicTransitFirst
      case "taxifirst" | "taxi_first" => TaxiFirst
      case "flightallowed" | "flight_allowed" => FlightAllowed
      case "trainpreferred" | "train_preferred" => TrainPreferred
      case _ => Any

final case class HotelPreference(value: String):
  override def toString: String = value

object HotelPreference:
  val Any: HotelPreference = HotelPreference("Any")
  val Budget: HotelPreference = HotelPreference("Budget")
  val Comfort: HotelPreference = HotelPreference("Comfort")
  val Premium: HotelPreference = HotelPreference("Premium")
  val CentralLocation: HotelPreference = HotelPreference("CentralLocation")
  val FamilyFriendly: HotelPreference = HotelPreference("FamilyFriendly")

  def fromText(value: String): HotelPreference =
    value.trim.toLowerCase match
      case "budget" => Budget
      case "comfort" => Comfort
      case "premium" => Premium
      case "centrallocation" | "central_location" => CentralLocation
      case "familyfriendly" | "family_friendly" => FamilyFriendly
      case _ => Any

final case class TripPace(value: String):
  override def toString: String = value

object TripPace:
  val Compact: TripPace = TripPace("Compact")
  val Balanced: TripPace = TripPace("Balanced")
  val Relaxed: TripPace = TripPace("Relaxed")

  def fromText(value: String): TripPace =
    value.trim.toLowerCase match
      case "compact" => Compact
      case "relaxed" => Relaxed
      case _ => Balanced

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

final case class TripPlanStatus(value: String):
  override def toString: String = value

object TripPlanStatus:
  val Draft: TripPlanStatus = TripPlanStatus("Draft")
  val CandidateGenerated: TripPlanStatus = TripPlanStatus("CandidateGenerated")
  val CandidateSelected: TripPlanStatus = TripPlanStatus("CandidateSelected")
  val Saved: TripPlanStatus = TripPlanStatus("Saved")
  val Archived: TripPlanStatus = TripPlanStatus("Archived")

  def fromText(value: String): TripPlanStatus =
    value.trim.toLowerCase match
      case "candidategenerated" | "candidate_generated" => CandidateGenerated
      case "candidateselected" | "candidate_selected" => CandidateSelected
      case "saved" => Saved
      case "archived" => Archived
      case _ => Draft

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

final case class PlannedTransportMode(value: String):
  override def toString: String = value

object PlannedTransportMode:
  val Walk: PlannedTransportMode = PlannedTransportMode("Walk")
  val Metro: PlannedTransportMode = PlannedTransportMode("Metro")
  val Taxi: PlannedTransportMode = PlannedTransportMode("Taxi")
  val Train: PlannedTransportMode = PlannedTransportMode("Train")
  val Flight: PlannedTransportMode = PlannedTransportMode("Flight")
  val Ferry: PlannedTransportMode = PlannedTransportMode("Ferry")
  val Bus: PlannedTransportMode = PlannedTransportMode("Bus")
  val Custom: PlannedTransportMode = PlannedTransportMode("Custom")

  def fromText(value: String): PlannedTransportMode =
    value.trim.toLowerCase match
      case "walk" => Walk
      case "metro" => Metro
      case "taxi" => Taxi
      case "train" => Train
      case "flight" => Flight
      case "ferry" => Ferry
      case "bus" => Bus
      case _ => Custom

final case class PlannerCandidate(
    candidateId: PlannerCandidateId,
    strategy: PlannerStrategy,
    score: PlannerScore,
    days: Vector[TripPlanDay],
    estimatedBudget: Option[Money],
    warnings: Vector[PlanningWarning]
)

final case class PlannerStrategy(value: String):
  override def toString: String = value

object PlannerStrategy:
  val Balanced: PlannerStrategy = PlannerStrategy("Balanced")
  val BudgetFirst: PlannerStrategy = PlannerStrategy("BudgetFirst")
  val ComfortFirst: PlannerStrategy = PlannerStrategy("ComfortFirst")
  val AttractionMaximizing: PlannerStrategy = PlannerStrategy("AttractionMaximizing")

  def fromText(value: String): PlannerStrategy =
    value.trim.toLowerCase match
      case "budgetfirst" | "budget_first" => BudgetFirst
      case "comfortfirst" | "comfort_first" => ComfortFirst
      case "attractionmaximizing" | "attraction_maximizing" => AttractionMaximizing
      case _ => Balanced

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

sealed trait PlanningError extends DomainError

object PlanningError:
  final case class PlannerRequestWasInvalid(reason: String) extends PlanningError:
    val message: String = s"Planner request was invalid: $reason"

  final case class CandidateGenerationFailed(strategy: PlannerStrategy, reason: String) extends PlanningError:
    val message: String = s"Planner candidate generation failed for '$strategy': $reason"

  final case class ConstraintViolationFound(reason: String) extends PlanningError:
    val message: String = s"Planner constraint violation: $reason"

  final case class TripPlanWasNotFound(tripPlanId: TripPlanId) extends PlanningError:
    val message: String = s"Trip plan '${tripPlanId.value}' was not found"

  final case class CandidateWasNotFound(candidateId: PlannerCandidateId) extends PlanningError:
    val message: String = s"Planner candidate '${candidateId.value}' was not found"

final case class MatchingStatus(value: String):
  override def toString: String = value

object MatchingStatus:
  val NotRequested: MatchingStatus = MatchingStatus("NotRequested")
  val NotMatched: MatchingStatus = MatchingStatus("NotMatched")
  val PartiallyMatched: MatchingStatus = MatchingStatus("PartiallyMatched")
  val Matched: MatchingStatus = MatchingStatus("Matched")
  val Confirmed: MatchingStatus = MatchingStatus("Confirmed")

  def fromText(value: String): MatchingStatus =
    value.trim.toLowerCase match
      case "notmatched" | "not_matched" => NotMatched
      case "partiallymatched" | "partially_matched" => PartiallyMatched
      case "matched" => Matched
      case "confirmed" => Confirmed
      case _ => NotRequested

// TODO(stage-2): add application-facing domain service contracts once orchestration begins.
