package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.Base64
import java.util.UUID

import TourGroupConversationPlainSqlSupport.*
import TourGroupChatMessagePlainSqlSupport.*
import TourGroupAttachmentPlainSqlSupport.*

object SendMessagePlainSql:
  def sendGroupChatMessage(connection: Connection, groupId: String, currentUserId: String, request: SendTourGroupMessagePlannerRequest, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    ConversationPlainSql.listConversations(connection, groupId, currentUserId, now).flatMap { list =>
      val conversationId = list.groupChatConversationId.getOrElse {
        throw TourGroupError.ConversationWasNotFound(TourGroupConversationId(groupId))
      }
      IO.blocking {
        val conversation = requireConversation(connection, conversationId)
        requireConversationAccess(connection, conversation, currentUserId)
        insertMessage(connection, conversationId, currentUserId, request, now)
        TourGroupMessageListPlannerResponse(loadMessages(connection, conversationId, currentUserId))
      }
    }

  def sendMessage(connection: Connection, conversationId: String, currentUserId: String, request: SendTourGroupMessagePlannerRequest, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    IO.blocking {
      val conversation = requireConversation(connection, conversationId)
      requireConversationAccess(connection, conversation, currentUserId)
      insertMessage(connection, conversationId, currentUserId, request, now)
      TourGroupMessageListPlannerResponse(loadMessages(connection, conversationId, currentUserId))
    }

  def editMessage(connection: Connection, messageId: String, currentUserId: String, content: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { message =>
      if message.senderUserId.value != currentUserId then throw TourGroupError.MessageAccessWasDenied(message.messageId, UserId(currentUserId))
      if content.trim.isEmpty then throw TourGroupError.MessageContentWasEmpty()
      PlainSqlSupport.withStatement(connection, "update tour_group_messages set content = ?, status = ?, updated_at = ? where message_id = ?") { statement =>
        statement.setString(1, content.trim)
        statement.setString(2, TourGroupMessageStatus.Edited.toString)
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, messageId)
        statement.executeUpdate()
      }
    }

  def deleteMessage(connection: Connection, messageId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { message =>
      if message.senderUserId.value != currentUserId then throw TourGroupError.MessageAccessWasDenied(message.messageId, UserId(currentUserId))
      PlainSqlSupport.withStatement(connection, "update tour_group_messages set status = ?, deleted_at = ?, updated_at = ? where message_id = ?") { statement =>
        statement.setString(1, TourGroupMessageStatus.Deleted.toString)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, messageId)
        statement.executeUpdate()
      }
    }

  def recallMessage(connection: Connection, messageId: String, currentUserId: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { message =>
      if message.senderUserId.value != currentUserId then throw TourGroupError.MessageAccessWasDenied(message.messageId, UserId(currentUserId))
      PlainSqlSupport.withStatement(connection, "update tour_group_messages set status = ?, recalled_at = ?, updated_at = ? where message_id = ?") { statement =>
        statement.setString(1, TourGroupMessageStatus.Recalled.toString)
        statement.setTimestamp(2, Timestamp.from(now))
        statement.setTimestamp(3, Timestamp.from(now))
        statement.setString(4, messageId)
        statement.executeUpdate()
      }
    }

  def addReaction(connection: Connection, messageId: String, currentUserId: String, reactionType: String, now: Instant): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, now) { _ =>
      validateReactionType(reactionType)
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_group_message_reactions(reaction_id, message_id, user_id, reaction_type, created_at) values (?, ?, ?, ?, ?) on conflict do nothing"
      ) { statement =>
        statement.setString(1, s"reaction-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, messageId)
        statement.setString(3, currentUserId)
        statement.setString(4, reactionType.trim)
        statement.setTimestamp(5, Timestamp.from(now))
        statement.executeUpdate()
      }
    }

  def removeReaction(connection: Connection, messageId: String, currentUserId: String, reactionType: String): IO[TourGroupMessageListPlannerResponse] =
    mutateMessage(connection, messageId, currentUserId, Instant.now()) { _ =>
      PlainSqlSupport.withStatement(connection, "delete from tour_group_message_reactions where message_id = ? and user_id = ? and reaction_type = ?") { statement =>
        statement.setString(1, messageId)
        statement.setString(2, currentUserId)
        statement.setString(3, reactionType.trim)
        statement.executeUpdate()
      }
    }

  def uploadAttachment(connection: Connection, currentUserId: String, request: UploadConversationAttachmentPlannerRequest, now: Instant): IO[TourGroupMessageAttachmentPlannerResponse] =
    IO.blocking {
      val bytes = Base64.getDecoder.decode(request.base64Content.trim)
      val assetId = s"asset-${UUID.randomUUID().toString.take(12)}"
      val extension = request.fileName.split("\\.").lastOption.getOrElse("bin").trim.toLowerCase
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into uploaded_binary_assets(asset_id, owner_user_id, asset_category, original_file_name, file_extension, mime_type, file_size, binary_content, created_at)
          values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, assetId)
        statement.setString(2, currentUserId)
        statement.setString(3, "tour-group-chat")
        statement.setString(4, request.fileName)
        statement.setString(5, extension)
        statement.setString(6, request.mimeType)
        statement.setLong(7, bytes.length.toLong)
        statement.setBytes(8, bytes)
        statement.setTimestamp(9, Timestamp.from(now))
        statement.executeUpdate()
      }
      TourGroupMessageAttachmentPlannerResponse(
        attachmentId = assetId,
        attachmentType = if request.mimeType.toLowerCase.startsWith("image/") then "Image" else "File",
        publicUrl = s"/uploads/assets/$assetId",
        storagePath = s"/uploads/assets/$assetId",
        originalFileName = request.fileName,
        mimeType = request.mimeType,
        fileSize = bytes.length.toLong,
        sortOrder = 0,
        createdAt = now.toString
      )
    }
