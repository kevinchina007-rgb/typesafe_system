package com.typesafe.travel.traveler.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

trait TravelerProfileService[F[_]]:
  def registerDraft(
      ownerUserId: UserId,
      fullName: PersonName,
      birthDate: BirthDate,
      preferences: TravelerPreferences,
      emergencyContact: Option[EmergencyContact]
  ): F[TravelerProfile]

  def addDocument(
      travelerId: TravelerId,
      document: IdentityDocument
  ): F[TravelerProfile]

  def verifyProfile(
      travelerId: TravelerId,
      onDate: LocalDate
  ): F[TravelerProfile]

  def archiveProfile(travelerId: TravelerId): F[TravelerProfile]

final class LiveTravelerProfileService[F[_]: MonadThrow](
    repository: TravelerProfileRepository[F]
) extends TravelerProfileService[F]:

  override def registerDraft(
      ownerUserId: UserId,
      fullName: PersonName,
      birthDate: BirthDate,
      preferences: TravelerPreferences,
      emergencyContact: Option[EmergencyContact]
  ): F[TravelerProfile] =
    repository.nextId.flatMap { travelerId =>
      repository.save(
        TravelerProfile.draft(
          id = travelerId,
          ownerUserId = ownerUserId,
          fullName = fullName,
          birthDate = birthDate,
          preferences = preferences,
          emergencyContact = emergencyContact
        )
      )
    }

  override def addDocument(
      travelerId: TravelerId,
      document: IdentityDocument
  ): F[TravelerProfile] =
    loadProfile(travelerId)
      .map(_.addDocument(document))
      .flatMap(repository.save)

  override def verifyProfile(
      travelerId: TravelerId,
      onDate: LocalDate
  ): F[TravelerProfile] =
    loadProfile(travelerId)
      .flatMap(profile => MonadThrow[F].fromEither(profile.verify(onDate)))
      .flatMap(repository.save)

  override def archiveProfile(travelerId: TravelerId): F[TravelerProfile] =
    loadProfile(travelerId)
      .flatMap(profile => MonadThrow[F].fromEither(profile.archive))
      .flatMap(repository.save)

  private def loadProfile(travelerId: TravelerId): F[TravelerProfile] =
    repository
      .findById(travelerId)
      .flatMap(_.liftTo[F](TravelerDomainError.ProfileNotFound(travelerId)))
