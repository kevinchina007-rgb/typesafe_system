package com.typesafe.travel.persistence.attraction

import com.typesafe.travel.attraction.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.parse

import java.sql.{Connection, Date}
import java.time.LocalDate

object AttractionPlannerInventorySql:
  private final case class AttractionInventoryReservation(
      orderItemId: String,
      ticketTypeId: String,
      sessionId: Option[String],
      useDate: LocalDate,
      quantity: Int
  )

  def consumeInventoryForPaidOrder(connection: Connection, orderId: String): Unit =
    val reservations = readOrderInventoryReservations(connection, orderId)
    reservations.foreach(validateReservationHasInventory(connection, _))
    reservations.foreach(decrementReservationInventory(connection, _))

  def restoreInventoryForCancelledOrder(connection: Connection, orderId: String): Unit =
    val reservations = readOrderInventoryReservations(connection, orderId)
    reservations.foreach(incrementReservationInventory(connection, _))

  private def readOrderInventoryReservations(connection: Connection, orderId: String): Vector[AttractionInventoryReservation] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select order_item_id, ticket_type_id, use_date, traveler_ids_json, snapshot_json
        from order_line_items
        where order_id = ? and item_kind = ?
        order by sort_index, order_item_id
      """
    ) { statement =>
      statement.setString(1, orderId)
      statement.setString(2, "Attraction")
      PlainSqlSupport.queryList(statement) { resultSet =>
        val ticketTypeId = Option(resultSet.getString("ticket_type_id")).getOrElse("").trim
        val useDate = resultSet.getDate("use_date").toLocalDate
        val travelerCount =
          Option(resultSet.getString("traveler_ids_json"))
            .flatMap(value => parse(value).toOption)
            .flatMap(_.asArray)
            .map(_.size)
            .getOrElse(0)
        if travelerCount <= 0 then throw new IllegalArgumentException(s"Attraction order item '${resultSet.getString("order_item_id")}' is missing travelers")
        val sessionId =
          Option(resultSet.getString("snapshot_json"))
            .flatMap(value => parse(value).toOption)
            .flatMap(_.hcursor.get[String]("sessionId").toOption)
            .map(_.trim)
            .filter(_.nonEmpty)
        AttractionInventoryReservation(
          orderItemId = resultSet.getString("order_item_id"),
          ticketTypeId = ticketTypeId,
          sessionId = sessionId,
          useDate = useDate,
          quantity = travelerCount
        )
      }.toVector
    }

  private def hasTicketTypeSessions(connection: Connection, ticketTypeId: String): Boolean =
    PlainSqlSupport.withStatement(connection, "select 1 from attraction_ticket_sessions where ticket_type_id = ? limit 1") { statement =>
      statement.setString(1, ticketTypeId)
      val resultSet = statement.executeQuery()
      try resultSet.next()
      finally resultSet.close()
    }

  private def validateReservationHasInventory(connection: Connection, reservation: AttractionInventoryReservation): Unit =
    reservation.sessionId match
      case Some(sessionId) =>
        PlainSqlSupport.withStatement(
          connection,
          """
            select capacity
            from attraction_ticket_sessions
            where session_id = ? and ticket_type_id = ? and use_date = ?
          """
        ) { statement =>
          statement.setString(1, sessionId)
          statement.setString(2, reservation.ticketTypeId)
          statement.setDate(3, Date.valueOf(reservation.useDate))
          val resultSet = statement.executeQuery()
          try
            if !resultSet.next() then
              throw new IllegalArgumentException(s"Session '$sessionId' was not found for ticket type '${reservation.ticketTypeId}' on '${reservation.useDate}'")
            val remainingQuantity = resultSet.getInt("capacity")
            if remainingQuantity < reservation.quantity then
              throw AttractionError.AttractionTicketSessionInventoryWasNotAvailable(AttractionTicketSessionId(sessionId), reservation.quantity, remainingQuantity)
          finally resultSet.close()
        }
      case None =>
        if hasTicketTypeSessions(connection, reservation.ticketTypeId) then
          throw new IllegalArgumentException(s"Ticket type '${reservation.ticketTypeId}' requires a session")
        PlainSqlSupport.withStatement(connection, "select total_quantity from ticket_types where ticket_type_id = ?") { statement =>
          statement.setString(1, reservation.ticketTypeId)
          val resultSet = statement.executeQuery()
          try
            if !resultSet.next() then
              throw new IllegalArgumentException(s"Ticket type '${reservation.ticketTypeId}' was not found")
            val remainingQuantity = resultSet.getInt("total_quantity")
            if remainingQuantity < reservation.quantity then
              throw AttractionError.TicketTypeInventoryWasNotAvailable(TicketTypeId(reservation.ticketTypeId), reservation.useDate, reservation.quantity, remainingQuantity)
          finally resultSet.close()
        }

  private def decrementReservationInventory(connection: Connection, reservation: AttractionInventoryReservation): Unit =
    reservation.sessionId match
      case Some(sessionId) =>
        PlainSqlSupport.withStatement(
          connection,
          """
            update attraction_ticket_sessions
            set capacity = capacity - ?
            where session_id = ? and ticket_type_id = ? and use_date = ? and capacity >= ?
          """
        ) { statement =>
          statement.setInt(1, reservation.quantity)
          statement.setString(2, sessionId)
          statement.setString(3, reservation.ticketTypeId)
          statement.setDate(4, Date.valueOf(reservation.useDate))
          statement.setInt(5, reservation.quantity)
          if statement.executeUpdate() != 1 then
            throw new IllegalArgumentException(s"Session '$sessionId' does not have enough inventory for '${reservation.quantity}' tickets")
        }
      case None =>
        PlainSqlSupport.withStatement(
          connection,
          """
            update ticket_types
            set total_quantity = total_quantity - ?
            where ticket_type_id = ? and total_quantity >= ?
          """
        ) { statement =>
          statement.setInt(1, reservation.quantity)
          statement.setString(2, reservation.ticketTypeId)
          statement.setInt(3, reservation.quantity)
          if statement.executeUpdate() != 1 then
            throw new IllegalArgumentException(s"Ticket type '${reservation.ticketTypeId}' does not have enough inventory for '${reservation.quantity}' tickets")
        }

  private def incrementReservationInventory(connection: Connection, reservation: AttractionInventoryReservation): Unit =
    reservation.sessionId match
      case Some(sessionId) =>
        PlainSqlSupport.withStatement(
          connection,
          """
            update attraction_ticket_sessions
            set capacity = capacity + ?
            where session_id = ? and ticket_type_id = ? and use_date = ?
          """
        ) { statement =>
          statement.setInt(1, reservation.quantity)
          statement.setString(2, sessionId)
          statement.setString(3, reservation.ticketTypeId)
          statement.setDate(4, Date.valueOf(reservation.useDate))
          if statement.executeUpdate() != 1 then
            throw new IllegalArgumentException(s"Session '$sessionId' was not found for restoration")
        }
      case None =>
        PlainSqlSupport.withStatement(
          connection,
          """
            update ticket_types
            set total_quantity = total_quantity + ?
            where ticket_type_id = ?
          """
        ) { statement =>
          statement.setInt(1, reservation.quantity)
          statement.setString(2, reservation.ticketTypeId)
          if statement.executeUpdate() != 1 then
            throw new IllegalArgumentException(s"Ticket type '${reservation.ticketTypeId}' was not found for restoration")
        }
