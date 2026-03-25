package com.typesafe.travel.traveler.domain

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.identity.domain.*
import com.typesafe.travel.shared.kernel.*
import java.time.LocalDate

trait TravelerProfileService[F[_]]:
  def createTravelerProfile(
      ownerUserId: UserId,
      travelerFullName: PersonName,
      travelerDocumentType: TravelerDocumentType,
      travelerDocumentNumber: DocumentNumber,
      travelerPhoneNumber: ContactNumber,
      travelerBirthDate: BirthDate,
      travelerPreferences: TravelerPreferences,
      travelerEmergencyContact: Option[TravelerEmergencyContact],
      requestedDefaultTravelerProfile: Boolean
  ): F[TravelerProfile]

  def updateTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId,
      travelerFullName: PersonName,
      travelerDocumentType: TravelerDocumentType,
      travelerDocumentNumber: DocumentNumber,
      travelerPhoneNumber: ContactNumber,
      travelerBirthDate: BirthDate,
      travelerPreferences: TravelerPreferences,
      travelerEmergencyContact: Option[TravelerEmergencyContact]
  ): F[TravelerProfile]

  def addTravelerIdentityDocument(
      ownerUserId: UserId,
      travelerId: TravelerId,
      travelerIdentityDocument: TravelerIdentityDocument
  ): F[TravelerProfile]

  def verifyTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId,
      validationDate: LocalDate
  ): F[TravelerProfile]

  def archiveTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId
  ): F[TravelerProfile]

  def setDefaultTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId
  ): F[TravelerProfile]

