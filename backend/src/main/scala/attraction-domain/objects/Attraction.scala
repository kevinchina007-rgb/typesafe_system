package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.parser.decode

import java.time.{DayOfWeek, Instant, LocalDate, Period}

enum AttractionStatus:
  case Draft, Published, Closed

enum TicketTypeStatus:
  case Active, Inactive

enum TicketEligibilityRuleType:
  case AgeLessThan, AgeBetween, AgeAtLeast, DocumentTypeEquals, DocumentNumberPrefix

sealed trait TicketEligibilityRuleConfig
object TicketEligibilityRuleConfig:
  final case class AgeLessThan(maxExclusive: Int) extends TicketEligibilityRuleConfig
  final case class AgeBetween(minInclusive: Int, maxInclusive: Int) extends TicketEligibilityRuleConfig
  final case class AgeAtLeast(minInclusive: Int) extends TicketEligibilityRuleConfig
  final case class DocumentTypeEquals(documentType: TravelerDocumentType) extends TicketEligibilityRuleConfig
  final case class DocumentNumberPrefix(prefix: String) extends TicketEligibilityRuleConfig

  given Encoder[TravelerDocumentType] = Encoder.encodeString.contramap(_.toString)
  given Decoder[TravelerDocumentType] = Decoder.decodeString.emap { rawValue =>
    TravelerDocumentType.values
      .find(_.toString == rawValue)
      .toRight(s"Unknown traveler document type: $rawValue")
  }

  given Encoder[AgeLessThan] = deriveEncoder
  given Decoder[AgeLessThan] = deriveDecoder
  given Encoder[AgeBetween] = deriveEncoder
  given Decoder[AgeBetween] = deriveDecoder
  given Encoder[AgeAtLeast] = deriveEncoder
  given Decoder[AgeAtLeast] = deriveDecoder
  given Encoder[DocumentTypeEquals] = deriveEncoder
  given Decoder[DocumentTypeEquals] = deriveDecoder
  given Encoder[DocumentNumberPrefix] = deriveEncoder
  given Decoder[DocumentNumberPrefix] = deriveDecoder

