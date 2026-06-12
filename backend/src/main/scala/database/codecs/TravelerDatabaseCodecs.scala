// TravelerDatabaseCodecs 定义数据库 codec 映射。

package com.typesafe.travel.persistence.codecs

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import java.time.LocalDate

object TravelerDatabaseCodecs:
  final case class SerializedTravelerIdentityDocument(
      travelerDocumentType: String,
      travelerDocumentNumber: String,
      issuingCountryCode: String,
      expirationDate: String
  )

  final case class SerializedTravelerLoyaltyMembership(
      loyaltyProgramName: String,
      loyaltyMembershipNumber: String
  )

  final case class SerializedTravelerEmergencyContact(
      emergencyContactName: String,
      emergencyContactPhoneNumber: String
  )

  final case class SerializedTravelerPreferences(
      travelerSeatPreference: String,
      travelerMealPreference: String,
      accessibilityRequestNotes: Option[String]
  )

  final case class SerializedTravelerIds(
      travelerIds: Vector[String]
  )

  given Encoder[SerializedTravelerIdentityDocument] = deriveEncoder
  given Decoder[SerializedTravelerIdentityDocument] = deriveDecoder
  given Encoder[SerializedTravelerLoyaltyMembership] = deriveEncoder
  given Decoder[SerializedTravelerLoyaltyMembership] = deriveDecoder
  given Encoder[SerializedTravelerEmergencyContact] = deriveEncoder
  given Decoder[SerializedTravelerEmergencyContact] = deriveDecoder
  given Encoder[SerializedTravelerPreferences] = deriveEncoder
  given Decoder[SerializedTravelerPreferences] = deriveDecoder
  given Encoder[SerializedTravelerIds] = deriveEncoder
  given Decoder[SerializedTravelerIds] = deriveDecoder

  def encodeTravelerIdentityDocuments(travelerIdentityDocuments: List[TravelerIdentityDocument]): String =
    travelerIdentityDocuments.map { travelerIdentityDocument =>
      SerializedTravelerIdentityDocument(
        travelerDocumentType = travelerIdentityDocument.travelerDocumentType.toString,
        travelerDocumentNumber = travelerIdentityDocument.travelerDocumentNumber.value,
        issuingCountryCode = travelerIdentityDocument.issuingCountryCode.value,
        expirationDate = travelerIdentityDocument.expirationDate.toString
      )
    }.asJson.noSpaces

  def decodeTravelerIdentityDocuments(serializedValue: String): Either[Throwable, List[TravelerIdentityDocument]] =
    io.circe.parser.decode[List[SerializedTravelerIdentityDocument]](serializedValue).flatMap { serializedTravelerIdentityDocuments =>
      serializedTravelerIdentityDocuments.traverse { serializedTravelerIdentityDocument =>
        for
          travelerDocumentNumber <- DocumentNumber.create(serializedTravelerIdentityDocument.travelerDocumentNumber)
          issuingCountryCode <- CountryCode.create(serializedTravelerIdentityDocument.issuingCountryCode)
        yield travelerIdentityDocument(
          travelerDocumentType = TravelerDocumentType.fromText(serializedTravelerIdentityDocument.travelerDocumentType),
          travelerDocumentNumber = travelerDocumentNumber,
          issuingCountryCode = issuingCountryCode,
          expirationDate = LocalDate.parse(serializedTravelerIdentityDocument.expirationDate)
        )
      }
    }.left.map(error => new IllegalArgumentException(s"Could not decode traveler identity documents: ${error.getMessage}", error))

  def encodeTravelerLoyaltyMemberships(travelerLoyaltyMemberships: List[TravelerLoyaltyMembership]): String =
    travelerLoyaltyMemberships.map { travelerLoyaltyMembership =>
      SerializedTravelerLoyaltyMembership(
        loyaltyProgramName = travelerLoyaltyMembership.loyaltyProgramName.value,
        loyaltyMembershipNumber = travelerLoyaltyMembership.loyaltyMembershipNumber
      )
    }.asJson.noSpaces

  def decodeTravelerLoyaltyMemberships(serializedValue: String): Either[Throwable, List[TravelerLoyaltyMembership]] =
    io.circe.parser.decode[List[SerializedTravelerLoyaltyMembership]](serializedValue).flatMap { serializedTravelerLoyaltyMemberships =>
      serializedTravelerLoyaltyMemberships.traverse { serializedTravelerLoyaltyMembership =>
        for
          loyaltyProgramName <- LoyaltyProgramName.create(serializedTravelerLoyaltyMembership.loyaltyProgramName)
          travelerLoyaltyMembership <- travelerLoyaltyMembership(
            loyaltyProgramName = loyaltyProgramName,
            loyaltyMembershipNumber = serializedTravelerLoyaltyMembership.loyaltyMembershipNumber
          )
        yield travelerLoyaltyMembership
      }
    }.left.map(error => new IllegalArgumentException(s"Could not decode traveler loyalty memberships: ${error.getMessage}", error))

  def encodeTravelerPreferences(travelerPreferences: TravelerPreferences): String =
    SerializedTravelerPreferences(
      travelerSeatPreference = travelerPreferences.travelerSeatPreference.toString,
      travelerMealPreference = travelerPreferences.travelerMealPreference.toString,
      accessibilityRequestNotes = travelerPreferences.accessibilityRequestNotes
    ).asJson.noSpaces

  def decodeTravelerPreferences(serializedValue: String): Either[Throwable, TravelerPreferences] =
    io.circe.parser.decode[SerializedTravelerPreferences](serializedValue).flatMap { serializedTravelerPreferences =>
      travelerPreferences(
        travelerSeatPreference = SeatPreference.fromText(serializedTravelerPreferences.travelerSeatPreference),
        travelerMealPreference = MealPreference.fromText(serializedTravelerPreferences.travelerMealPreference),
        accessibilityRequestNotes = serializedTravelerPreferences.accessibilityRequestNotes
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode traveler preferences: ${error.getMessage}", error))

  def encodeTravelerEmergencyContact(travelerEmergencyContact: Option[TravelerEmergencyContact]): Option[String] =
    travelerEmergencyContact.map { emergencyContact =>
      SerializedTravelerEmergencyContact(
        emergencyContactName = emergencyContact.emergencyContactName.value,
        emergencyContactPhoneNumber = emergencyContact.emergencyContactPhoneNumber.value
      ).asJson.noSpaces
    }

  def decodeTravelerEmergencyContact(serializedValue: Option[String]): Either[Throwable, Option[TravelerEmergencyContact]] =
    serializedValue match
      case None => Right(None)
      case Some(value) =>
        io.circe.parser.decode[SerializedTravelerEmergencyContact](value).flatMap { serializedTravelerEmergencyContact =>
          for
            emergencyContactName <- PersonName.create(serializedTravelerEmergencyContact.emergencyContactName)
            emergencyContactPhoneNumber <- ContactNumber.create(serializedTravelerEmergencyContact.emergencyContactPhoneNumber)
          yield Some(travelerEmergencyContact(emergencyContactName, emergencyContactPhoneNumber))
        }.left.map(error => new IllegalArgumentException(s"Could not decode traveler emergency contact: ${error.getMessage}", error))

  def encodeTravelerIds(travelerIds: Vector[TravelerId]): String =
    SerializedTravelerIds(travelerIds.map(_.value)).asJson.noSpaces

  def decodeTravelerIds(serializedValue: String): Either[Throwable, Vector[TravelerId]] =
    io.circe.parser.decode[SerializedTravelerIds](serializedValue)
      .map(_.travelerIds.map(TravelerId.apply))
      .left
      .map(error => new IllegalArgumentException(s"Could not decode traveler ids: ${error.getMessage}", error))
