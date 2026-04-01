package com.typesafe.travel.attraction.domain

import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import munit.FunSuite

import java.time.{Instant, LocalDate}

final class TicketEligibilityServiceSpec extends FunSuite:
  private type TestResult[A] = Either[Throwable, A]
  private val service = TicketEligibilityService[TestResult]()
  private val createdAt = Instant.parse("2026-03-28T00:00:00Z")

  test("age and document rules are evaluated for a single traveler") {
    val traveler = sampleTraveler(
      travelerId = TravelerId("traveler-1"),
      birthDate = LocalDate.parse("2012-03-28"),
      documentType = TravelerDocumentType.NationalIdentityCard,
      documentNumber = "310123456789"
    )
    val ticketType = sampleTicketType(
      Vector(
        createTicketEligibilityRule(
            TicketEligibilityRuleId("rule-1"),
            TicketTypeId("ticket-1"),
            TicketEligibilityRuleType.AgeLessThan,
            TicketEligibilityRuleConfig.AgeLessThan(18),
            createdAt
          ).fold(throw _, identity),
        createTicketEligibilityRule(
            TicketEligibilityRuleId("rule-2"),
            TicketTypeId("ticket-1"),
            TicketEligibilityRuleType.DocumentTypeEquals,
            TicketEligibilityRuleConfig.DocumentTypeEquals(TravelerDocumentType.NationalIdentityCard),
            createdAt
          ).fold(throw _, identity)
      )
    )

    val result = service.evaluateTraveler(traveler, ticketType, LocalDate.parse("2026-04-05")).fold(throw _, identity)
    assert(result.eligible)
    assertEquals(result.failureReasons, Vector.empty)
  }

  test("prefix and range rules fail when traveler is not eligible") {
    val traveler = sampleTraveler(
      travelerId = TravelerId("traveler-2"),
      birthDate = LocalDate.parse("1980-03-28"),
      documentType = TravelerDocumentType.Passport,
      documentNumber = "AB1234567"
    )
    val ticketType = sampleTicketType(
      Vector(
        createTicketEligibilityRule(
            TicketEligibilityRuleId("rule-3"),
            TicketTypeId("ticket-1"),
            TicketEligibilityRuleType.AgeBetween,
            TicketEligibilityRuleConfig.AgeBetween(60, 70),
            createdAt
          ).fold(throw _, identity),
        createTicketEligibilityRule(
            TicketEligibilityRuleId("rule-4"),
            TicketTypeId("ticket-1"),
            TicketEligibilityRuleType.DocumentNumberPrefix,
            TicketEligibilityRuleConfig.DocumentNumberPrefix("310"),
            createdAt
          ).fold(throw _, identity)
      )
    )

    val result = service.evaluateTraveler(traveler, ticketType, LocalDate.parse("2026-04-05")).fold(throw _, identity)
    assert(!result.eligible)
    assertEquals(result.failureReasons.size, 2)
  }

  private def sampleTicketType(rules: Vector[TicketEligibilityRule]): TicketType =
      restorePersistedTicketType(
      ticketTypeId = TicketTypeId("ticket-1"),
      attractionId = AttractionId("attraction-1"),
      ticketTypeName = "Student Ticket",
      description = "Student offer",
      unitPrice = Money.unsafe(80, Currency.CNY),
      ticketTypeStatus = TicketTypeStatus.Active,
      eligibilityRules = rules,
      createdAt = createdAt
    )

  private def sampleTraveler(
      travelerId: TravelerId,
      birthDate: LocalDate,
      documentType: TravelerDocumentType,
      documentNumber: String
  ): TravelerProfile =
    restoreTravelerProfile(
      travelerId = travelerId,
      ownerUserId = UserId("user-1"),
      travelerFullName = PersonName.unsafe("Eligible Traveler"),
      travelerDocumentType = documentType,
      travelerDocumentNumber = DocumentNumber.unsafe(documentNumber),
      travelerPhoneNumber = ContactNumber.unsafe("13800000000"),
      travelerBirthDate = BirthDate.unsafe(birthDate),
      travelerType = TravelerType.AdultTraveler,
      travelerIdentityDocuments = Nil,
      travelerEmergencyContact = None,
      travelerLoyaltyMemberships = Nil,
      travelerPreferences = TravelerPreferences.defaultTravelerPreferences,
      travelerProfileStatus = TravelerProfileStatus.Verified,
      isDefaultTravelerProfile = false
    )

