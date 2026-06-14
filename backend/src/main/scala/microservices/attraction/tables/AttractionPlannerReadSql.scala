package com.typesafe.travel.persistence.attraction

import cats.effect.IO
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.parse

import java.sql.{Connection, ResultSet}
import java.time.{DayOfWeek, LocalDate}
import scala.util.Try

object AttractionPlannerReadSql:
  def suggestions(connection: Connection, input: AttractionSuggestionRequest): IO[AttractionSuggestionListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select attraction_id, name, city, location
          from attractions
          where status = ?
            and (name ilike ? or city ilike ? or location ilike ? or description ilike ?)
          order by
            case when lower(name) = lower(?) then 0 else 1 end,
            case when name ilike ? then 0 when city ilike ? then 1 when location ilike ? then 2 when description ilike ? then 3 else 4 end,
            created_at,
            attraction_id
          limit 12
        """
      ) { statement =>
        val q = s"%${input.q.trim}%"
        statement.setString(1, AttractionStatus.Published.toString)
        statement.setString(2, q)
        statement.setString(3, q)
        statement.setString(4, q)
        statement.setString(5, q)
        statement.setString(6, input.q.trim)
        statement.setString(7, q)
        statement.setString(8, q)
        statement.setString(9, q)
        statement.setString(10, q)
        AttractionSuggestionListPlannerResponse(
          PlainSqlSupport.queryList(statement) { resultSet =>
            AttractionSuggestionPlannerResponse(
              "attraction",
              resultSet.getString("attraction_id"),
              resultSet.getString("name"),
              s"${resultSet.getString("city")} 路 ${resultSet.getString("location")}"
            )
          }
        )
      }
    }

  def list(connection: Connection, input: ListAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    IO.blocking {
      val city = input.city.map(_.trim).filter(_.nonEmpty)
      val keyword = input.keyword.map(_.trim).filter(_.nonEmpty)
      val cityFilterSql = city.map(_ => " and city ilike ?").getOrElse("")
      val keywordFilterSql = keyword.map(_ => " and (name ilike ? or city ilike ? or location ilike ? or description ilike ?)").getOrElse("")
      val citySortSql = city.map(_ => "case when lower(city) = lower(?) then 0 else 1 end,").getOrElse("")
      val keywordSortSql = keyword
        .map(_ => "case when lower(name) = lower(?) then 0 when name ilike ? then 1 when location ilike ? then 2 when description ilike ? then 3 else 4 end,")
        .getOrElse("")
      val sql =
        s"""
           select attraction_id, manager_id, name, city, location, description, image_url, status, created_at
           from attractions
           where status = ?$cityFilterSql$keywordFilterSql
           order by ${citySortSql}${keywordSortSql}created_at, attraction_id
         """
      PlainSqlSupport.withStatement(connection, sql) { statement =>
        var index = 1
        statement.setString(index, AttractionStatus.Published.toString)
        index += 1
        city.foreach { value =>
          statement.setString(index, s"%$value%")
          index += 1
        }
        keyword.foreach { value =>
          val pattern = s"%$value%"
          statement.setString(index, pattern)
          statement.setString(index + 1, pattern)
          statement.setString(index + 2, pattern)
          statement.setString(index + 3, pattern)
          index += 4
        }
        city.foreach { value =>
          statement.setString(index, value)
          index += 1
        }
        keyword.foreach { value =>
          val pattern = s"%$value%"
          statement.setString(index, value)
          statement.setString(index + 1, pattern)
          statement.setString(index + 2, pattern)
          statement.setString(index + 3, pattern)
        }
        AttractionListPlannerResponse(PlainSqlSupport.queryList(statement)(readAttraction(connection)))
      }
    }

  def details(connection: Connection, input: GetAttractionDetailsPlannerRequest): IO[Attraction] =
    IO.blocking(readDetails(connection, input.attractionId))

  def reloadDetails(connection: Connection, attractionId: String): Attraction =
    readDetails(connection, attractionId)

  def listManaged(connection: Connection, input: ListManagedAttractionsPlannerRequest): IO[AttractionListPlannerResponse] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        "select attraction_id, manager_id, name, city, location, description, image_url, status, created_at from attractions where manager_id = ? order by created_at, attraction_id"
      ) { statement =>
        statement.setString(1, input.managerId)
        AttractionListPlannerResponse(PlainSqlSupport.queryList(statement)(readAttraction(connection)))
      }
    }

  private def readDetails(connection: Connection, attractionId: String): Attraction =
    PlainSqlSupport.withStatement(
      connection,
      "select attraction_id, manager_id, name, city, location, description, image_url, status, created_at from attractions where attraction_id = ?"
    ) { statement =>
      statement.setString(1, attractionId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then readAttraction(connection)(resultSet)
        else throw AttractionError.AttractionWasNotFound(AttractionId(attractionId))
      finally resultSet.close()
    }

  private def readAttraction(connection: Connection)(resultSet: ResultSet): Attraction =
    val attractionId = AttractionId(resultSet.getString("attraction_id"))
    Attraction(
      attractionId = attractionId,
      managerId = ManagerId(resultSet.getString("manager_id")),
      attractionName = resultSet.getString("name"),
      city = resultSet.getString("city"),
      location = resultSet.getString("location"),
      description = resultSet.getString("description"),
      imageUrl = Option(resultSet.getString("image_url")).filter(_.nonEmpty),
      attractionStatus = AttractionStatus.fromText(resultSet.getString("status")),
      ticketTypes = loadTicketTypes(connection, attractionId),
      createdAt = resultSet.getTimestamp("created_at").toInstant
    )

  private def loadTicketTypes(connection: Connection, attractionId: AttractionId): Vector[TicketType] =
    PlainSqlSupport.withStatement(
      connection,
      "select ticket_type_id, name, description, price_amount, price_currency, available_from_date, available_to_date, total_quantity, valid_weekdays, status, created_at from ticket_types where attraction_id = ? order by created_at, ticket_type_id"
    ) { statement =>
      statement.setString(1, attractionId.value)
      PlainSqlSupport.queryList(statement) { resultSet =>
        val ticketTypeId = TicketTypeId(resultSet.getString("ticket_type_id"))
        TicketType(
          ticketTypeId,
          attractionId,
          resultSet.getString("name"),
          resultSet.getString("description"),
          Money.create(resultSet.getBigDecimal("price_amount"), Currency.fromText(resultSet.getString("price_currency"))).fold(throw _, identity),
          Option(resultSet.getDate("available_from_date")).map(_.toLocalDate).getOrElse(java.time.LocalDate.now()),
          Option(resultSet.getDate("available_to_date")).map(_.toLocalDate).getOrElse(java.time.LocalDate.now()),
          resultSet.getInt("total_quantity"),
          decodeWeekdays(Option(resultSet.getString("valid_weekdays")).getOrElse("")),
          TicketTypeStatus.fromText(resultSet.getString("status")),
          loadSessions(connection, ticketTypeId),
          loadEligibilityRules(connection, ticketTypeId),
          resultSet.getTimestamp("created_at").toInstant
        )
      }.toVector
    }

  private def loadEligibilityRules(connection: Connection, ticketTypeId: TicketTypeId): Vector[TicketEligibilityRule] =
    PlainSqlSupport.withStatement(
      connection,
      "select rule_id, rule_type, rule_config_json, created_at from ticket_type_rules where ticket_type_id = ? order by created_at, rule_id"
    ) { statement =>
      statement.setString(1, ticketTypeId.value)
      PlainSqlSupport.queryList(statement) { resultSet =>
        TicketEligibilityRule(
          TicketEligibilityRuleId(resultSet.getString("rule_id")),
          ticketTypeId,
          TicketEligibilityRuleType.fromText(resultSet.getString("rule_type")),
          resultSet.getString("rule_config_json"),
          resultSet.getTimestamp("created_at").toInstant
        )
      }.toVector
    }

  private def loadSessions(connection: Connection, ticketTypeId: TicketTypeId): Vector[AttractionTicketSession] =
    PlainSqlSupport.withStatement(
      connection,
      "select session_id, session_name, use_date, starts_at, ends_at, capacity, status, created_at from attraction_ticket_sessions where ticket_type_id = ? order by use_date, starts_at"
    ) { statement =>
      statement.setString(1, ticketTypeId.value)
      PlainSqlSupport.queryList(statement) { resultSet =>
        AttractionTicketSession(
          AttractionTicketSessionId(resultSet.getString("session_id")),
          ticketTypeId,
          resultSet.getString("session_name"),
          resultSet.getDate("use_date").toLocalDate,
          resultSet.getTimestamp("starts_at").toInstant,
          resultSet.getTimestamp("ends_at").toInstant,
          resultSet.getInt("capacity"),
          AttractionTicketSessionStatus.fromText(resultSet.getString("status")),
          resultSet.getTimestamp("created_at").toInstant
        )
      }.toVector
    }

  private def decodeWeekdays(rawValue: String): Set[DayOfWeek] =
    rawValue.split(",").toList.map(_.trim).filter(_.nonEmpty).flatMap(value => Try(DayOfWeek.valueOf(value)).toOption).toSet
