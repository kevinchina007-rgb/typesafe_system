package com.typesafe.travel.traveler.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.auth.domain.AuthError
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.LocalDate
import java.util.UUID

object CreateTravelerPlanner extends ConnectionApiPlan[CreateTravelerPlannerRequest, TravelerPlannerResponse]:
  override val name: String = "CreateTravelerPlanner"

  override def plan(input: CreateTravelerPlannerRequest, connection: Connection): IO[TravelerPlannerResponse] =
    for
      ownerUserId <- requireActor(input.actingUserId, input.ownerUserId)
      existingProfiles <- TravelerPlannerPlainSql.listByOwner(connection, ownerUserId)
      parsed <- parseTravelerInput(input.traveler)
      _ <- ensureDocumentAvailable(connection, parsed.documentType, parsed.documentNumber, None)
      today <- IO.delay(LocalDate.now())
      travelerId <- IO.delay(TravelerId(s"traveler-${UUID.randomUUID().toString.take(12)}"))
      shouldBecomeDefault = input.traveler.isDefaultTraveler || existingProfiles.isEmpty
      created = newTravelerProfile(
        travelerId = travelerId,
        ownerUserId = ownerUserId,
        travelerFullName = parsed.fullName,
        travelerDocumentType = parsed.documentType,
        travelerDocumentNumber = parsed.documentNumber,
        travelerPhoneNumber = parsed.phone,
        travelerBirthDate = parsed.birthDate,
        travelerType = deriveTravelerTypeFromBirthDate(parsed.birthDate, today),
        travelerPreferences = parsed.preferences,
        travelerEmergencyContact = parsed.emergencyContact,
        isDefaultTravelerProfile = shouldBecomeDefault,
        travelerGender = parsed.gender,
        travelerNationality = parsed.nationality,
        travelerDocumentExpiryDate = parsed.documentExpiryDate,
        travelerEmail = parsed.email,
        quietSeatPreferred = parsed.quietSeatPreferred,
        assistanceType = parsed.assistanceType,
        specialRequirementNote = parsed.specialRequirementNote,
        hasLargeLuggage = parsed.hasLargeLuggage,
        luggageNote = parsed.luggageNote
      )
      _ <- if shouldBecomeDefault then
        existingProfiles.traverse(profile => TravelerPlannerPlainSql.save(connection, clearTravelerProfileDefault(profile))).void
      else IO.unit
      saved <- TravelerPlannerPlainSql.save(connection, created)
      _ <- if shouldBecomeDefault then TravelerPlannerPlainSql.updateUserDefaultTraveler(connection, ownerUserId, Some(saved.travelerId)) else IO.unit
    yield travelerPlannerResponseFromDomain(saved)

object UpdateTravelerPlanner extends ConnectionApiPlan[UpdateTravelerPlannerRequest, TravelerPlannerResponse]:
  override val name: String = "UpdateTravelerPlanner"

  override def plan(input: UpdateTravelerPlannerRequest, connection: Connection): IO[TravelerPlannerResponse] =
    for
      ownerUserId <- requireActor(input.actingUserId, input.ownerUserId)
      travelerId = TravelerId(input.travelerId)
      existing <- TravelerPlannerPlainSql.findById(connection, travelerId).flatMap(_.liftTo[IO](TravelerError.TravelerProfileWasNotFound(travelerId)))
      _ <- ensureTravelerProfileOwnedBy(existing, ownerUserId).liftTo[IO]
      parsed <- parseTravelerInput(input.traveler)
      _ <- ensureDocumentAvailable(connection, parsed.documentType, parsed.documentNumber, Some(travelerId))
      today <- IO.delay(LocalDate.now())
      updated <- updateTravelerProfileDetails(
        travelerProfile = existing,
        updatedTravelerFullName = parsed.fullName,
        updatedTravelerDocumentType = parsed.documentType,
        updatedTravelerDocumentNumber = parsed.documentNumber,
        updatedTravelerPhoneNumber = parsed.phone,
        updatedTravelerBirthDate = parsed.birthDate,
        updatedTravelerType = deriveTravelerTypeFromBirthDate(parsed.birthDate, today),
        updatedTravelerPreferences = parsed.preferences,
        updatedTravelerEmergencyContact = parsed.emergencyContact,
        updatedTravelerGender = parsed.gender,
        updatedTravelerNationality = parsed.nationality,
        updatedTravelerDocumentExpiryDate = parsed.documentExpiryDate,
        updatedTravelerEmail = parsed.email,
        updatedQuietSeatPreferred = parsed.quietSeatPreferred,
        updatedAssistanceType = parsed.assistanceType,
        updatedSpecialRequirementNote = parsed.specialRequirementNote,
        updatedHasLargeLuggage = parsed.hasLargeLuggage,
        updatedLuggageNote = parsed.luggageNote
      ).liftTo[IO]
      saved <- TravelerPlannerPlainSql.save(connection, updated)
    yield travelerPlannerResponseFromDomain(saved)

