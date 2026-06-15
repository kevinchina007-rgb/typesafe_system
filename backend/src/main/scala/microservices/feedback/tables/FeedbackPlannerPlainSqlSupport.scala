// FeedbackPlannerPlainSqlSupport 提供内容模块反馈线程和反馈消息的查询、保存及 JSON 解析辅助函数。
package com.typesafe.travel.persistence.feedback

import com.typesafe.travel.feedback.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.decode
import io.circe.syntax.*

import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.Instant

object FeedbackPlannerPlainSqlSupport:
  def queryThreads(connection: Connection, sql: String, values: List[String]): List[FeedbackThread] =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      PlainSqlSupport.queryList(statement)(readThread)
    }

  def listMessagesUnsafe(connection: Connection, threadId: SupportTicketId): List[FeedbackMessage] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select message_id, thread_id, sender_role, sender_display_name, body, sent_at,
               coalesce(message_type, 'text') as message_type,
               payload_json,
               coalesce(is_read, false) as is_read
        from feedback_messages
        where thread_id = ?
        order by sent_at asc
      """
    ) { statement =>
      statement.setString(1, threadId.value)
      PlainSqlSupport.queryList(statement)(readMessage)
    }

  def saveThreadUnsafe(connection: Connection, thread: FeedbackThread): Unit =
    val updatedRows = PlainSqlSupport.withStatement(
      connection,
      """
        update feedback_threads
        set kind = ?, manager_type = ?, owner_user_id = ?, owner_user_display_name = ?, title = ?, subtitle = ?,
            resource_type = ?, resource_summary_title = ?, order_id = ?, order_item_id = ?, review_id = ?,
            related_thread_id = ?, manager_actor_id = ?, site_admin_actor_id = ?,
            unread_by_user = ?, unread_by_manager = ?, unread_by_site_admin = ?, created_at = ?, updated_at = ?
        where thread_id = ?
      """
    ) { statement =>
      setThread(statement, thread, 1)
      statement.setString(20, thread.threadId.value)
      statement.executeUpdate()
    }
    if updatedRows == 0 then
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into feedback_threads(
            thread_id, kind, manager_type, owner_user_id, owner_user_display_name,
            title, subtitle, resource_type, resource_summary_title, order_id, order_item_id,
            review_id, related_thread_id, manager_actor_id, site_admin_actor_id,
            unread_by_user, unread_by_manager, unread_by_site_admin,
            created_at, updated_at
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, thread.threadId.value)
        setThread(statement, thread, 2)
        statement.executeUpdate()
      }
    ()

  def setThread(statement: PreparedStatement, thread: FeedbackThread, start: Int): Unit =
    statement.setString(start, thread.kind.toString)
    statement.setString(start + 1, thread.managerType.toString)
    statement.setString(start + 2, thread.ownerUserId.map(_.value).orNull)
    statement.setString(start + 3, thread.ownerUserDisplayName)
    statement.setString(start + 4, thread.title)
    statement.setString(start + 5, thread.subtitle)
    statement.setString(start + 6, thread.resourceType)
    statement.setString(start + 7, thread.resourceSummaryTitle)
    statement.setString(start + 8, thread.orderId.map(_.value).orNull)
    statement.setString(start + 9, thread.orderItemId.map(_.value).orNull)
    statement.setString(start + 10, thread.reviewId.map(_.value).orNull)
    statement.setString(start + 11, thread.relatedThreadId.map(_.value).orNull)
    statement.setString(start + 12, thread.managerActorId.orNull)
    statement.setString(start + 13, thread.siteAdminActorId.orNull)
    statement.setInt(start + 14, thread.unreadByUser)
    statement.setInt(start + 15, thread.unreadByManager)
    statement.setInt(start + 16, thread.unreadBySiteAdmin)
    statement.setTimestamp(start + 17, Timestamp.from(thread.createdAt))
    statement.setTimestamp(start + 18, Timestamp.from(thread.updatedAt))

  def readThread(resultSet: ResultSet): FeedbackThread =
    FeedbackThread(
      SupportTicketId(resultSet.getString("thread_id")),
      FeedbackThreadKind.fromText(resultSet.getString("kind")),
      FeedbackManagerType.fromText(resultSet.getString("manager_type")),
      Option(resultSet.getString("owner_user_id")).map(UserId.apply),
      resultSet.getString("owner_user_display_name"),
      resultSet.getString("title"),
      resultSet.getString("subtitle"),
      resultSet.getString("resource_type"),
      resultSet.getString("resource_summary_title"),
      Option(resultSet.getString("order_id")).map(OrderId.apply),
      Option(resultSet.getString("order_item_id")).map(OrderItemId.apply),
      Option(resultSet.getString("review_id")).map(ReviewId.apply),
      Option(resultSet.getString("related_thread_id")).map(SupportTicketId.apply),
      Option(resultSet.getString("manager_actor_id")),
      Option(resultSet.getString("site_admin_actor_id")),
      resultSet.getInt("unread_by_user"),
      resultSet.getInt("unread_by_manager"),
      resultSet.getInt("unread_by_site_admin"),
      resultSet.getTimestamp("created_at").toInstant,
      resultSet.getTimestamp("updated_at").toInstant
    )

  def readMessage(resultSet: ResultSet): FeedbackMessage =
    val messageId = SupportMessageId(resultSet.getString("message_id"))
    val threadId = SupportTicketId(resultSet.getString("thread_id"))
    val senderDisplayName = resultSet.getString("sender_display_name")
    val messageType = FeedbackMessageType.fromText(resultSet.getString("message_type"))
    val payloadJson = Option(resultSet.getString("payload_json"))
    FeedbackMessage(
      messageId = messageId,
      threadId = threadId,
      senderId = senderDisplayName,
      senderRole = FeedbackSenderRole.fromText(resultSet.getString("sender_role")),
      senderDisplayName = senderDisplayName,
      messageType = messageType,
      content = resultSet.getString("body"),
      payload =
        if messageType == FeedbackMessageType.OrderCancellationRequest then
          payloadJson.flatMap(json => decode[OrderCancellationRequestPayload](json).toOption)
        else None,
      complaintPayload =
        if messageType == FeedbackMessageType.ComplaintCard then
          payloadJson.flatMap(json => decode[ComplaintCardPayload](json).toOption)
        else None,
      isRead = resultSet.getBoolean("is_read"),
      createdAt = resultSet.getTimestamp("sent_at").toInstant
    )

  def extractOrderTitle(snapshotJson: String): Option[String] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap { json =>
      val cursor = json.hcursor
      cursor.get[String]("airlineName").toOption
        .orElse(cursor.get[String]("flightNumber").toOption)
        .orElse(cursor.get[String]("hotelName").toOption)
        .orElse(cursor.get[String]("trainNumber").toOption)
        .orElse(cursor.get[String]("attractionName").toOption)
    }

  def extractHotelDetails(snapshotJson: String): Option[(Option[String], Option[String], Option[String])] =
    decode[io.circe.Json](snapshotJson).toOption.map { json =>
      val cursor = json.hcursor
      (
        cursor.get[String]("hotelId").toOption.filter(_.trim.nonEmpty),
        cursor.get[String]("hotelName").toOption.filter(_.trim.nonEmpty),
        cursor.get[String]("hotelLocation").toOption.orElse(cursor.get[String]("location").toOption).filter(_.trim.nonEmpty)
      )
    }.filter(details => details._1.isDefined || details._2.isDefined || details._3.isDefined)

  def extractJoinedHotelDetails(resultSet: ResultSet): Option[(Option[String], Option[String], Option[String])] =
    val hotelId = Option(resultSet.getString("joined_hotel_id")).map(_.trim).filter(_.nonEmpty)
    val hotelName = Option(resultSet.getString("joined_hotel_name")).map(_.trim).filter(_.nonEmpty)
    val hotelLocation = Option(resultSet.getString("joined_hotel_location")).map(_.trim).filter(_.nonEmpty)
    if hotelId.isDefined || hotelName.isDefined || hotelLocation.isDefined then Some((hotelId, hotelName, hotelLocation))
    else None

  def managerScopeFieldFor(managerType: FeedbackManagerType): Option[String] =
    managerType match
      case FeedbackManagerType.Hotel      => Some("hotelId")
      case FeedbackManagerType.Airline    => Some("airlineId")
      case FeedbackManagerType.Train      => Some("trainId")
      case FeedbackManagerType.Attraction => Some("managerId")
      case _                              => None

  def extractStringField(snapshotJson: String, fieldName: String): Option[String] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[String](fieldName).toOption).filter(_.trim.nonEmpty)
