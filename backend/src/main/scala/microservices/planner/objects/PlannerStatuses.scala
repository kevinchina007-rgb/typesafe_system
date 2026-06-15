// PlannerStatuses 定义planner模块的状态模型。

// 这个文件只保存 planner 引擎内部会用到的状态值对象与文本解析逻辑。
// 这些状态用于描述规划偏好、行程计划阶段、匹配状态等后端流程，不需要也不会在前端单独镜像成同名模块。
package com.typesafe.travel.planner.domain

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
