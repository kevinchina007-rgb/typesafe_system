package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

enum TravelerProfileStatus:
  case Draft, Verified, Archived

enum TravelerDocumentType:
  case Passport, NationalIdentityCard, ResidencePermit, OtherGovernmentDocument

enum TravelerType:
  case AdultTraveler, ChildTraveler, InfantTraveler

enum SeatPreference:
  case Window, Aisle, Middle, NoPreference

enum MealPreference:
  case Standard, Vegetarian, Vegan, Halal, Kosher, ChildMeal, NoPreference

final case class TravelerIdentityDocument private (
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    issuingCountryCode: CountryCode,
    expirationDate: LocalDate
):
  def isValidOn(validationDate: LocalDate): Boolean =
    !expirationDate.isBefore(validationDate)

object TravelerIdentityDocument:
  def create(
      travelerDocumentType: TravelerDocumentType,
      travelerDocumentNumber: DocumentNumber,
      issuingCountryCode: CountryCode,
      expirationDate: LocalDate
  ): TravelerIdentityDocument =
    TravelerIdentityDocument(travelerDocumentType, travelerDocumentNumber, issuingCountryCode, expirationDate)

final case class TravelerLoyaltyMembership private (
    loyaltyProgramName: LoyaltyProgramName,
    loyaltyMembershipNumber: String
)

object TravelerLoyaltyMembership:
  def create(
      loyaltyProgramName: LoyaltyProgramName,
      loyaltyMembershipNumber: String
  ): Either[TravelerError, TravelerLoyaltyMembership] =
    val normalizedMembershipNumber = loyaltyMembershipNumber.trim
    if normalizedMembershipNumber.nonEmpty then
      Right(TravelerLoyaltyMembership(loyaltyProgramName, normalizedMembershipNumber))
    else Left(TravelerError.TravelerLoyaltyMembershipNumberWasEmpty)

  def unsafe(
      loyaltyProgramName: LoyaltyProgramName,
      loyaltyMembershipNumber: String
  ): TravelerLoyaltyMembership =
    create(loyaltyProgramName, loyaltyMembershipNumber).fold(throw _, identity)

final case class TravelerEmergencyContact private (
    emergencyContactName: PersonName,
    emergencyContactPhoneNumber: ContactNumber
)

object TravelerEmergencyContact:
  def create(
      emergencyContactName: PersonName,
      emergencyContactPhoneNumber: ContactNumber
  ): TravelerEmergencyContact =
    TravelerEmergencyContact(emergencyContactName, emergencyContactPhoneNumber)

object TravelerType:
  def deriveFromBirthDate(travelerBirthDate: BirthDate, evaluationDate: LocalDate): TravelerType =
    val ageInYears = java.time.Period.between(travelerBirthDate.value, evaluationDate).getYears
    if ageInYears < 2 then TravelerType.InfantTraveler
    else if ageInYears < 12 then TravelerType.ChildTraveler
    else TravelerType.AdultTraveler

final case class TravelerPreferences private (
    travelerSeatPreference: SeatPreference,
    travelerMealPreference: MealPreference,
    accessibilityRequestNotes: Option[String]
)

object TravelerPreferences:
  val defaultTravelerPreferences: TravelerPreferences =
    TravelerPreferences(
      travelerSeatPreference = SeatPreference.NoPreference,
      travelerMealPreference = MealPreference.NoPreference,
      accessibilityRequestNotes = None
    )

  def create(
      travelerSeatPreference: SeatPreference,
      travelerMealPreference: MealPreference,
      accessibilityRequestNotes: Option[String]
  ): Either[TravelerError, TravelerPreferences] =
    accessibilityRequestNotes match
      case Some(notes) if notes.trim.length > 500 =>
        Left(TravelerError.TravelerAccessibilityNotesWereTooLong(notes.trim.length))
      case Some(notes) =>
        Right(TravelerPreferences(travelerSeatPreference, travelerMealPreference, Some(notes.trim)))
      case None =>
        Right(TravelerPreferences(travelerSeatPreference, travelerMealPreference, None))

  def unsafe(
      travelerSeatPreference: SeatPreference,
      travelerMealPreference: MealPreference,
      accessibilityRequestNotes: Option[String]
  ): TravelerPreferences =
    create(travelerSeatPreference, travelerMealPreference, accessibilityRequestNotes).fold(throw _, identity)