object ListTravelersPlanner extends ConnectionApiPlan[ListTravelersPlannerRequest, TravelerListPlannerResponse]:
  override val name: String = "ListTravelersPlanner"

  override def plan(input: ListTravelersPlannerRequest, connection: Connection): IO[TravelerListPlannerResponse] =
    for
      ownerUserId <- requireActor(input.actingUserId, input.ownerUserId)
      travelers <- TravelerPlannerPlainSql.listByOwner(connection, ownerUserId)
    yield TravelerListPlannerResponse(travelers.map(travelerPlannerResponseFromDomain))

object DeleteTravelerPlanner extends ConnectionApiPlan[DeleteTravelerPlannerRequest, TravelerDeletedPlannerResponse]:
  override val name: String = "DeleteTravelerPlanner"

  override def plan(input: DeleteTravelerPlannerRequest, connection: Connection): IO[TravelerDeletedPlannerResponse] =
    for
      ownerUserId <- requireActor(input.actingUserId, input.ownerUserId)
      travelerId = TravelerId(input.travelerId)
      traveler <- TravelerPlannerPlainSql.findById(connection, travelerId).flatMap(_.liftTo[IO](TravelerError.TravelerProfileWasNotFound(travelerId)))
      _ <- ensureTravelerProfileOwnedBy(traveler, ownerUserId).liftTo[IO]
      existingProfiles <- TravelerPlannerPlainSql.listByOwner(connection, ownerUserId)
      _ <- TravelerPlannerPlainSql.deleteById(connection, travelerId)
      remainingProfiles = existingProfiles.filterNot(_.travelerId == travelerId)
      _ <- if traveler.isDefaultTravelerProfile then
        remainingProfiles match
          case nextDefault :: rest =>
            for
              promoted <- markTravelerProfileAsDefault(nextDefault).liftTo[IO]
              _ <- TravelerPlannerPlainSql.save(connection, promoted)
              _ <- rest.traverse(profile => TravelerPlannerPlainSql.save(connection, clearTravelerProfileDefault(profile))).void
              _ <- TravelerPlannerPlainSql.updateUserDefaultTraveler(connection, ownerUserId, Some(promoted.travelerId))
            yield ()
          case Nil =>
            TravelerPlannerPlainSql.updateUserDefaultTraveler(connection, ownerUserId, None)
      else IO.unit
    yield TravelerDeletedPlannerResponse(deleted = true)

private final case class ParsedTravelerInput(
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

private def requireActor(actingUserIdValue: String, ownerUserIdValue: String): IO[UserId] =
  val actingUserId = UserId(actingUserIdValue)
  val ownerUserId = UserId(ownerUserIdValue)
  if actingUserId == ownerUserId then IO.pure(ownerUserId)
  else IO.raiseError(AuthError.SessionActorDidNotMatch)

private def parseTravelerInput(input: TravelerProfileInput): IO[ParsedTravelerInput] =
  for
    fullName <- PersonName.create(input.fullName).liftTo[IO]
    documentNumber <- DocumentNumber.create(input.documentNumber).liftTo[IO]
    phone <- ContactNumber.create(input.phone).liftTo[IO]
    birthDate <- IO.delay(LocalDate.parse(input.birthDate)).flatMap(date => BirthDate.create(date, LocalDate.now()).liftTo[IO])
    basicInfo = input.basicInfo.getOrElse(TravelerBasicInfo(input.fullName, "unspecified", input.birthDate, "China"))
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
    gender = normalizeOptionalText(basicInfo.gender, "unspecified"),
    nationality = normalizeOptionalText(basicInfo.nationality, "China"),
    documentExpiryDate = documentExpiryDate,
    email = contactInfo.email.map(_.trim).filter(_.nonEmpty),
    quietSeatPreferred = preferenceInfo.quietSeatPreferred,
    assistanceType = normalizeOptionalText(specialRequirementInfo.assistanceType, "none"),
    specialRequirementNote = specialRequirementInfo.requirementNote.map(_.trim).filter(_.nonEmpty),
    hasLargeLuggage = specialRequirementInfo.hasLargeLuggage,
    luggageNote = specialRequirementInfo.luggageNote.map(_.trim).filter(_.nonEmpty)
  )

private def normalizeOptionalText(value: String, fallback: String): String =
  val normalized = value.trim
  if normalized.nonEmpty then normalized else fallback

private def ensureDocumentAvailable(
    connection: Connection,
    documentType: TravelerDocumentType,
    documentNumber: DocumentNumber,
    currentTravelerId: Option[TravelerId]
): IO[Unit] =
  TravelerPlannerPlainSql.findByDocument(connection, documentType, documentNumber).flatMap { matches =>
    val conflicts = matches.filterNot(profile => currentTravelerId.contains(profile.travelerId))
    if conflicts.isEmpty then IO.unit
    else IO.raiseError(TravelerError.TravelerDocumentNumberAlreadyExists(documentNumber))
  }
