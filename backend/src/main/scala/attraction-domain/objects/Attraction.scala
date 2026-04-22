package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import AttractionRuleJson.*

import java.time.{DayOfWeek, Instant, LocalDate, Period}

final case class AttractionStatus(value: String):
  override def toString: String = value

object AttractionStatus:
  val Draft: AttractionStatus = AttractionStatus("Draft")
  val Published: AttractionStatus = AttractionStatus("Published")
  val Closed: AttractionStatus = AttractionStatus("Closed")

  def fromText(value: String): AttractionStatus =
    value.trim.toLowerCase match
      case "draft" => Draft
      case "published" => Published
      case "closed" => Closed
      case _ => Draft

final case class TicketTypeStatus(value: String):
  override def toString: String = value

object TicketTypeStatus:
  val Active: TicketTypeStatus = TicketTypeStatus("Active")
  val Inactive: TicketTypeStatus = TicketTypeStatus("Inactive")

  def fromText(value: String): TicketTypeStatus =
    value.trim.toLowerCase match
      case "active" => Active
      case "inactive" => Inactive
      case _ => Inactive

final case class AttractionTicketSessionStatus(value: String):
  override def toString: String = value

object AttractionTicketSessionStatus:
  val Active: AttractionTicketSessionStatus = AttractionTicketSessionStatus("Active")
  val Closed: AttractionTicketSessionStatus = AttractionTicketSessionStatus("Closed")

  def fromText(value: String): AttractionTicketSessionStatus =
    value.trim.toLowerCase match
      case "active" => Active
      case "closed" => Closed
      case _ => Closed

final case class TicketEligibilityRuleType(value: String):
  override def toString: String = value

object TicketEligibilityRuleType:
  val AgeLessThan: TicketEligibilityRuleType = TicketEligibilityRuleType("AgeLessThan")
  val AgeBetween: TicketEligibilityRuleType = TicketEligibilityRuleType("AgeBetween")
  val AgeAtLeast: TicketEligibilityRuleType = TicketEligibilityRuleType("AgeAtLeast")
  val DocumentTypeEquals: TicketEligibilityRuleType = TicketEligibilityRuleType("DocumentTypeEquals")
  val DocumentNumberPrefix: TicketEligibilityRuleType = TicketEligibilityRuleType("DocumentNumberPrefix")

  def fromText(value: String): TicketEligibilityRuleType =
    value.trim.toLowerCase match
      case "agelessthan" | "age_less_than" => AgeLessThan
      case "agebetween" | "age_between" => AgeBetween
      case "ageatleast" | "age_at_least" => AgeAtLeast
      case "documenttypeequals" | "document_type_equals" => DocumentTypeEquals
      case "documentnumberprefix" | "document_number_prefix" => DocumentNumberPrefix
      case _ => DocumentNumberPrefix

sealed trait TicketEligibilityRuleConfig
object TicketEligibilityRuleConfig:
  final case class AgeLessThan(maxExclusive: Int) extends TicketEligibilityRuleConfig
  final case class AgeBetween(minInclusive: Int, maxInclusive: Int) extends TicketEligibilityRuleConfig
  final case class AgeAtLeast(minInclusive: Int) extends TicketEligibilityRuleConfig
  final case class DocumentTypeEquals(documentType: TravelerDocumentType) extends TicketEligibilityRuleConfig
  final case class DocumentNumberPrefix(prefix: String) extends TicketEligibilityRuleConfig

final case class TicketEligibilityRule(
    ruleId: TicketEligibilityRuleId,
    ticketTypeId: TicketTypeId,
    ruleType: TicketEligibilityRuleType,
    ruleConfigJson: String,
    createdAt: Instant
):
  def parseConfig: Either[AttractionError, TicketEligibilityRuleConfig] =
    decodeRuleConfig(ticketTypeId, ruleType, ruleConfigJson)

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
    sessions: Vector[AttractionTicketSession],
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

  def addSession(ticketSession: AttractionTicketSession): Either[AttractionError, TicketType] =
    if ticketSession.ticketTypeId != ticketTypeId then Left(AttractionError.AttractionTicketSessionDidNotBelongToTicketType(ticketSession.sessionId, ticketTypeId))
    else Right(copy(sessions = sessions :+ ticketSession))

final case class AttractionTicketSession(
    sessionId: AttractionTicketSessionId,
    ticketTypeId: TicketTypeId,
    sessionName: String,
    useDate: LocalDate,
    startsAt: Instant,
    endsAt: Instant,
    capacity: Int,
    status: AttractionTicketSessionStatus,
    createdAt: Instant
):
  def isActive: Boolean = status == AttractionTicketSessionStatus.Active

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

