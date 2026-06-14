// Traveler planner data details and shared input models.
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerBasicInfo(
    fullName: String,
    gender: String,
    birthDate: String,
    nationality: String
)

object TravelerBasicInfo:
  given sourceEncoder: Encoder[TravelerBasicInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerBasicInfo] = deriveDecoder

final case class TravelerDocumentInfo(
    documentType: String,
    documentNumber: String,
    documentExpiryDate: Option[String]
)

object TravelerDocumentInfo:
  given sourceEncoder: Encoder[TravelerDocumentInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerDocumentInfo] = deriveDecoder

final case class TravelerContactInfo(
    phone: String,
    email: Option[String]
)

object TravelerContactInfo:
  given sourceEncoder: Encoder[TravelerContactInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerContactInfo] = deriveDecoder

final case class TravelerPreferenceInfo(
    seatPreference: String,
    mealPreference: String,
    quietSeatPreferred: Boolean
)

object TravelerPreferenceInfo:
  given sourceEncoder: Encoder[TravelerPreferenceInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerPreferenceInfo] = deriveDecoder

final case class TravelerSpecialRequirementInfo(
    assistanceType: String,
    requirementNote: Option[String],
    hasLargeLuggage: Boolean,
    luggageNote: Option[String]
)

object TravelerSpecialRequirementInfo:
  given sourceEncoder: Encoder[TravelerSpecialRequirementInfo] = deriveEncoder
  given sourceDecoder: Decoder[TravelerSpecialRequirementInfo] = deriveDecoder

final case class TravelerServiceSummary(
    age: Option[Int],
    documentLabel: String,
    contactLabel: String,
    preferenceLabel: String,
    requirementLabel: String,
    warningLevel: String
)

object TravelerServiceSummary:
  given sourceEncoder: Encoder[TravelerServiceSummary] = deriveEncoder
  given sourceDecoder: Decoder[TravelerServiceSummary] = deriveDecoder

final case class TravelerProfileInput(
    fullName: String,
    documentType: String,
    documentNumber: String,
    phone: String,
    birthDate: String,
    seatPreference: String,
    mealPreference: String,
    accessibilityRequestNotes: Option[String],
    emergencyContactName: Option[String],
    emergencyContactPhoneNumber: Option[String],
    isDefaultTraveler: Boolean,
    basicInfo: Option[TravelerBasicInfo] = None,
    documentInfo: Option[TravelerDocumentInfo] = None,
    contactInfo: Option[TravelerContactInfo] = None,
    preferenceInfo: Option[TravelerPreferenceInfo] = None,
    specialRequirementInfo: Option[TravelerSpecialRequirementInfo] = None
)

object TravelerProfileInput:
  given sourceEncoder: Encoder[TravelerProfileInput] = deriveEncoder
  given sourceDecoder: Decoder[TravelerProfileInput] = deriveDecoder
