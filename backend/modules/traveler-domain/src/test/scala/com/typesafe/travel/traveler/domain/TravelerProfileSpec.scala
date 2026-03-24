package com.typesafe.travel.traveler.domain

import munit.FunSuite
import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

final class TravelerProfileSpec extends FunSuite:

  test("draft traveler profile can be verified when it has a valid document") {
    val profile =
      TravelerProfile
        .draft(
          id = TravelerId("traveler-1"),
          ownerUserId = UserId("user-1"),
          fullName = PersonName("Ada Lovelace"),
          birthDate = BirthDate(LocalDate.parse("1990-12-10")),
          preferences = TravelerPreferences(
            seatPreference = SeatPreference.Window,
            mealPreference = MealPreference.Vegetarian,
            accessibilityNotes = None
          )
        )
        .addDocument(
          IdentityDocument(
            documentType = IdentityDocumentType.Passport,
            documentNumber = DocumentNumber("P1234567"),
            issuingCountry = CountryCode("CN"),
            expiresOn = LocalDate.parse("2030-01-01")
          )
        )

    val result = profile.verify(LocalDate.parse("2026-03-24"))

    assertEquals(result.map(_.status), Right(TravelerProfileStatus.Verified))
  }

  test("traveler profile verification fails without documents") {
    val profile =
      TravelerProfile.draft(
        id = TravelerId("traveler-2"),
        ownerUserId = UserId("user-1"),
        fullName = PersonName("Grace Hopper"),
        birthDate = BirthDate(LocalDate.parse("1988-12-09")),
        preferences = TravelerPreferences(
          seatPreference = SeatPreference.NoPreference,
          mealPreference = MealPreference.Standard,
          accessibilityNotes = None
        )
      )

    val result = profile.verify(LocalDate.parse("2026-03-24"))

    assert(result.swap.exists(_.isInstanceOf[TravelerDomainError.MissingIdentityDocuments]))
  }
