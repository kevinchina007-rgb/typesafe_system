package com.typesafe.travel.traveler.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.codecs.DatabaseCodecs
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, Date, ResultSet}

object TravelerPlannerPlainSql:
  private val selectTravelerProfilesSql =
    """
      select traveler_id, owner_user_id, full_name, birth_date, document_type, document_number, phone,
             traveler_type, status, is_default, preferences_json, emergency_contact_json,
             identity_documents_json, loyalty_memberships_json,
             gender, nationality, document_expiry_date, email, quiet_seat_preferred,
             assistance_type, special_requirement_note, has_large_luggage, luggage_note
      from traveler_profiles
    """

  def listByOwner(connection: Connection, ownerUserId: UserId): IO[List[TravelerProfile]] =
    queryProfiles(connection, selectTravelerProfilesSql + " where owner_user_id = ? order by traveler_id", List(ownerUserId.value))

  def findById(connection: Connection, travelerId: TravelerId): IO[Option[TravelerProfile]] =
    queryProfiles(connection, selectTravelerProfilesSql + " where traveler_id = ?", List(travelerId.value)).map(_.headOption)

  def findByDocument(connection: Connection, documentType: TravelerDocumentType, documentNumber: DocumentNumber): IO[List[TravelerProfile]] =
    queryProfiles(
      connection,
      selectTravelerProfilesSql + " where document_type = ? and document_number = ? order by traveler_id",
      List(documentType.toString, documentNumber.value)
    )

  def save(connection: Connection, travelerProfile: TravelerProfile): IO[TravelerProfile] =
    IO.blocking {
      val preferencesJson = DatabaseCodecs.encodeTravelerPreferences(travelerProfile.travelerPreferences)
      val emergencyContactJson = DatabaseCodecs.encodeTravelerEmergencyContact(travelerProfile.travelerEmergencyContact)
      val identityDocumentsJson = DatabaseCodecs.encodeTravelerIdentityDocuments(travelerProfile.travelerIdentityDocuments)
      val loyaltyMembershipsJson = DatabaseCodecs.encodeTravelerLoyaltyMemberships(travelerProfile.travelerLoyaltyMemberships)
      val updatedRows = PlainSqlSupport.withStatement(
        connection,
        """
          update traveler_profiles
          set owner_user_id = ?, full_name = ?, birth_date = ?, document_type = ?, document_number = ?, phone = ?,
              traveler_type = ?, status = ?, is_default = ?, preferences_json = ?, emergency_contact_json = ?,
              identity_documents_json = ?, loyalty_memberships_json = ?, gender = ?, nationality = ?,
              document_expiry_date = ?, email = ?, quiet_seat_preferred = ?, assistance_type = ?,
              special_requirement_note = ?, has_large_luggage = ?, luggage_note = ?
          where traveler_id = ?
        """
      ) { statement =>
        setProfileFields(statement, travelerProfile, preferencesJson, emergencyContactJson, identityDocumentsJson, loyaltyMembershipsJson, startsAt = 1)
        statement.setString(23, travelerProfile.travelerId.value)
        statement.executeUpdate()
      }
      if updatedRows == 0 then
        PlainSqlSupport.withStatement(
          connection,
          """
            insert into traveler_profiles (
              traveler_id, owner_user_id, full_name, birth_date, document_type, document_number, phone,
              traveler_type, status, is_default, preferences_json, emergency_contact_json,
              identity_documents_json, loyalty_memberships_json, gender, nationality, document_expiry_date,
              email, quiet_seat_preferred, assistance_type, special_requirement_note, has_large_luggage, luggage_note
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
          """
        ) { statement =>
          statement.setString(1, travelerProfile.travelerId.value)
          setProfileFields(statement, travelerProfile, preferencesJson, emergencyContactJson, identityDocumentsJson, loyaltyMembershipsJson, startsAt = 2)
          statement.executeUpdate()
        }
      travelerProfile
    }

  def updateUserDefaultTraveler(connection: Connection, ownerUserId: UserId, travelerId: Option[TravelerId]): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update users set default_traveler_id = ? where user_id = ?") { statement =>
        statement.setString(1, travelerId.map(_.value).orNull)
        statement.setString(2, ownerUserId.value)
        statement.executeUpdate()
      }
      ()
    }

  private def queryProfiles(connection: Connection, sql: String, values: List[String]): IO[List[TravelerProfile]] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, sql) { statement =>
        values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
        PlainSqlSupport.queryList(statement)(readTravelerProfile)
      }
    }

  private def setProfileFields(
      statement: java.sql.PreparedStatement,
      travelerProfile: TravelerProfile,
      preferencesJson: String,
      emergencyContactJson: Option[String],
      identityDocumentsJson: String,
      loyaltyMembershipsJson: String,
      startsAt: Int
  ): Unit =
    statement.setString(startsAt, travelerProfile.ownerUserId.value)
    statement.setString(startsAt + 1, travelerProfile.travelerFullName.value)
    statement.setDate(startsAt + 2, Date.valueOf(travelerProfile.travelerBirthDate.value))
    statement.setString(startsAt + 3, travelerProfile.travelerDocumentType.toString)
    statement.setString(startsAt + 4, travelerProfile.travelerDocumentNumber.value)
    statement.setString(startsAt + 5, travelerProfile.travelerPhoneNumber.value)
    statement.setString(startsAt + 6, travelerProfile.travelerType.toString)
    statement.setString(startsAt + 7, travelerProfile.travelerProfileStatus.toString)
    statement.setBoolean(startsAt + 8, travelerProfile.isDefaultTravelerProfile)
    statement.setString(startsAt + 9, preferencesJson)
    statement.setString(startsAt + 10, emergencyContactJson.orNull)
    statement.setString(startsAt + 11, identityDocumentsJson)
    statement.setString(startsAt + 12, loyaltyMembershipsJson)
    statement.setString(startsAt + 13, travelerProfile.travelerGender)
    statement.setString(startsAt + 14, travelerProfile.travelerNationality)
    travelerProfile.travelerDocumentExpiryDate match
      case Some(value) => statement.setDate(startsAt + 15, Date.valueOf(value))
      case None => statement.setDate(startsAt + 15, null)
    statement.setString(startsAt + 16, travelerProfile.travelerEmail.orNull)
    statement.setBoolean(startsAt + 17, travelerProfile.quietSeatPreferred)
    statement.setString(startsAt + 18, travelerProfile.assistanceType)
    statement.setString(startsAt + 19, travelerProfile.specialRequirementNote.orNull)
    statement.setBoolean(startsAt + 20, travelerProfile.hasLargeLuggage)
    statement.setString(startsAt + 21, travelerProfile.luggageNote.orNull)

  private def readTravelerProfile(resultSet: ResultSet): TravelerProfile =
    restoreTravelerProfile(
      travelerId = TravelerId(resultSet.getString("traveler_id")),
      ownerUserId = UserId(resultSet.getString("owner_user_id")),
      travelerFullName = PersonName.create(resultSet.getString("full_name")).fold(throw _, identity),
      travelerDocumentType = TravelerDocumentType.fromText(resultSet.getString("document_type")),
      travelerDocumentNumber = DocumentNumber.create(resultSet.getString("document_number")).fold(throw _, identity),
      travelerPhoneNumber = ContactNumber.create(resultSet.getString("phone")).fold(throw _, identity),
      travelerBirthDate = BirthDate.unsafe(resultSet.getDate("birth_date").toLocalDate),
      travelerType = TravelerType.fromText(resultSet.getString("traveler_type")),
      travelerIdentityDocuments = DatabaseCodecs.decodeTravelerIdentityDocuments(resultSet.getString("identity_documents_json")).fold(throw _, identity),
      travelerEmergencyContact = DatabaseCodecs.decodeTravelerEmergencyContact(Option(resultSet.getString("emergency_contact_json"))).fold(throw _, identity),
      travelerLoyaltyMemberships = DatabaseCodecs.decodeTravelerLoyaltyMemberships(resultSet.getString("loyalty_memberships_json")).fold(throw _, identity),
      travelerPreferences = DatabaseCodecs.decodeTravelerPreferences(resultSet.getString("preferences_json")).fold(throw _, identity),
      travelerProfileStatus = TravelerProfileStatus.fromText(resultSet.getString("status")),
      isDefaultTravelerProfile = resultSet.getBoolean("is_default"),
      travelerGender = readOptionalString(resultSet, "gender").getOrElse("未填写"),
      travelerNationality = readOptionalString(resultSet, "nationality").getOrElse("中国"),
      travelerDocumentExpiryDate = Option(resultSet.getDate("document_expiry_date")).map(_.toLocalDate),
      travelerEmail = readOptionalString(resultSet, "email"),
      quietSeatPreferred = resultSet.getBoolean("quiet_seat_preferred"),
      assistanceType = readOptionalString(resultSet, "assistance_type").getOrElse("无"),
      specialRequirementNote = readOptionalString(resultSet, "special_requirement_note"),
      hasLargeLuggage = resultSet.getBoolean("has_large_luggage"),
      luggageNote = readOptionalString(resultSet, "luggage_note")
    )

  private def readOptionalString(resultSet: ResultSet, columnName: String): Option[String] =
    Option(resultSet.getString(columnName)).map(_.trim).filter(_.nonEmpty)