final case class TravelerProfile private[domain] (
    travelerId: TravelerId,
    ownerUserId: UserId,
    travelerFullName: PersonName,
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    travelerPhoneNumber: ContactNumber,
    travelerBirthDate: BirthDate,
    travelerType: TravelerType,
    travelerIdentityDocuments: List[TravelerIdentityDocument],
    travelerEmergencyContact: Option[TravelerEmergencyContact],
    travelerLoyaltyMemberships: List[TravelerLoyaltyMembership],
    travelerPreferences: TravelerPreferences,
    travelerProfileStatus: TravelerProfileStatus,
    isDefaultTravelerProfile: Boolean
):
  def ensureOwnedBy(ownerUserIdToCheck: UserId): Either[TravelerError, TravelerProfile] =
    if ownerUserId == ownerUserIdToCheck then Right(this)
    else Left(TravelerError.TravelerProfileDidNotBelongToUser(travelerId, ownerUserId, ownerUserIdToCheck))

  def updateTravelerProfile(
      updatedTravelerFullName: PersonName,
      updatedTravelerDocumentType: TravelerDocumentType,
      updatedTravelerDocumentNumber: DocumentNumber,
      updatedTravelerPhoneNumber: ContactNumber,
      updatedTravelerBirthDate: BirthDate,
      updatedTravelerType: TravelerType,
      updatedTravelerPreferences: TravelerPreferences,
      updatedTravelerEmergencyContact: Option[TravelerEmergencyContact]
  ): Either[TravelerError, TravelerProfile] =
    travelerProfileStatus match
      case TravelerProfileStatus.Archived =>
        Left(TravelerError.ArchivedTravelerProfileCannotBeUpdated(travelerId))
      case _ =>
        Right(
          copy(
            travelerFullName = updatedTravelerFullName,
            travelerDocumentType = updatedTravelerDocumentType,
            travelerDocumentNumber = updatedTravelerDocumentNumber,
            travelerPhoneNumber = updatedTravelerPhoneNumber,
            travelerBirthDate = updatedTravelerBirthDate,
            travelerType = updatedTravelerType,
            travelerPreferences = updatedTravelerPreferences,
            travelerEmergencyContact = updatedTravelerEmergencyContact
          )
        )

  def addTravelerIdentityDocument(
      travelerIdentityDocument: TravelerIdentityDocument
  ): Either[TravelerError, TravelerProfile] =
    if travelerIdentityDocuments.exists(_.travelerDocumentNumber == travelerIdentityDocument.travelerDocumentNumber) then
      Left(TravelerError.DuplicateTravelerIdentityDocument(travelerId, travelerIdentityDocument.travelerDocumentNumber))
    else
      Right(copy(travelerIdentityDocuments = travelerIdentityDocuments :+ travelerIdentityDocument))

  def verifyTravelerProfile(validationDate: LocalDate): Either[TravelerError, TravelerProfile] =
    travelerProfileStatus match
      case TravelerProfileStatus.Archived =>
        Left(TravelerError.InvalidTravelerProfileTransition(travelerId, travelerProfileStatus, TravelerProfileStatus.Verified))
      case _ if travelerIdentityDocuments.isEmpty =>
        Left(TravelerError.TravelerIdentityDocumentsWereMissing(travelerId))
      case _ if travelerIdentityDocuments.exists(_.isValidOn(validationDate)) =>
        Right(copy(travelerProfileStatus = TravelerProfileStatus.Verified))
      case _ =>
        Left(TravelerError.TravelerDidNotHaveValidIdentityDocument(travelerId, validationDate))

  def archiveTravelerProfile: Either[TravelerError, TravelerProfile] =
    if isDefaultTravelerProfile then
      Left(TravelerError.DefaultTravelerProfileCannotBeArchived(travelerId))
    else
      travelerProfileStatus match
        case TravelerProfileStatus.Archived =>
          Left(TravelerError.InvalidTravelerProfileTransition(travelerId, travelerProfileStatus, TravelerProfileStatus.Archived))
        case _ =>
          Right(copy(travelerProfileStatus = TravelerProfileStatus.Archived))

  def markAsDefaultTravelerProfile: Either[TravelerError, TravelerProfile] =
    travelerProfileStatus match
      case TravelerProfileStatus.Archived =>
        Left(TravelerError.ArchivedTravelerProfileCannotBeDefault(travelerId))
      case _ =>
        Right(copy(isDefaultTravelerProfile = true))

  def clearDefaultTravelerProfile: TravelerProfile =
    copy(isDefaultTravelerProfile = false)

