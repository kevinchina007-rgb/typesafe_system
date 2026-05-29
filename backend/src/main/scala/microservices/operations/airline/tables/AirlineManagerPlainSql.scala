package com.typesafe.travel.persistence.operations

import cats.effect.IO
import com.typesafe.travel.operations.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import io.circe.parser.decode

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Instant, LocalDate, OffsetDateTime}
import java.util.UUID

object AirlineManagerPlainSql:
  private final case class ManagerFlightCabinRow(flight: ManagerFlightPlannerResponse, cabinInventory: Option[ManagerCabinInventoryPlannerResponse])
  private final case class ManagerFlightOrderRow(
      orderId: String,
      orderItemId: String,
      buyerUserId: String,
      buyerNickname: String,
      cabinClass: String,
      orderStatus: String,
      orderCreatedAt: String,
      travelerIds: List[String]
  )

  def registerAirline(connection: Connection, input: RegisterAirlineManagerPlannerRequest, passwordHash: String, now: Instant): IO[ManagerSessionPlannerResponse] =
    ManagerPlannerPlainSql.registerAirline(connection, input, passwordHash, now)

  def listFlights(connection: Connection, input: ManagerFlightsPlannerRequest): IO[ManagerFlightListPlannerResponse] =
    IO.blocking {
      val filters = scala.collection.mutable.ListBuffer.empty[String]
      val parameters = scala.collection.mutable.ListBuffer[AnyRef](input.managerId)

      input.departureAirports.map(_.filter(_.trim.nonEmpty).map(_.trim.toUpperCase)).filter(_.nonEmpty).foreach { airports =>
        filters += s"f.departure_airport in (${airports.map(_ => "?").mkString(", ")})"
        parameters ++= airports
      }

      input.arrivalAirports.map(_.filter(_.trim.nonEmpty).map(_.trim.toUpperCase)).filter(_.nonEmpty).foreach { airports =>
        filters += s"f.arrival_airport in (${airports.map(_ => "?").mkString(", ")})"
        parameters ++= airports
      }

      input.departureDate.map(_.trim).filter(_.nonEmpty).foreach { departureDate =>
        filters += "cast(f.departure_time as date) = cast(? as date)"
        parameters += departureDate
      }

      input.timeRange.map(_.trim).filter(value => value.nonEmpty && value != "all").foreach { timeRange =>
        val parts = timeRange.split("-").toList
        if parts.size == 2 then
          filters += "cast(f.departure_time as time) between cast(? as time) and cast(? as time)"
          parameters += parts.head
          parameters += parts(1)
      }

      val extraWhere = if filters.isEmpty then "" else filters.mkString(" and ", " and ", "")
      val sortDirection = input.sortDirection.map(_.trim.toLowerCase).filter(_ == "desc").map(_ => "desc").getOrElse("asc")
      PlainSqlSupport.withStatement(
        connection,
        s"""
          select f.flight_id, f.airline_id, a.name as airline_name, a.code as airline_code, f.flight_number,
                 f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.status,
                 f.base_price_amount, f.base_price_currency, f.created_at,
                 ci.inventory_id, ci.cabin_class, ci.available_seats, ci.unit_price_amount,
                 ci.unit_price_currency, ci.status as cabin_inventory_status
          from airline_managers m
          join airlines a on a.airline_id = m.airline_id
          join flights f on f.airline_id = a.airline_id
          left join flight_cabin_inventories ci on ci.flight_id = f.flight_id
          where m.manager_id = ?
          $extraWhere
          order by
            f.departure_time $sortDirection,
            f.flight_id,
            case upper(ci.cabin_class)
              when 'ECONOMY' then 1
              when 'PREMIUM_ECONOMY' then 2
              when 'BUSINESS' then 3
              when 'FIRST' then 4
              else 5
            end,
            ci.inventory_id
          limit 800
        """
      ) { statement =>
        parameters.zipWithIndex.foreach { case (parameter, index) =>
          statement.setObject(index + 1, parameter)
        }
        val rows = PlainSqlSupport.queryList(statement)(readFlightWithOptionalCabin)
        val orderedFlightIds = rows.map(_.flight.flightId).distinct
        val flightsById = rows.groupMap(_.flight.flightId)(_.flight).view.mapValues(_.head).toMap
        val inventoriesByFlightId = rows.groupMap(_.flight.flightId)(_.cabinInventory).view.mapValues(_.flatten).toMap
        ManagerFlightListPlannerResponse(
          orderedFlightIds.flatMap(flightId => flightsById.get(flightId).map(_.copy(cabinInventories = inventoriesByFlightId.getOrElse(flightId, Nil))))
        )
      }
    }

  def listFlightOrders(connection: Connection, input: ManagerFlightOrdersPlannerRequest): IO[ManagerFlightOrderListPlannerResponse] =
    IO.blocking {
      requireManagedFlightBlocking(connection, input.managerId, input.flightId)
      val rows = PlainSqlSupport.withStatement(
        connection,
        """
          select o.order_id, o.buyer_user_id, u.nickname as buyer_nickname, o.status as order_status, o.created_at,
                 li.order_item_id, li.cabin_class, li.traveler_ids_json, li.snapshot_json
          from order_line_items li
          join orders o on o.order_id = li.order_id
          join users u on u.user_id = o.buyer_user_id
          where lower(li.item_kind) = lower(?)
            and (li.flight_id = ? or li.snapshot_json::jsonb ->> 'flightId' = ?)
          order by o.created_at desc, li.order_item_id
        """
      ) { statement =>
        statement.setString(1, "Flight")
        statement.setString(2, input.flightId)
        statement.setString(3, input.flightId)
        PlainSqlSupport.queryList(statement)(readManagerFlightOrderRow)
      }
      val travelerIds = rows.flatMap(_.travelerIds).distinct
      val travelersById = listTravelersByIds(connection, travelerIds).map(traveler => traveler.travelerId -> traveler).toMap
      ManagerFlightOrderListPlannerResponse(
        rows.map(row =>
          ManagerFlightOrderPlannerResponse(
            orderId = row.orderId,
            orderItemId = row.orderItemId,
            buyerUserId = row.buyerUserId,
            buyerNickname = row.buyerNickname,
            cabinClass = row.cabinClass,
            orderStatus = row.orderStatus,
            orderCreatedAt = row.orderCreatedAt,
            travelerIds = row.travelerIds,
            travelers = row.travelerIds.flatMap(travelersById.get)
          )
        )
      )
    }

  def findAirlineIdForManager(connection: Connection, managerId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select airline_id from airline_managers where manager_id = ?") { statement =>
        statement.setString(1, managerId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then resultSet.getString("airline_id") else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
        finally resultSet.close()
      }
    }

  def updateAirlineManagerDisplayName(connection: Connection, managerId: String, displayName: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update airline_managers set display_name = ? where manager_id = ?") { statement =>
        statement.setString(1, displayName.trim)
        statement.setString(2, managerId)
        statement.executeUpdate()
      }
    }

  def updateAirlineProfile(connection: Connection, airlineId: String, airlineName: String, airlineCode: String, logoAssetPath: Option[String]): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update airlines set name = ?, code = ?, logo_asset_path = ? where airline_id = ?") { statement =>
        statement.setString(1, airlineName.trim)
        statement.setString(2, airlineCode.trim)
        statement.setString(3, logoAssetPath.map(_.trim).filter(_.nonEmpty).orNull)
        statement.setString(4, airlineId)
        statement.executeUpdate()
      }
    }

  def readAirlineManagerSession(connection: Connection, managerId: String, fallbackCreatedAt: Instant): IO[ManagerSessionPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select m.manager_id, m.email, m.display_name, m.status, m.airline_id, a.logo_asset_path, m.created_at
          from airline_managers m
          join airlines a on a.airline_id = m.airline_id
          where m.manager_id = ?
        """
      ) { statement =>
        statement.setString(1, managerId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            ManagerSessionPlannerResponse(
              managerId = resultSet.getString("manager_id"),
              managerType = "Airline",
              email = resultSet.getString("email"),
              displayName = resultSet.getString("display_name"),
              status = resultSet.getString("status"),
              scopeId = resultSet.getString("airline_id"),
              logoAssetPath = Option(resultSet.getString("logo_asset_path")).map(_.trim).filter(_.nonEmpty),
              createdAt = Option(resultSet.getTimestamp("created_at")).map(_.toInstant.toString).getOrElse(fallbackCreatedAt.toString)
            )
          else throw new IllegalArgumentException(s"Manager '$managerId' was not found")
        finally resultSet.close()
      }
    }

  def insertFlight(
      connection: Connection,
      flightId: String,
      airlineId: String,
      input: CreateManagerFlightPlannerRequest,
      departureTime: OffsetDateTime,
      arrivalTime: OffsetDateTime,
      basePrice: BigDecimal,
      now: Instant
  ): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into flights(flight_id, airline_id, flight_number, departure_airport, arrival_airport, departure_time, arrival_time, status, base_price_amount, base_price_currency, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, flightId)
        statement.setString(2, airlineId)
        statement.setString(3, input.flightNumber.trim)
        statement.setString(4, input.departureAirport.trim.toUpperCase)
        statement.setString(5, input.arrivalAirport.trim.toUpperCase)
        statement.setObject(6, departureTime)
        statement.setObject(7, arrivalTime)
        statement.setString(8, "OpenForBooking")
        statement.setBigDecimal(9, basePrice.bigDecimal)
        statement.setString(10, input.currency.trim.toUpperCase)
        statement.setTimestamp(11, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def insertCabinInventory(connection: Connection, flightId: String, cabinClass: String, seats: Int, price: BigDecimal, currency: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "insert into flight_cabin_inventories(inventory_id, flight_id, cabin_class, available_seats, unit_price_amount, unit_price_currency, status) values (?, ?, ?, ?, ?, ?, ?)") { statement =>
        statement.setString(1, s"cabin-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, flightId)
        statement.setString(3, cabinClass)
        statement.setInt(4, seats)
        statement.setBigDecimal(5, price.bigDecimal)
        statement.setString(6, currency.trim.toUpperCase)
        statement.setString(7, "Open")
        statement.executeUpdate()
      }
    }

  def requireManagedFlight(connection: Connection, managerId: String, flightId: String): IO[Unit] =
    IO.blocking {
      requireManagedFlightBlocking(connection, managerId, flightId)
    }

  def findFlightStatus(connection: Connection, flightId: String): IO[String] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "select status from flights where flight_id = ?") { statement =>
        statement.setString(1, flightId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then resultSet.getString("status") else throw new IllegalArgumentException(s"Flight '$flightId' was not found")
        finally resultSet.close()
      }
    }

  def updateFlightStatus(connection: Connection, flightId: String, status: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update flights set status = ? where flight_id = ?") { statement =>
        statement.setString(1, status)
        statement.setString(2, flightId)
        statement.executeUpdate()
      }
    }

  def updateCabinInventoryStatusForFlight(connection: Connection, flightId: String, status: String): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update flight_cabin_inventories set status = ? where flight_id = ?") { statement =>
        statement.setString(1, status)
        statement.setString(2, flightId)
        statement.executeUpdate()
      }
    }

  def readFlight(connection: Connection, flightId: String): IO[ManagerFlightPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select f.flight_id, f.airline_id, a.name as airline_name, a.code as airline_code, f.flight_number,
                 f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.status,
                 f.base_price_amount, f.base_price_currency, f.created_at
          from flights f
          join airlines a on a.airline_id = f.airline_id
          where f.flight_id = ?
        """
      ) { statement =>
        statement.setString(1, flightId)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then
            readFlightWithoutCabins(resultSet).copy(cabinInventories = listCabinInventories(connection, flightId))
          else throw new IllegalStateException(s"Flight '$flightId' could not be read")
        finally resultSet.close()
      }
    }

  private def listCabinInventories(connection: Connection, flightId: String): List[ManagerCabinInventoryPlannerResponse] =
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

  private def readFlightWithoutCabins(resultSet: ResultSet): ManagerFlightPlannerResponse =
    ManagerFlightPlannerResponse(
      flightId = resultSet.getString("flight_id"),
      airlineId = resultSet.getString("airline_id"),
      airlineName = resultSet.getString("airline_name"),
      airlineCode = resultSet.getString("airline_code"),
      flightNumber = resultSet.getString("flight_number"),
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

  private def readCabinInventory(resultSet: ResultSet): ManagerCabinInventoryPlannerResponse =
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

  private def readFlightWithOptionalCabin(resultSet: ResultSet): ManagerFlightCabinRow =
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

  private def readManagerFlightOrderRow(resultSet: ResultSet): ManagerFlightOrderRow =
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

  private def listTravelersByIds(connection: Connection, travelerIds: List[String]): List[ManagerFlightOrderTravelerPlannerResponse] =
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
          ManagerFlightOrderTravelerPlannerResponse(
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

  private def requireManagedFlightBlocking(connection: Connection, managerId: String, flightId: String): Unit =
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

  private def readOptionalString(resultSet: ResultSet, columnName: String): Option[String] =
    Option(resultSet.getString(columnName)).map(_.trim).filter(_.nonEmpty)

  private def parseStringListJson(jsonText: String): Option[List[String]] =
    decode[List[String]](jsonText).toOption.map(_.filter(_.trim.nonEmpty))

  private def extractStringField(snapshotJson: String, fieldName: String): Option[String] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[String](fieldName).toOption).filter(_.trim.nonEmpty)

  private def extractStringListField(snapshotJson: String, fieldName: String): Option[List[String]] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[List[String]](fieldName).toOption).map(_.filter(_.trim.nonEmpty))
