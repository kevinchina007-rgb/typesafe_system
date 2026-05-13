package com.typesafe.travel.api.dto

import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.TravelerDocumentType
import java.time.{DayOfWeek, LocalDate}
import scala.util.Try

final case class RegisterAttractionManagerRequestDto(
    email: String,
    displayName: String,
    password: String
)

final case class AttractionAdminLoginRequestDto(
    email: String,
    password: String
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
    currency: String,
    availableFromDate: String,
    availableToDate: String,
    totalQuantity: Int,
    validWeekdays: List[String]
)

final case class CreateTicketSessionRequestDto(
    managerId: String,
    attractionId: String,
    ticketTypeId: String,
    sessionName: String,
    useDate: String,
    startsAt: String,
    endsAt: String,
    capacity: Int
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
    attractionId: String,
    ticketTypeId: String,
    sessionId: Option[String],
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
    availableFromDate: String,
    availableToDate: String,
    totalQuantity: Int,
    validWeekdays: List[String],
    availableQuantityForRequestedDate: Option[Int],
    isAvailableForRequestedDate: Boolean,
    status: String,
    sessions: List[AttractionTicketSessionResponseDto],
    rules: List[AttractionTicketTypeRuleResponseDto]
)

final case class AttractionTicketSessionResponseDto(
    sessionId: String,
    sessionName: String,
    useDate: String,
    startsAt: String,
    endsAt: String,
    capacity: Int,
    availableQuantity: Option[Int],
    status: String
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

def attractionResponseDto(
    attraction: Attraction,
    requestedUseDate: Option[LocalDate] = None,
    remainingQuantityByTicketTypeId: Map[TicketTypeId, Int] = Map.empty
): AttractionResponseDto =
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
        availableFromDate = ticketType.availableFromDate.toString,
        availableToDate = ticketType.availableToDate.toString,
        totalQuantity = ticketType.totalQuantity,
        validWeekdays = ticketType.validWeekdays.toList.sortBy(_.getValue).map(_.toString),
        availableQuantityForRequestedDate = requestedUseDate.map { _ =>
          remainingQuantityByTicketTypeId.getOrElse(ticketType.ticketTypeId, ticketType.totalQuantity)
        },
        isAvailableForRequestedDate = requestedUseDate.forall(ticketTypeSupportsUseDate(ticketType, _)),
        status = ticketType.ticketTypeStatus.toString,
        sessions = ticketType.sessions.map(session =>
          AttractionTicketSessionResponseDto(
            sessionId = session.sessionId.value,
            sessionName = session.sessionName,
            useDate = session.useDate.toString,
            startsAt = session.startsAt.toString,
            endsAt = session.endsAt.toString,
            capacity = session.capacity,
            availableQuantity = None,
            status = session.status.toString
          )
        ).toList,
        rules = ticketType.eligibilityRules.map(rule =>
          AttractionTicketTypeRuleResponseDto(
            ruleId = rule.ruleId.value,
            ruleType = rule.ruleType.toString,
            summary = ticketEligibilityRuleHumanReadableSummary(rule)
          )
        ).toList
      )
    ).toList
  )

object AttractionDtoMappers:
  def toDayOfWeek(weekdayValue: String): Either[Throwable, DayOfWeek] =
    Try(DayOfWeek.valueOf(weekdayValue.trim.toUpperCase)).toEither

  def toValidWeekdays(weekdayValues: List[String]): Either[Throwable, Set[DayOfWeek]] =
    weekdayValues.traverse(toDayOfWeek).map(_.toSet)

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
