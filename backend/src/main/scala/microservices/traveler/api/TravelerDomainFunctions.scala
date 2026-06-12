// TravelerDomainFunctions 定义旅客模块的领域辅助函数。

package com.typesafe.travel.traveler.domain

import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

def deriveTravelerTypeFromBirthDate(travelerBirthDate: BirthDate, evaluationDate: LocalDate): TravelerType =
  val ageInYears = java.time.Period.between(travelerBirthDate.value, evaluationDate).getYears
  if ageInYears < 2 then TravelerType.InfantTraveler
  else if ageInYears < 12 then TravelerType.ChildTraveler
  else TravelerType.AdultTraveler

def travelerIdentityDocumentIsValidOn(travelerIdentityDocument: TravelerIdentityDocument, validationDate: LocalDate): Boolean =
  !travelerIdentityDocument.expirationDate.isBefore(validationDate)

def ensureTravelerProfileOwnedBy(
    travelerProfile: TravelerProfile,
    ownerUserIdToCheck: UserId
): Either[TravelerError, TravelerProfile] =
  if travelerProfile.ownerUserId == ownerUserIdToCheck then Right(travelerProfile)
  else Left(TravelerError.TravelerProfileDidNotBelongToUser(travelerProfile.travelerId, travelerProfile.ownerUserId, ownerUserIdToCheck))

def updateTravelerProfileDetails(
    travelerProfile: TravelerProfile,
    updatedTravelerFullName: PersonName,
    updatedTravelerDocumentType: TravelerDocumentType,
    updatedTravelerDocumentNumber: DocumentNumber,
    updatedTravelerPhoneNumber: ContactNumber,
    updatedTravelerBirthDate: BirthDate,
    updatedTravelerType: TravelerType,
    updatedTravelerPreferences: TravelerPreferences,
    updatedTravelerEmergencyContact: Option[TravelerEmergencyContact],
    updatedTravelerGender: String = "未填写",
    updatedTravelerNationality: String = "中国",
    updatedTravelerDocumentExpiryDate: Option[LocalDate] = None,
    updatedTravelerEmail: Option[String] = None,
    updatedQuietSeatPreferred: Boolean = false,
    updatedAssistanceType: String = "无",
    updatedSpecialRequirementNote: Option[String] = None,
    updatedHasLargeLuggage: Boolean = false,
    updatedLuggageNote: Option[String] = None
): Either[TravelerError, TravelerProfile] =
  travelerProfile.travelerProfileStatus match
    case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
      Left(TravelerError.ArchivedTravelerProfileCannotBeUpdated(travelerProfile.travelerId))
    case _ =>
      Right(
        travelerProfile.copy(
          travelerFullName = updatedTravelerFullName,
          travelerDocumentType = updatedTravelerDocumentType,
          travelerDocumentNumber = updatedTravelerDocumentNumber,
          travelerPhoneNumber = updatedTravelerPhoneNumber,
          travelerBirthDate = updatedTravelerBirthDate,
          travelerType = updatedTravelerType,
          travelerPreferences = updatedTravelerPreferences,
          travelerEmergencyContact = updatedTravelerEmergencyContact,
          travelerGender = updatedTravelerGender,
          travelerNationality = updatedTravelerNationality,
          travelerDocumentExpiryDate = updatedTravelerDocumentExpiryDate,
          travelerEmail = updatedTravelerEmail,
          quietSeatPreferred = updatedQuietSeatPreferred,
          assistanceType = updatedAssistanceType,
          specialRequirementNote = updatedSpecialRequirementNote,
          hasLargeLuggage = updatedHasLargeLuggage,
          luggageNote = updatedLuggageNote
        )
      )

def addTravelerIdentityDocumentToProfile(
    travelerProfile: TravelerProfile,
    travelerIdentityDocument: TravelerIdentityDocument
): Either[TravelerError, TravelerProfile] =
  if travelerProfile.travelerIdentityDocuments.exists(_.travelerDocumentNumber == travelerIdentityDocument.travelerDocumentNumber) then
    Left(TravelerError.DuplicateTravelerIdentityDocument(travelerProfile.travelerId, travelerIdentityDocument.travelerDocumentNumber))
  else
    Right(travelerProfile.copy(travelerIdentityDocuments = travelerProfile.travelerIdentityDocuments :+ travelerIdentityDocument))

def verifyTravelerProfile(
    travelerProfile: TravelerProfile,
    validationDate: LocalDate
): Either[TravelerError, TravelerProfile] =
  travelerProfile.travelerProfileStatus match
    case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
      Left(TravelerError.InvalidTravelerProfileTransition(travelerProfile.travelerId, travelerProfile.travelerProfileStatus, TravelerProfileStatus.Verified))
    case _ if travelerProfile.travelerIdentityDocuments.isEmpty =>
      Left(TravelerError.TravelerIdentityDocumentsWereMissing(travelerProfile.travelerId))
    case _ if travelerProfile.travelerIdentityDocuments.exists(travelerIdentityDocumentIsValidOn(_, validationDate)) =>
      Right(travelerProfile.copy(travelerProfileStatus = TravelerProfileStatus.Verified))
    case _ =>
      Left(TravelerError.TravelerDidNotHaveValidIdentityDocument(travelerProfile.travelerId, validationDate))

def archiveTravelerProfile(travelerProfile: TravelerProfile): Either[TravelerError, TravelerProfile] =
  if travelerProfile.isDefaultTravelerProfile then
    Left(TravelerError.DefaultTravelerProfileCannotBeArchived(travelerProfile.travelerId))
  else
    travelerProfile.travelerProfileStatus match
      case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
        Left(TravelerError.InvalidTravelerProfileTransition(travelerProfile.travelerId, travelerProfile.travelerProfileStatus, TravelerProfileStatus.Archived))
      case _ =>
        Right(travelerProfile.copy(travelerProfileStatus = TravelerProfileStatus.Archived))

