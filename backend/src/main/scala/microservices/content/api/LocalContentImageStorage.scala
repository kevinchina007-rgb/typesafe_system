// LocalContentImageStorage 定义内容模块的图片存储实现。

package com.typesafe.travel.api.storage

import cats.effect.IO
import cats.effect.kernel.Clock
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
    storagePath: String
)

trait ContentImageStorage:
  def storeImage(
      collection: ContentImageCollection,
      ownerUserId: UserId,
      originalFileName: String,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): IO[StoredContentImageFile]

final class LocalContentImageStorage private (
    uploadRootDirectoryPath: Path
) extends ContentImageStorage:
  override def storeImage(
      collection: ContentImageCollection,
      ownerUserId: UserId,
      originalFileName: String,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): IO[StoredContentImageFile] =
    Clock[IO].realTimeInstant.flatMap { currentInstant =>
      IO.blocking {
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
          storagePath = targetPath.toString
        )
      }
    }

object LocalContentImageStorage:
  def create(uploadRootDirectoryPath: Path): LocalContentImageStorage =
    new LocalContentImageStorage(uploadRootDirectoryPath)
