package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

final case class TravelerProfileStatus(value: String):
  override def toString: String = value

object TravelerProfileStatus:
  val Draft: TravelerProfileStatus = TravelerProfileStatus("Draft")
  val Verified: TravelerProfileStatus = TravelerProfileStatus("Verified")
  val Archived: TravelerProfileStatus = TravelerProfileStatus("Archived")
  val all: Vector[TravelerProfileStatus] = Vector(Draft, Verified, Archived)

  def fromText(value: String): TravelerProfileStatus =
    value.trim.toLowerCase match
      case "verified" => Verified
      case "archived" => Archived
      case _ => Draft

final case class TravelerDocumentType(value: String):
  override def toString: String = value

object TravelerDocumentType:
  val Passport: TravelerDocumentType = TravelerDocumentType("Passport")
  val NationalIdentityCard: TravelerDocumentType = TravelerDocumentType("NationalIdentityCard")
  val ResidencePermit: TravelerDocumentType = TravelerDocumentType("ResidencePermit")
  val OtherGovernmentDocument: TravelerDocumentType = TravelerDocumentType("OtherGovernmentDocument")
  val all: Vector[TravelerDocumentType] = Vector(Passport, NationalIdentityCard, ResidencePermit, OtherGovernmentDocument)

  def fromText(value: String): TravelerDocumentType =
    value.trim.toLowerCase match
      case "nationalidentitycard" | "national_identity_card" => NationalIdentityCard
      case "residencepermit" | "residence_permit" => ResidencePermit
      case "othergovernmentdocument" | "other_government_document" => OtherGovernmentDocument
      case _ => Passport

final case class TravelerType(value: String):
  override def toString: String = value

object TravelerType:
  val AdultTraveler: TravelerType = TravelerType("AdultTraveler")
  val ChildTraveler: TravelerType = TravelerType("ChildTraveler")
  val InfantTraveler: TravelerType = TravelerType("InfantTraveler")
  val all: Vector[TravelerType] = Vector(AdultTraveler, ChildTraveler, InfantTraveler)

  def fromText(value: String): TravelerType =
    value.trim.toLowerCase match
      case "childtraveler" | "child_traveler" => ChildTraveler
      case "infanttraveler" | "infant_traveler" => InfantTraveler
      case _ => AdultTraveler

  def deriveFromBirthDate(travelerBirthDate: BirthDate, evaluationDate: LocalDate): TravelerType =
    val ageInYears = java.time.Period.between(travelerBirthDate.value, evaluationDate).getYears
    if ageInYears < 2 then InfantTraveler
    else if ageInYears < 12 then ChildTraveler
    else AdultTraveler

final case class SeatPreference(value: String):
  override def toString: String = value

object SeatPreference:
  val Window: SeatPreference = SeatPreference("Window")
  val Aisle: SeatPreference = SeatPreference("Aisle")
  val Middle: SeatPreference = SeatPreference("Middle")
  val NoPreference: SeatPreference = SeatPreference("NoPreference")
  val all: Vector[SeatPreference] = Vector(Window, Aisle, Middle, NoPreference)

  def fromText(value: String): SeatPreference =
    value.trim.toLowerCase match
      case "window" => Window
      case "aisle" => Aisle
      case "middle" => Middle
      case _ => NoPreference

final case class MealPreference(value: String):
  override def toString: String = value

object MealPreference:
  val Standard: MealPreference = MealPreference("Standard")
  val Vegetarian: MealPreference = MealPreference("Vegetarian")
  val Vegan: MealPreference = MealPreference("Vegan")
  val Halal: MealPreference = MealPreference("Halal")
  val Kosher: MealPreference = MealPreference("Kosher")
  val ChildMeal: MealPreference = MealPreference("ChildMeal")
  val NoPreference: MealPreference = MealPreference("NoPreference")
  val all: Vector[MealPreference] = Vector(Standard, Vegetarian, Vegan, Halal, Kosher, ChildMeal, NoPreference)

  def fromText(value: String): MealPreference =
    value.trim.toLowerCase match
      case "vegetarian" => Vegetarian
      case "vegan" => Vegan
      case "halal" => Halal
      case "kosher" => Kosher
      case "childmeal" | "child_meal" => ChildMeal
      case "nopreference" | "no_preference" => NoPreference
      case _ => Standard