sealed trait AttractionError extends DomainError:
  def message: String

object AttractionError:
  final case class AttractionWasNotFound(attractionId: AttractionId) extends AttractionError:
    override val message: String = s"Attraction '${attractionId.value}' was not found"

  final case class TicketTypeWasNotFound(ticketTypeId: TicketTypeId) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' was not found"

  final case class TicketTypeWasInactive(ticketTypeId: TicketTypeId) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' is inactive"

  final case class TicketTypeNameAlreadyExists(attractionId: AttractionId, ticketTypeName: String) extends AttractionError:
    override val message: String = s"Attraction '${attractionId.value}' already has ticket type '$ticketTypeName'"

  final case class TicketTypeDidNotBelongToAttraction(ticketTypeId: TicketTypeId, attractionId: AttractionId) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' does not belong to attraction '${attractionId.value}'"

  final case class TicketEligibilityRuleDidNotBelongToTicketType(ruleId: TicketEligibilityRuleId, ticketTypeId: TicketTypeId) extends AttractionError:
    override val message: String = s"Eligibility rule '${ruleId.value}' does not belong to ticket type '${ticketTypeId.value}'"

  final case class TicketEligibilityRuleConfigWasInvalid(ticketTypeId: TicketTypeId, reason: String) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' has invalid eligibility rule config: $reason"

  final case class AttractionManagerWasNotFound(managerId: ManagerId) extends AttractionError:
    override val message: String = s"Attraction manager '${managerId.value}' was not found"

  final case class AttractionWasNotOwnedByManager(attractionId: AttractionId, managerId: ManagerId) extends AttractionError:
    override val message: String = s"Attraction '${attractionId.value}' is not managed by '${managerId.value}'"

  final case class AttractionUseDateWasMissing() extends AttractionError:
    override val message: String = "Attraction ticket requires a visit date"

  final case class AttractionTravelerWasNotEligible(ticketTypeId: TicketTypeId, travelerId: TravelerId, reason: String) extends AttractionError:
    override val message: String = s"Traveler '${travelerId.value}' is not eligible for ticket '${ticketTypeId.value}': $reason"

  final case class AttractionTravelerSelectionWasInvalid(reason: String) extends AttractionError:
    override val message: String = reason

  final case class TicketTypeAvailabilityWasInvalid(ticketTypeId: TicketTypeId, availableFromDate: LocalDate, availableToDate: LocalDate) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' has invalid date range '$availableFromDate' to '$availableToDate'"

  final case class TicketTypeTotalQuantityWasInvalid(ticketTypeId: TicketTypeId, totalQuantity: Int) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' must have positive quantity but received '$totalQuantity'"

  final case class TicketTypeWeekdaysWereInvalid(ticketTypeId: TicketTypeId) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' must have at least one valid weekday"

  final case class TicketTypeUseDateWasUnavailable(ticketTypeId: TicketTypeId, useDate: LocalDate) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' is not available on '$useDate'"

  final case class TicketTypeInventoryWasNotAvailable(ticketTypeId: TicketTypeId, useDate: LocalDate, requestedQuantity: Int, remainingQuantity: Int) extends AttractionError:
    override val message: String = s"Ticket type '${ticketTypeId.value}' on '$useDate' has only '$remainingQuantity' remaining for request '$requestedQuantity'"

  final case class AttractionTicketSessionDidNotBelongToTicketType(sessionId: AttractionTicketSessionId, ticketTypeId: TicketTypeId) extends AttractionError:
    override val message: String = s"Session '${sessionId.value}' does not belong to ticket type '${ticketTypeId.value}'"

  final case class AttractionTicketSessionWasNotFound(sessionId: AttractionTicketSessionId) extends AttractionError:
    override val message: String = s"Session '${sessionId.value}' was not found"

  final case class AttractionTicketSessionWasInactive(sessionId: AttractionTicketSessionId) extends AttractionError:
    override val message: String = s"Session '${sessionId.value}' is inactive"

  final case class AttractionTicketSessionCapacityWasInvalid(sessionId: AttractionTicketSessionId, capacity: Int) extends AttractionError:
    override val message: String = s"Session '${sessionId.value}' must have positive capacity but received '$capacity'"

  final case class AttractionTicketSessionTimeRangeWasInvalid(sessionId: AttractionTicketSessionId) extends AttractionError:
    override val message: String = s"Session '${sessionId.value}' must end after it starts"

  final case class AttractionTicketSessionInventoryWasNotAvailable(sessionId: AttractionTicketSessionId, requestedQuantity: Int, remainingQuantity: Int) extends AttractionError:
    override val message: String = s"Session '${sessionId.value}' has only '$remainingQuantity' remaining for request '$requestedQuantity'"
