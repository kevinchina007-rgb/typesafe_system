// TravelerPlannerSupport 定义旅客模块的业务入口。

package com.typesafe.travel.traveler.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.auth.domain.AuthError
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.LocalDate

final case class ParsedTravelerInput(
    fullName: PersonName,
    documentType: TravelerDocumentType,
    documentNumber: DocumentNumber,
    phone: ContactNumber,
    birthDate: BirthDate,
    preferences: TravelerPreferences,
    emergencyContact: Option[TravelerEmergencyContact],
    gender: String,
    nationality: String,
    documentExpiryDate: Option[LocalDate],
    email: Option[String],
    quietSeatPreferred: Boolean,
    assistanceType: String,
    specialRequirementNote: Option[String],
    hasLargeLuggage: Boolean,
    luggageNote: Option[String]
)

def requireActor(actingUserIdValue: String, ownerUserIdValue: String): IO[UserId] =
  val actingUserId = UserId(actingUserIdValue)
  val ownerUserId = UserId(ownerUserIdValue)
  if actingUserId == ownerUserId then IO.pure(ownerUserId)
  else IO.raiseError(AuthError.SessionActorDidNotMatch)

def parseTravelerInput(input: TravelerProfileInput): IO[ParsedTravelerInput] =
  for
    fullName <- PersonName.create(input.fullName).liftTo[IO]
    documentNumber <- DocumentNumber.create(input.documentNumber).liftTo[IO]
    phone <- ContactNumber.create(input.phone).liftTo[IO]
    birthDate <- IO.delay(LocalDate.parse(input.birthDate)).flatMap(date => BirthDate.create(date, LocalDate.now()).liftTo[IO])
    basicInfo = input.basicInfo.getOrElse(TravelerBasicInfo(input.fullName, "unknown", input.birthDate, "China"))
    documentInfo = input.documentInfo.getOrElse(TravelerDocumentInfo(input.documentType, input.documentNumber, None))
    contactInfo = input.contactInfo.getOrElse(TravelerContactInfo(input.phone, None))
    preferenceInfo = input.preferenceInfo.getOrElse(TravelerPreferenceInfo(input.seatPreference, input.mealPreference, quietSeatPreferred = false))
    specialRequirementInfo = input.specialRequirementInfo.getOrElse(
      TravelerSpecialRequirementInfo("none", input.accessibilityRequestNotes, hasLargeLuggage = false, None)
    )
    documentExpiryDate <- documentInfo.documentExpiryDate match
      case Some(value) if value.trim.nonEmpty => IO.delay(Some(LocalDate.parse(value)))
      case _ => IO.pure(None)
    preferences <- travelerPreferences(
      travelerSeatPreference = SeatPreference.fromText(preferenceInfo.seatPreference),
      travelerMealPreference = MealPreference.fromText(preferenceInfo.mealPreference),
      accessibilityRequestNotes = specialRequirementInfo.requirementNote.orElse(input.accessibilityRequestNotes)
    ).liftTo[IO]
    emergencyContact <- (input.emergencyContactName, input.emergencyContactPhoneNumber) match
      case (Some(name), Some(phoneNumber)) =>
        for
          contactName <- PersonName.create(name).liftTo[IO]
          contactPhone <- ContactNumber.create(phoneNumber).liftTo[IO]
        yield Some(travelerEmergencyContact(contactName, contactPhone))
      case _ => IO.pure(None)
  yield ParsedTravelerInput(
    fullName = fullName,
    documentType = TravelerDocumentType.fromText(input.documentType),
    documentNumber = documentNumber,
    phone = phone,
    birthDate = birthDate,
    preferences = preferences,
    emergencyContact = emergencyContact,
    gender = normalizeOptionalText(basicInfo.gender, "unknown"),
    nationality = normalizeOptionalText(basicInfo.nationality, "China"),
    documentExpiryDate = documentExpiryDate,
    email = contactInfo.email.map(_.trim).filter(_.nonEmpty),
    quietSeatPreferred = preferenceInfo.quietSeatPreferred,
    assistanceType = normalizeOptionalText(specialRequirementInfo.assistanceType, "none"),
    specialRequirementNote = specialRequirementInfo.requirementNote.map(_.trim).filter(_.nonEmpty),
    hasLargeLuggage = specialRequirementInfo.hasLargeLuggage,
    luggageNote = specialRequirementInfo.luggageNote.map(_.trim).filter(_.nonEmpty)
  )

def normalizeOptionalText(value: String, fallback: String): String =
  val normalized = value.trim
  if normalized.nonEmpty then normalized else fallback

def ensureDocumentAvailable(
    connection: Connection,
    documentType: TravelerDocumentType,
    documentNumber: DocumentNumber,
    currentTravelerId: Option[TravelerId]
): IO[Unit] =
  TravelerPlannerPlainSql.findByDocument(connection, documentType, documentNumber).flatMap { matches =>
    val conflicts = matches
      .filterNot(profile => currentTravelerId.contains(profile.travelerId))
      .filterNot(_.travelerProfileStatus == TravelerProfileStatus.Archived)
    if conflicts.isEmpty then IO.unit
    else IO.raiseError(TravelerError.TravelerDocumentNumberAlreadyExists(documentNumber))
  }
