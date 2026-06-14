package com.typesafe.travel.persistence.attraction

import cats.effect.IO
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, Date, Timestamp}
import java.time.{Instant, LocalDate}
import java.util.UUID

object AttractionPlannerWriteSql:
  def create(connection: Connection, input: CreateAttractionPlannerRequest, now: Instant): IO[Attraction] =
    IO.blocking {
      val attraction = Attraction(
        attractionId = AttractionId(s"attraction-${UUID.randomUUID().toString.take(12)}"),
        managerId = ManagerId(input.managerId),
        attractionName = input.attractionName.trim,
        city = input.city.trim,
        location = input.location.trim,
        description = input.description.trim,
        imageUrl = input.imageUrl.map(_.trim).filter(_.nonEmpty),
        attractionStatus = AttractionStatus.Published,
        ticketTypes = Vector.empty,
        createdAt = now
      )
      PlainSqlSupport.withStatement(
        connection,
        "insert into attractions(attraction_id, manager_id, name, city, location, description, image_url, status, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, attraction.attractionId.value)
        statement.setString(2, attraction.managerId.value)
        statement.setString(3, attraction.attractionName)
        statement.setString(4, attraction.city)
        statement.setString(5, attraction.location)
        statement.setString(6, attraction.description)
        statement.setString(7, attraction.imageUrl.orNull)
        statement.setString(8, attraction.attractionStatus.toString)
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
      attraction
    }

  def createTicketType(connection: Connection, input: CreateAttractionTicketTypePlannerRequest, now: Instant): IO[Attraction] =
    IO.blocking {
      verifyManagerOwnsAttraction(connection, input.managerId, input.attractionId)
      val ticketTypeId = s"ticket-type-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into ticket_types(
            ticket_type_id, attraction_id, name, description, price_amount, price_currency, available_from_date,
            available_to_date, total_quantity, valid_weekdays, status, created_at
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, ticketTypeId)
        statement.setString(2, input.attractionId)
        statement.setString(3, input.ticketTypeName.trim)
        statement.setString(4, input.description.trim)
        statement.setBigDecimal(5, BigDecimal(input.unitPrice).bigDecimal)
        statement.setString(6, input.currency.trim)
        statement.setDate(7, Date.valueOf(LocalDate.parse(input.availableFromDate)))
        statement.setDate(8, Date.valueOf(LocalDate.parse(input.availableToDate)))
        statement.setInt(9, input.totalQuantity)
        statement.setString(10, input.validWeekdays.map(_.trim).filter(_.nonEmpty).mkString(","))
        statement.setString(11, TicketTypeStatus.Active.toString)
        statement.setTimestamp(12, Timestamp.from(now))
        statement.executeUpdate()
      }
      AttractionPlannerReadSql.reloadDetails(connection, input.attractionId)
    }

  def createTicketSession(connection: Connection, input: CreateAttractionTicketSessionPlannerRequest, now: Instant): IO[Attraction] =
    IO.blocking {
      verifyManagerOwnsAttraction(connection, input.managerId, input.attractionId)
      val sessionId = s"ticket-session-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into attraction_ticket_sessions(session_id, ticket_type_id, session_name, use_date, starts_at, ends_at, capacity, status, created_at)
          values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, sessionId)
        statement.setString(2, input.ticketTypeId)
        statement.setString(3, input.sessionName.trim)
        statement.setDate(4, Date.valueOf(LocalDate.parse(input.useDate)))
        statement.setTimestamp(5, Timestamp.from(Instant.parse(input.startsAt)))
        statement.setTimestamp(6, Timestamp.from(Instant.parse(input.endsAt)))
        statement.setInt(7, input.capacity)
        statement.setString(8, AttractionTicketSessionStatus.Active.toString)
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
      AttractionPlannerReadSql.reloadDetails(connection, input.attractionId)
    }

  def createTicketRule(connection: Connection, input: CreateAttractionTicketRulePlannerRequest, now: Instant): IO[Attraction] =
    IO.blocking {
      verifyManagerOwnsAttraction(connection, input.managerId, input.attractionId)
      val ruleId = s"ticket-rule-${UUID.randomUUID().toString.take(12)}"
      PlainSqlSupport.withStatement(
        connection,
        "insert into ticket_type_rules(rule_id, ticket_type_id, rule_type, rule_config_json, created_at) values (?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, ruleId)
        statement.setString(2, input.ticketTypeId)
        statement.setString(3, input.ruleType.trim)
        statement.setString(4, ruleConfigJson(input))
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
      AttractionPlannerReadSql.reloadDetails(connection, input.attractionId)
    }

  private def verifyManagerOwnsAttraction(connection: Connection, managerId: String, attractionId: String): Unit =
    PlainSqlSupport.withStatement(connection, "select 1 from attractions where attraction_id = ? and manager_id = ?") { statement =>
      statement.setString(1, attractionId)
      statement.setString(2, managerId)
      val resultSet = statement.executeQuery()
      try if !resultSet.next() then throw AttractionError.AttractionWasNotOwnedByManager(AttractionId(attractionId), ManagerId(managerId))
      finally resultSet.close()
    }

  private def ruleConfigJson(input: CreateAttractionTicketRulePlannerRequest): String =
    def field(name: String, value: String): String = s""""$name":"${jsonEscape(value)}""""
    def numberField(name: String, value: Int): String = s""""$name":$value"""
    val fields =
      List(
        Some(field("ruleType", input.ruleType.trim)),
        input.ageValue.map(numberField("ageValue", _)),
        input.minAge.map(numberField("minAge", _)),
        input.maxAge.map(numberField("maxAge", _)),
        input.documentType.map(field("documentType", _)),
        input.documentNumberPrefix.map(field("documentNumberPrefix", _))
      ).flatten
    fields.mkString("{", ",", "}")

  private def jsonEscape(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")
