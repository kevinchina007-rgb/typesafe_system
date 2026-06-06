package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import AttractionRuleJson.{decodeRuleConfig, encodeRuleConfig}

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
      ruleConfigJson = encodeRuleConfig(ruleConfig),
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
    imageUrl: Option[String],
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
    imageUrl = imageUrl.map(_.trim).filter(_.nonEmpty),
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
    imageUrl: Option[String],
    attractionStatus: AttractionStatus,
    ticketTypes: Vector[TicketType],
    createdAt: Instant
): Attraction =
  Attraction(attractionId, managerId, attractionName, city, location, description, imageUrl.map(_.trim).filter(_.nonEmpty), attractionStatus, ticketTypes, createdAt)

def addTicketType(attraction: Attraction, ticketType: TicketType): Either[AttractionError, Attraction] =
  if ticketType.attractionId != attraction.attractionId then Left(AttractionError.TicketTypeDidNotBelongToAttraction(ticketType.ticketTypeId, attraction.attractionId))
  else if attraction.ticketTypes.exists(_.ticketTypeName.equalsIgnoreCase(ticketType.ticketTypeName)) then
    Left(AttractionError.TicketTypeNameAlreadyExists(attraction.attractionId, ticketType.ticketTypeName))
  else Right(attraction.copy(ticketTypes = attraction.ticketTypes :+ ticketType))

def replaceTicketType(attraction: Attraction, updatedTicketType: TicketType): Either[AttractionError, Attraction] =
  attraction.ticketTypes.indexWhere(_.ticketTypeId == updatedTicketType.ticketTypeId) match
    case -1 => Left(AttractionError.TicketTypeWasNotFound(updatedTicketType.ticketTypeId))
    case ticketTypeIndex => Right(attraction.copy(ticketTypes = attraction.ticketTypes.updated(ticketTypeIndex, updatedTicketType)))

def findTicketType(attraction: Attraction, ticketTypeId: TicketTypeId): Either[AttractionError, TicketType] =
  attraction.ticketTypes.find(_.ticketTypeId == ticketTypeId).toRight(AttractionError.TicketTypeWasNotFound(ticketTypeId))


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
    sessions = Vector.empty,
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
    sessions: Vector[AttractionTicketSession],
    eligibilityRules: Vector[TicketEligibilityRule],
    createdAt: Instant
): TicketType =
  TicketType(ticketTypeId, attractionId, ticketTypeName, description, unitPrice, availableFromDate, availableToDate, totalQuantity, validWeekdays, ticketTypeStatus, sessions, eligibilityRules, createdAt)

def ticketTypeIsActive(ticketType: TicketType): Boolean =
  ticketType.ticketTypeStatus == TicketTypeStatus.Active

def ticketTypeSupportsUseDate(ticketType: TicketType, useDate: LocalDate): Boolean =
  !useDate.isBefore(ticketType.availableFromDate) &&
    !useDate.isAfter(ticketType.availableToDate) &&
    ticketType.validWeekdays.contains(useDate.getDayOfWeek)

def addEligibilityRule(ticketType: TicketType, ticketEligibilityRule: TicketEligibilityRule): Either[AttractionError, TicketType] =
  if ticketEligibilityRule.ticketTypeId != ticketType.ticketTypeId then Left(AttractionError.TicketEligibilityRuleDidNotBelongToTicketType(ticketEligibilityRule.ruleId, ticketType.ticketTypeId))
  else Right(ticketType.copy(eligibilityRules = ticketType.eligibilityRules :+ ticketEligibilityRule))

def addTicketSession(ticketType: TicketType, ticketSession: AttractionTicketSession): Either[AttractionError, TicketType] =
  if ticketSession.ticketTypeId != ticketType.ticketTypeId then Left(AttractionError.AttractionTicketSessionDidNotBelongToTicketType(ticketSession.sessionId, ticketType.ticketTypeId))
  else Right(ticketType.copy(sessions = ticketType.sessions :+ ticketSession))


def createAttractionTicketSession(
    sessionId: AttractionTicketSessionId,
    ticketTypeId: TicketTypeId,
    sessionName: String,
    useDate: LocalDate,
    startsAt: Instant,
    endsAt: Instant,
    capacity: Int,
    createdAt: Instant
): Either[AttractionError, AttractionTicketSession] =
  for
    validatedSessionName <- validateText("ticket-session-name", sessionName, 120)
    _ <- if endsAt.isAfter(startsAt) then Right(()) else Left(AttractionError.AttractionTicketSessionTimeRangeWasInvalid(sessionId))
    _ <- if capacity > 0 then Right(()) else Left(AttractionError.AttractionTicketSessionCapacityWasInvalid(sessionId, capacity))
  yield AttractionTicketSession(
    sessionId = sessionId,
    ticketTypeId = ticketTypeId,
    sessionName = validatedSessionName,
    useDate = useDate,
    startsAt = startsAt,
    endsAt = endsAt,
    capacity = capacity,
    status = AttractionTicketSessionStatus.Active,
    createdAt = createdAt
  )


def restorePersistedAttractionTicketSession(
    sessionId: AttractionTicketSessionId,
    ticketTypeId: TicketTypeId,
    sessionName: String,
    useDate: LocalDate,
    startsAt: Instant,
    endsAt: Instant,
    capacity: Int,
    status: AttractionTicketSessionStatus,
    createdAt: Instant
): AttractionTicketSession =
  AttractionTicketSession(sessionId, ticketTypeId, sessionName, useDate, startsAt, endsAt, capacity, status, createdAt)

def attractionTicketSessionIsActive(ticketSession: AttractionTicketSession): Boolean =
  ticketSession.status == AttractionTicketSessionStatus.Active

def parseTicketEligibilityRuleConfig(ticketEligibilityRule: TicketEligibilityRule): Either[AttractionError, TicketEligibilityRuleConfig] =
  decodeRuleConfig(ticketEligibilityRule.ticketTypeId, ticketEligibilityRule.ruleType, ticketEligibilityRule.ruleConfigJson)

def ticketEligibilityRuleHumanReadableSummary(ticketEligibilityRule: TicketEligibilityRule): String =
  parseTicketEligibilityRuleConfig(ticketEligibilityRule).fold(
    _ => ticketEligibilityRule.ruleType.toString,
    {
      case TicketEligibilityRuleConfig.AgeLessThan(maxExclusive) => s"Traveler age must be below $maxExclusive on visit date"
      case TicketEligibilityRuleConfig.AgeBetween(minInclusive, maxInclusive) =>
        s"Traveler age must be between $minInclusive and $maxInclusive on visit date"
      case TicketEligibilityRuleConfig.AgeAtLeast(minInclusive) => s"Traveler age must be at least $minInclusive on visit date"
      case TicketEligibilityRuleConfig.DocumentTypeEquals(documentType) => s"Traveler document type must be ${documentType.toString}"
      case TicketEligibilityRuleConfig.DocumentNumberPrefix(prefix) => s"Traveler document number must start with $prefix"
    }
  )


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
