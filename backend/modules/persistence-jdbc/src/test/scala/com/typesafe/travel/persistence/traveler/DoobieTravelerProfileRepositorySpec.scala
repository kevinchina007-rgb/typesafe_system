package com.typesafe.travel.persistence.traveler

import cats.effect.unsafe.implicits.global
import com.typesafe.travel.identity.domain.User
import com.typesafe.travel.persistence.*
import com.typesafe.travel.persistence.identity.DoobieUserRepository
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import munit.FunSuite

import java.time.{Instant, LocalDate}

final class DoobieTravelerProfileRepositorySpec extends FunSuite:
  test("traveler repository round-trips saved traveler profile with preferences and documents") {
    val transactor = PersistenceTestSupport.createTestTransactor()
    SchemaInitializer.initialize(transactor).unsafeRunSync()

    val userRepository = DoobieUserRepository[cats.effect.IO](transactor)
    val travelerRepository = DoobieTravelerProfileRepository[cats.effect.IO](transactor)

    userRepository
      .saveUser(
        registerNewUser(
          userId = UserId("user-traveler-owner"),
          primaryEmailAddress = EmailAddress.unsafe("traveler-owner@example.com"),
          userDisplayName = PersonName.unsafe("Traveler Owner"),
          userPhoneNumber = ContactNumber.unsafe("+15550000002"),
          registeredAt = Instant.parse("2026-03-26T01:10:00Z")
        )
      )
      .unsafeRunSync()

    val savedTravelerProfile =
      restoreTravelerProfile(
        travelerId = TravelerId("traveler-roundtrip"),
        ownerUserId = UserId("user-traveler-owner"),
        travelerFullName = PersonName.unsafe("Round Trip Traveler"),
        travelerDocumentType = TravelerDocumentType.Passport,
        travelerDocumentNumber = DocumentNumber.unsafe("P12345001"),
        travelerPhoneNumber = ContactNumber.unsafe("+15550000003"),
        travelerBirthDate = BirthDate.unsafe(LocalDate.parse("1992-05-18")),
        travelerType = TravelerType.AdultTraveler,
        travelerIdentityDocuments = List(
          TravelerIdentityDocument.create(
            travelerDocumentType = TravelerDocumentType.Passport,
            travelerDocumentNumber = DocumentNumber.unsafe("P12345001"),
            issuingCountryCode = CountryCode.unsafe("CN"),
            expirationDate = LocalDate.parse("2032-05-18")
          )
        ),
        travelerEmergencyContact = Some(
          TravelerEmergencyContact.create(
            emergencyContactName = PersonName.unsafe("Emergency Person"),
            emergencyContactPhoneNumber = ContactNumber.unsafe("+15550000004")
          )
        ),
        travelerLoyaltyMemberships = List(
          TravelerLoyaltyMembership.unsafe(
            loyaltyProgramName = LoyaltyProgramName.unsafe("Sky Club"),
            loyaltyMembershipNumber = "SC-9988"
          )
        ),
        travelerPreferences = TravelerPreferences.unsafe(SeatPreference.Window, MealPreference.Vegetarian, Some("Wheelchair")),
        travelerProfileStatus = TravelerProfileStatus.Verified,
        isDefaultTravelerProfile = true
      )

    travelerRepository.saveTravelerProfile(savedTravelerProfile).unsafeRunSync()
    val loadedTraveler = travelerRepository.findTravelerProfileById(savedTravelerProfile.travelerId).unsafeRunSync()

    assertEquals(loadedTraveler, Some(savedTravelerProfile))
  }

