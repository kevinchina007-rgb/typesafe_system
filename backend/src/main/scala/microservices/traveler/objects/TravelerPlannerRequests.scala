// Traveler planner request models.
package com.typesafe.travel.traveler.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

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
