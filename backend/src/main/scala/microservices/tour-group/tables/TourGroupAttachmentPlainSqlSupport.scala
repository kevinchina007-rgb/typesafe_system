package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.UUID

object TourGroupAttachmentPlainSqlSupport:
  def loadAttachments(connection: Connection, messageId: String): List[TourGroupMessageAttachmentPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select attachment_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
        from tour_group_message_attachments
        where message_id = ?
        order by sort_order asc, created_at asc
      """
    ) { statement =>
      statement.setString(1, messageId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupMessageAttachmentPlannerResponse(
          attachmentId = row.getString("attachment_id"),
          attachmentType = row.getString("attachment_type"),
          publicUrl = row.getString("public_url"),
          storagePath = Option(row.getString("storage_path")).filter(_.nonEmpty).getOrElse(row.getString("public_url")),
          originalFileName = row.getString("original_file_name"),
          mimeType = row.getString("mime_type"),
          fileSize = row.getLong("file_size"),
          sortOrder = row.getInt("sort_order"),
          createdAt = row.getTimestamp("created_at").toInstant.toString
        )
      }
    }

  def insertAttachments(connection: Connection, messageId: String, attachments: List[SendTourGroupMessageAttachmentPlannerRequest], now: Instant): Unit =
    attachments.zipWithIndex.foreach { case (attachment, index) =>
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into tour_group_message_attachments(
            attachment_id, message_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, s"attachment-${UUID.randomUUID().toString.take(12)}")
        statement.setString(2, messageId)
        statement.setString(3, attachment.attachmentType)
        statement.setString(4, attachment.publicUrl)
        statement.setString(5, attachment.publicUrl.stripPrefix("/"))
        statement.setString(6, attachment.originalFileName)
        statement.setString(7, attachment.mimeType)
        statement.setLong(8, attachment.fileSize)
        statement.setInt(9, attachment.sortOrder.max(index))
        statement.setTimestamp(10, Timestamp.from(now))
        statement.executeUpdate()
      }
    }