final case class TravelerIdentityDocument(
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    issuingCountryCode: CountryCode,
    expirationDate: LocalDate
):
  def isValidOn(validationDate: LocalDate): Boolean =
    !expirationDate.isBefore(validationDate)

final case class TravelerLoyaltyMembership(
    loyaltyProgramName: LoyaltyProgramName,
    loyaltyMembershipNumber: String
)

final case class TravelerEmergencyContact(
    emergencyContactName: PersonName,
    emergencyContactPhoneNumber: ContactNumber
)

final case class TravelerPreferences(
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

final case class TravelerProfile(
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
      case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
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
      case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
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
        case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
          Left(TravelerError.InvalidTravelerProfileTransition(travelerId, travelerProfileStatus, TravelerProfileStatus.Archived))
        case _ =>
          Right(copy(travelerProfileStatus = TravelerProfileStatus.Archived))

  def markAsDefaultTravelerProfile: Either[TravelerError, TravelerProfile] =
    travelerProfileStatus match
      case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
        Left(TravelerError.ArchivedTravelerProfileCannotBeDefault(travelerId))
      case _ =>
        Right(copy(isDefaultTravelerProfile = true))

  def clearDefaultTravelerProfile: TravelerProfile =
    copy(isDefaultTravelerProfile = false)

sealed trait TravelerError extends DomainError

object TravelerError:
  final case class TravelerProfileWasNotFound(travelerId: TravelerId) extends TravelerError:
    val message: String = s"Traveler profile '${travelerId.value}' was not found"

  final case class TravelerOwnerUserWasNotFound(ownerUserId: UserId) extends TravelerError:
    val message: String = s"Owner user '${ownerUserId.value}' was not found"

  final case class TravelerProfileDidNotBelongToUser(
      travelerId: TravelerId,
      actualOwnerUserId: UserId,
      requestedOwnerUserId: UserId
  ) extends TravelerError:
    val message: String =
      s"Traveler profile '${travelerId.value}' belongs to '${actualOwnerUserId.value}' instead of '${requestedOwnerUserId.value}'"

  final case class DuplicateTravelerIdentityDocument(
      travelerId: TravelerId,
      travelerDocumentNumber: DocumentNumber
  ) extends TravelerError:
    val message: String = s"Traveler profile '${travelerId.value}' already has document '${travelerDocumentNumber.value}'"

  final case class TravelerIdentityDocumentsWereMissing(travelerId: TravelerId) extends TravelerError:
    val message: String = s"Traveler profile '${travelerId.value}' cannot be verified without identity documents"

  final case class TravelerDidNotHaveValidIdentityDocument(
      travelerId: TravelerId,
      validationDate: LocalDate
  ) extends TravelerError:
    val message: String = s"Traveler profile '${travelerId.value}' has no valid identity document on $validationDate"

  final case class InvalidTravelerProfileTransition(
      travelerId: TravelerId,
      currentTravelerProfileStatus: TravelerProfileStatus,
      targetTravelerProfileStatus: TravelerProfileStatus
  ) extends TravelerError:
    val message: String =
      s"Traveler profile '${travelerId.value}' cannot transition from $currentTravelerProfileStatus to $targetTravelerProfileStatus"

  final case class ArchivedTravelerProfileCannotBeUpdated(travelerId: TravelerId) extends TravelerError:
    val message: String = s"Archived traveler profile '${travelerId.value}' cannot be updated"

  final case class ArchivedTravelerProfileCannotBeDefault(travelerId: TravelerId) extends TravelerError:
    val message: String = s"Archived traveler profile '${travelerId.value}' cannot become default"

  final case class DefaultTravelerProfileCannotBeArchived(travelerId: TravelerId) extends TravelerError:
    val message: String = s"Default traveler profile '${travelerId.value}' cannot be archived"

  final case class TravelerDocumentNumberAlreadyExists(travelerDocumentNumber: DocumentNumber) extends TravelerError:
    val message: String = s"Traveler document '${travelerDocumentNumber.value}' already exists"

  case object TravelerLoyaltyMembershipNumberWasEmpty extends TravelerError:
    val message: String = "Traveler loyalty membership number must not be empty"

  final case class TravelerAccessibilityNotesWereTooLong(actualLength: Int) extends TravelerError:
    val message: String = s"Traveler accessibility notes must be at most 500 characters but were $actualLength"

def travelerIdentityDocument(
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    issuingCountryCode: CountryCode,
    expirationDate: LocalDate
): TravelerIdentityDocument =
  TravelerIdentityDocument(travelerDocumentType, travelerDocumentNumber, issuingCountryCode, expirationDate)

def travelerLoyaltyMembership(
    loyaltyProgramName: LoyaltyProgramName,
    loyaltyMembershipNumber: String
): Either[TravelerError, TravelerLoyaltyMembership] =
  val normalizedMembershipNumber = loyaltyMembershipNumber.trim
  if normalizedMembershipNumber.nonEmpty then Right(TravelerLoyaltyMembership(loyaltyProgramName, normalizedMembershipNumber))
  else Left(TravelerError.TravelerLoyaltyMembershipNumberWasEmpty)

def unsafeTravelerLoyaltyMembership(
    loyaltyProgramName: LoyaltyProgramName,
    loyaltyMembershipNumber: String
): TravelerLoyaltyMembership =
  travelerLoyaltyMembership(loyaltyProgramName, loyaltyMembershipNumber).fold(throw _, identity)

def travelerEmergencyContact(
    emergencyContactName: PersonName,
    emergencyContactPhoneNumber: ContactNumber
): TravelerEmergencyContact =
  TravelerEmergencyContact(emergencyContactName, emergencyContactPhoneNumber)

def travelerPreferences(
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

def unsafeTravelerPreferences(
    travelerSeatPreference: SeatPreference,
    travelerMealPreference: MealPreference,
    accessibilityRequestNotes: Option[String]
): TravelerPreferences =
  travelerPreferences(travelerSeatPreference, travelerMealPreference, accessibilityRequestNotes).fold(throw _, identity)

def newTravelerProfile(
    travelerId: TravelerId,
    ownerUserId: UserId,
    travelerFullName: PersonName,
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    travelerPhoneNumber: ContactNumber,
    travelerBirthDate: BirthDate,
    travelerType: TravelerType,
    travelerPreferences: TravelerPreferences,
    travelerEmergencyContact: Option[TravelerEmergencyContact],
    isDefaultTravelerProfile: Boolean
): TravelerProfile =
  TravelerProfile(
    travelerId = travelerId,
    ownerUserId = ownerUserId,
    travelerFullName = travelerFullName,
    travelerDocumentType = travelerDocumentType,
    travelerDocumentNumber = travelerDocumentNumber,
    travelerPhoneNumber = travelerPhoneNumber,
    travelerBirthDate = travelerBirthDate,
    travelerType = travelerType,
    travelerIdentityDocuments = Nil,
    travelerEmergencyContact = travelerEmergencyContact,
    travelerLoyaltyMemberships = Nil,
    travelerPreferences = travelerPreferences,
    travelerProfileStatus = TravelerProfileStatus.Draft,
    isDefaultTravelerProfile = isDefaultTravelerProfile
  )

def restoreTravelerProfile(
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
): TravelerProfile =
  TravelerProfile(
    travelerId = travelerId,
    ownerUserId = ownerUserId,
    travelerFullName = travelerFullName,
    travelerDocumentType = travelerDocumentType,
    travelerDocumentNumber = travelerDocumentNumber,
    travelerPhoneNumber = travelerPhoneNumber,
    travelerBirthDate = travelerBirthDate,
    travelerType = travelerType,
    travelerIdentityDocuments = travelerIdentityDocuments,
    travelerEmergencyContact = travelerEmergencyContact,
    travelerLoyaltyMemberships = travelerLoyaltyMemberships,
    travelerPreferences = travelerPreferences,
    travelerProfileStatus = travelerProfileStatus,
    isDefaultTravelerProfile = isDefaultTravelerProfile
  )
