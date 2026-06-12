// AirlineManagerPlannerModels 负责operations相关实现。

package com.typesafe.travel.operations.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RegisterAirlineManagerPlannerRequest(email: String, displayName: String, airlineName: String, airlineCode: String, password: String)
object RegisterAirlineManagerPlannerRequest:
  given sourceEncoder: Encoder[RegisterAirlineManagerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[RegisterAirlineManagerPlannerRequest] = deriveDecoder

final case class ManagerFlightsPlannerRequest(
    managerId: String,
    managerType: String,
    departureAirports: Option[List[String]],
    arrivalAirports: Option[List[String]],
    departureDate: Option[String],
    timeRange: Option[String],
    sortDirection: Option[String]
)
object ManagerFlightsPlannerRequest:
  given sourceEncoder: Encoder[ManagerFlightsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightsPlannerRequest] = deriveDecoder

final case class ManagerFlightOrdersPlannerRequest(managerId: String, flightId: String)
object ManagerFlightOrdersPlannerRequest:
  given sourceEncoder: Encoder[ManagerFlightOrdersPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrdersPlannerRequest] = deriveDecoder

final case class UpdateAirlineManagerProfilePlannerRequest(
    managerId: String,
    displayName: String,
    airlineName: String,
    airlineCode: String,
    logoAssetPath: Option[String]
)
object UpdateAirlineManagerProfilePlannerRequest:
  given sourceEncoder: Encoder[UpdateAirlineManagerProfilePlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[UpdateAirlineManagerProfilePlannerRequest] = deriveDecoder

final case class ManagerCabinPricingPlannerInput(
    seatCount: Int,
    originalPrice: String,
    discounted: Boolean,
    discountRate: String
)
object ManagerCabinPricingPlannerInput:
  given sourceEncoder: Encoder[ManagerCabinPricingPlannerInput] = deriveEncoder
  given sourceDecoder: Decoder[ManagerCabinPricingPlannerInput] = deriveDecoder

final case class CreateManagerFlightPlannerRequest(
    managerId: String,
    flightNumber: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    economyCabin: ManagerCabinPricingPlannerInput,
    premiumEconomyCabin: ManagerCabinPricingPlannerInput,
    businessCabin: ManagerCabinPricingPlannerInput,
    firstCabin: ManagerCabinPricingPlannerInput,
    currency: String
)
object CreateManagerFlightPlannerRequest:
  given sourceEncoder: Encoder[CreateManagerFlightPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateManagerFlightPlannerRequest] = deriveDecoder

final case class ToggleManagerFlightStatusPlannerRequest(managerId: String, flightId: String)
object ToggleManagerFlightStatusPlannerRequest:
  given sourceEncoder: Encoder[ToggleManagerFlightStatusPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ToggleManagerFlightStatusPlannerRequest] = deriveDecoder

final case class ManagerCabinInventoryPlannerResponse(
    inventoryId: String,
    cabinClass: String,
    availableSeats: Int,
    unitPrice: String,
    currency: String,
    status: String,
    isBookable: Boolean
)
object ManagerCabinInventoryPlannerResponse:
  given sourceEncoder: Encoder[ManagerCabinInventoryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerCabinInventoryPlannerResponse] = deriveDecoder

final case class ManagerFlightPlannerResponse(
    flightId: String,
    airlineId: String,
    airlineName: String,
    airlineCode: String,
    flightNumber: String,
    departureAirport: String,
    arrivalAirport: String,
    departureTime: String,
    arrivalTime: String,
    status: String,
    basePrice: String,
    currency: String,
    createdAt: String,
    cabinInventories: List[ManagerCabinInventoryPlannerResponse]
)
object ManagerFlightPlannerResponse:
  given sourceEncoder: Encoder[ManagerFlightPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightPlannerResponse] = deriveDecoder

final case class ManagerFlightListPlannerResponse(flights: List[ManagerFlightPlannerResponse])
object ManagerFlightListPlannerResponse:
  given sourceEncoder: Encoder[ManagerFlightListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightListPlannerResponse] = deriveDecoder

final case class ManagerTravelerBasicInfo(
    fullName: String,
    gender: String,
    birthDate: String,
    nationality: String
)
object ManagerTravelerBasicInfo:
  given sourceEncoder: Encoder[ManagerTravelerBasicInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerBasicInfo] = deriveDecoder

final case class ManagerTravelerDocumentInfo(
    documentType: String,
    documentNumber: String,
    documentExpiryDate: Option[String]
)
object ManagerTravelerDocumentInfo:
  given sourceEncoder: Encoder[ManagerTravelerDocumentInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerDocumentInfo] = deriveDecoder

final case class ManagerTravelerContactInfo(
    phone: String,
    email: Option[String]
)
object ManagerTravelerContactInfo:
  given sourceEncoder: Encoder[ManagerTravelerContactInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerContactInfo] = deriveDecoder

final case class ManagerTravelerPreferenceInfo(
    seatPreference: String,
    mealPreference: String,
    quietSeatPreferred: Boolean
)
object ManagerTravelerPreferenceInfo:
  given sourceEncoder: Encoder[ManagerTravelerPreferenceInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerPreferenceInfo] = deriveDecoder

final case class ManagerTravelerSpecialRequirementInfo(
    assistanceType: String,
    requirementNote: Option[String],
    hasLargeLuggage: Boolean,
    luggageNote: Option[String]
)
object ManagerTravelerSpecialRequirementInfo:
  given sourceEncoder: Encoder[ManagerTravelerSpecialRequirementInfo] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerSpecialRequirementInfo] = deriveDecoder

final case class ManagerTravelerServiceSummary(
    age: Option[Int],
    documentLabel: String,
    contactLabel: String,
    preferenceLabel: String,
    requirementLabel: String,
    warningLevel: String
)
object ManagerTravelerServiceSummary:
  given sourceEncoder: Encoder[ManagerTravelerServiceSummary] = deriveEncoder
  given sourceDecoder: Decoder[ManagerTravelerServiceSummary] = deriveDecoder

final case class ManagerFlightOrderTravelerPlannerResponse(
    travelerId: String,
    fullName: String,
    documentNumber: String,
    basicInfo: ManagerTravelerBasicInfo,
    documentInfo: ManagerTravelerDocumentInfo,
    contactInfo: ManagerTravelerContactInfo,
    preferenceInfo: ManagerTravelerPreferenceInfo,
    specialRequirementInfo: ManagerTravelerSpecialRequirementInfo,
    serviceSummary: ManagerTravelerServiceSummary
)
object ManagerFlightOrderTravelerPlannerResponse:
  given sourceEncoder: Encoder[ManagerFlightOrderTravelerPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrderTravelerPlannerResponse] = deriveDecoder

final case class ManagerFlightOrderPlannerResponse(
    orderId: String,
    orderItemId: String,
    buyerUserId: String,
    buyerNickname: String,
    cabinClass: String,
    orderStatus: String,
    orderCreatedAt: String,
    travelerIds: List[String],
    travelers: List[ManagerFlightOrderTravelerPlannerResponse]
)
object ManagerFlightOrderPlannerResponse:
  given sourceEncoder: Encoder[ManagerFlightOrderPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrderPlannerResponse] = deriveDecoder

final case class ManagerFlightOrderListPlannerResponse(orders: List[ManagerFlightOrderPlannerResponse])
object ManagerFlightOrderListPlannerResponse:
  given sourceEncoder: Encoder[ManagerFlightOrderListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[ManagerFlightOrderListPlannerResponse] = deriveDecoder
