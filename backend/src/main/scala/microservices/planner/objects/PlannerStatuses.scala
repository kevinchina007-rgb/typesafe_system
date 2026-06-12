// PlannerStatuses 定义planner模块的状态模型。

package com.typesafe.travel.planner.domain

final case class PreferenceLevel(value: String):
  override def toString: String = value

object PreferenceLevel:
  val Low: PreferenceLevel = PreferenceLevel("Low")
  val Medium: PreferenceLevel = PreferenceLevel("Medium")
  val High: PreferenceLevel = PreferenceLevel("High")
  export PlannerStatusSupport.parsePreferenceLevel as fromText

final case class TransportPreference(value: String):
  override def toString: String = value

object TransportPreference:
  val Any: TransportPreference = TransportPreference("Any")
  val WalkFirst: TransportPreference = TransportPreference("WalkFirst")
  val PublicTransitFirst: TransportPreference = TransportPreference("PublicTransitFirst")
  val TaxiFirst: TransportPreference = TransportPreference("TaxiFirst")
  val FlightAllowed: TransportPreference = TransportPreference("FlightAllowed")
  val TrainPreferred: TransportPreference = TransportPreference("TrainPreferred")

  export PlannerStatusSupport.parseTransportPreference as fromText

final case class HotelPreference(value: String):
  override def toString: String = value

object HotelPreference:
  val Any: HotelPreference = HotelPreference("Any")
  val Budget: HotelPreference = HotelPreference("Budget")
  val Comfort: HotelPreference = HotelPreference("Comfort")
  val Premium: HotelPreference = HotelPreference("Premium")
  val CentralLocation: HotelPreference = HotelPreference("CentralLocation")
  val FamilyFriendly: HotelPreference = HotelPreference("FamilyFriendly")

  export PlannerStatusSupport.parseHotelPreference as fromText

final case class TripPace(value: String):
  override def toString: String = value

object TripPace:
  val Compact: TripPace = TripPace("Compact")
  val Balanced: TripPace = TripPace("Balanced")
  val Relaxed: TripPace = TripPace("Relaxed")

  export PlannerStatusSupport.parseTripPace as fromText

final case class TripPlanStatus(value: String):
  override def toString: String = value

object TripPlanStatus:
  val Draft: TripPlanStatus = TripPlanStatus("Draft")
  val CandidateGenerated: TripPlanStatus = TripPlanStatus("CandidateGenerated")
  val CandidateSelected: TripPlanStatus = TripPlanStatus("CandidateSelected")
  val Saved: TripPlanStatus = TripPlanStatus("Saved")
  val Archived: TripPlanStatus = TripPlanStatus("Archived")

  export PlannerStatusSupport.parseTripPlanStatus as fromText

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

  export PlannerStatusSupport.parsePlannedTransportMode as fromText

final case class PlannerStrategy(value: String):
  override def toString: String = value

object PlannerStrategy:
  val Balanced: PlannerStrategy = PlannerStrategy("Balanced")
  val BudgetFirst: PlannerStrategy = PlannerStrategy("BudgetFirst")
  val ComfortFirst: PlannerStrategy = PlannerStrategy("ComfortFirst")
  val AttractionMaximizing: PlannerStrategy = PlannerStrategy("AttractionMaximizing")

  export PlannerStatusSupport.parsePlannerStrategy as fromText

final case class MatchingStatus(value: String):
  override def toString: String = value

object MatchingStatus:
  val NotRequested: MatchingStatus = MatchingStatus("NotRequested")
  val NotMatched: MatchingStatus = MatchingStatus("NotMatched")
  val PartiallyMatched: MatchingStatus = MatchingStatus("PartiallyMatched")
  val Matched: MatchingStatus = MatchingStatus("Matched")
  val Confirmed: MatchingStatus = MatchingStatus("Confirmed")

  export PlannerStatusSupport.parseMatchingStatus as fromText
