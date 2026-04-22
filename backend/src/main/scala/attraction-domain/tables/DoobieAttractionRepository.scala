package com.typesafe.travel.persistence.attraction

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import doobie.*
import doobie.implicits.*

import java.time.{DayOfWeek, Instant, LocalDate}
import java.util.UUID

final class DoobieAttractionRepository[F[_]: Async](
    transactor: Transactor[F]
) extends AttractionRepository[F]:
  override def nextAttractionId: F[AttractionId] =
    Sync[F].delay(AttractionId(s"attraction-${UUID.randomUUID().toString.take(12)}"))

  override def nextTicketTypeId: F[TicketTypeId] =
    Sync[F].delay(TicketTypeId(s"ticket-type-${UUID.randomUUID().toString.take(12)}"))

  override def nextTicketEligibilityRuleId: F[TicketEligibilityRuleId] =
    Sync[F].delay(TicketEligibilityRuleId(s"ticket-rule-${UUID.randomUUID().toString.take(12)}"))

  override def nextAttractionTicketSessionId: F[AttractionTicketSessionId] =
    Sync[F].delay(AttractionTicketSessionId(s"ticket-session-${UUID.randomUUID().toString.take(12)}"))

  override def findAttractionById(attractionId: AttractionId): F[Option[Attraction]] =
    sql"""
      select attraction_id, manager_id, name, city, location, description, status, created_at
      from attractions
      where attraction_id = ${attractionId.value}
    """.query[(String, String, String, String, String, String, String, Instant)].option.transact(transactor).flatMap(_.traverse(buildAttraction))

  override def findAttractionsByManagerId(managerId: ManagerId): F[List[Attraction]] =
    sql"""
      select attraction_id, manager_id, name, city, location, description, status, created_at
      from attractions
      where manager_id = ${managerId.value}
      order by created_at, attraction_id
    """.query[(String, String, String, String, String, String, String, Instant)].to[List].transact(transactor).flatMap(_.traverse(buildAttraction))

  override def listPublishedAttractions: F[List[Attraction]] =
    sql"""
      select attraction_id, manager_id, name, city, location, description, status, created_at
      from attractions
      where status = ${AttractionStatus.Published.toString}
      order by created_at, attraction_id
    """.query[(String, String, String, String, String, String, String, Instant)].to[List].transact(transactor).flatMap(_.traverse(buildAttraction))

  override def saveAttraction(attraction: Attraction): F[Attraction] =
    (
      for
        updatedRowCount <- sql"""
          update attractions
          set manager_id = ${attraction.managerId.value},
              name = ${attraction.attractionName},
              city = ${attraction.city},
              location = ${attraction.location},
              description = ${attraction.description},
              status = ${attraction.attractionStatus.toString},
              created_at = ${attraction.createdAt}
          where attraction_id = ${attraction.attractionId.value}
        """.update.run
        _ <- if updatedRowCount > 0 then ().pure[ConnectionIO]
        else
          sql"""
            insert into attractions(attraction_id, manager_id, name, city, location, description, status, created_at)
            values(
              ${attraction.attractionId.value},
              ${attraction.managerId.value},
              ${attraction.attractionName},
              ${attraction.city},
              ${attraction.location},
              ${attraction.description},
              ${attraction.attractionStatus.toString},
              ${attraction.createdAt}
            )
          """.update.run.void
        _ <- sql"delete from attraction_ticket_sessions where ticket_type_id in (select ticket_type_id from ticket_types where attraction_id = ${attraction.attractionId.value})".update.run
        _ <- sql"delete from ticket_type_rules where ticket_type_id in (select ticket_type_id from ticket_types where attraction_id = ${attraction.attractionId.value})".update.run
        _ <- sql"delete from ticket_types where attraction_id = ${attraction.attractionId.value}".update.run
        _ <- attraction.ticketTypes.traverse_ { ticketType =>
          for
            _ <- sql"""
              insert into ticket_types(ticket_type_id, attraction_id, name, description, price_amount, price_currency, available_from_date, available_to_date, total_quantity, valid_weekdays, status, created_at)
              values(
                ${ticketType.ticketTypeId.value},
                ${attraction.attractionId.value},
                ${ticketType.ticketTypeName},
                ${ticketType.description},
                ${ticketType.unitPrice.amount},
                ${ticketType.unitPrice.currency.toString},
                ${ticketType.availableFromDate},
                ${ticketType.availableToDate},
                ${ticketType.totalQuantity},
                ${encodeWeekdays(ticketType.validWeekdays)},
                ${ticketType.ticketTypeStatus.toString},
                ${ticketType.createdAt}
              )
            """.update.run
            _ <- ticketType.sessions.traverse_ { session =>
              sql"""
                insert into attraction_ticket_sessions(
                  session_id, ticket_type_id, session_name, use_date, starts_at, ends_at, capacity, status, created_at
                ) values (
                  ${session.sessionId.value},
                  ${ticketType.ticketTypeId.value},
                  ${session.sessionName},
                  ${session.useDate},
                  ${session.startsAt},
                  ${session.endsAt},
                  ${session.capacity},
                  ${session.status.toString},
                  ${session.createdAt}
                )
              """.update.run
            }
            _ <- ticketType.eligibilityRules.traverse_ { rule =>
              sql"""
                insert into ticket_type_rules(rule_id, ticket_type_id, rule_type, rule_config_json, created_at)
                values(
                  ${rule.ruleId.value},
                  ${ticketType.ticketTypeId.value},
                  ${rule.ruleType.toString},
                  ${rule.ruleConfigJson},
                  ${rule.createdAt}
                )
              """.update.run
            }
          yield ()
        }
      yield ()
    ).transact(transactor).as(attraction)

  private def buildAttraction(row: (String, String, String, String, String, String, String, Instant)): F[Attraction] =
    val (attractionIdValue, managerIdValue, nameValue, cityValue, locationValue, descriptionValue, statusValue, createdAtValue) = row
    loadTicketTypes(AttractionId(attractionIdValue)).map { ticketTypes =>
      restorePersistedAttraction(
        attractionId = AttractionId(attractionIdValue),
        managerId = ManagerId(managerIdValue),
        attractionName = nameValue,
        city = cityValue,
        location = locationValue,
        description = descriptionValue,
        attractionStatus = AttractionStatus.fromText(statusValue),
        ticketTypes = ticketTypes,
        createdAt = createdAtValue
      )
    }

  private def loadTicketTypes(attractionId: AttractionId): F[Vector[TicketType]] =
    sql"""
      select ticket_type_id, name, description, price_amount, price_currency, available_from_date, available_to_date, total_quantity, valid_weekdays, status, created_at
      from ticket_types
      where attraction_id = ${attractionId.value}
      order by created_at, ticket_type_id
    """.query[(String, String, String, BigDecimal, String, LocalDate, LocalDate, Int, String, String, Instant)].to[List].transact(transactor).flatMap(
      _.traverse { case (ticketTypeIdValue, nameValue, descriptionValue, amountValue, currencyValue, availableFromDateValue, availableToDateValue, totalQuantityValue, validWeekdaysValue, statusValue, createdAtValue) =>
        for
          currency <- Async[F].fromEither(Either.catchNonFatal(Currency.valueOf(currencyValue)))
          unitPrice <- Async[F].fromEither(Money.create(amountValue, currency))
          rules <- loadRules(TicketTypeId(ticketTypeIdValue))
          sessions <- loadSessions(TicketTypeId(ticketTypeIdValue))
        yield restorePersistedTicketType(
          ticketTypeId = TicketTypeId(ticketTypeIdValue),
          attractionId = attractionId,
          ticketTypeName = nameValue,
          description = descriptionValue,
          unitPrice = unitPrice,
          availableFromDate = availableFromDateValue,
          availableToDate = availableToDateValue,
          totalQuantity = totalQuantityValue,
          validWeekdays = decodeWeekdays(validWeekdaysValue),
          ticketTypeStatus = TicketTypeStatus.fromText(statusValue),
          sessions = sessions,
          eligibilityRules = rules,
          createdAt = createdAtValue
        )
      }.map(_.toVector)
    )

  private def loadRules(ticketTypeId: TicketTypeId): F[Vector[TicketEligibilityRule]] =
    sql"""
      select rule_id, rule_type, rule_config_json, created_at
      from ticket_type_rules
      where ticket_type_id = ${ticketTypeId.value}
      order by created_at, rule_id
    """.query[(String, String, String, Instant)].to[List].transact(transactor).map(
      _.map { case (ruleIdValue, ruleTypeValue, ruleConfigJsonValue, createdAtValue) =>
        restorePersistedTicketEligibilityRule(
          ruleId = TicketEligibilityRuleId(ruleIdValue),
          ticketTypeId = ticketTypeId,
          ruleType = TicketEligibilityRuleType.fromText(ruleTypeValue),
          ruleConfigJson = ruleConfigJsonValue,
          createdAt = createdAtValue
        )
      }.toVector
    )

  private def loadSessions(ticketTypeId: TicketTypeId): F[Vector[AttractionTicketSession]] =
    sql"""
      select session_id, session_name, use_date, starts_at, ends_at, capacity, status, created_at
      from attraction_ticket_sessions
      where ticket_type_id = ${ticketTypeId.value}
      order by use_date, starts_at, session_id
    """.query[(String, String, LocalDate, Instant, Instant, Int, String, Instant)].to[List].transact(transactor).map(
      _.map { case (sessionIdValue, sessionNameValue, useDateValue, startsAtValue, endsAtValue, capacityValue, statusValue, createdAtValue) =>
        restorePersistedAttractionTicketSession(
          sessionId = AttractionTicketSessionId(sessionIdValue),
          ticketTypeId = ticketTypeId,
          sessionName = sessionNameValue,
          useDate = useDateValue,
          startsAt = startsAtValue,
          endsAt = endsAtValue,
          capacity = capacityValue,
          status = AttractionTicketSessionStatus.fromText(statusValue),
          createdAt = createdAtValue
        )
      }.toVector
    )

  private def encodeWeekdays(validWeekdays: Set[DayOfWeek]): String =
    validWeekdays.toList.sortBy(_.getValue).map(_.toString).mkString(",")

  private def decodeWeekdays(rawValue: String): Set[DayOfWeek] =
    rawValue
      .split(",")
      .toList
      .map(_.trim)
      .filter(_.nonEmpty)
      .flatMap(value => Either.catchNonFatal(DayOfWeek.valueOf(value)).toOption)
      .toSet

