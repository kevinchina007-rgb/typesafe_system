// DatabaseCodecs 定义数据库 codec 映射。

package com.typesafe.travel.persistence.codecs

import cats.effect.IO

import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.order.domain.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.*
import com.typesafe.travel.traveler.domain.*
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.*
import io.circe.parser.decode
import io.circe.syntax.*

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneOffset}

object DatabaseCodecs:
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

  final case class SerializedTrainBookingSnapshot(
      trainId: String,
      trainNumber: String,
      fromStopId: String,
      fromStopSequenceNo: Option[Int],
      fromStationCode: String,
      fromStationName: String,
      toStopId: String,
      toStopSequenceNo: Option[Int],
      toStationCode: String,
      toStationName: String,
      departureTime: String,
      arrivalTime: String,
      seatInventoryId: String,
      seatClass: String,
      requestedSeatPreference: Option[String],
      seatAssignments: Option[Vector[SerializedTrainSeatAssignment]],
      travelerIds: Vector[String],
      saleStartsAt: String,
      unitPriceAmount: BigDecimal,
      unitPriceCurrency: String
  )

  final case class SerializedTrainSeatAssignment(
      travelerId: String,
      seatId: String,
      carriageNo: Int,
      seatNo: String,
      seatLabel: String,
      seatPositionType: String
  )

  final case class SerializedAttractionTicketSnapshot(
      attractionId: String,
      managerId: String,
      attractionName: String,
      ticketTypeId: String,
      ticketTypeName: String,
      sessionId: Option[String],
      sessionName: Option[String],
      sessionStartsAt: Option[String],
      sessionEndsAt: Option[String],
      useDate: String,
      travelerIds: Vector[String],
      unitPriceAmount: BigDecimal,
      unitPriceCurrency: String,
      ruleSummaries: Vector[String],
      eligibilityValidatedAt: String
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
  given Encoder[SerializedTrainBookingSnapshot] = deriveEncoder
  given Decoder[SerializedTrainBookingSnapshot] = Decoder.instance { cursor =>
    (for
      trainId <- cursor.downField("trainId").as[String]
      trainNumber <- cursor.downField("trainNumber").as[String]
      fromStationCode <- cursor.downField("fromStationCode").as[String]
        .orElse(cursor.downField("departureStationCode").as[String])
        .orElse(Right(trainId))
      fromStationName <- cursor.downField("fromStationName").as[String]
        .orElse(cursor.downField("departureStation").as[String])
        .orElse(Right(fromStationCode))
      toStationCode <- cursor.downField("toStationCode").as[String]
        .orElse(cursor.downField("arrivalStationCode").as[String])
        .orElse(Right(trainId))
      toStationName <- cursor.downField("toStationName").as[String]
        .orElse(cursor.downField("arrivalStation").as[String])
        .orElse(Right(toStationCode))
      departureTime <- cursor.downField("departureTime").as[String].orElse(Right(java.time.Instant.EPOCH.toString))
      arrivalTime <- cursor.downField("arrivalTime").as[String].orElse(Right(departureTime))
      seatClassText <- cursor.downField("seatClass").as[String]
      requestedSeatPreference <- cursor.downField("requestedSeatPreference").as[Option[String]]
      seatAssignments <- cursor.downField("seatAssignments").as[Option[Vector[SerializedTrainSeatAssignment]]]
      travelerIds <- cursor.downField("travelerIds").as[Option[Vector[String]]].map(_.getOrElse(Vector.empty))
      saleStartsAt <- cursor.downField("saleStartsAt").as[String].orElse(Right(departureTime))
      fromStopId <- cursor.downField("fromStopId").as[String]
        .orElse(cursor.downField("departureStationCode").as[String])
        .orElse(Right(fromStationCode))
      fromStopSequenceNo <- cursor.downField("fromStopSequenceNo").as[Option[Int]]
      toStopId <- cursor.downField("toStopId").as[String]
        .orElse(cursor.downField("arrivalStationCode").as[String])
        .orElse(Right(toStationCode))
      toStopSequenceNo <- cursor.downField("toStopSequenceNo").as[Option[Int]]
      seatInventoryId <- cursor.downField("seatInventoryId").as[String].orElse(Right(s"legacy-$seatClassText"))
      unitPriceAmount <- cursor.downField("unitPriceAmount").as[BigDecimal]
        .orElse(cursor.downField("unitPrice").as[BigDecimal])
        .orElse(cursor.downField("totalPriceAmount").as[BigDecimal])
        .orElse(cursor.downField("totalPrice").as[BigDecimal])
        .orElse(Right(BigDecimal(0)))
      unitPriceCurrency <- cursor.downField("unitPriceCurrency").as[String]
        .orElse(cursor.downField("currency").as[String])
        .orElse(cursor.downField("totalPriceCurrency").as[String])
        .orElse(Right("CNY"))
    yield SerializedTrainBookingSnapshot(
      trainId = trainId,
      trainNumber = trainNumber,
      fromStopId = fromStopId,
      fromStopSequenceNo = fromStopSequenceNo,
      fromStationCode = fromStationCode,
      fromStationName = fromStationName,
      toStopId = toStopId,
      toStopSequenceNo = toStopSequenceNo,
      toStationCode = toStationCode,
      toStationName = toStationName,
      departureTime = departureTime,
      arrivalTime = arrivalTime,
      seatInventoryId = seatInventoryId,
      seatClass = seatClassText,
      requestedSeatPreference = requestedSeatPreference,
      seatAssignments = seatAssignments,
      travelerIds = travelerIds,
      saleStartsAt = saleStartsAt,
      unitPriceAmount = unitPriceAmount,
      unitPriceCurrency = unitPriceCurrency
    )
    )
  }
  given Encoder[SerializedTrainSeatAssignment] = deriveEncoder
  given Decoder[SerializedTrainSeatAssignment] = deriveDecoder
  given Encoder[SerializedAttractionTicketSnapshot] = deriveEncoder
  given Decoder[SerializedAttractionTicketSnapshot] = deriveDecoder
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
    decode[List[SerializedTravelerLoyaltyMembership]](serializedValue).flatMap { serializedTravelerLoyaltyMemberships =>
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
    decode[SerializedTravelerPreferences](serializedValue).flatMap { serializedTravelerPreferences =>
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
        decode[SerializedTravelerEmergencyContact](value).flatMap { serializedTravelerEmergencyContact =>
          for
            emergencyContactName <- PersonName.create(serializedTravelerEmergencyContact.emergencyContactName)
            emergencyContactPhoneNumber <- ContactNumber.create(serializedTravelerEmergencyContact.emergencyContactPhoneNumber)
          yield Some(travelerEmergencyContact(emergencyContactName, emergencyContactPhoneNumber))
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
      case trainOrderItem: TrainOrderItem =>
        SerializedTrainBookingSnapshot(
          trainId = trainOrderItem.trainBookingSnapshot.trainId.value,
          trainNumber = trainOrderItem.trainBookingSnapshot.trainNumber.value,
          fromStopId = trainOrderItem.trainBookingSnapshot.fromStopId.value,
          fromStopSequenceNo = Some(trainOrderItem.trainBookingSnapshot.fromStopSequenceNo),
          fromStationCode = trainOrderItem.trainBookingSnapshot.fromStationCode.value,
          fromStationName = trainOrderItem.trainBookingSnapshot.fromStationName.value,
          toStopId = trainOrderItem.trainBookingSnapshot.toStopId.value,
          toStopSequenceNo = Some(trainOrderItem.trainBookingSnapshot.toStopSequenceNo),
          toStationCode = trainOrderItem.trainBookingSnapshot.toStationCode.value,
          toStationName = trainOrderItem.trainBookingSnapshot.toStationName.value,
          departureTime = trainOrderItem.trainBookingSnapshot.departureTime.toString,
          arrivalTime = trainOrderItem.trainBookingSnapshot.arrivalTime.toString,
          seatInventoryId = trainOrderItem.trainBookingSnapshot.seatInventoryId.value,
          seatClass = trainOrderItem.trainBookingSnapshot.seatClass.value,
          requestedSeatPreference = trainOrderItem.trainBookingSnapshot.requestedSeatPreference.map(_.toString),
          seatAssignments = Some(trainOrderItem.trainBookingSnapshot.seatAssignments.map(assignment =>
            SerializedTrainSeatAssignment(
              travelerId = assignment.travelerId.value,
              seatId = assignment.seatId.value,
              carriageNo = assignment.carriageNo,
              seatNo = assignment.seatNo,
              seatLabel = assignment.seatLabel,
              seatPositionType = assignment.seatPositionType.toString
            )
          )),
          travelerIds = trainOrderItem.trainBookingSnapshot.travelerIds.map(_.value),
          saleStartsAt = trainOrderItem.trainBookingSnapshot.saleStartsAt.toString,
          unitPriceAmount = trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.amount,
          unitPriceCurrency = trainOrderItem.trainBookingSnapshot.unitPriceSnapshot.currency.toString
        ).asJson.noSpaces
      case attractionOrderItem: AttractionOrderItem =>
        SerializedAttractionTicketSnapshot(
          attractionId = attractionOrderItem.attractionTicketSnapshot.attractionId.value,
          managerId = attractionOrderItem.attractionTicketSnapshot.managerId.value,
          attractionName = attractionOrderItem.attractionTicketSnapshot.attractionName,
          ticketTypeId = attractionOrderItem.attractionTicketSnapshot.ticketTypeId.value,
          ticketTypeName = attractionOrderItem.attractionTicketSnapshot.ticketTypeName,
          sessionId = attractionOrderItem.attractionTicketSnapshot.sessionId.map(_.value),
          sessionName = attractionOrderItem.attractionTicketSnapshot.sessionName,
          sessionStartsAt = attractionOrderItem.attractionTicketSnapshot.sessionStartsAt.map(_.toString),
          sessionEndsAt = attractionOrderItem.attractionTicketSnapshot.sessionEndsAt.map(_.toString),
          useDate = attractionOrderItem.attractionTicketSnapshot.useDate.toString,
          travelerIds = attractionOrderItem.attractionTicketSnapshot.travelerIds.map(_.value),
          unitPriceAmount = attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.amount,
          unitPriceCurrency = attractionOrderItem.attractionTicketSnapshot.unitPriceSnapshot.currency.toString,
          ruleSummaries = attractionOrderItem.attractionTicketSnapshot.ruleSummaries,
          eligibilityValidatedAt = attractionOrderItem.attractionTicketSnapshot.eligibilityValidatedAt.toString
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

  def decodeTrainBookingSnapshot(serializedValue: String): Either[Throwable, TrainBookingSnapshot] =
    decode[SerializedTrainBookingSnapshot](serializedValue).flatMap { serializedTrainBookingSnapshot =>
      for
        trainNumber <- TrainNumber.create(serializedTrainBookingSnapshot.trainNumber)
        fromStationCode <- TrainStationCode.create(serializedTrainBookingSnapshot.fromStationCode)
        fromStationName <- TrainStationName.create(serializedTrainBookingSnapshot.fromStationName)
        toStationCode <- TrainStationCode.create(serializedTrainBookingSnapshot.toStationCode)
        toStationName <- TrainStationName.create(serializedTrainBookingSnapshot.toStationName)
        seatClass <- TrainSeatClass.create(serializedTrainBookingSnapshot.seatClass)
        unitPriceCurrency <- parseCurrency(serializedTrainBookingSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedTrainBookingSnapshot.unitPriceAmount, unitPriceCurrency)
      yield TrainBookingSnapshot(
        trainId = TrainId(serializedTrainBookingSnapshot.trainId),
        trainNumber = trainNumber,
        fromStopId = TrainStopId(serializedTrainBookingSnapshot.fromStopId),
        fromStopSequenceNo = serializedTrainBookingSnapshot.fromStopSequenceNo.getOrElse(0),
        fromStationCode = fromStationCode,
        fromStationName = fromStationName,
        toStopId = TrainStopId(serializedTrainBookingSnapshot.toStopId),
        toStopSequenceNo = serializedTrainBookingSnapshot.toStopSequenceNo.getOrElse(1),
        toStationCode = toStationCode,
        toStationName = toStationName,
        departureTime = Instant.parse(serializedTrainBookingSnapshot.departureTime),
        arrivalTime = Instant.parse(serializedTrainBookingSnapshot.arrivalTime),
        seatInventoryId = TrainSeatInventoryId(serializedTrainBookingSnapshot.seatInventoryId),
        seatClass = seatClass,
        requestedSeatPreference = serializedTrainBookingSnapshot.requestedSeatPreference.map(TrainSeatPreference.fromText),
        seatAssignments = serializedTrainBookingSnapshot.seatAssignments.getOrElse(Vector.empty).map(assignment =>
          TrainTravelerSeatAssignment(
            travelerId = TravelerId(assignment.travelerId),
            seatId = TrainSeatId(assignment.seatId),
            carriageNo = assignment.carriageNo,
            seatNo = assignment.seatNo,
            seatLabel = assignment.seatLabel,
            seatPositionType = TrainSeatPositionType.fromText(assignment.seatPositionType)
          )
        ),
        travelerIds = serializedTrainBookingSnapshot.travelerIds.map(TravelerId.apply),
        saleStartsAt = Instant.parse(serializedTrainBookingSnapshot.saleStartsAt),
        unitPriceSnapshot = unitPriceSnapshot
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode train booking snapshot: ${error.getMessage}", error))

  def decodeAttractionTicketSnapshot(serializedValue: String): Either[Throwable, AttractionTicketSnapshot] =
    decode[SerializedAttractionTicketSnapshot](serializedValue).flatMap { serializedAttractionTicketSnapshot =>
      for
        unitPriceCurrency <- parseCurrency(serializedAttractionTicketSnapshot.unitPriceCurrency)
        unitPriceSnapshot <- Money.create(serializedAttractionTicketSnapshot.unitPriceAmount, unitPriceCurrency)
      yield AttractionTicketSnapshot(
        attractionId = AttractionId(serializedAttractionTicketSnapshot.attractionId),
        managerId = ManagerId(serializedAttractionTicketSnapshot.managerId),
        attractionName = serializedAttractionTicketSnapshot.attractionName,
        ticketTypeId = TicketTypeId(serializedAttractionTicketSnapshot.ticketTypeId),
        ticketTypeName = serializedAttractionTicketSnapshot.ticketTypeName,
        sessionId = serializedAttractionTicketSnapshot.sessionId.map(AttractionTicketSessionId.apply),
        sessionName = serializedAttractionTicketSnapshot.sessionName,
        sessionStartsAt = serializedAttractionTicketSnapshot.sessionStartsAt.map(Instant.parse),
        sessionEndsAt = serializedAttractionTicketSnapshot.sessionEndsAt.map(Instant.parse),
        useDate = LocalDate.parse(serializedAttractionTicketSnapshot.useDate),
        travelerIds = serializedAttractionTicketSnapshot.travelerIds.map(TravelerId.apply),
        unitPriceSnapshot = unitPriceSnapshot,
        ruleSummaries = serializedAttractionTicketSnapshot.ruleSummaries,
        eligibilityValidatedAt = Instant.parse(serializedAttractionTicketSnapshot.eligibilityValidatedAt)
      )
    }.left.map(error => new IllegalArgumentException(s"Could not decode attraction ticket snapshot: ${error.getMessage}", error))

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
