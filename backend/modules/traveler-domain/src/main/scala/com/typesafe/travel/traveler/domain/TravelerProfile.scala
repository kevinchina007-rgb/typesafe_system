package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

enum TravelerProfileStatus:
  case Draft, Verified, Archived

enum IdentityDocumentType:
  case Passport, NationalId, ResidencePermit, OtherGovernmentId

enum SeatPreference:
  case Window, Aisle, Middle, NoPreference

enum MealPreference:
  case Standard, Vegetarian, Vegan, Halal, Kosher, ChildMeal, NoPreference

final case class IdentityDocument(
    documentType: IdentityDocumentType,
    documentNumber: DocumentNumber,
    issuingCountry: CountryCode,
    expiresOn: LocalDate
):
  def isValidOn(date: LocalDate): Boolean = !expiresOn.isBefore(date)

final case class TravelerLoyaltyMembership(
    programName: LoyaltyProgramName,
    membershipNumber: String
):
  require(membershipNumber.trim.nonEmpty, "Membership number must be non-empty")

final case class EmergencyContact(
    name: PersonName,
    phone: ContactNumber
)

final case class TravelerPreferences(
    seatPreference: SeatPreference,
    mealPreference: MealPreference,
    accessibilityNotes: Option[String]
)

final case class TravelerProfile(
    id: TravelerId,
    ownerUserId: UserId,
    fullName: PersonName,
    birthDate: BirthDate,
    documents: List[IdentityDocument],
    emergencyContact: Option[EmergencyContact],
    loyaltyMemberships: List[TravelerLoyaltyMembership],
    preferences: TravelerPreferences,
    status: TravelerProfileStatus
):
  def addDocument(document: IdentityDocument): TravelerProfile =
    copy(documents = documents :+ document)

  def verify(onDate: LocalDate): Either[TravelerDomainError, TravelerProfile] =
    status match
      case TravelerProfileStatus.Archived =>
        Left(TravelerDomainError.InvalidProfileTransition(id, status, TravelerProfileStatus.Verified))
      case _ if documents.isEmpty =>
        Left(TravelerDomainError.MissingIdentityDocuments(id))
      case _ if documents.exists(_.isValidOn(onDate)) =>
        Right(copy(status = TravelerProfileStatus.Verified))
      case _ =>
        Left(TravelerDomainError.NoValidDocument(id, onDate))

  def archive: Either[TravelerDomainError, TravelerProfile] =
    status match
      case TravelerProfileStatus.Archived =>
        Left(TravelerDomainError.InvalidProfileTransition(id, status, TravelerProfileStatus.Archived))
      case _ =>
        Right(copy(status = TravelerProfileStatus.Archived))

object TravelerProfile:
  def draft(
      id: TravelerId,
      ownerUserId: UserId,
      fullName: PersonName,
      birthDate: BirthDate,
      preferences: TravelerPreferences,
      emergencyContact: Option[EmergencyContact] = None
  ): TravelerProfile =
    TravelerProfile(
      id = id,
      ownerUserId = ownerUserId,
      fullName = fullName,
      birthDate = birthDate,
      documents = Nil,
      emergencyContact = emergencyContact,
      loyaltyMemberships = Nil,
      preferences = preferences,
      status = TravelerProfileStatus.Draft
    )

enum TravelerDomainError(val message: String) extends DomainError:
  case ProfileNotFound(id: TravelerId)
      extends TravelerDomainError(s"Traveler profile ${id.value} was not found")
  case MissingIdentityDocuments(id: TravelerId)
      extends TravelerDomainError(s"Traveler profile ${id.value} cannot be verified without identity documents")
  case NoValidDocument(id: TravelerId, onDate: LocalDate)
      extends TravelerDomainError(s"Traveler profile ${id.value} has no valid document on $onDate")
  case InvalidProfileTransition(id: TravelerId, from: TravelerProfileStatus, to: TravelerProfileStatus)
      extends TravelerDomainError(s"Traveler profile ${id.value} cannot transition from $from to $to")