final class LiveTravelerProfileService[F[_]: MonadThrow](
    travelerProfileRepository: TravelerProfileRepository[F],
    userRepository: UserRepository[F]
) extends TravelerProfileService[F]:

  override def createTravelerProfile(
      ownerUserId: UserId,
      travelerFullName: PersonName,
      travelerDocumentType: TravelerDocumentType,
      travelerDocumentNumber: DocumentNumber,
      travelerPhoneNumber: ContactNumber,
      travelerBirthDate: BirthDate,
      travelerPreferences: TravelerPreferences,
      travelerEmergencyContact: Option[TravelerEmergencyContact],
      requestedDefaultTravelerProfile: Boolean
  ): F[TravelerProfile] =
    for
      ownerUser <- loadOwnerUser(ownerUserId)
      existingTravelerProfiles <- travelerProfileRepository.findTravelerProfilesByOwnerUserId(ownerUserId)
      travelerId <- travelerProfileRepository.nextTravelerId
      shouldBecomeDefaultTravelerProfile = requestedDefaultTravelerProfile || existingTravelerProfiles.isEmpty
      createdTravelerProfile = TravelerProfile.createTravelerProfile(
        travelerId = travelerId,
        ownerUserId = ownerUser.userId,
        travelerFullName = travelerFullName,
        travelerDocumentType = travelerDocumentType,
        travelerDocumentNumber = travelerDocumentNumber,
        travelerPhoneNumber = travelerPhoneNumber,
        travelerBirthDate = travelerBirthDate,
        travelerType = TravelerType.deriveFromBirthDate(travelerBirthDate, LocalDate.now()),
        travelerPreferences = travelerPreferences,
        travelerEmergencyContact = travelerEmergencyContact,
        isDefaultTravelerProfile = shouldBecomeDefaultTravelerProfile
      )
      _ <- saveAdjustedDefaultTravelerProfiles(ownerUserId, existingTravelerProfiles, createdTravelerProfile, shouldBecomeDefaultTravelerProfile)
      _ <- updateOwnerDefaultTraveler(ownerUser, createdTravelerProfile, shouldBecomeDefaultTravelerProfile)
    yield createdTravelerProfile

  override def updateTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId,
      travelerFullName: PersonName,
      travelerDocumentType: TravelerDocumentType,
      travelerDocumentNumber: DocumentNumber,
      travelerPhoneNumber: ContactNumber,
      travelerBirthDate: BirthDate,
      travelerPreferences: TravelerPreferences,
      travelerEmergencyContact: Option[TravelerEmergencyContact]
  ): F[TravelerProfile] =
    loadTravelerProfile(ownerUserId, travelerId)
      .flatMap(
        _.updateTravelerProfile(
          updatedTravelerFullName = travelerFullName,
          updatedTravelerDocumentType = travelerDocumentType,
          updatedTravelerDocumentNumber = travelerDocumentNumber,
          updatedTravelerPhoneNumber = travelerPhoneNumber,
          updatedTravelerBirthDate = travelerBirthDate,
          updatedTravelerType = TravelerType.deriveFromBirthDate(travelerBirthDate, LocalDate.now()),
          updatedTravelerPreferences = travelerPreferences,
          updatedTravelerEmergencyContact = travelerEmergencyContact
        ).liftTo[F]
      )
      .flatMap(travelerProfileRepository.saveTravelerProfile)

  override def addTravelerIdentityDocument(
      ownerUserId: UserId,
      travelerId: TravelerId,
      travelerIdentityDocument: TravelerIdentityDocument
  ): F[TravelerProfile] =
    loadTravelerProfile(ownerUserId, travelerId)
      .flatMap(_.addTravelerIdentityDocument(travelerIdentityDocument).liftTo[F])
      .flatMap(travelerProfileRepository.saveTravelerProfile)

  override def verifyTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId,
      validationDate: LocalDate
  ): F[TravelerProfile] =
    loadTravelerProfile(ownerUserId, travelerId)
      .flatMap(_.verifyTravelerProfile(validationDate).liftTo[F])
      .flatMap(travelerProfileRepository.saveTravelerProfile)

  override def archiveTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId
  ): F[TravelerProfile] =
    loadTravelerProfile(ownerUserId, travelerId)
      .flatMap(_.archiveTravelerProfile.liftTo[F])
      .flatMap(travelerProfileRepository.saveTravelerProfile)

  override def setDefaultTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId
  ): F[TravelerProfile] =
    for
      ownerUser <- loadOwnerUser(ownerUserId)
      ownerTravelerProfiles <- travelerProfileRepository.findTravelerProfilesByOwnerUserId(ownerUserId)
      selectedTravelerProfile <- loadTravelerProfile(ownerUserId, travelerId)
      defaultTravelerProfile <- selectedTravelerProfile.markAsDefaultTravelerProfile.liftTo[F]
      _ <- ownerTravelerProfiles
        .filterNot(_.travelerId == travelerId)
        .traverse(travelerProfile => travelerProfileRepository.saveTravelerProfile(travelerProfile.clearDefaultTravelerProfile))
      savedDefaultTravelerProfile <- travelerProfileRepository.saveTravelerProfile(defaultTravelerProfile)
      updatedOwnerUser <- ownerUser.assignDefaultTravelerProfile(travelerId).liftTo[F]
      _ <- userRepository.saveUser(updatedOwnerUser)
    yield savedDefaultTravelerProfile

  private def saveAdjustedDefaultTravelerProfiles(
      ownerUserId: UserId,
      existingTravelerProfiles: List[TravelerProfile],
      createdTravelerProfile: TravelerProfile,
      shouldBecomeDefaultTravelerProfile: Boolean
  ): F[Unit] =
    val profilesToClear = if shouldBecomeDefaultTravelerProfile then existingTravelerProfiles else Nil
    profilesToClear
      .traverse(travelerProfile => travelerProfileRepository.saveTravelerProfile(travelerProfile.clearDefaultTravelerProfile))
      .flatMap(_ => travelerProfileRepository.saveTravelerProfile(createdTravelerProfile).void)

  private def updateOwnerDefaultTraveler(
      ownerUser: User,
      createdTravelerProfile: TravelerProfile,
      shouldBecomeDefaultTravelerProfile: Boolean
  ): F[Unit] =
    if shouldBecomeDefaultTravelerProfile then
      ownerUser
        .assignDefaultTravelerProfile(createdTravelerProfile.travelerId)
        .liftTo[F]
        .flatMap(userRepository.saveUser)
        .void
    else
      MonadThrow[F].unit

  private def loadOwnerUser(ownerUserId: UserId): F[User] =
    userRepository
      .findByUserId(ownerUserId)
      .flatMap(_.liftTo[F](TravelerError.TravelerOwnerUserWasNotFound(ownerUserId)))

  private def loadTravelerProfile(
      ownerUserId: UserId,
      travelerId: TravelerId
  ): F[TravelerProfile] =
    travelerProfileRepository
      .findTravelerProfileById(travelerId)
      .flatMap(_.liftTo[F](TravelerError.TravelerProfileWasNotFound(travelerId)))
      .flatMap(_.ensureOwnedBy(ownerUserId).liftTo[F])
