package com.typesafe.travel.persistence.traveler

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import doobie.*
import doobie.implicits.*

import java.time.LocalDate
import java.util.UUID

final class DoobieTravelerProfileRepository[F[_]: Async](
    transactor: Transactor[F]
) extends TravelerProfileRepository[F]:
  override def nextTravelerId: F[TravelerId] =
    Sync[F].delay(TravelerId(s"traveler-${UUID.randomUUID().toString.take(12)}"))

  override def findTravelerProfileById(travelerId: TravelerId): F[Option[TravelerProfile]] =
    selectTravelerProfiles(
      sql"""
        select
          traveler_id,
          owner_user_id,
          full_name,
          birth_date,
          document_type,
          document_number,
          phone,
          traveler_type,
          status,
          is_default,
          preferences_json,
          emergency_contact_json,
          identity_documents_json,
          loyalty_memberships_json
        from traveler_profiles
        where traveler_id = ${travelerId.value}
      """.query[TravelerRow]
    ).map(_.headOption)

  override def findTravelerProfilesByDocumentIdentity(
      travelerDocumentType: TravelerDocumentType,
      travelerDocumentNumber: DocumentNumber
  ): F[List[TravelerProfile]] =
    selectTravelerProfiles(
      sql"""
        select
          traveler_id,
          owner_user_id,
          full_name,
          birth_date,
          document_type,
          document_number,
          phone,
          traveler_type,
          status,
          is_default,
          preferences_json,
          emergency_contact_json,
          identity_documents_json,
          loyalty_memberships_json
        from traveler_profiles
        where document_type = ${travelerDocumentType.toString}
          and document_number = ${travelerDocumentNumber.value}
        order by traveler_id
      """.query[TravelerRow]
    )

  override def findTravelerProfilesByOwnerUserId(ownerUserId: UserId): F[List[TravelerProfile]] =
    selectTravelerProfiles(
      sql"""
        select
          traveler_id,
          owner_user_id,
          full_name,
          birth_date,
          document_type,
          document_number,
          phone,
          traveler_type,
          status,
          is_default,
          preferences_json,
          emergency_contact_json,
          identity_documents_json,
          loyalty_memberships_json
        from traveler_profiles
        where owner_user_id = ${ownerUserId.value}
        order by traveler_id
      """.query[TravelerRow]
    )

  override def saveTravelerProfile(travelerProfile: TravelerProfile): F[TravelerProfile] =
    val preferencesJson = DatabaseCodecs.encodeTravelerPreferences(travelerProfile.travelerPreferences)
    val emergencyContactJson = DatabaseCodecs.encodeTravelerEmergencyContact(travelerProfile.travelerEmergencyContact)
    val identityDocumentsJson = DatabaseCodecs.encodeTravelerIdentityDocuments(travelerProfile.travelerIdentityDocuments)
    val loyaltyMembershipsJson = DatabaseCodecs.encodeTravelerLoyaltyMemberships(travelerProfile.travelerLoyaltyMemberships)

    val updateExistingTravelerProfile =
      sql"""
        update traveler_profiles
        set
          owner_user_id = ${travelerProfile.ownerUserId.value},
          full_name = ${travelerProfile.travelerFullName.value},
          birth_date = ${travelerProfile.travelerBirthDate.value},
          document_type = ${travelerProfile.travelerDocumentType.toString},
          document_number = ${travelerProfile.travelerDocumentNumber.value},
          phone = ${travelerProfile.travelerPhoneNumber.value},
          traveler_type = ${travelerProfile.travelerType.toString},
          status = ${travelerProfile.travelerProfileStatus.toString},
          is_default = ${travelerProfile.isDefaultTravelerProfile},
          preferences_json = $preferencesJson,
          emergency_contact_json = $emergencyContactJson,
          identity_documents_json = $identityDocumentsJson,
          loyalty_memberships_json = $loyaltyMembershipsJson
        where traveler_id = ${travelerProfile.travelerId.value}
      """.update.run

    val insertNewTravelerProfile =
      sql"""
        insert into traveler_profiles (
          traveler_id, owner_user_id, full_name, birth_date, document_type, document_number, phone,
          traveler_type, status, is_default, preferences_json, emergency_contact_json,
          identity_documents_json, loyalty_memberships_json
        ) values (
          ${travelerProfile.travelerId.value},
          ${travelerProfile.ownerUserId.value},
          ${travelerProfile.travelerFullName.value},
          ${travelerProfile.travelerBirthDate.value},
          ${travelerProfile.travelerDocumentType.toString},
          ${travelerProfile.travelerDocumentNumber.value},
          ${travelerProfile.travelerPhoneNumber.value},
          ${travelerProfile.travelerType.toString},
          ${travelerProfile.travelerProfileStatus.toString},
          ${travelerProfile.isDefaultTravelerProfile},
          $preferencesJson,
          $emergencyContactJson,
          $identityDocumentsJson,
          $loyaltyMembershipsJson
        )
      """.update.run

    updateExistingTravelerProfile.transact(transactor).flatMap { updatedRowCount =>
      if updatedRowCount > 0 then Async[F].pure(travelerProfile)
      else insertNewTravelerProfile.transact(transactor).as(travelerProfile)
    }

  override def deleteTravelerProfile(travelerId: TravelerId): F[Unit] =
    sql"delete from traveler_profiles where traveler_id = ${travelerId.value}".update.run.transact(transactor).void

  private def selectTravelerProfiles(travelerProfileQuery: Query0[TravelerRow]): F[List[TravelerProfile]] =
    travelerProfileQuery.to[List].transact(transactor).flatMap(_.traverse(buildTravelerProfile))

  private def buildTravelerProfile(travelerRow: TravelerRow): F[TravelerProfile] =
    for
      travelerFullName <- Async[F].fromEither(PersonName.create(travelerRow.fullName))
      travelerDocumentNumber <- Async[F].fromEither(DocumentNumber.create(travelerRow.documentNumber))
      travelerPhoneNumber <- Async[F].fromEither(ContactNumber.create(travelerRow.phone))
      travelerBirthDate = BirthDate.unsafe(travelerRow.birthDate)
      travelerPreferences <- Async[F].fromEither(DatabaseCodecs.decodeTravelerPreferences(travelerRow.preferencesJson))
      travelerEmergencyContact <- Async[F].fromEither(DatabaseCodecs.decodeTravelerEmergencyContact(travelerRow.emergencyContactJson))
      travelerIdentityDocuments <- Async[F].fromEither(DatabaseCodecs.decodeTravelerIdentityDocuments(travelerRow.identityDocumentsJson))
      travelerLoyaltyMemberships <- Async[F].fromEither(DatabaseCodecs.decodeTravelerLoyaltyMemberships(travelerRow.loyaltyMembershipsJson))
    yield restoreTravelerProfile(
      travelerId = TravelerId(travelerRow.travelerId),
      ownerUserId = UserId(travelerRow.ownerUserId),
      travelerFullName = travelerFullName,
      travelerDocumentType = TravelerDocumentType.valueOf(travelerRow.documentType),
      travelerDocumentNumber = travelerDocumentNumber,
      travelerPhoneNumber = travelerPhoneNumber,
      travelerBirthDate = travelerBirthDate,
      travelerType = TravelerType.valueOf(travelerRow.travelerType),
      travelerIdentityDocuments = travelerIdentityDocuments,
      travelerEmergencyContact = travelerEmergencyContact,
      travelerLoyaltyMemberships = travelerLoyaltyMemberships,
      travelerPreferences = travelerPreferences,
      travelerProfileStatus = TravelerProfileStatus.valueOf(travelerRow.status),
      isDefaultTravelerProfile = travelerRow.isDefault
    )

  private final case class TravelerRow(
      travelerId: String,
      ownerUserId: String,
      fullName: String,
      birthDate: LocalDate,
      documentType: String,
      documentNumber: String,
      phone: String,
      travelerType: String,
      status: String,
      isDefault: Boolean,
      preferencesJson: String,
      emergencyContactJson: Option[String],
      identityDocumentsJson: String,
      loyaltyMembershipsJson: String
  )

