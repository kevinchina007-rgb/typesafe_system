// AirlineManagerPlainSqlSupport 提供航司管理后台 flight / order / traveler 结果集读取与 JSON 解析辅助函数。
package com.typesafe.travel.persistence.operations.airline

import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import io.circe.parser.decode

import java.sql.{Connection, ResultSet}
import java.time.{LocalDate, OffsetDateTime}

object AirlineManagerPlainSqlSupport:
  final case class ManagerFlightCabinRow(flight: ManagerFlightPlannerResponse, cabinInventory: Option[ManagerCabinInventoryPlannerResponse])
  final case class ManagerFlightOrderRow(
      orderId: String,
      orderItemId: String,
      buyerUserId: String,
      buyerNickname: String,
      cabinClass: String,
      orderStatus: String,
      orderCreatedAt: String,
      travelerIds: List[String]
  )

  def listCabinInventories(connection: Connection, flightId: String): List[ManagerCabinInventoryPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select inventory_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status
        from flight_cabin_inventories
        where flight_id = ?
        order by
          case upper(cabin_class)
            when 'ECONOMY' then 1
            when 'PREMIUM_ECONOMY' then 2
            when 'BUSINESS' then 3
            when 'FIRST' then 4
            else 5
          end,
          inventory_id
      """
    ) { statement =>
      statement.setString(1, flightId)
      PlainSqlSupport.queryList(statement)(readCabinInventory)
    }

  def readFlightWithoutCabins(resultSet: ResultSet): ManagerFlightPlannerResponse =
    ManagerFlightPlannerResponse(
      flightId = resultSet.getString("flight_id"),
      airlineId = resultSet.getString("airline_id"),
      airlineName = resultSet.getString("airline_name"),
      airlineCode = resultSet.getString("airline_code"),
      airlineLogoPath = Option(resultSet.getString("airline_logo_path")).map(_.trim).filter(_.nonEmpty),
      flightNumber = resultSet.getString("flight_number"),
      aircraftModel = Option(resultSet.getString("aircraft_model")).map(_.trim).filter(_.nonEmpty),
      departureAirport = resultSet.getString("departure_airport"),
      arrivalAirport = resultSet.getString("arrival_airport"),
      departureTime = resultSet.getObject("departure_time", classOf[OffsetDateTime]).toString,
      arrivalTime = resultSet.getObject("arrival_time", classOf[OffsetDateTime]).toString,
      status = resultSet.getString("status"),
      basePrice = resultSet.getBigDecimal("base_price_amount").toString,
      currency = resultSet.getString("base_price_currency"),
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      cabinInventories = Nil
    )

  def readCabinInventory(resultSet: ResultSet): ManagerCabinInventoryPlannerResponse =
    val status = resultSet.getString("status")
    val availableSeats = resultSet.getInt("available_seats")
    ManagerCabinInventoryPlannerResponse(
      inventoryId = resultSet.getString("inventory_id"),
      cabinClass = resultSet.getString("cabin_class"),
      availableSeats = availableSeats,
      unitPrice = resultSet.getBigDecimal("unit_price_amount").toString,
      currency = resultSet.getString("unit_price_currency"),
      status = status,
      isBookable = status == "Open" && availableSeats > 0
    )

  def readFlightWithOptionalCabin(resultSet: ResultSet): ManagerFlightCabinRow =
    val inventoryId = resultSet.getString("inventory_id")
    ManagerFlightCabinRow(
      flight = readFlightWithoutCabins(resultSet),
      cabinInventory =
        Option(inventoryId).map { id =>
          val status = resultSet.getString("cabin_inventory_status")
          val availableSeats = resultSet.getInt("available_seats")
          ManagerCabinInventoryPlannerResponse(
            inventoryId = id,
            cabinClass = resultSet.getString("cabin_class"),
            availableSeats = availableSeats,
            unitPrice = resultSet.getBigDecimal("unit_price_amount").toString,
            currency = resultSet.getString("unit_price_currency"),
            status = status,
            isBookable = status == "Open" && availableSeats > 0
          )
        }
    )

  def readManagerFlightOrderRow(resultSet: ResultSet): ManagerFlightOrderRow =
    val snapshotJson = Option(resultSet.getString("snapshot_json"))
    val typedTravelerIds = Option(resultSet.getString("traveler_ids_json")).flatMap(parseStringListJson).getOrElse(Nil)
    val snapshotTravelerIds = snapshotJson.flatMap(json => extractStringListField(json, "travelerIds")).getOrElse(Nil)
    ManagerFlightOrderRow(
      orderId = resultSet.getString("order_id"),
      orderItemId = resultSet.getString("order_item_id"),
      buyerUserId = resultSet.getString("buyer_user_id"),
      buyerNickname = resultSet.getString("buyer_nickname"),
      cabinClass = Option(resultSet.getString("cabin_class"))
        .filter(_.trim.nonEmpty)
        .orElse(snapshotJson.flatMap(json => extractStringField(json, "cabinClass")))
        .getOrElse("ECONOMY"),
      orderStatus = resultSet.getString("order_status"),
      orderCreatedAt = resultSet.getTimestamp("created_at").toInstant.toString,
      travelerIds = if typedTravelerIds.nonEmpty then typedTravelerIds else snapshotTravelerIds
    )

  def listTravelersByIds(connection: Connection, travelerIds: List[String]): List[ManagerFlightOrderTravelerResponse] =
    if travelerIds.isEmpty then Nil
    else
      PlainSqlSupport.withStatement(
        connection,
        s"""
          select traveler_id, full_name, birth_date, document_type, document_number, phone,
                 gender, nationality, document_expiry_date, email, preferences_json,
                 quiet_seat_preferred, assistance_type, special_requirement_note,
                 has_large_luggage, luggage_note
          from traveler_profiles
          where traveler_id in (${travelerIds.map(_ => "?").mkString(", ")})
          order by full_name, traveler_id
        """
      ) { statement =>
        travelerIds.zipWithIndex.foreach { case (travelerId, index) => statement.setString(index + 1, travelerId) }
        PlainSqlSupport.queryList(statement) { resultSet =>
          val fullName = resultSet.getString("full_name")
          val birthDate = resultSet.getDate("birth_date").toLocalDate
          val documentType = resultSet.getString("document_type")
          val documentNumber = resultSet.getString("document_number")
          val phone = resultSet.getString("phone")
          val preferences = com.typesafe.travel.persistence.codecs.DatabaseCodecs
            .decodeTravelerPreferences(resultSet.getString("preferences_json"))
            .fold(throw _, identity)
          val basicInfo = ManagerTravelerBasicInfo(
            fullName = fullName,
            gender = readOptionalString(resultSet, "gender").getOrElse("unspecified"),
            birthDate = birthDate.toString,
            nationality = readOptionalString(resultSet, "nationality").getOrElse("China")
          )
          val documentInfo = ManagerTravelerDocumentInfo(
            documentType = documentType,
            documentNumber = documentNumber,
            documentExpiryDate = Option(resultSet.getDate("document_expiry_date")).map(_.toLocalDate.toString)
          )
          val contactInfo = ManagerTravelerContactInfo(
            phone = phone,
            email = readOptionalString(resultSet, "email")
          )
          val preferenceInfo = ManagerTravelerPreferenceInfo(
            seatPreference = preferences.travelerSeatPreference.toString,
            mealPreference = preferences.travelerMealPreference.toString,
            quietSeatPreferred = resultSet.getBoolean("quiet_seat_preferred")
          )
          val specialRequirementInfo = ManagerTravelerSpecialRequirementInfo(
            assistanceType = readOptionalString(resultSet, "assistance_type").getOrElse("none"),
            requirementNote = readOptionalString(resultSet, "special_requirement_note").orElse(preferences.accessibilityRequestNotes),
            hasLargeLuggage = resultSet.getBoolean("has_large_luggage"),
            luggageNote = readOptionalString(resultSet, "luggage_note")
          )
          val requirementLabel =
            List(
              Option.when(specialRequirementInfo.assistanceType != "none")(specialRequirementInfo.assistanceType),
              specialRequirementInfo.requirementNote,
              Option.when(specialRequirementInfo.hasLargeLuggage)("largeLuggage"),
              specialRequirementInfo.luggageNote
            ).flatten.mkString(" / ")
          val serviceSummary = ManagerTravelerServiceSummary(
            age = Some(java.time.Period.between(birthDate, LocalDate.now()).getYears),
            documentLabel = s"$documentType $documentNumber",
            contactLabel = List(Some(phone), contactInfo.email).flatten.mkString(" / "),
            preferenceLabel = List(preferenceInfo.seatPreference, preferenceInfo.mealPreference, Option.when(preferenceInfo.quietSeatPreferred)("quietSeat").getOrElse("")).filter(_.nonEmpty).mkString(" / "),
            requirementLabel = if requirementLabel.nonEmpty then requirementLabel else "none",
            warningLevel = if specialRequirementInfo.assistanceType != "none" || specialRequirementInfo.requirementNote.exists(_.trim.nonEmpty) then "attention" else "normal"
          )
          ManagerFlightOrderTravelerResponse(
            travelerId = resultSet.getString("traveler_id"),
            fullName = fullName,
            documentNumber = documentNumber,
            basicInfo = basicInfo,
            documentInfo = documentInfo,
            contactInfo = contactInfo,
            preferenceInfo = preferenceInfo,
            specialRequirementInfo = specialRequirementInfo,
            serviceSummary = serviceSummary
          )
        }
      }

  def requireManagedFlightBlocking(connection: Connection, managerId: String, flightId: String): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        select 1
        from airline_managers m
        join flights f on f.airline_id = m.airline_id
        where m.manager_id = ? and f.flight_id = ?
        limit 1
      """
    ) { statement =>
      statement.setString(1, managerId)
      statement.setString(2, flightId)
      val resultSet = statement.executeQuery()
      try
        if !resultSet.next() then throw new IllegalArgumentException(s"Flight '$flightId' does not belong to manager '$managerId'")
      finally resultSet.close()
    }

  def readOptionalString(resultSet: ResultSet, columnName: String): Option[String] =
    Option(resultSet.getString(columnName)).map(_.trim).filter(_.nonEmpty)

  def parseStringListJson(jsonText: String): Option[List[String]] =
    decode[List[String]](jsonText).toOption.map(_.filter(_.trim.nonEmpty))

  def extractStringField(snapshotJson: String, fieldName: String): Option[String] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[String](fieldName).toOption).filter(_.trim.nonEmpty)

  def extractStringListField(snapshotJson: String, fieldName: String): Option[List[String]] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[List[String]](fieldName).toOption).map(_.filter(_.trim.nonEmpty))
