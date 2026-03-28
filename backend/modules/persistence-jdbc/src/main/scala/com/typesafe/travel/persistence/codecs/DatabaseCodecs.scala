package com.typesafe.travel.persistence.codecs

import cats.syntax.all.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.traveler.domain.*
import doobie.implicits.javasql.DateMeta
import doobie.implicits.javatimedrivernative.JavaOffsetDateTimeMeta
import doobie.util.meta.Meta
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.*
import io.circe.parser.decode
import io.circe.syntax.*

import java.sql.Date
import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

object DatabaseCodecs:
  given Meta[OffsetDateTime] = JavaOffsetDateTimeMeta
  given Meta[Date] = DateMeta
  given Meta[Instant] = summon[Meta[OffsetDateTime]].timap(_.toInstant)(_.atOffset(ZoneOffset.UTC))
  given Meta[LocalDate] = summon[Meta[Date]].timap(_.toLocalDate)((localDate: LocalDate) => Date.valueOf(localDate))

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

  final case class SerializedFlightBookingSnapshot(
      airlineId: String,
      airlineName: String,
      airlineCode: String,
      flightId: String,
      flightNumber: String,
      departureAt: String,
      arrivalAt: String,
      departureAirportCode: String,
      arrivalAirportCode: String,
      cabinClass: String,
      travelerIds: Vector[String],
      unitPriceAmount: BigDecimal,
      unitPriceCurrency: String
  )

  final case class SerializedHotelBookingSnapshot(
      hotelId: String,
      hotelName: String,
      hotelLocation: String,
      roomTypeId: String,
      roomTypeName: String,
      checkInDate: String,
      checkOutDate: String,
      guestTravelerIds: Vector[String],
      roomCount: Int,
      unitPriceAmount: BigDecimal,
      unitPriceCurrency: String
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
  given Encoder[SerializedFlightBookingSnapshot] = deriveEncoder
  given Decoder[SerializedFlightBookingSnapshot] = deriveDecoder
  given Encoder[SerializedHotelBookingSnapshot] = deriveEncoder
  given Decoder[SerializedHotelBookingSnapshot] = deriveDecoder
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
    decode[List[SerializedTravelerIdentityDocument]](serializedValue).flatMap { serializedTravelerIdentityDocuments =>
      serializedTravelerIdentityDocuments.traverse { serializedTravelerIdentityDocument =>
        for
          travelerDocumentNumber <- DocumentNumber.create(serializedTravelerIdentityDocument.travelerDocumentNumber)
          issuingCountryCode <- CountryCode.create(serializedTravelerIdentityDocument.issuingCountryCode)
        yield TravelerIdentityDocument.create(
          travelerDocumentType = TravelerDocumentType.valueOf(serializedTravelerIdentityDocument.travelerDocumentType),
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
    decode[List[SerializedTravelerLoyaltyMembership]](serializedValue).flatMap { serializedTravelerLoyaltyMemberships =>
      serializedTravelerLoyaltyMemberships.traverse { serializedTravelerLoyaltyMembership =>
        for
          loyaltyProgramName <- LoyaltyProgramName.create(serializedTravelerLoyaltyMembership.loyaltyProgramName)
          travelerLoyaltyMembership <- TravelerLoyaltyMembership.create(
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
    decode[SerializedTravelerPreferences](serializedValue).flatMap { serializedTravelerPreferences =>
      TravelerPreferences.create(
        travelerSeatPreference = SeatPreference.valueOf(serializedTravelerPreferences.travelerSeatPreference),
        travelerMealPreference = MealPreference.valueOf(serializedTravelerPreferences.travelerMealPreference),
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
        decode[SerializedTravelerEmergencyContact](value).flatMap { serializedTravelerEmergencyContact =>
          for
            emergencyContactName <- PersonName.create(serializedTravelerEmergencyContact.emergencyContactName)
            emergencyContactPhoneNumber <- ContactNumber.create(serializedTravelerEmergencyContact.emergencyContactPhoneNumber)
          yield Some(TravelerEmergencyContact.create(emergencyContactName, emergencyContactPhoneNumber))
        }.left.map(error => new IllegalArgumentException(s"Could not decode traveler emergency contact: ${error.getMessage}", error))

  def encodeOrderLineItemSnapshot(orderLineItem: OrderLineItem): String =
    orderLineItem match
      case flightOrderItem: FlightOrderItem =>
        SerializedFlightBookingSnapshot(
          airlineId = flightOrderItem.flightBookingSnapshot.airlineId.value,
          airlineName = flightOrderItem.flightBookingSnapshot.airlineName.value,
          airlineCode = flightOrderItem.flightBookingSnapshot.airlineCode.value,
          flightId = flightOrderItem.flightBookingSnapshot.flightId.value,
          flightNumber = flightOrderItem.flightBookingSnapshot.flightNumber.value,
          departureAt = flightOrderItem.flightBookingSnapshot.flightSchedule.departureAt.toString,
          arrivalAt = flightOrderItem.flightBookingSnapshot.flightSchedule.arrivalAt.toString,
          departureAirportCode = flightOrderItem.flightBookingSnapshot.departureAirportCode.value,
          arrivalAirportCode = flightOrderItem.flightBookingSnapshot.arrivalAirportCode.value,
          cabinClass = flightOrderItem.flightBookingSnapshot.cabinClass.value,
          travelerIds = flightOrderItem.flightBookingSnapshot.travelerIds.map(_.value),
          unitPriceAmount = flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.amount,
          unitPriceCurrency = flightOrderItem.flightBookingSnapshot.unitPriceSnapshot.currency.toString
        ).asJson.noSpaces
      case hotelOrderItem: HotelOrderItem =>
        SerializedHotelBookingSnapshot(
          hotelId = hotelOrderItem.hotelBookingSnapshot.hotelId.value,
          hotelName = hotelOrderItem.hotelBookingSnapshot.hotelName.value,
          hotelLocation = hotelOrderItem.hotelBookingSnapshot.hotelLocation.value,
          roomTypeId = hotelOrderItem.hotelBookingSnapshot.roomTypeId.value,
          roomTypeName = hotelOrderItem.hotelBookingSnapshot.roomTypeName.value,
          checkInDate = hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkIn.toString,
          checkOutDate = hotelOrderItem.hotelBookingSnapshot.stayPeriod.checkOut.toString,
          guestTravelerIds = hotelOrderItem.hotelBookingSnapshot.guestTravelerIds.map(_.value),
          roomCount = hotelOrderItem.hotelBookingSnapshot.roomCount.value,
          unitPriceAmount = hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.amount,
          unitPriceCurrency = hotelOrderItem.hotelBookingSnapshot.unitPriceSnapshot.currency.toString
        ).asJson.noSpaces

  def decodeFlightBookingSnapshot(serializedValue: String): Either[Throwable, FlightBookingSnapshot] =
    decode[SerializedFlightBookingSnapshot](serializedValue).flatMap { serializedFlightBookingSnapshot =>
      for
        airlineName <- AirlineName.create(serializedFlightBookingSnapshot.airlineName)
        airlineCode <- AirlineCode.create(serializedFlightBookingSnapshot.airlineCode)
        flightNumber <- FlightNumber.create(serializedFlightBookingSnapshot.flightNumber)
        departureAirportCode <- AirportCode.create(serializedFlightBookingSnapshot.departureAirportCode)
        arrivalAirportCode <- AirportCode.create(serializedFlightBookingSnapshot.arrivalAirportCode)
        cabinClass <- CabinClass.create(serializedFlightBookingSnapshot.cabinClass)
        flightSchedule <- FlightSchedule.create(
          departureAt = OffsetDateTime.parse(serializedFlightBookingSnapshot.departureAt),
          arrivalAt = OffsetDateTime.parse(serializedFlightBookingSnapshot.arrivalAt)
        )
        unitPriceCurrency <- parseCurrency(serializedFlightBookingSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedFlightBookingSnapshot.unitPriceAmount, unitPriceCurrency)
      yield FlightBookingSnapshot(
        airlineId = AirlineId(serializedFlightBookingSnapshot.airlineId),
        airlineName = airlineName,
        airlineCode = airlineCode,
        flightId = FlightId(serializedFlightBookingSnapshot.flightId),
        flightNumber = flightNumber,
        flightSchedule = flightSchedule,
        departureAirportCode = departureAirportCode,
        arrivalAirportCode = arrivalAirportCode,
        cabinClass = cabinClass,
        travelerIds = serializedFlightBookingSnapshot.travelerIds.map(TravelerId.apply),
        unitPriceSnapshot = unitPriceSnapshot
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode flight booking snapshot: ${error.getMessage}", error))

  def decodeHotelBookingSnapshot(serializedValue: String): Either[Throwable, HotelBookingSnapshot] =
    decode[SerializedHotelBookingSnapshot](serializedValue).flatMap { serializedHotelBookingSnapshot =>
      for
        hotelName <- HotelName.create(serializedHotelBookingSnapshot.hotelName)
        hotelLocation <- HotelLocation.create(serializedHotelBookingSnapshot.hotelLocation)
        roomTypeName <- RoomTypeName.create(serializedHotelBookingSnapshot.roomTypeName)
        stayPeriod <- StayPeriod.create(
          checkIn = LocalDate.parse(serializedHotelBookingSnapshot.checkInDate),
          checkOut = LocalDate.parse(serializedHotelBookingSnapshot.checkOutDate)
        )
        roomCount <- RoomCount.create(serializedHotelBookingSnapshot.roomCount)
        unitPriceCurrency <- parseCurrency(serializedHotelBookingSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedHotelBookingSnapshot.unitPriceAmount, unitPriceCurrency)
      yield HotelBookingSnapshot(
        hotelId = HotelId(serializedHotelBookingSnapshot.hotelId),
        hotelName = hotelName,
        hotelLocation = hotelLocation,
        roomTypeId = RoomTypeId(serializedHotelBookingSnapshot.roomTypeId),
        roomTypeName = roomTypeName,
        stayPeriod = stayPeriod,
        guestTravelerIds = serializedHotelBookingSnapshot.guestTravelerIds.map(TravelerId.apply),
        roomCount = roomCount,
        unitPriceSnapshot = unitPriceSnapshot
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode hotel booking snapshot: ${error.getMessage}", error))

  def parseCurrency(currencyValue: String): Either[Throwable, Currency] =
    Either.catchNonFatal(Currency.valueOf(currencyValue))

  def encodeTravelerIds(travelerIds: Vector[TravelerId]): String =
    SerializedTravelerIds(travelerIds.map(_.value)).asJson.noSpaces

  def decodeTravelerIds(serializedValue: String): Either[Throwable, Vector[TravelerId]] =
    decode[SerializedTravelerIds](serializedValue)
      .map(_.travelerIds.map(TravelerId.apply))
      .left
      .map(error => new IllegalArgumentException(s"Could not decode traveler ids: ${error.getMessage}", error))

  def parseJson(jsonValue: String): Either[Throwable, Json] =
    io.circe.parser.parse(jsonValue).left.map(error => new IllegalArgumentException(error.getMessage, error))