def markTravelerProfileAsDefault(travelerProfile: TravelerProfile): Either[TravelerError, TravelerProfile] =
  travelerProfile.travelerProfileStatus match
    case currentStatus if currentStatus == TravelerProfileStatus.Archived =>
      Left(TravelerError.ArchivedTravelerProfileCannotBeDefault(travelerProfile.travelerId))
    case _ =>
      Right(travelerProfile.copy(isDefaultTravelerProfile = true))

def clearTravelerProfileDefault(travelerProfile: TravelerProfile): TravelerProfile =
  travelerProfile.copy(isDefaultTravelerProfile = false)

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
    isDefaultTravelerProfile = isDefaultTravelerProfile,
    travelerGender = travelerGender,
    travelerNationality = travelerNationality,
    travelerDocumentExpiryDate = travelerDocumentExpiryDate,
    travelerEmail = travelerEmail,
    quietSeatPreferred = quietSeatPreferred,
    assistanceType = assistanceType,
    specialRequirementNote = specialRequirementNote,
    hasLargeLuggage = hasLargeLuggage,
    luggageNote = luggageNote
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
    isDefaultTravelerProfile = isDefaultTravelerProfile,
    travelerGender = travelerGender,
    travelerNationality = travelerNationality,
    travelerDocumentExpiryDate = travelerDocumentExpiryDate,
    travelerEmail = travelerEmail,
    quietSeatPreferred = quietSeatPreferred,
    assistanceType = assistanceType,
    specialRequirementNote = specialRequirementNote,
    hasLargeLuggage = hasLargeLuggage,
    luggageNote = luggageNote
  )

def travelerPlannerResponseFromDomain(travelerProfile: TravelerProfile): TravelerPlannerResponse =
  val basicInfo = TravelerBasicInfo(
    fullName = travelerProfile.travelerFullName.value,
    gender = travelerProfile.travelerGender,
    birthDate = travelerProfile.travelerBirthDate.value.toString,
    nationality = travelerProfile.travelerNationality
  )
  val documentInfo = TravelerDocumentInfo(
    documentType = travelerProfile.travelerDocumentType.toString,
    documentNumber = travelerProfile.travelerDocumentNumber.value,
    documentExpiryDate = travelerProfile.travelerDocumentExpiryDate.map(_.toString)
  )
  val contactInfo = TravelerContactInfo(
    phone = travelerProfile.travelerPhoneNumber.value,
    email = travelerProfile.travelerEmail
  )
  val preferenceInfo = TravelerPreferenceInfo(
    seatPreference = travelerProfile.travelerPreferences.travelerSeatPreference.toString,
    mealPreference = travelerProfile.travelerPreferences.travelerMealPreference.toString,
    quietSeatPreferred = travelerProfile.quietSeatPreferred
  )
  val specialRequirementInfo = TravelerSpecialRequirementInfo(
    assistanceType = travelerProfile.assistanceType,
    requirementNote = travelerProfile.specialRequirementNote.orElse(travelerProfile.travelerPreferences.accessibilityRequestNotes),
    hasLargeLuggage = travelerProfile.hasLargeLuggage,
    luggageNote = travelerProfile.luggageNote
  )
  val age = Some(java.time.Period.between(travelerProfile.travelerBirthDate.value, LocalDate.now()).getYears)
  val requirementLabel =
    List(
      Option.when(specialRequirementInfo.assistanceType != "无")(specialRequirementInfo.assistanceType),
      specialRequirementInfo.requirementNote,
      Option.when(specialRequirementInfo.hasLargeLuggage)("大件行李"),
      specialRequirementInfo.luggageNote
    ).flatten.mkString("，")
  val serviceSummary = TravelerServiceSummary(
    age = age,
    documentLabel = s"${documentInfo.documentType} ${documentInfo.documentNumber}",
    contactLabel = List(Some(contactInfo.phone), contactInfo.email).flatten.mkString(" / "),
    preferenceLabel = List(preferenceInfo.seatPreference, preferenceInfo.mealPreference, Option.when(preferenceInfo.quietSeatPreferred)("安静座位").getOrElse("")).filter(_.nonEmpty).mkString(" / "),
    requirementLabel = if requirementLabel.nonEmpty then requirementLabel else "无特殊要求",
    warningLevel = if specialRequirementInfo.assistanceType != "无" || specialRequirementInfo.requirementNote.exists(_.trim.nonEmpty) then "attention" else "normal"
  )
  TravelerPlannerResponse(
    travelerId = travelerProfile.travelerId.value,
    ownerUserId = travelerProfile.ownerUserId.value,
    fullName = travelerProfile.travelerFullName.value,
    documentType = travelerProfile.travelerDocumentType.toString,
    documentNumber = travelerProfile.travelerDocumentNumber.value,
    phone = travelerProfile.travelerPhoneNumber.value,
    birthDate = travelerProfile.travelerBirthDate.value.toString,
    travelerType = travelerProfile.travelerType.toString,
    status = travelerProfile.travelerProfileStatus.toString,
    isHidden = travelerProfile.travelerProfileStatus == TravelerProfileStatus.Archived,
    isDefault = travelerProfile.isDefaultTravelerProfile,
    basicInfo = basicInfo,
    documentInfo = documentInfo,
    contactInfo = contactInfo,
    preferenceInfo = preferenceInfo,
    specialRequirementInfo = specialRequirementInfo,
    serviceSummary = serviceSummary
  )
