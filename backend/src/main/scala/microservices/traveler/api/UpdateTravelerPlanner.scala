// UpdateTravelerPlanner 是旅客模块的更新入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.traveler.domain

import cats.effect.IO
import cats.syntax.all.*
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.shared.kernel.*

import java.sql.Connection
import java.time.LocalDate

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
