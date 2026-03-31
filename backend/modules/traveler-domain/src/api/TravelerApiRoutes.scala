package com.typesafe.travel.api.routes

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.api.*
import com.typesafe.travel.api.dto.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

import java.time.LocalDate

trait TravelerApiRoutes[F[_]: Async] extends Http4sDsl[F]:
  this: ApiRouter[F] =>

  import JsonCodecs.given

  protected final def travelerRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "api" / "users" / userIdValue / "travelers" =>
      for
        createTravelerRequestDto <- request.as[CreateTravelerRequestDto]
        travelerFullName <- fromEither(PersonName.create(createTravelerRequestDto.fullName))
        travelerDocumentNumber <- fromEither(DocumentNumber.create(createTravelerRequestDto.documentNumber))
        travelerPhoneNumber <- fromEither(ContactNumber.create(createTravelerRequestDto.phone))
        currentDate <- currentLocalDateF
        travelerBirthDate <- fromEither(BirthDate.create(LocalDate.parse(createTravelerRequestDto.birthDate), currentDate))
        travelerPreferences <- fromEither(
          TravelerPreferences.create(
            travelerSeatPreference = TravelerDtoMappers.toSeatPreference(createTravelerRequestDto.seatPreference),
            travelerMealPreference = TravelerDtoMappers.toMealPreference(createTravelerRequestDto.mealPreference),
            accessibilityRequestNotes = createTravelerRequestDto.accessibilityRequestNotes
          )
        )
        travelerEmergencyContact <- createTravelerEmergencyContact(createTravelerRequestDto)
        createdTravelerProfile <- travelerProfileService.createTravelerProfile(
          ownerUserId = UserId(userIdValue),
          travelerFullName = travelerFullName,
          travelerDocumentType = TravelerDtoMappers.toTravelerDocumentType(createTravelerRequestDto.documentType),
          travelerDocumentNumber = travelerDocumentNumber,
          travelerPhoneNumber = travelerPhoneNumber,
          travelerBirthDate = travelerBirthDate,
          travelerPreferences = travelerPreferences,
          travelerEmergencyContact = travelerEmergencyContact,
          requestedDefaultTravelerProfile = createTravelerRequestDto.isDefaultTraveler
        )
        response <- Created(TravelerResponseDto.fromDomain(createdTravelerProfile).asJson)
      yield response

    case GET -> Root / "api" / "users" / userIdValue / "travelers" =>
      travelerProfileRepository
        .findTravelerProfilesByOwnerUserId(UserId(userIdValue))
        .flatMap(travelers => Ok(TravelerListResponseDto(travelers.map(TravelerResponseDto.fromDomain)).asJson))

    case DELETE -> Root / "api" / "users" / userIdValue / "travelers" / travelerIdValue =>
      travelerProfileService
        .deleteTravelerProfile(UserId(userIdValue), TravelerId(travelerIdValue))
        .flatMap(_ => NoContent())

    case request @ PUT -> Root / "api" / "users" / userIdValue / "travelers" / travelerIdValue =>
      for
        updateTravelerRequestDto <- request.as[CreateTravelerRequestDto]
        travelerFullName <- fromEither(PersonName.create(updateTravelerRequestDto.fullName))
        travelerDocumentNumber <- fromEither(DocumentNumber.create(updateTravelerRequestDto.documentNumber))
        travelerPhoneNumber <- fromEither(ContactNumber.create(updateTravelerRequestDto.phone))
        currentDate <- currentLocalDateF
        travelerBirthDate <- fromEither(BirthDate.create(LocalDate.parse(updateTravelerRequestDto.birthDate), currentDate))
        travelerPreferences <- fromEither(
          TravelerPreferences.create(
            travelerSeatPreference = TravelerDtoMappers.toSeatPreference(updateTravelerRequestDto.seatPreference),
            travelerMealPreference = TravelerDtoMappers.toMealPreference(updateTravelerRequestDto.mealPreference),
            accessibilityRequestNotes = updateTravelerRequestDto.accessibilityRequestNotes
          )
        )
        travelerEmergencyContact <- createTravelerEmergencyContact(updateTravelerRequestDto)
        updatedTravelerProfile <- travelerProfileService.updateTravelerProfile(
          ownerUserId = UserId(userIdValue),
          travelerId = TravelerId(travelerIdValue),
          travelerFullName = travelerFullName,
          travelerDocumentType = TravelerDtoMappers.toTravelerDocumentType(updateTravelerRequestDto.documentType),
          travelerDocumentNumber = travelerDocumentNumber,
          travelerPhoneNumber = travelerPhoneNumber,
          travelerBirthDate = travelerBirthDate,
          travelerPreferences = travelerPreferences,
          travelerEmergencyContact = travelerEmergencyContact
        )
        response <- Ok(TravelerResponseDto.fromDomain(updatedTravelerProfile).asJson)
      yield response
  }
