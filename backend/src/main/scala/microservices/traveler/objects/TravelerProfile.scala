package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.time.LocalDate
import TravelerProfileSourceJsonCodecs.given

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

  given sourceEncoder: Encoder[TravelerProfileStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TravelerProfileStatus] = Decoder.decodeString.map(fromText)

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
      case "passport" => Passport
      case "identity-card" | "identity_card" | "nationalidentitycard" | "national_identity_card" => NationalIdentityCard
      case "residence-permit" | "residence_permit" | "residencepermit" => ResidencePermit
      case "other" | "other-government-document" | "other_government_document" | "othergovernmentdocument" => OtherGovernmentDocument
      case _ => Passport

  given sourceEncoder: Encoder[TravelerDocumentType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TravelerDocumentType] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[TravelerType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TravelerType] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[SeatPreference] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[SeatPreference] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[MealPreference] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[MealPreference] = Decoder.decodeString.map(fromText)

final case class TravelerIdentityDocument(
    travelerDocumentType: TravelerDocumentType,
    travelerDocumentNumber: DocumentNumber,
    issuingCountryCode: CountryCode,
    expirationDate: LocalDate
)

object TravelerIdentityDocument:
  given sourceEncoder: Encoder[TravelerIdentityDocument] = deriveEncoder
  given sourceDecoder: Decoder[TravelerIdentityDocument] = deriveDecoder

final case class TravelerLoyaltyMembership(
    loyaltyProgramName: LoyaltyProgramName,
    loyaltyMembershipNumber: String
)

object TravelerLoyaltyMembership:
  given sourceEncoder: Encoder[TravelerLoyaltyMembership] = deriveEncoder
  given sourceDecoder: Decoder[TravelerLoyaltyMembership] = deriveDecoder

final case class TravelerEmergencyContact(
    emergencyContactName: PersonName,
    emergencyContactPhoneNumber: ContactNumber
)

object TravelerEmergencyContact:
  given sourceEncoder: Encoder[TravelerEmergencyContact] = deriveEncoder
  given sourceDecoder: Decoder[TravelerEmergencyContact] = deriveDecoder

final case class TravelerPreferences(
    travelerSeatPreference: SeatPreference,
    travelerMealPreference: MealPreference,
    accessibilityRequestNotes: Option[String]
)

object TravelerPreferences:
  given sourceEncoder: Encoder[TravelerPreferences] = deriveEncoder
  given sourceDecoder: Decoder[TravelerPreferences] = deriveDecoder

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
    isDefaultTravelerProfile: Boolean,
    travelerGender: String = "未填写",
    travelerNationality: String = "中国",
    travelerDocumentExpiryDate: Option[LocalDate] = None,
    travelerEmail: Option[String] = None,
    quietSeatPreferred: Boolean = false,
    assistanceType: String = "无",
    specialRequirementNote: Option[String] = None,
    hasLargeLuggage: Boolean = false,
    luggageNote: Option[String] = None
)

object TravelerProfile:
  given sourceEncoder: Encoder[TravelerProfile] = deriveEncoder
  given sourceDecoder: Decoder[TravelerProfile] = deriveDecoder

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
