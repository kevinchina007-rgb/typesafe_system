// LocalTourGroupChatAttachmentStorage 定义团体游模块的业务入口。

package com.typesafe.travel.api.application

import cats.effect.IO
import cats.effect.kernel.Clock
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.UserId

import java.nio.file.{Files, Path}
import java.time.format.DateTimeFormatter
import java.util.UUID

enum TourGroupChatAttachmentCollection:
  case Image
  case File

final case class StoredTourGroupChatAttachmentFile(
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long,
    storagePath: String
)

trait TourGroupChatAttachmentStorage:
  def storeAttachment(
      ownerUserId: UserId,
      collection: TourGroupChatAttachmentCollection,
      originalFileName: String,
      fileExtension: String,
      mimeType: String,
      fileBytes: Array[Byte]
  ): IO[StoredTourGroupChatAttachmentFile]

final class LocalTourGroupChatAttachmentStorage private (
    uploadRootDirectoryPath: Path
) extends TourGroupChatAttachmentStorage:

  override def storeAttachment(
      ownerUserId: UserId,
      collection: TourGroupChatAttachmentCollection,
      originalFileName: String,
      fileExtension: String,
      mimeType: String,
      fileBytes: Array[Byte]
  ): IO[StoredTourGroupChatAttachmentFile] =
    Clock[IO].realTimeInstant.flatMap { currentInstant =>
      IO.blocking {
        val collectionDirectoryName =
          collection match
            case TourGroupChatAttachmentCollection.Image => "chat-images"
            case TourGroupChatAttachmentCollection.File  => "chat-files"
        val collectionDirectoryPath = uploadRootDirectoryPath.resolve(collectionDirectoryName)
        Files.createDirectories(collectionDirectoryPath)
        val timestampText =
          DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentInstant.atZone(java.time.ZoneOffset.UTC))
        val normalizedExtension = fileExtension.toLowerCase
        val fileName = s"${ownerUserId.value}-$timestampText-${UUID.randomUUID().toString.take(8)}.$normalizedExtension"
        val targetPath = collectionDirectoryPath.resolve(fileName)
        Files.write(targetPath, fileBytes)
        StoredTourGroupChatAttachmentFile(
          publicUrl = s"/uploads/content/$collectionDirectoryName/$fileName",
          originalFileName = originalFileName,
          mimeType = mimeType,
          fileSize = fileBytes.length.toLong,
          storagePath = targetPath.toString
        )
      }
    }

object LocalTourGroupChatAttachmentStorage:
  def create(uploadRootDirectoryPath: Path): LocalTourGroupChatAttachmentStorage =
    new LocalTourGroupChatAttachmentStorage(uploadRootDirectoryPath)
