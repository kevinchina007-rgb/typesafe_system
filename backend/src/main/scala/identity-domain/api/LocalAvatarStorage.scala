package com.typesafe.travel.api.storage

import cats.effect.kernel.{Clock, Sync}
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.UserId

import java.nio.file.{Files, Path}
import java.time.format.DateTimeFormatter

final case class StoredAvatarFile(
    publicUrl: String,
    storagePath: String
)

trait AvatarStorage[F[_]]:
  def storeAvatar(
      userId: UserId,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredAvatarFile]

final class LocalAvatarStorage[F[_]: Sync: Clock] private (
    uploadRootDirectoryPath: Path
) extends AvatarStorage[F]:
  override def storeAvatar(
      userId: UserId,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredAvatarFile] =
    Clock[F].realTimeInstant.flatMap { currentInstant =>
      Sync[F].blocking {
        Files.createDirectories(uploadRootDirectoryPath)
        val timestampText =
          DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentInstant.atZone(java.time.ZoneOffset.UTC))
        val normalizedExtension = fileExtension.toLowerCase
        val fileName = s"${userId.value}-$timestampText.$normalizedExtension"
        val targetPath = uploadRootDirectoryPath.resolve(fileName)
        Files.write(targetPath, fileBytes)
        StoredAvatarFile(
          publicUrl = s"/uploads/avatars/$fileName",
          storagePath = targetPath.toString
        )
      }
    }

object LocalAvatarStorage:
  def create[F[_]: Sync: Clock](uploadRootDirectoryPath: Path): LocalAvatarStorage[F] =
    new LocalAvatarStorage[F](uploadRootDirectoryPath)