enum TravelerError(val message: String) extends DomainError:
  case TravelerProfileWasNotFound(travelerId: TravelerId)
      extends TravelerError(s"Traveler profile '${travelerId.value}' was not found")
  case TravelerOwnerUserWasNotFound(ownerUserId: UserId)
      extends TravelerError(s"Owner user '${ownerUserId.value}' was not found")
  case TravelerProfileDidNotBelongToUser(travelerId: TravelerId, actualOwnerUserId: UserId, requestedOwnerUserId: UserId)
      extends TravelerError(
        s"Traveler profile '${travelerId.value}' belongs to '${actualOwnerUserId.value}' instead of '${requestedOwnerUserId.value}'"
      )
  case DuplicateTravelerIdentityDocument(travelerId: TravelerId, travelerDocumentNumber: DocumentNumber)
      extends TravelerError(
        s"Traveler profile '${travelerId.value}' already has document '${travelerDocumentNumber.value}'"
      )
  case TravelerIdentityDocumentsWereMissing(travelerId: TravelerId)
      extends TravelerError(s"Traveler profile '${travelerId.value}' cannot be verified without identity documents")
  case TravelerDidNotHaveValidIdentityDocument(travelerId: TravelerId, validationDate: LocalDate)
      extends TravelerError(s"Traveler profile '${travelerId.value}' has no valid identity document on $validationDate")
  case InvalidTravelerProfileTransition(
      travelerId: TravelerId,
      currentTravelerProfileStatus: TravelerProfileStatus,
      targetTravelerProfileStatus: TravelerProfileStatus
  ) extends TravelerError(
        s"Traveler profile '${travelerId.value}' cannot transition from $currentTravelerProfileStatus to $targetTravelerProfileStatus"
      )
  case ArchivedTravelerProfileCannotBeUpdated(travelerId: TravelerId)
      extends TravelerError(s"Archived traveler profile '${travelerId.value}' cannot be updated")
  case ArchivedTravelerProfileCannotBeDefault(travelerId: TravelerId)
      extends TravelerError(s"Archived traveler profile '${travelerId.value}' cannot become default")
  case DefaultTravelerProfileCannotBeArchived(travelerId: TravelerId)
      extends TravelerError(s"Default traveler profile '${travelerId.value}' cannot be archived")
  case TravelerDocumentNumberAlreadyExists(travelerDocumentNumber: DocumentNumber)
      extends TravelerError(s"Traveler document '${travelerDocumentNumber.value}' already exists")
  case TravelerLoyaltyMembershipNumberWasEmpty
      extends TravelerError("Traveler loyalty membership number must not be empty")
  case TravelerAccessibilityNotesWereTooLong(actualLength: Int)
      extends TravelerError(s"Traveler accessibility notes must be at most 500 characters but were $actualLength")
