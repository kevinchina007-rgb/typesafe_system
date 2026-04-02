package com.typesafe.travel.api.storage

import cats.effect.kernel.{Clock, Sync}
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.UserId

import java.nio.file.{Files, Path}
import java.time.format.DateTimeFormatter
import java.util.UUID

enum ContentImageCollection:
  case Blog, Review

final case class StoredContentImageFile(
    publicUrl: String,
    originalFileName: String,
    absolutePath: Path
)

trait ContentImageStorage[F[_]]:
  def storeImage(
      collection: ContentImageCollection,
      ownerUserId: UserId,
      originalFileName: String,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredContentImageFile]

final class LocalContentImageStorage[F[_]: Sync: Clock] private (
    uploadRootDirectoryPath: Path
) extends ContentImageStorage[F]:
  override def storeImage(
      collection: ContentImageCollection,
      ownerUserId: UserId,
      originalFileName: String,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredContentImageFile] =
    Clock[F].realTimeInstant.flatMap { currentInstant =>
      Sync[F].blocking {
        val collectionDirectoryName =
          collection match
            case ContentImageCollection.Blog   => "blog"
            case ContentImageCollection.Review => "reviews"
        val collectionDirectoryPath = uploadRootDirectoryPath.resolve(collectionDirectoryName)
        Files.createDirectories(collectionDirectoryPath)
        val timestampText =
          DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentInstant.atZone(java.time.ZoneOffset.UTC))
        val normalizedExtension = fileExtension.toLowerCase
        val fileName = s"${ownerUserId.value}-$timestampText-${UUID.randomUUID().toString.take(8)}.$normalizedExtension"
        val targetPath = collectionDirectoryPath.resolve(fileName)
        Files.write(targetPath, fileBytes)
        StoredContentImageFile(
          publicUrl = s"/uploads/content/$collectionDirectoryName/$fileName",
          originalFileName = originalFileName,
          absolutePath = targetPath
        )
      }
    }

object LocalContentImageStorage:
  def create[F[_]: Sync: Clock](uploadRootDirectoryPath: Path): LocalContentImageStorage[F] =
    new LocalContentImageStorage[F](uploadRootDirectoryPath)
