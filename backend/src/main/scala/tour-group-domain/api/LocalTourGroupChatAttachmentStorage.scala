package com.typesafe.travel.api.application

import cats.effect.kernel.{Clock, Sync}
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
    absolutePath: Path
)

trait TourGroupChatAttachmentStorage[F[_]]:
  def storeAttachment(
      ownerUserId: UserId,
      collection: TourGroupChatAttachmentCollection,
      originalFileName: String,
      fileExtension: String,
      mimeType: String,
      fileBytes: Array[Byte]
  ): F[StoredTourGroupChatAttachmentFile]

final class LocalTourGroupChatAttachmentStorage[F[_]: Sync: Clock] private (
    uploadRootDirectoryPath: Path
) extends TourGroupChatAttachmentStorage[F]:

  override def storeAttachment(
      ownerUserId: UserId,
      collection: TourGroupChatAttachmentCollection,
      originalFileName: String,
      fileExtension: String,
      mimeType: String,
      fileBytes: Array[Byte]
  ): F[StoredTourGroupChatAttachmentFile] =
    Clock[F].realTimeInstant.flatMap { currentInstant =>
      Sync[F].blocking {
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
          absolutePath = targetPath
        )
      }
    }

object LocalTourGroupChatAttachmentStorage:
  def create[F[_]: Sync: Clock](uploadRootDirectoryPath: Path): LocalTourGroupChatAttachmentStorage[F] =
    new LocalTourGroupChatAttachmentStorage[F](uploadRootDirectoryPath)
