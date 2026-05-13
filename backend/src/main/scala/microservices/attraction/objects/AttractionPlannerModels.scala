package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionSuggestionRequest(q: String)
object AttractionSuggestionRequest:
  given sourceEncoder: Encoder[AttractionSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[AttractionSuggestionRequest] = deriveDecoder

final case class ListAttractionsPlannerRequest(city: Option[String], useDate: Option[String])
object ListAttractionsPlannerRequest:
  given sourceEncoder: Encoder[ListAttractionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListAttractionsPlannerRequest] = deriveDecoder

final case class GetAttractionDetailsPlannerRequest(attractionId: String, useDate: Option[String])
object GetAttractionDetailsPlannerRequest:
  given sourceEncoder: Encoder[GetAttractionDetailsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[GetAttractionDetailsPlannerRequest] = deriveDecoder

final case class ListManagedAttractionsPlannerRequest(managerId: String)
object ListManagedAttractionsPlannerRequest:
  given sourceEncoder: Encoder[ListManagedAttractionsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListManagedAttractionsPlannerRequest] = deriveDecoder

final case class CreateAttractionPlannerRequest(managerId: String, attractionName: String, city: String, location: String, description: String)
object CreateAttractionPlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionPlannerRequest] = deriveDecoder

final case class AttractionListPlannerResponse(attractions: List[Attraction])
object AttractionListPlannerResponse:
  import AttractionSourceJsonCodecs.given
  given sourceEncoder: Encoder[AttractionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionListPlannerResponse] = deriveDecoder

final case class AttractionSuggestionPlannerResponse(resourceType: String, value: String, title: String, subtitle: String)
object AttractionSuggestionPlannerResponse:
  given sourceEncoder: Encoder[AttractionSuggestionPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionSuggestionPlannerResponse] = deriveDecoder

final case class AttractionSuggestionListPlannerResponse(suggestions: List[AttractionSuggestionPlannerResponse])
object AttractionSuggestionListPlannerResponse:
  given sourceEncoder: Encoder[AttractionSuggestionListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[AttractionSuggestionListPlannerResponse] = deriveDecoder
