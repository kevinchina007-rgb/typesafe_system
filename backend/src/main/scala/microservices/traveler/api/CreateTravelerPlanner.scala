// CreateTravelerPlanner 是旅客模块的创建入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.traveler.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
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
