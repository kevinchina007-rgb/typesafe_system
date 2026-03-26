package com.typesafe.travel.api.storage

import cats.effect.kernel.Sync
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.UserId

import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.time.Instant
import java.time.format.DateTimeFormatter

final case class StoredAvatarFile(
    publicUrl: String,
    absolutePath: Path
)

trait AvatarStorage[F[_]]:
  def storeAvatar(
      userId: UserId,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredAvatarFile]

final class LocalAvatarStorage[F[_]: Sync] private (
    uploadRootDirectoryPath: Path
) extends AvatarStorage[F]:
  override def storeAvatar(
      userId: UserId,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredAvatarFile] =
    Sync[F].blocking {
      Files.createDirectories(uploadRootDirectoryPath)
      val timestampText =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(Instant.now().atZone(java.time.ZoneOffset.UTC))
      val normalizedExtension = fileExtension.toLowerCase
      val fileName = s"${userId.value}-$timestampText.$normalizedExtension"
      val targetPath = uploadRootDirectoryPath.resolve(fileName)
      Files.write(targetPath, fileBytes)
      StoredAvatarFile(
        publicUrl = s"/uploads/avatars/$fileName",
        absolutePath = targetPath
      )
    }

object LocalAvatarStorage:
  def create[F[_]: Sync](uploadRootDirectoryPath: Path): LocalAvatarStorage[F] =
    new LocalAvatarStorage[F](uploadRootDirectoryPath)
