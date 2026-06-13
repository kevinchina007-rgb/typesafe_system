// FeedbackPlannerPlainSql 封装内容模块的plain SQL 实现。

package com.typesafe.travel.persistence.content

import cats.effect.IO
import com.typesafe.travel.content.domain.*
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.content.FeedbackPlannerPlainSqlSupport.*
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
             ft.review_id, ft.related_thread_id, ft.manager_actor_id, ft.site_admin_actor_id,
             ft.unread_by_user, ft.unread_by_manager, ft.unread_by_site_admin,
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
        FeedbackManagerType.fromText(managerType) match
          case FeedbackManagerType.Airline =>
            IO.blocking {
              queryThreads(
                connection,
                selectThreadSql +
                  """
                     join order_line_items li on li.order_id = ft.order_id and li.order_item_id = ft.order_item_id
                     join airlines a on a.airline_id = ?
                     where ft.kind = ? and ft.manager_type = ?
                       and (
                         li.snapshot_json::jsonb ->> 'airlineId' = ?
                         or li.snapshot_json::jsonb ->> 'airlineCode' = a.code
                         or li.flight_id in (select flight_id from flights where airline_id = ?)
                       )
                     order by ft.updated_at desc
                   """,
                List(scope, FeedbackThreadKind.ServiceReview.toString, FeedbackManagerType.Airline.toString, scope, scope)
              )
            }
          case managerKind =>
            val scopeField = managerScopeFieldFor(managerKind).getOrElse("hotelId")
            IO.blocking {
              queryThreads(
                connection,
                selectThreadSql +
                  s"""
                     join order_line_items li on li.order_id = ft.order_id and li.order_item_id = ft.order_item_id
                     where ft.kind = ? and ft.manager_type = ? and li.snapshot_json::jsonb ->> '$scopeField' = ?
                     order by ft.updated_at desc
                   """,
                List(FeedbackThreadKind.ServiceReview.toString, managerKind.toString, scope)
              )
            }
      case _ =>
        IO.blocking {
          queryThreads(connection, selectThreadSql + " where ft.kind = ? and ft.manager_type = ? order by ft.updated_at desc", List(FeedbackThreadKind.ServiceReview.toString, managerType))
        }

  def listManagerParticipantThreads(connection: Connection, managerType: String, managerActorId: String): IO[List[FeedbackThread]] =
    IO.blocking {
      queryThreads(
        connection,
        selectThreadSql + " where ft.kind = ? and ft.manager_type = ? and ft.manager_actor_id = ? order by ft.updated_at desc",
        List(FeedbackThreadKind.ManagerEscalation.toString, FeedbackManagerType.fromText(managerType).toString, managerActorId)
      )
    }

  def listSiteAdminParticipantThreads(connection: Connection, channel: String, siteAdminActorId: String): IO[List[FeedbackThread]] =
    val kind = if channel.trim.equalsIgnoreCase("manager") then FeedbackThreadKind.ManagerEscalation else FeedbackThreadKind.ServiceReview
    IO.blocking {
      if kind == FeedbackThreadKind.ManagerEscalation then
        queryThreads(
          connection,
          selectThreadSql + " where ft.kind = ? and ft.site_admin_actor_id = ? order by ft.updated_at desc",
          List(kind.toString, siteAdminActorId)
        )
      else
        queryThreads(connection, selectThreadSql + " where ft.kind = ? order by ft.updated_at desc", List(kind.toString))
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

  def findManagerSiteAdminThread(connection: Connection, managerActorId: String, siteAdminActorId: String): IO[Option[FeedbackThread]] =
    IO.blocking {
      queryThreads(
        connection,
        selectThreadSql + " where ft.kind = ? and ft.manager_actor_id = ? and ft.site_admin_actor_id = ? order by ft.updated_at desc",
        List(FeedbackThreadKind.ManagerEscalation.toString, managerActorId, siteAdminActorId)
      ).headOption
    }

  def findDefaultSiteAdminActorId(connection: Connection): IO[Option[String]] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select manager_id
          from site_admin_managers
          where coalesce(status, 'Active') = 'Active'
          order by created_at asc
          limit 1
        """
      ) { statement =>
        val resultSet = statement.executeQuery()
        try if resultSet.next() then Option(resultSet.getString("manager_id")).map(_.trim).filter(_.nonEmpty) else None
        finally resultSet.close()
      }
    }

  def findManagerActorForFeedbackThread(connection: Connection, thread: FeedbackThread): IO[Option[String]] =
    thread.managerActorId.map(_.trim).filter(_.nonEmpty) match
      case Some(actorId) => IO.pure(Some(actorId))
      case None =>
        thread.managerType match
          case FeedbackManagerType.Airline =>
            thread.orderId.map(_.value).filter(_.trim.nonEmpty) match
              case Some(orderId) =>
                IO.blocking {
                  PlainSqlSupport.withStatement(
                    connection,
                    """
                      select am.manager_id
                      from order_line_items li
                      left join airlines a on a.airline_id = li.snapshot_json::jsonb ->> 'airlineId'
                        or a.code = li.snapshot_json::jsonb ->> 'airlineCode'
                      join airline_managers am on am.airline_id = a.airline_id
                      where li.order_id = ?
                      order by am.created_at asc
                      limit 1
                    """
                  ) { statement =>
                    statement.setString(1, orderId)
                    val resultSet = statement.executeQuery()
                    try if resultSet.next() then Option(resultSet.getString("manager_id")).map(_.trim).filter(_.nonEmpty) else None
                    finally resultSet.close()
                  }
                }
              case None => IO.pure(None)
          case _ => IO.pure(None)

  def listMessagesByIds(connection: Connection, threadId: SupportTicketId, messageIds: List[String]): IO[List[FeedbackMessage]] =
    IO.blocking {
      val wantedIds = messageIds.map(_.trim).filter(_.nonEmpty)
      if wantedIds.isEmpty then Nil
      else
        val placeholders = wantedIds.map(_ => "?").mkString(", ")
        PlainSqlSupport.withStatement(
          connection,
          s"""
             select message_id, thread_id, sender_role, sender_display_name, body, sent_at,
                    coalesce(message_type, 'text') as message_type,
                    payload_json,
                    coalesce(is_read, false) as is_read
             from feedback_messages
             where thread_id = ? and message_id in ($placeholders)
             order by sent_at asc
           """
        ) { statement =>
          statement.setString(1, threadId.value)
          wantedIds.zipWithIndex.foreach { case (messageId, index) => statement.setString(index + 2, messageId) }
          PlainSqlSupport.queryList(statement)(readMessage)
        }
    }

  def findManagerActorLogoAssetPath(connection: Connection, managerType: FeedbackManagerType, managerActorId: Option[String]): IO[Option[String]] =
    (managerType, managerActorId.map(_.trim).filter(_.nonEmpty)) match
      case (FeedbackManagerType.Airline, Some(actorId)) =>
        IO.blocking {
          PlainSqlSupport.withStatement(
            connection,
            """
              select a.logo_asset_path
              from airline_managers m
              join airlines a on a.airline_id = m.airline_id
              where m.manager_id = ?
            """
          ) { statement =>
            statement.setString(1, actorId)
            val resultSet = statement.executeQuery()
            try if resultSet.next() then Option(resultSet.getString("logo_asset_path")).map(_.trim).filter(_.nonEmpty) else None
            finally resultSet.close()
          }
        }
      case _ => IO.pure(None)

  def findSiteAdminActorLogoAssetPath(connection: Connection, siteAdminActorId: Option[String]): IO[Option[String]] =
    siteAdminActorId.map(_.trim).filter(_.nonEmpty) match
      case Some(actorId) =>
        IO.blocking {
          PlainSqlSupport.withStatement(connection, "select logo_asset_path from site_admin_managers where manager_id = ?") { statement =>
            statement.setString(1, actorId)
            val resultSet = statement.executeQuery()
            try if resultSet.next() then Option(resultSet.getString("logo_asset_path")).map(_.trim).filter(_.nonEmpty) else None
            finally resultSet.close()
          }
        }
      case None => IO.pure(None)

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
        val payloadJson =
          message.messageType match
            case FeedbackMessageType.ComplaintCard => message.complaintPayload.map(_.asJson.noSpaces)
            case _                                 => message.payload.map(_.asJson.noSpaces)
        statement.setString(8, payloadJson.orNull)
        statement.setBoolean(9, message.isRead)
        statement.executeUpdate()
      }
      ()
    }

  def updateMessagePayload(connection: Connection, message: FeedbackMessage): IO[Unit] =
    IO.blocking {
      PlainSqlSupport.withStatement(connection, "update feedback_messages set payload_json = ?, body = ?, message_type = ?, is_read = ? where thread_id = ? and message_id = ?") { statement =>
        val payloadJson =
          message.messageType match
            case FeedbackMessageType.ComplaintCard => message.complaintPayload.map(_.asJson.noSpaces)
            case _                                 => message.payload.map(_.asJson.noSpaces)
        statement.setString(1, payloadJson.orNull)
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
      PlainSqlSupport.withStatement(connection, "delete from train_seat_allocations where order_id = ?") { statement =>
        statement.setString(1, orderId)
        statement.executeUpdate()
      }
      ()
    }

  def hasOpenOrderCancellationRequest(connection: Connection, threadId: SupportTicketId): IO[Boolean] =
    IO.blocking {
      listMessagesUnsafe(connection, threadId).exists { message =>
        message.messageType == FeedbackMessageType.OrderCancellationRequest &&
        message.payload.exists(payload =>
          payload.status == OrderCancellationRequestStatus.Pending ||
            payload.status == OrderCancellationRequestStatus.NeedMoreInfo
        )
      }
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
