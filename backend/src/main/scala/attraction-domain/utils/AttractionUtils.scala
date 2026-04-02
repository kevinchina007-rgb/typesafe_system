package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.parser.decode

import java.time.{DayOfWeek, Instant, LocalDate, Period}

def createTicketEligibilityRule(
    ruleId: TicketEligibilityRuleId,
    ticketTypeId: TicketTypeId,
    ruleType: TicketEligibilityRuleType,
    ruleConfig: TicketEligibilityRuleConfig,
    createdAt: Instant
): Either[AttractionError, TicketEligibilityRule] =
  validateTicketEligibilityRuleConfig(ruleType, ruleConfig).map(_ =>
    TicketEligibilityRule(
      ruleId = ruleId,
      ticketTypeId = ticketTypeId,
      ruleType = ruleType,
      ruleConfigJson = encodeTicketEligibilityRuleConfig(ruleConfig),
      createdAt = createdAt
    )
  )


def restorePersistedTicketEligibilityRule(
    ruleId: TicketEligibilityRuleId,
    ticketTypeId: TicketTypeId,
    ruleType: TicketEligibilityRuleType,
    ruleConfigJson: String,
    createdAt: Instant
): TicketEligibilityRule =
  TicketEligibilityRule(ruleId, ticketTypeId, ruleType, ruleConfigJson, createdAt)


def createAttraction(
    attractionId: AttractionId,
    managerId: ManagerId,
    attractionName: String,
    city: String,
    location: String,
    description: String,
    createdAt: Instant
): Either[AttractionError, Attraction] =
  for
    validatedAttractionName <- validateText("attraction-name", attractionName, 160)
    validatedCity <- validateText("attraction-city", city, 120)
    validatedLocation <- validateText("attraction-location", location, 160)
    validatedDescription <- validateText("attraction-description", description, 500)
  yield Attraction(
    attractionId = attractionId,
    managerId = managerId,
    attractionName = validatedAttractionName,
    city = validatedCity,
    location = validatedLocation,
    description = validatedDescription,
    attractionStatus = AttractionStatus.Published,
    ticketTypes = Vector.empty,
    createdAt = createdAt
  )


def restorePersistedAttraction(
    attractionId: AttractionId,
    managerId: ManagerId,
    attractionName: String,
    city: String,
    location: String,
    description: String,
    attractionStatus: AttractionStatus,
    ticketTypes: Vector[TicketType],
    createdAt: Instant
): Attraction =
  Attraction(attractionId, managerId, attractionName, city, location, description, attractionStatus, ticketTypes, createdAt)


private def validateText(fieldName: String, value: String, maxLength: Int): Either[AttractionError, String] =
  val normalized = value.trim
  if normalized.isEmpty then Left(AttractionError.AttractionTravelerSelectionWasInvalid(s"$fieldName must not be empty"))
  else if normalized.length > maxLength then Left(AttractionError.AttractionTravelerSelectionWasInvalid(s"$fieldName is too long"))
  else Right(normalized)


def createTicketType(
    ticketTypeId: TicketTypeId,
    attractionId: AttractionId,
    ticketTypeName: String,
    description: String,
    unitPrice: Money,
    availableFromDate: LocalDate,
    availableToDate: LocalDate,
    totalQuantity: Int,
    validWeekdays: Set[DayOfWeek],
    createdAt: Instant
): Either[AttractionError, TicketType] =
  for
    validatedTicketTypeName <- validateText("ticket-type-name", ticketTypeName, 120)
    validatedDescription <- validateText("ticket-type-description", description, 240)
    _ <- if availableToDate.isBefore(availableFromDate) then Left(AttractionError.TicketTypeAvailabilityWasInvalid(ticketTypeId, availableFromDate, availableToDate)) else Right(())
    _ <- if totalQuantity <= 0 then Left(AttractionError.TicketTypeTotalQuantityWasInvalid(ticketTypeId, totalQuantity)) else Right(())
    normalizedWeekdays = validWeekdays
    _ <- if normalizedWeekdays.isEmpty then Left(AttractionError.TicketTypeWeekdaysWereInvalid(ticketTypeId)) else Right(())
  yield TicketType(
    ticketTypeId = ticketTypeId,
    attractionId = attractionId,
    ticketTypeName = validatedTicketTypeName,
    description = validatedDescription,
    unitPrice = unitPrice,
    availableFromDate = availableFromDate,
    availableToDate = availableToDate,
    totalQuantity = totalQuantity,
    validWeekdays = normalizedWeekdays,
    ticketTypeStatus = TicketTypeStatus.Active,
    eligibilityRules = Vector.empty,
    createdAt = createdAt
  )


def restorePersistedTicketType(
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
): TicketType =
  TicketType(ticketTypeId, attractionId, ticketTypeName, description, unitPrice, availableFromDate, availableToDate, totalQuantity, validWeekdays, ticketTypeStatus, eligibilityRules, createdAt)


private def encodeTicketEligibilityRuleConfig(ruleConfig: TicketEligibilityRuleConfig): String =
  ruleConfig match
    case value: TicketEligibilityRuleConfig.AgeLessThan          => value.asJson.noSpaces
    case value: TicketEligibilityRuleConfig.AgeBetween           => value.asJson.noSpaces
    case value: TicketEligibilityRuleConfig.AgeAtLeast           => value.asJson.noSpaces
    case value: TicketEligibilityRuleConfig.DocumentTypeEquals   => value.asJson.noSpaces
    case value: TicketEligibilityRuleConfig.DocumentNumberPrefix => value.asJson.noSpaces


private def validateTicketEligibilityRuleConfig(
    ruleType: TicketEligibilityRuleType,
    ruleConfig: TicketEligibilityRuleConfig
): Either[AttractionError, Unit] =
  val configMatchesRuleType = ruleConfig match
    case TicketEligibilityRuleConfig.AgeLessThan(maxExclusive) if ruleType == TicketEligibilityRuleType.AgeLessThan && maxExclusive > 0 =>
      true
    case TicketEligibilityRuleConfig.AgeBetween(minInclusive, maxInclusive)
        if ruleType == TicketEligibilityRuleType.AgeBetween && minInclusive >= 0 && maxInclusive >= minInclusive =>
      true
    case TicketEligibilityRuleConfig.AgeAtLeast(minInclusive) if ruleType == TicketEligibilityRuleType.AgeAtLeast && minInclusive >= 0 =>
      true
    case TicketEligibilityRuleConfig.DocumentTypeEquals(_) if ruleType == TicketEligibilityRuleType.DocumentTypeEquals =>
      true
    case TicketEligibilityRuleConfig.DocumentNumberPrefix(prefix)
        if ruleType == TicketEligibilityRuleType.DocumentNumberPrefix && prefix.trim.nonEmpty =>
      true
    case _ =>
      false

  if configMatchesRuleType then Right(())
  else Left(AttractionError.AttractionTravelerSelectionWasInvalid(s"Eligibility rule config did not match $ruleType"))
