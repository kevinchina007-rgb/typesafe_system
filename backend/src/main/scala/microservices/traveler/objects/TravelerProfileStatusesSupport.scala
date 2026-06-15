// 本文件仅保存 traveler 后端内部状态解析 helper，不对应前端镜像文件。
package com.typesafe.travel.traveler.domain

object TravelerProfileStatusesSupport:
  def parseTravelerProfileStatus(value: String): TravelerProfileStatus =
    value.trim.toLowerCase match
      case "verified" => TravelerProfileStatus.Verified
      case "archived" => TravelerProfileStatus.Archived
      case _ => TravelerProfileStatus.Draft

  def parseTravelerDocumentType(value: String): TravelerDocumentType =
    value.trim.toLowerCase match
      case "passport" => TravelerDocumentType.Passport
      case "identity-card" | "identity_card" | "nationalidentitycard" | "national_identity_card" => TravelerDocumentType.NationalIdentityCard
      case "residence-permit" | "residence_permit" | "residencepermit" => TravelerDocumentType.ResidencePermit
      case "other" | "other-government-document" | "other_government_document" | "othergovernmentdocument" => TravelerDocumentType.OtherGovernmentDocument
      case _ => TravelerDocumentType.Passport

  def parseTravelerType(value: String): TravelerType =
    value.trim.toLowerCase match
      case "childtraveler" | "child_traveler" => TravelerType.ChildTraveler
      case "infanttraveler" | "infant_traveler" => TravelerType.InfantTraveler
      case _ => TravelerType.AdultTraveler

  def parseSeatPreference(value: String): SeatPreference =
    value.trim.toLowerCase match
      case "window" => SeatPreference.Window
      case "aisle" => SeatPreference.Aisle
      case "middle" => SeatPreference.Middle
      case _ => SeatPreference.NoPreference

  def parseMealPreference(value: String): MealPreference =
    value.trim.toLowerCase match
      case "vegetarian" => MealPreference.Vegetarian
      case "vegan" => MealPreference.Vegan
      case "halal" => MealPreference.Halal
      case "kosher" => MealPreference.Kosher
      case "childmeal" | "child_meal" => MealPreference.ChildMeal
      case "nopreference" | "no_preference" => MealPreference.NoPreference
      case _ => MealPreference.Standard
