package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*

import java.time.{DayOfWeek, Instant, LocalDate, Period}

final case class AttractionStatus(value: String):
  override def toString: String = value

object AttractionStatus:
  val Draft: AttractionStatus = AttractionStatus("Draft")
  val Published: AttractionStatus = AttractionStatus("Published")
  val Closed: AttractionStatus = AttractionStatus("Closed")
  given sourceEncoder: Encoder[AttractionStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AttractionStatus] = Decoder.decodeString.map(fromText)

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
  given sourceEncoder: Encoder[TicketTypeStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TicketTypeStatus] = Decoder.decodeString.map(fromText)

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
  given sourceEncoder: Encoder[AttractionTicketSessionStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[AttractionTicketSessionStatus] = Decoder.decodeString.map(fromText)

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
  given sourceEncoder: Encoder[TicketEligibilityRuleType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TicketEligibilityRuleType] = Decoder.decodeString.map(fromText)

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

  import AttractionSourceJsonCodecs.given
  given sourceAgeLessThanEncoder: Encoder[AgeLessThan] = deriveEncoder
  given sourceAgeLessThanDecoder: Decoder[AgeLessThan] = deriveDecoder
  given sourceAgeBetweenEncoder: Encoder[AgeBetween] = deriveEncoder
  given sourceAgeBetweenDecoder: Decoder[AgeBetween] = deriveDecoder
  given sourceAgeAtLeastEncoder: Encoder[AgeAtLeast] = deriveEncoder
  given sourceAgeAtLeastDecoder: Decoder[AgeAtLeast] = deriveDecoder
  given sourceDocumentTypeEqualsEncoder: Encoder[DocumentTypeEquals] = deriveEncoder
  given sourceDocumentTypeEqualsDecoder: Decoder[DocumentTypeEquals] = deriveDecoder
  given sourceDocumentNumberPrefixEncoder: Encoder[DocumentNumberPrefix] = deriveEncoder
  given sourceDocumentNumberPrefixDecoder: Decoder[DocumentNumberPrefix] = deriveDecoder

  given sourceEncoder: Encoder[TicketEligibilityRuleConfig] =
    Encoder.instance {
      case config: AgeLessThan =>
        config.asJson.deepMerge(Json.obj("ruleConfigType" -> Json.fromString("AgeLessThan")))
      case config: AgeBetween =>
        config.asJson.deepMerge(Json.obj("ruleConfigType" -> Json.fromString("AgeBetween")))
      case config: AgeAtLeast =>
        config.asJson.deepMerge(Json.obj("ruleConfigType" -> Json.fromString("AgeAtLeast")))
      case config: DocumentTypeEquals =>
        config.asJson.deepMerge(Json.obj("ruleConfigType" -> Json.fromString("DocumentTypeEquals")))
      case config: DocumentNumberPrefix =>
        config.asJson.deepMerge(Json.obj("ruleConfigType" -> Json.fromString("DocumentNumberPrefix")))
    }

  given sourceDecoder: Decoder[TicketEligibilityRuleConfig] =
    Decoder.instance { cursor =>
      cursor.downField("ruleConfigType").as[String].flatMap {
        case "AgeLessThan"          => cursor.as[AgeLessThan]
        case "AgeBetween"           => cursor.as[AgeBetween]
        case "AgeAtLeast"           => cursor.as[AgeAtLeast]
        case "DocumentTypeEquals"   => cursor.as[DocumentTypeEquals]
        case "DocumentNumberPrefix" => cursor.as[DocumentNumberPrefix]
        case other                  => Left(io.circe.DecodingFailure(s"Unknown ticket eligibility rule config type: $other", cursor.history))
      }
    }

final case class TicketEligibilityRule(
    ruleId: TicketEligibilityRuleId,
    ticketTypeId: TicketTypeId,
    ruleType: TicketEligibilityRuleType,
    ruleConfigJson: String,
    createdAt: Instant
)
object TicketEligibilityRule:
  import AttractionSourceJsonCodecs.given
  given sourceEncoder: Encoder[TicketEligibilityRule] = deriveEncoder
  given sourceDecoder: Decoder[TicketEligibilityRule] = deriveDecoder

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
)
object TicketType:
  import AttractionSourceJsonCodecs.given
  given sourceEncoder: Encoder[TicketType] = deriveEncoder
  given sourceDecoder: Decoder[TicketType] = deriveDecoder

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
)
object AttractionTicketSession:
  import AttractionSourceJsonCodecs.given
  given sourceEncoder: Encoder[AttractionTicketSession] = deriveEncoder
  given sourceDecoder: Decoder[AttractionTicketSession] = deriveDecoder

final case class Attraction(
    attractionId: AttractionId,
    managerId: ManagerId,
    attractionName: String,
    city: String,
    location: String,
    description: String,
    imageUrl: Option[String],
    attractionStatus: AttractionStatus,
    ticketTypes: Vector[TicketType],
    createdAt: Instant
)
object Attraction:
  import AttractionSourceJsonCodecs.given
  given sourceEncoder: Encoder[Attraction] = deriveEncoder
  given sourceDecoder: Decoder[Attraction] = deriveDecoder

final case class TicketEligibilityResult(
    travelerId: TravelerId,
    eligible: Boolean,
    failureReasons: Vector[String]
)
object TicketEligibilityResult:
  import AttractionSourceJsonCodecs.given
  given sourceEncoder: Encoder[TicketEligibilityResult] = deriveEncoder
  given sourceDecoder: Decoder[TicketEligibilityResult] = deriveDecoder

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
