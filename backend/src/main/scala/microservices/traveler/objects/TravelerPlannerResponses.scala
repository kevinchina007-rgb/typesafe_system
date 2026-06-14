// Traveler planner response models.
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TravelerPlannerResponse(
    travelerId: String,
    ownerUserId: String,
    fullName: String,
    documentType: String,
    documentNumber: String,
    phone: String,
    birthDate: String,
    travelerType: String,
    status: String,
    isHidden: Boolean,
    isDefault: Boolean,
    basicInfo: TravelerBasicInfo,
    documentInfo: TravelerDocumentInfo,
    contactInfo: TravelerContactInfo,
    preferenceInfo: TravelerPreferenceInfo,
    specialRequirementInfo: TravelerSpecialRequirementInfo,
    serviceSummary: TravelerServiceSummary
)

object TravelerPlannerResponse:
  given sourceEncoder: Encoder[TravelerPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TravelerPlannerResponse] = deriveDecoder

final case class TravelerListPlannerResponse(
    travelers: List[TravelerPlannerResponse]
)

object TravelerListPlannerResponse:
  given sourceEncoder: Encoder[TravelerListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TravelerListPlannerResponse] = deriveDecoder

final case class TravelerDeletedPlannerResponse(
    deleted: Boolean,
    hidden: Boolean
)

object TravelerDeletedPlannerResponse:
  given sourceEncoder: Encoder[TravelerDeletedPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TravelerDeletedPlannerResponse] = deriveDecoder