final case class TicketEligibilityRule private[domain] (
    ruleId: TicketEligibilityRuleId,
    ticketTypeId: TicketTypeId,
    ruleType: TicketEligibilityRuleType,
    ruleConfigJson: String,
    createdAt: Instant
):
  def parseConfig: Either[AttractionError, TicketEligibilityRuleConfig] =
    ruleType match
      case TicketEligibilityRuleType.AgeLessThan =>
        decode[TicketEligibilityRuleConfig.AgeLessThan](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.AgeBetween =>
        decode[TicketEligibilityRuleConfig.AgeBetween](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.AgeAtLeast =>
        decode[TicketEligibilityRuleConfig.AgeAtLeast](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.DocumentTypeEquals =>
        decode[TicketEligibilityRuleConfig.DocumentTypeEquals](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))
      case TicketEligibilityRuleType.DocumentNumberPrefix =>
        decode[TicketEligibilityRuleConfig.DocumentNumberPrefix](ruleConfigJson)
          .left
          .map(error => AttractionError.TicketEligibilityRuleConfigWasInvalid(ticketTypeId, error.getMessage))

  def humanReadableSummary: String =
    parseConfig.fold(
      _ => ruleType.toString,
      {
        case TicketEligibilityRuleConfig.AgeLessThan(maxExclusive) => s"Traveler age must be below $maxExclusive on visit date"
        case TicketEligibilityRuleConfig.AgeBetween(minInclusive, maxInclusive) =>
          s"Traveler age must be between $minInclusive and $maxInclusive on visit date"
        case TicketEligibilityRuleConfig.AgeAtLeast(minInclusive) => s"Traveler age must be at least $minInclusive on visit date"
        case TicketEligibilityRuleConfig.DocumentTypeEquals(documentType) => s"Traveler document type must be ${documentType.toString}"
        case TicketEligibilityRuleConfig.DocumentNumberPrefix(prefix) => s"Traveler document number must start with $prefix"
      }
    )

final case class TicketType(
    ticketTypeId: TicketTypeId,
    attractionId: AttractionId,
    ticketTypeName: String,
    description: String,
    unitPrice: Money,
    availableFromDate: LocalDate,
    availableToDate: LocalDate,
    totalQuantity: Int,
    validWeekdays: Set[DayOfWeek],
    ticketTypeStatus: TicketTypeStatus,
    eligibilityRules: Vector[TicketEligibilityRule],
    createdAt: Instant
):
  def isActive: Boolean = ticketTypeStatus == TicketTypeStatus.Active

  def supportsUseDate(useDate: LocalDate): Boolean =
    !useDate.isBefore(availableFromDate) &&
      !useDate.isAfter(availableToDate) &&
      validWeekdays.contains(useDate.getDayOfWeek)

  def addEligibilityRule(ticketEligibilityRule: TicketEligibilityRule): Either[AttractionError, TicketType] =
    if ticketEligibilityRule.ticketTypeId != ticketTypeId then Left(AttractionError.TicketEligibilityRuleDidNotBelongToTicketType(ticketEligibilityRule.ruleId, ticketTypeId))
    else Right(copy(eligibilityRules = eligibilityRules :+ ticketEligibilityRule))

final case class Attraction(
    attractionId: AttractionId,
    managerId: ManagerId,
    attractionName: String,
    city: String,
    location: String,
    description: String,
    attractionStatus: AttractionStatus,
    ticketTypes: Vector[TicketType],
    createdAt: Instant
):
  def addTicketType(ticketType: TicketType): Either[AttractionError, Attraction] =
    if ticketType.attractionId != attractionId then Left(AttractionError.TicketTypeDidNotBelongToAttraction(ticketType.ticketTypeId, attractionId))
    else if ticketTypes.exists(_.ticketTypeName.equalsIgnoreCase(ticketType.ticketTypeName)) then
      Left(AttractionError.TicketTypeNameAlreadyExists(attractionId, ticketType.ticketTypeName))
    else Right(copy(ticketTypes = ticketTypes :+ ticketType))

  def replaceTicketType(updatedTicketType: TicketType): Either[AttractionError, Attraction] =
    ticketTypes.indexWhere(_.ticketTypeId == updatedTicketType.ticketTypeId) match
      case -1 => Left(AttractionError.TicketTypeWasNotFound(updatedTicketType.ticketTypeId))
      case ticketTypeIndex => Right(copy(ticketTypes = ticketTypes.updated(ticketTypeIndex, updatedTicketType)))

  def findTicketType(ticketTypeId: TicketTypeId): Either[AttractionError, TicketType] =
    ticketTypes.find(_.ticketTypeId == ticketTypeId).toRight(AttractionError.TicketTypeWasNotFound(ticketTypeId))

final case class TicketEligibilityResult(
    travelerId: TravelerId,
    eligible: Boolean,
    failureReasons: Vector[String]
)

enum AttractionError(val message: String) extends DomainError:
  case AttractionWasNotFound(attractionId: AttractionId)
      extends AttractionError(s"Attraction '${attractionId.value}' was not found")
  case TicketTypeWasNotFound(ticketTypeId: TicketTypeId)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' was not found")
  case TicketTypeWasInactive(ticketTypeId: TicketTypeId)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' is inactive")
  case TicketTypeNameAlreadyExists(attractionId: AttractionId, ticketTypeName: String)
      extends AttractionError(s"Attraction '${attractionId.value}' already has ticket type '$ticketTypeName'")
  case TicketTypeDidNotBelongToAttraction(ticketTypeId: TicketTypeId, attractionId: AttractionId)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' does not belong to attraction '${attractionId.value}'")
  case TicketEligibilityRuleDidNotBelongToTicketType(ruleId: TicketEligibilityRuleId, ticketTypeId: TicketTypeId)
      extends AttractionError(s"Eligibility rule '${ruleId.value}' does not belong to ticket type '${ticketTypeId.value}'")
  case TicketEligibilityRuleConfigWasInvalid(ticketTypeId: TicketTypeId, reason: String)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' has invalid eligibility rule config: $reason")
  case AttractionManagerWasNotFound(managerId: ManagerId)
      extends AttractionError(s"Attraction manager '${managerId.value}' was not found")
  case AttractionWasNotOwnedByManager(attractionId: AttractionId, managerId: ManagerId)
      extends AttractionError(s"Attraction '${attractionId.value}' is not managed by '${managerId.value}'")
  case AttractionUseDateWasMissing
      extends AttractionError("Attraction ticket requires a visit date")
  case AttractionTravelerWasNotEligible(ticketTypeId: TicketTypeId, travelerId: TravelerId, reason: String)
      extends AttractionError(s"Traveler '${travelerId.value}' is not eligible for ticket '${ticketTypeId.value}': $reason")
  case AttractionTravelerSelectionWasInvalid(reason: String)
      extends AttractionError(reason)
  case TicketTypeAvailabilityWasInvalid(ticketTypeId: TicketTypeId, availableFromDate: LocalDate, availableToDate: LocalDate)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' has invalid date range '$availableFromDate' to '$availableToDate'")
  case TicketTypeTotalQuantityWasInvalid(ticketTypeId: TicketTypeId, totalQuantity: Int)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' must have positive quantity but received '$totalQuantity'")
  case TicketTypeWeekdaysWereInvalid(ticketTypeId: TicketTypeId)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' must have at least one valid weekday")
  case TicketTypeUseDateWasUnavailable(ticketTypeId: TicketTypeId, useDate: LocalDate)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' is not available on '$useDate'")
  case TicketTypeInventoryWasNotAvailable(ticketTypeId: TicketTypeId, useDate: LocalDate, requestedQuantity: Int, remainingQuantity: Int)
      extends AttractionError(s"Ticket type '${ticketTypeId.value}' on '$useDate' has only '$remainingQuantity' remaining for request '$requestedQuantity'")

