package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.AttractionAdminSession
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerDocumentType

final case class RegisterAttractionManagerRequestDto(
    email: String,
    displayName: String
)

final case class AttractionAdminLoginRequestDto(
    email: String
)

final case class CreateAttractionRequestDto(
    managerId: String,
    attractionName: String,
    city: String,
    location: String,
    description: String
)

final case class CreateTicketTypeRequestDto(
    managerId: String,
    attractionId: String,
    ticketTypeName: String,
    description: String,
    unitPrice: String,
    currency: String
)

final case class CreateTicketEligibilityRuleRequestDto(
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

final case class BookAttractionItemRequestDto(
    buyerUserId: String,
    orderId: String,
    attractionId: String,
    ticketTypeId: String,
    travelerIds: List[String],
    useDate: String
)

final case class AttractionTicketTypeRuleResponseDto(
    ruleId: String,
    ruleType: String,
    summary: String
)

final case class AttractionTicketTypeResponseDto(
    ticketTypeId: String,
    ticketTypeName: String,
    description: String,
    priceAmount: String,
    priceCurrency: String,
    status: String,
    rules: List[AttractionTicketTypeRuleResponseDto]
)

final case class AttractionResponseDto(
    attractionId: String,
    attractionName: String,
    city: String,
    location: String,
    description: String,
    status: String,
    ticketTypes: List[AttractionTicketTypeResponseDto]
)

final case class AttractionListResponseDto(
    attractions: List[AttractionResponseDto]
)

final case class AttractionAdminSessionResponseDto(
    managerId: String,
    email: String,
    displayName: String,
    status: String,
    managedAttractions: List[AttractionResponseDto]
)

object AttractionResponseDto:
  def fromDomain(attraction: Attraction): AttractionResponseDto =
    AttractionResponseDto(
      attractionId = attraction.attractionId.value,
      attractionName = attraction.attractionName,
      city = attraction.city,
      location = attraction.location,
      description = attraction.description,
      status = attraction.attractionStatus.toString,
      ticketTypes = attraction.ticketTypes.map(ticketType =>
        AttractionTicketTypeResponseDto(
          ticketTypeId = ticketType.ticketTypeId.value,
          ticketTypeName = ticketType.ticketTypeName,
          description = ticketType.description,
          priceAmount = ticketType.unitPrice.amount.toString,
          priceCurrency = ticketType.unitPrice.currency.toString,
          status = ticketType.ticketTypeStatus.toString,
          rules = ticketType.eligibilityRules.map(rule =>
            AttractionTicketTypeRuleResponseDto(
              ruleId = rule.ruleId.value,
              ruleType = rule.ruleType.toString,
              summary = rule.humanReadableSummary
            )
          ).toList
        )
      ).toList
    )

object AttractionAdminSessionResponseDto:
  def fromApplication(attractionAdminSession: AttractionAdminSession): AttractionAdminSessionResponseDto =
    AttractionAdminSessionResponseDto(
      managerId = attractionAdminSession.attractionManager.managerId.value,
      email = attractionAdminSession.attractionManager.primaryEmailAddress.value,
      displayName = attractionAdminSession.attractionManager.displayName.value,
      status = attractionAdminSession.attractionManager.managerStatus.toString,
      managedAttractions = attractionAdminSession.managedAttractions.map(AttractionResponseDto.fromDomain)
    )

object AttractionDtoMappers:
  def toRuleType(ruleTypeValue: String): TicketEligibilityRuleType =
    ruleTypeValue.trim.toLowerCase match
      case "age_less_than" | "agelessthan"              => TicketEligibilityRuleType.AgeLessThan
      case "age_between" | "agebetween"                 => TicketEligibilityRuleType.AgeBetween
      case "age_at_least" | "ageatleast"                => TicketEligibilityRuleType.AgeAtLeast
      case "document_type_equals" | "documenttypeequals" => TicketEligibilityRuleType.DocumentTypeEquals
      case _                                             => TicketEligibilityRuleType.DocumentNumberPrefix

  def toDocumentType(documentTypeValue: String): TravelerDocumentType =
    TravelerDtoMappers.toTravelerDocumentType(documentTypeValue)

  def toRuleConfig(request: CreateTicketEligibilityRuleRequestDto): Either[Throwable, TicketEligibilityRuleConfig] =
    toRuleType(request.ruleType) match
      case TicketEligibilityRuleType.AgeLessThan =>
        request.ageValue.toRight(new IllegalArgumentException("ageValue is required")).map(TicketEligibilityRuleConfig.AgeLessThan.apply)
      case TicketEligibilityRuleType.AgeBetween =>
        (request.minAge, request.maxAge) match
          case (Some(minAge), Some(maxAge)) => Right(TicketEligibilityRuleConfig.AgeBetween(minAge, maxAge))
          case _                            => Left(new IllegalArgumentException("minAge and maxAge are required"))
      case TicketEligibilityRuleType.AgeAtLeast =>
        request.ageValue.toRight(new IllegalArgumentException("ageValue is required")).map(TicketEligibilityRuleConfig.AgeAtLeast.apply)
      case TicketEligibilityRuleType.DocumentTypeEquals =>
        request.documentType.toRight(new IllegalArgumentException("documentType is required")).map(value =>
          TicketEligibilityRuleConfig.DocumentTypeEquals(toDocumentType(value))
        )
      case TicketEligibilityRuleType.DocumentNumberPrefix =>
        request.documentNumberPrefix
          .map(_.trim)
          .filter(_.nonEmpty)
          .toRight(new IllegalArgumentException("documentNumberPrefix is required"))
          .map(TicketEligibilityRuleConfig.DocumentNumberPrefix.apply)
