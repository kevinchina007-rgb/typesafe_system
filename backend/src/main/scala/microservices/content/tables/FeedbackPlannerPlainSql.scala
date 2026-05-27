package com.typesafe.travel.persistence.content

import cats.effect.IO
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.decode
import io.circe.syntax.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant

final case class FeedbackOrderCancellationSummary(
    orderId: String,
    buyerUserId: String,
    orderType: String,
    itemKind: String,
    orderItemId: Option[String],
    airlineName: Option[String],
    airlineCode: Option[String],
    hotelId: Option[String],
    hotelName: Option[String],
    hotelLocation: Option[String],
    orderTitle: Option[String],
    requestedRefundAmount: Option[BigDecimal]
)

object FeedbackPlannerPlainSql:
  private val selectThreadSql =
    """
      select ft.thread_id, ft.kind, ft.manager_type, ft.owner_user_id, ft.owner_user_display_name,
             ft.title, ft.subtitle, ft.resource_type, ft.resource_summary_title, ft.order_id, ft.order_item_id,
             ft.review_id, ft.related_thread_id, ft.unread_by_user, ft.unread_by_manager, ft.unread_by_site_admin,
             ft.created_at, ft.updated_at
      from feedback_threads ft
    """

  def listAll(connection: Connection): IO[List[FeedbackThread]] =
    IO.blocking {
      queryThreads(connection, selectThreadSql + " order by updated_at desc", Nil)
    }

  def listByOwnerUserId(connection: Connection, userId: String): IO[List[FeedbackThread]] =
    IO.blocking {
      queryThreads(connection, selectThreadSql + " where owner_user_id = ? order by updated_at desc", List(userId))
    }

  def listServiceReviewsByManagerType(connection: Connection, managerType: String, scopeId: Option[String]): IO[List[FeedbackThread]] =
    scopeId.map(_.trim).filter(_.nonEmpty) match
      case Some(scope) if FeedbackManagerType.fromText(managerType) == FeedbackManagerType.Hotel ||
        FeedbackManagerType.fromText(managerType) == FeedbackManagerType.Airline ||
        FeedbackManagerType.fromText(managerType) == FeedbackManagerType.Train ||
        FeedbackManagerType.fromText(managerType) == FeedbackManagerType.Attraction =>
        val scopeField = managerScopeFieldFor(FeedbackManagerType.fromText(managerType)).getOrElse("hotelId")
        IO.blocking {
          queryThreads(
            connection,
            selectThreadSql +
              s"""
                 join order_line_items li on li.order_id = ft.order_id and li.order_item_id = ft.order_item_id
                 where ft.kind = ? and ft.manager_type = ? and li.snapshot_json::jsonb ->> '$scopeField' = ?
                 order by ft.updated_at desc
               """,
            List(FeedbackThreadKind.ServiceReview.toString, FeedbackManagerType.fromText(managerType).toString, scope)
          )
        }
      case _ =>
        IO.blocking {
          queryThreads(connection, selectThreadSql + " where ft.kind = ? and ft.manager_type = ? order by ft.updated_at desc", List(FeedbackThreadKind.ServiceReview.toString, managerType))
        }

  def listByKind(connection: Connection, kind: FeedbackThreadKind): IO[List[FeedbackThread]] =
    IO.blocking {
      queryThreads(connection, selectThreadSql + " where kind = ? order by updated_at desc", List(kind.toString))
    }

  def findByReviewId(connection: Connection, reviewId: ReviewId): IO[Option[FeedbackThread]] =
    IO.blocking {
      queryThreads(connection, selectThreadSql + " where ft.review_id = ? order by ft.updated_at desc", List(reviewId.value)).headOption
    }

  def findByThreadId(connection: Connection, threadId: SupportTicketId): IO[Option[FeedbackThread]] =
    IO.blocking {
      queryThreads(connection, selectThreadSql + " where ft.thread_id = ?", List(threadId.value)).headOption
    }

  def findByOwnerAndResource(connection: Connection, userId: String, resourceType: String, resourceSummaryTitle: String): IO[Option[FeedbackThread]] =
    IO.blocking {
      queryThreads(
        connection,
        selectThreadSql + " where ft.owner_user_id = ? and ft.resource_type = ? and ft.resource_summary_title = ? order by ft.updated_at desc",
        List(userId, resourceType, resourceSummaryTitle)
      ).headOption
    }

  def findByOrderId(connection: Connection, orderId: String): IO[Option[FeedbackThread]] =
    IO.blocking {
      queryThreads(
        connection,
        selectThreadSql + " where ft.order_id = ? order by ft.updated_at desc",
        List(orderId)
      ).headOption
    }

  def listMessages(connection: Connection, threadId: SupportTicketId): IO[List[FeedbackMessage]] =
    IO.blocking {
      listMessagesUnsafe(connection, threadId)
    }

  def findMessage(connection: Connection, threadId: SupportTicketId, messageId: SupportMessageId): IO[Option[FeedbackMessage]] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select message_id, thread_id, sender_role, sender_display_name, body, sent_at,
                 coalesce(message_type, 'text') as message_type,
                 payload_json,
                 coalesce(is_read, false) as is_read
          from feedback_messages
          where thread_id = ? and message_id = ?
        """
      ) { statement =>
        statement.setString(1, threadId.value)
        statement.setString(2, messageId.value)
        PlainSqlSupport.queryOptional(statement)(readMessage)
      }
    }

  def insertMessage(connection: Connection, message: FeedbackMessage): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        "insert into feedback_messages(message_id, thread_id, sender_role, sender_display_name, body, sent_at, message_type, payload_json, is_read) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, message.messageId.value)
        statement.setString(2, message.threadId.value)
        statement.setString(3, message.senderRole.toString)
        statement.setString(4, message.senderDisplayName)
        statement.setString(5, message.content)
        statement.setTimestamp(6, Timestamp.from(message.createdAt))
        statement.setString(7, message.messageType.toString)
        statement.setString(8, message.payload.map(_.asJson.noSpaces).orNull)
        statement.setBoolean(9, message.isRead)
        statement.executeUpdate()
      }
      ()
    }

  def updateMessagePayload(connection: Connection, message: FeedbackMessage): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update feedback_messages set payload_json = ?, body = ?, message_type = ?, is_read = ? where thread_id = ? and message_id = ?") { statement =>
        statement.setString(1, message.payload.map(_.asJson.noSpaces).orNull)
        statement.setString(2, message.content)
        statement.setString(3, message.messageType.toString)
        statement.setBoolean(4, message.isRead)
        statement.setString(5, message.threadId.value)
        statement.setString(6, message.messageId.value)
        statement.executeUpdate()
      }
      ()
    }

  def markOrderRefunded(connection: Connection, orderId: String, now: Instant): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          update orders
          set status = ?, remaining_refundable_amount = 0, completed_at = coalesce(completed_at, ?)
          where order_id = ?
        """
      ) { statement =>
        statement.setString(1, "Refunded")
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setString(3, orderId)
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(connection, "update order_line_items set item_status = ? where order_id = ?") { statement =>
        statement.setString(1, "Refunded")
        statement.setString(2, orderId)
        statement.executeUpdate()
      }
      PlainSqlSupport.withStatement(
        connection,
        """
          update order_refunds
          set refund_status = ?, approved_at = coalesce(approved_at, ?), settled_at = coalesce(settled_at, ?)
          where order_id = ? and refund_status in (?, ?)
        """
      ) { statement =>
        statement.setString(1, "Settled")
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, orderId)
        statement.setString(5, "Requested")
        statement.setString(6, "Approved")
        statement.executeUpdate()
      }
      ()
    }

  def saveThread(connection: Connection, thread: FeedbackThread): IO[Unit] =
    IO.blocking {
      saveThreadUnsafe(connection, thread)
    }

  def findOrderCancellationSummary(connection: Connection, orderId: String): IO[Option[FeedbackOrderCancellationSummary]] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select o.order_id, o.buyer_user_id, o.order_type, o.remaining_refundable_amount,
                 li.order_item_id, li.snapshot_json, li.item_kind, li.room_type_id,
                 rt.hotel_id as joined_hotel_id, rt.name as joined_room_type_name,
                 h.name as joined_hotel_name, h.location as joined_hotel_location
          from orders o
          left join order_line_items li on li.order_id = o.order_id
          left join hotel_room_types rt on rt.room_type_id = li.room_type_id
          left join hotels h on h.hotel_id = rt.hotel_id
          where o.order_id = ?
          order by li.sort_index
          limit 1
        """
      ) { statement =>
        statement.setString(1, orderId)
        PlainSqlSupport.queryOptional(statement) { resultSet =>
          val snapshotJson = Option(resultSet.getString("snapshot_json"))
          val snapshotTitle = snapshotJson.flatMap(extractOrderTitle)
          val hotelDetails =
            snapshotJson.flatMap(extractHotelDetails).orElse(extractJoinedHotelDetails(resultSet))
          FeedbackOrderCancellationSummary(
            orderId = resultSet.getString("order_id"),
            buyerUserId = resultSet.getString("buyer_user_id"),
            orderType = resultSet.getString("order_type"),
            itemKind = resultSet.getString("item_kind"),
            orderItemId = Option(resultSet.getString("order_item_id")),
            airlineName = snapshotJson.flatMap(extractStringField(_, "airlineName")),
            airlineCode = snapshotJson.flatMap(extractStringField(_, "airlineCode")),
            hotelId = hotelDetails.flatMap(_._1),
            hotelName = hotelDetails.flatMap(_._2),
            hotelLocation = hotelDetails.flatMap(_._3),
            orderTitle = snapshotTitle
              .orElse(Option(resultSet.getString("joined_hotel_name")).map(_.trim).filter(_.nonEmpty))
              .orElse(Option(resultSet.getString("item_kind"))),
            requestedRefundAmount = Option(resultSet.getBigDecimal("remaining_refundable_amount")).map(BigDecimal.apply)
          )
        }
      }
    }

  private def queryThreads(connection: Connection, sql: String, values: List[String]): List[FeedbackThread] =
    PlainSqlSupport.withStatement(connection, sql) { statement =>
      values.zipWithIndex.foreach { case (value, index) => statement.setString(index + 1, value) }
      PlainSqlSupport.queryList(statement)(readThread)
    }

  private def listMessagesUnsafe(connection: Connection, threadId: SupportTicketId): List[FeedbackMessage] =
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

  private def saveThreadUnsafe(connection: Connection, thread: FeedbackThread): Unit =
    val updatedRows = PlainSqlSupport.withStatement(
      connection,
      """
        update feedback_threads
        set kind = ?, manager_type = ?, owner_user_id = ?, owner_user_display_name = ?, title = ?, subtitle = ?,
            resource_type = ?, resource_summary_title = ?, order_id = ?, order_item_id = ?, review_id = ?,
            related_thread_id = ?, unread_by_user = ?, unread_by_manager = ?, unread_by_site_admin = ?, created_at = ?, updated_at = ?
        where thread_id = ?
      """
    ) { statement =>
      setThread(statement, thread, 1)
      statement.setString(18, thread.threadId.value)
      statement.executeUpdate()
    }
    if updatedRows == 0 then
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into feedback_threads(
            thread_id, kind, manager_type, owner_user_id, owner_user_display_name,
            title, subtitle, resource_type, resource_summary_title, order_id, order_item_id,
            review_id, related_thread_id, unread_by_user, unread_by_manager, unread_by_site_admin,
            created_at, updated_at
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, thread.threadId.value)
        setThread(statement, thread, 2)
        statement.executeUpdate()
      }
    ()

  private def setThread(statement: java.sql.PreparedStatement, thread: FeedbackThread, start: Int): Unit =
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
    statement.setInt(start + 12, thread.unreadByUser)
    statement.setInt(start + 13, thread.unreadByManager)
    statement.setInt(start + 14, thread.unreadBySiteAdmin)
    statement.setTimestamp(start + 15, Timestamp.from(thread.createdAt))
    statement.setTimestamp(start + 16, Timestamp.from(thread.updatedAt))

  private def readThread(resultSet: ResultSet): FeedbackThread =
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
      resultSet.getInt("unread_by_user"),
      resultSet.getInt("unread_by_manager"),
      resultSet.getInt("unread_by_site_admin"),
      resultSet.getTimestamp("created_at").toInstant,
      resultSet.getTimestamp("updated_at").toInstant
    )

  private def readMessage(resultSet: ResultSet): FeedbackMessage =
    val messageId = SupportMessageId(resultSet.getString("message_id"))
    val threadId = SupportTicketId(resultSet.getString("thread_id"))
    val senderDisplayName = resultSet.getString("sender_display_name")
    FeedbackMessage(
      messageId = messageId,
      threadId = threadId,
      senderId = senderDisplayName,
      senderRole = FeedbackSenderRole.fromText(resultSet.getString("sender_role")),
      senderDisplayName = senderDisplayName,
      messageType = FeedbackMessageType.fromText(resultSet.getString("message_type")),
      content = resultSet.getString("body"),
      payload = Option(resultSet.getString("payload_json")).flatMap { json =>
        decode[OrderCancellationRequestPayload](json).toOption
      },
      isRead = resultSet.getBoolean("is_read"),
      createdAt = resultSet.getTimestamp("sent_at").toInstant
    )

  private def extractOrderTitle(snapshotJson: String): Option[String] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap { json =>
      val cursor = json.hcursor
      cursor.get[String]("airlineName").toOption
        .orElse(cursor.get[String]("flightNumber").toOption)
        .orElse(cursor.get[String]("hotelName").toOption)
        .orElse(cursor.get[String]("trainNumber").toOption)
        .orElse(cursor.get[String]("attractionName").toOption)
    }

  private def extractHotelDetails(snapshotJson: String): Option[(Option[String], Option[String], Option[String])] =
    decode[io.circe.Json](snapshotJson).toOption.map { json =>
      val cursor = json.hcursor
      (
        cursor.get[String]("hotelId").toOption.filter(_.trim.nonEmpty),
        cursor.get[String]("hotelName").toOption.filter(_.trim.nonEmpty),
        cursor.get[String]("hotelLocation").toOption.orElse(cursor.get[String]("location").toOption).filter(_.trim.nonEmpty)
      )
    }.filter(details => details._1.isDefined || details._2.isDefined || details._3.isDefined)

  private def extractJoinedHotelDetails(resultSet: ResultSet): Option[(Option[String], Option[String], Option[String])] =
    val hotelId = Option(resultSet.getString("joined_hotel_id")).map(_.trim).filter(_.nonEmpty)
    val hotelName = Option(resultSet.getString("joined_hotel_name")).map(_.trim).filter(_.nonEmpty)
    val hotelLocation = Option(resultSet.getString("joined_hotel_location")).map(_.trim).filter(_.nonEmpty)
    if hotelId.isDefined || hotelName.isDefined || hotelLocation.isDefined then Some((hotelId, hotelName, hotelLocation))
    else None

  private def managerScopeFieldFor(managerType: FeedbackManagerType): Option[String] =
    managerType match
      case FeedbackManagerType.Hotel      => Some("hotelId")
      case FeedbackManagerType.Airline    => Some("airlineId")
      case FeedbackManagerType.Train      => Some("trainId")
      case FeedbackManagerType.Attraction => Some("managerId")
      case _                              => None

  private def extractStringField(snapshotJson: String, fieldName: String): Option[String] =
    decode[io.circe.Json](snapshotJson).toOption.flatMap(_.hcursor.get[String](fieldName).toOption).filter(_.trim.nonEmpty)
