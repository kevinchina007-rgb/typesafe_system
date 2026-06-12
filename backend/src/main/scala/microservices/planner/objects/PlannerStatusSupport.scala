// PlannerStatusSupport 定义planner模块的辅助定义。

package com.typesafe.travel.planner.domain

object PlannerStatusSupport:
  def parsePreferenceLevel(value: String): PreferenceLevel =
    value.trim.toLowerCase match
      case "low" => PreferenceLevel.Low
      case "high" => PreferenceLevel.High
      case _ => PreferenceLevel.Medium

  def parseTransportPreference(value: String): TransportPreference =
    value.trim.toLowerCase match
      case "walkfirst" | "walk_first" => TransportPreference.WalkFirst
      case "publictransitfirst" | "public_transit_first" => TransportPreference.PublicTransitFirst
      case "taxifirst" | "taxi_first" => TransportPreference.TaxiFirst
      case "flightallowed" | "flight_allowed" => TransportPreference.FlightAllowed
      case "trainpreferred" | "train_preferred" => TransportPreference.TrainPreferred
      case _ => TransportPreference.Any

  def parseHotelPreference(value: String): HotelPreference =
    value.trim.toLowerCase match
      case "budget" => HotelPreference.Budget
      case "comfort" => HotelPreference.Comfort
      case "premium" => HotelPreference.Premium
      case "centrallocation" | "central_location" => HotelPreference.CentralLocation
      case "familyfriendly" | "family_friendly" => HotelPreference.FamilyFriendly
      case _ => HotelPreference.Any

  def parseTripPace(value: String): TripPace =
    value.trim.toLowerCase match
      case "compact" => TripPace.Compact
      case "relaxed" => TripPace.Relaxed
      case _ => TripPace.Balanced

  def parseTripPlanStatus(value: String): TripPlanStatus =
    value.trim.toLowerCase match
      case "candidategenerated" | "candidate_generated" => TripPlanStatus.CandidateGenerated
      case "candidateselected" | "candidate_selected" => TripPlanStatus.CandidateSelected
      case "saved" => TripPlanStatus.Saved
      case "archived" => TripPlanStatus.Archived
      case _ => TripPlanStatus.Draft

  def parsePlannedTransportMode(value: String): PlannedTransportMode =
    value.trim.toLowerCase match
      case "walk" => PlannedTransportMode.Walk
      case "metro" => PlannedTransportMode.Metro
      case "taxi" => PlannedTransportMode.Taxi
      case "train" => PlannedTransportMode.Train
      case "flight" => PlannedTransportMode.Flight
      case "ferry" => PlannedTransportMode.Ferry
      case "bus" => PlannedTransportMode.Bus
      case _ => PlannedTransportMode.Custom

  def parsePlannerStrategy(value: String): PlannerStrategy =
    value.trim.toLowerCase match
      case "budgetfirst" | "budget_first" => PlannerStrategy.BudgetFirst
      case "comfortfirst" | "comfort_first" => PlannerStrategy.ComfortFirst
      case "attractionmaximizing" | "attraction_maximizing" => PlannerStrategy.AttractionMaximizing
      case _ => PlannerStrategy.Balanced

  def parseMatchingStatus(value: String): MatchingStatus =
    value.trim.toLowerCase match
      case "notmatched" | "not_matched" => MatchingStatus.NotMatched
      case "partiallymatched" | "partially_matched" => MatchingStatus.PartiallyMatched
      case "matched" => MatchingStatus.Matched
      case "confirmed" => MatchingStatus.Confirmed
      case _ => MatchingStatus.NotRequested
