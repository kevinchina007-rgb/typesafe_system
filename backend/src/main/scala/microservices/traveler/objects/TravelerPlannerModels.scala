package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
    isDefaultTraveler: Boolean
)

object TravelerProfileInput:
  given sourceEncoder: Encoder[TravelerProfileInput] = deriveEncoder
  given sourceDecoder: Decoder[TravelerProfileInput] = deriveDecoder

final case class CreateTravelerPlannerRequest(
    actingUserId: String,
    ownerUserId: String,
    traveler: TravelerProfileInput
)

object CreateTravelerPlannerRequest:
  given sourceEncoder: Encoder[CreateTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTravelerPlannerRequest] = deriveDecoder

final case class UpdateTravelerPlannerRequest(
    actingUserId: String,
    ownerUserId: String,
    travelerId: String,
    traveler: TravelerProfileInput
)

object UpdateTravelerPlannerRequest:
  given sourceEncoder: Encoder[UpdateTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateTravelerPlannerRequest] = deriveDecoder

final case class ListTravelersPlannerRequest(
    actingUserId: String,
    ownerUserId: String
)

object ListTravelersPlannerRequest:
  given sourceEncoder: Encoder[ListTravelersPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListTravelersPlannerRequest] = deriveDecoder

final case class DeleteTravelerPlannerRequest(
    actingUserId: String,
    ownerUserId: String,
    travelerId: String
)

object DeleteTravelerPlannerRequest:
  given sourceEncoder: Encoder[DeleteTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[DeleteTravelerPlannerRequest] = deriveDecoder

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
    isDefault: Boolean
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
    deleted: Boolean
)

object TravelerDeletedPlannerResponse:
  given sourceEncoder: Encoder[TravelerDeletedPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TravelerDeletedPlannerResponse] = deriveDecoder
