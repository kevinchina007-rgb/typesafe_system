// AttractionPlannerModels 定义景点模块的请求和响应模型。

package com.typesafe.travel.attraction.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class AttractionSuggestionRequest(q: String)
object AttractionSuggestionRequest:
  given sourceEncoder: Encoder[AttractionSuggestionRequest] = deriveEncoder
  given sourceDecoder: Decoder[AttractionSuggestionRequest] = deriveDecoder

final case class ListAttractionsPlannerRequest(city: Option[String], keyword: Option[String], useDate: Option[String])
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

final case class CreateAttractionPlannerRequest(managerId: String, attractionName: String, city: String, location: String, description: String, imageUrl: Option[String])
object CreateAttractionPlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionPlannerRequest] = deriveDecoder

final case class UploadAttractionImagePlannerRequest(
    originalFileName: String,
    mimeType: String,
    fileContentBase64: String
)
object UploadAttractionImagePlannerRequest:
  given sourceEncoder: Encoder[UploadAttractionImagePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UploadAttractionImagePlannerRequest] = deriveDecoder

final case class UploadAttractionImagePlannerResponse(
    assetId: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)
object UploadAttractionImagePlannerResponse:
  given sourceEncoder: Encoder[UploadAttractionImagePlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[UploadAttractionImagePlannerResponse] = deriveDecoder

final case class CreateAttractionTicketTypePlannerRequest(
    managerId: String,
    attractionId: String,
    ticketTypeName: String,
    description: String,
    unitPrice: String,
    currency: String,
    availableFromDate: String,
    availableToDate: String,
    totalQuantity: Int,
    validWeekdays: List[String]
)
object CreateAttractionTicketTypePlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionTicketTypePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionTicketTypePlannerRequest] = deriveDecoder

final case class CreateAttractionTicketSessionPlannerRequest(
    managerId: String,
    attractionId: String,
    ticketTypeId: String,
    sessionName: String,
    useDate: String,
    startsAt: String,
    endsAt: String,
    capacity: Int
)
object CreateAttractionTicketSessionPlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionTicketSessionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionTicketSessionPlannerRequest] = deriveDecoder

final case class CreateAttractionTicketRulePlannerRequest(
    managerId: String,
    attractionId: String,
    ticketTypeId: String,
    ruleType: String,
    ageValue: Option[Int],
    minAge: Option[Int],
    maxAge: Option[Int],
    documentType: Option[String],
    documentNumberPrefix: Option[String]
)
object CreateAttractionTicketRulePlannerRequest:
  given sourceEncoder: Encoder[CreateAttractionTicketRulePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateAttractionTicketRulePlannerRequest] = deriveDecoder

final case class BookAttractionItemPlannerRequest(
    userId: String,
    orderId: String,
    attractionId: String,
    ticketTypeId: String,
    sessionId: Option[String],
    travelerIds: List[String],
    useDate: String
)
object BookAttractionItemPlannerRequest:
  given sourceEncoder: Encoder[BookAttractionItemPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BookAttractionItemPlannerRequest] = deriveDecoder

final case class BookAttractionItemPlannerResponse(orderId: String, orderItemId: String)
object BookAttractionItemPlannerResponse:
  given sourceEncoder: Encoder[BookAttractionItemPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[BookAttractionItemPlannerResponse] = deriveDecoder

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
