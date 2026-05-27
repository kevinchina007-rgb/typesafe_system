package com.typesafe.travel.traveler.domain

import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.shared.kernel.*
import munit.FunSuite
import java.time.{Instant, LocalDate}

final class TravelerProfileSpec extends FunSuite:

  type TestEither[A] = Either[Throwable, A]

  test("first traveler profile becomes default automatically") {
    val travelerProfileRepository = InMemoryTravelerProfileRepository()
    val userRepository = InMemoryUserRepository(
      List(
        registerNewUser(
          userId = UserId("user-1"),
          primaryEmailAddress = EmailAddress.unsafe("ada@example.com"),
          userDisplayName = PersonName.unsafe("Ada Lovelace"),
          userPhoneNumber = ContactNumber.unsafe("+15550000011"),
          registeredAt = Instant.parse("2026-03-25T00:00:00Z")
        )
      )
    )
    val travelerProfileService =
      LiveTravelerProfileService[TestEither](travelerProfileRepository, userRepository, () => Right(LocalDate.parse("2026-03-25")))

    val createdTravelerProfile =
      travelerProfileService.createTravelerProfile(
        ownerUserId = UserId("user-1"),
        travelerFullName = PersonName.unsafe("Ada Traveler"),
        travelerDocumentType = TravelerDocumentType.Passport,
        travelerDocumentNumber = DocumentNumber.unsafe("P1234567"),
        travelerPhoneNumber = ContactNumber.unsafe("+15550000021"),
        travelerBirthDate = BirthDate.unsafe(LocalDate.parse("1990-12-10")),
        travelerPreferences = TravelerPreferences.defaultTravelerPreferences,
        travelerEmergencyContact = None,
        requestedDefaultTravelerProfile = false
      )

    assertEquals(createdTravelerProfile.map(_.isDefaultTravelerProfile), Right(true))
  }

  test("archiving default traveler profile is rejected") {
    val defaultTravelerProfile =
      newTravelerProfile(
        travelerId = TravelerId("traveler-1"),
        ownerUserId = UserId("user-1"),
        travelerFullName = PersonName.unsafe("Grace Hopper"),
        travelerDocumentType = TravelerDocumentType.Passport,
        travelerDocumentNumber = DocumentNumber.unsafe("P7654321"),
        travelerPhoneNumber = ContactNumber.unsafe("+15550000022"),
        travelerBirthDate = BirthDate.unsafe(LocalDate.parse("1988-12-09")),
        travelerType = TravelerType.AdultTraveler,
        travelerPreferences = TravelerPreferences.defaultTravelerPreferences,
        travelerEmergencyContact = None,
        isDefaultTravelerProfile = true
      )

    val archiveAttempt = defaultTravelerProfile.archiveTravelerProfile

    assert(archiveAttempt.swap.exists(_.isInstanceOf[TravelerError.DefaultTravelerProfileCannotBeArchived]))
  }

  test("duplicate document number is rejected") {
    val baseTravelerProfile =
      newTravelerProfile(
        travelerId = TravelerId("traveler-2"),
        ownerUserId = UserId("user-1"),
        travelerFullName = PersonName.unsafe("Linus Traveler"),
        travelerDocumentType = TravelerDocumentType.Passport,
        travelerDocumentNumber = DocumentNumber.unsafe("P7777777"),
        travelerPhoneNumber = ContactNumber.unsafe("+15550000023"),
        travelerBirthDate = BirthDate.unsafe(LocalDate.parse("1986-09-01")),
        travelerType = TravelerType.AdultTraveler,
        travelerPreferences = TravelerPreferences.defaultTravelerPreferences,
        travelerEmergencyContact = None,
        isDefaultTravelerProfile = false
      )

    val travelerIdentityDocument =
      TravelerIdentityDocument.create(
        travelerDocumentType = TravelerDocumentType.Passport,
        travelerDocumentNumber = DocumentNumber.unsafe("P123456"),
        issuingCountryCode = CountryCode.unsafe("CN"),
        expirationDate = LocalDate.parse("2032-01-01")
      )

    val duplicateAttempt =
      baseTravelerProfile
        .addTravelerIdentityDocument(travelerIdentityDocument)
        .flatMap(_.addTravelerIdentityDocument(travelerIdentityDocument))

    assert(duplicateAttempt.swap.exists(_.isInstanceOf[TravelerError.DuplicateTravelerIdentityDocument]))
  }

  test("document type parsing accepts the traveler form values") {
    assertEquals(TravelerDocumentType.fromText("passport"), TravelerDocumentType.Passport)
    assertEquals(TravelerDocumentType.fromText("identity-card"), TravelerDocumentType.NationalIdentityCard)
    assertEquals(TravelerDocumentType.fromText("residence-permit"), TravelerDocumentType.ResidencePermit)
    assertEquals(TravelerDocumentType.fromText("other"), TravelerDocumentType.OtherGovernmentDocument)
  }

  test("creating another traveler with the same document number is rejected") {
    val existingTravelerProfile =
      newTravelerProfile(
        travelerId = TravelerId("traveler-existing"),
        ownerUserId = UserId("user-1"),
        travelerFullName = PersonName.unsafe("Existing Traveler"),
        travelerDocumentType = TravelerDocumentType.Passport,
        travelerDocumentNumber = DocumentNumber.unsafe("P8888888"),
        travelerPhoneNumber = ContactNumber.unsafe("+15550000029"),
        travelerBirthDate = BirthDate.unsafe(LocalDate.parse("1991-02-01")),
        travelerType = TravelerType.AdultTraveler,
        travelerPreferences = TravelerPreferences.defaultTravelerPreferences,
        travelerEmergencyContact = None,
        isDefaultTravelerProfile = true
      )

    val travelerProfileRepository = InMemoryTravelerProfileRepository(
      Map(existingTravelerProfile.travelerId -> existingTravelerProfile)
    )
    val userRepository = InMemoryUserRepository(
      List(
        registerNewUser(
          userId = UserId("user-1"),
          primaryEmailAddress = EmailAddress.unsafe("ada@example.com"),
          userDisplayName = PersonName.unsafe("Ada Lovelace"),
          userPhoneNumber = ContactNumber.unsafe("+15550000011"),
          registeredAt = Instant.parse("2026-03-25T00:00:00Z")
        )
      )
    )
    val travelerProfileService =
      LiveTravelerProfileService[TestEither](travelerProfileRepository, userRepository, () => Right(LocalDate.parse("2026-03-25")))

    val duplicateAttempt =
      travelerProfileService.createTravelerProfile(
        ownerUserId = UserId("user-1"),
        travelerFullName = PersonName.unsafe("Duplicate Traveler"),
        travelerDocumentType = TravelerDocumentType.Passport,
        travelerDocumentNumber = DocumentNumber.unsafe("P8888888"),
        travelerPhoneNumber = ContactNumber.unsafe("+15550000031"),
        travelerBirthDate = BirthDate.unsafe(LocalDate.parse("1993-10-10")),
        travelerPreferences = TravelerPreferences.defaultTravelerPreferences,
        travelerEmergencyContact = None,
        requestedDefaultTravelerProfile = false
      )

    assert(duplicateAttempt.swap.exists(_.isInstanceOf[TravelerError.TravelerDocumentNumberAlreadyExists]))
  }

  private final case class InMemoryTravelerProfileRepository(
      storedTravelerProfiles: Map[TravelerId, TravelerProfile] = Map.empty
  ) extends TravelerProfileRepository[TestEither]:
    override def nextTravelerId: TestEither[TravelerId] =
      Right(TravelerId(s"traveler-${storedTravelerProfiles.size + 1}"))

    override def findTravelerProfileById(travelerId: TravelerId): TestEither[Option[TravelerProfile]] =
      Right(storedTravelerProfiles.get(travelerId))

    override def findTravelerProfilesByDocumentIdentity(
        travelerDocumentType: TravelerDocumentType,
        travelerDocumentNumber: DocumentNumber
    ): TestEither[List[TravelerProfile]] =
      Right(
        storedTravelerProfiles.values
          .filter(travelerProfile =>
            travelerProfile.travelerDocumentType == travelerDocumentType &&
              travelerProfile.travelerDocumentNumber == travelerDocumentNumber
          )
          .toList
      )

    override def findTravelerProfilesByOwnerUserId(ownerUserId: UserId): TestEither[List[TravelerProfile]] =
      Right(storedTravelerProfiles.values.filter(_.ownerUserId == ownerUserId).toList)

    override def saveTravelerProfile(travelerProfile: TravelerProfile): TestEither[TravelerProfile] =
      Right(travelerProfile)

    override def deleteTravelerProfile(travelerId: TravelerId): TestEither[Unit] =
      Right(())

  private final case class InMemoryUserRepository(
      storedUsers: List[User]
  ) extends UserRepository[TestEither]:
    override def nextUserId: TestEither[UserId] =
      Right(UserId("user-generated"))

    override def findByUserId(userId: UserId): TestEither[Option[User]] =
      Right(storedUsers.find(_.userId == userId))

    override def findByPrimaryEmailAddress(primaryEmailAddress: EmailAddress): TestEither[Option[User]] =
      Right(storedUsers.find(_.primaryEmailAddress == primaryEmailAddress))

    override def saveUser(user: User): TestEither[User] =
      Right(user)
