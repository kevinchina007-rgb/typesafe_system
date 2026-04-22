package com.typesafe.travel.api.storage

import cats.effect.kernel.{Clock, Sync}
import cats.syntax.all.*
import com.typesafe.travel.api.application.{StoredTourGroupChatAttachmentFile, TourGroupChatAttachmentCollection, TourGroupChatAttachmentStorage}
import com.typesafe.travel.persistence.{UploadedBinaryAsset, UploadedBinaryAssetRepository}
import com.typesafe.travel.shared.kernel.UserId

import java.time.format.DateTimeFormatter
import java.util.UUID

object UploadedBinaryAssetUrl:
  def build(assetId: String, originalFileName: String, fallbackExtension: String): String =
    val sanitizedFileName = sanitizeFileName(assetId, originalFileName, fallbackExtension)
    s"/uploads/assets/$assetId/$sanitizedFileName"

  private def sanitizeFileName(assetId: String, originalFileName: String, fallbackExtension: String): String =
    val normalizedBaseName =
      originalFileName
        .trim
        .toLowerCase
        .replaceAll("[^a-z0-9._-]+", "-")
        .replaceAll("-{2,}", "-")
        .stripPrefix("-")
        .stripSuffix("-")

    val normalizedFallbackExtension =
      fallbackExtension
        .trim
        .toLowerCase
        .replaceAll("[^a-z0-9]+", "")

    val safeFallbackExtension =
      if normalizedFallbackExtension.nonEmpty then normalizedFallbackExtension
      else "bin"

    val hasMeaningfulFileName =
      normalizedBaseName.nonEmpty &&
      normalizedBaseName.exists(_.isLetterOrDigit) &&
      !normalizedBaseName.startsWith(".") &&
      !normalizedBaseName.endsWith(".") &&
      normalizedBaseName != "." &&
      normalizedBaseName != ".."

    if hasMeaningfulFileName then normalizedBaseName
    else s"$assetId.$safeFallbackExtension"

final class DatabaseAvatarStorage[F[_]: Sync: Clock] private (
    uploadedBinaryAssetRepository: UploadedBinaryAssetRepository[F]
) extends AvatarStorage[F]:
  override def storeAvatar(
      userId: UserId,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredAvatarFile] =
    Clock[F].realTimeInstant.flatMap { currentInstant =>
      val timestampText =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentInstant.atZone(java.time.ZoneOffset.UTC))
      val normalizedExtension = fileExtension.toLowerCase
      val assetId = s"avatar-${UUID.randomUUID().toString.replace("-", "").take(20)}"
      val originalFileName = s"${userId.value}-$timestampText.$normalizedExtension"
      val mimeType =
        normalizedExtension match
          case "png"              => "image/png"
          case "jpg" | "jpeg"     => "image/jpeg"
          case other              => s"application/$other"

      uploadedBinaryAssetRepository
        .saveAsset(
          UploadedBinaryAsset(
            assetId = assetId,
            ownerUserId = Some(userId),
            assetCategory = "avatar",
            originalFileName = originalFileName,
            fileExtension = normalizedExtension,
            mimeType = mimeType,
            fileSize = fileBytes.length.toLong,
            binaryContent = fileBytes,
            createdAt = currentInstant
          )
        )
        .map { storedAsset =>
          StoredAvatarFile(
            publicUrl = UploadedBinaryAssetUrl.build(storedAsset.assetId, storedAsset.originalFileName, storedAsset.fileExtension),
            storagePath = s"db:${storedAsset.assetId}"
          )
        }
    }

object DatabaseAvatarStorage:
  def create[F[_]: Sync: Clock](uploadedBinaryAssetRepository: UploadedBinaryAssetRepository[F]): DatabaseAvatarStorage[F] =
    new DatabaseAvatarStorage[F](uploadedBinaryAssetRepository)

final class DatabaseContentImageStorage[F[_]: Sync: Clock] private (
    uploadedBinaryAssetRepository: UploadedBinaryAssetRepository[F]
) extends ContentImageStorage[F]:
  override def storeImage(
      collection: ContentImageCollection,
      ownerUserId: UserId,
      originalFileName: String,
      fileExtension: String,
      fileBytes: Array[Byte]
  ): F[StoredContentImageFile] =
    Clock[F].realTimeInstant.flatMap { currentInstant =>
      val normalizedExtension = fileExtension.toLowerCase
      val assetId = s"content-${UUID.randomUUID().toString.replace("-", "").take(20)}"
      val mimeType =
        normalizedExtension match
          case "png"              => "image/png"
          case "jpg" | "jpeg"     => "image/jpeg"
          case "webp"             => "image/webp"
          case other              => s"application/$other"
      val assetCategory =
        collection match
          case ContentImageCollection.Blog   => "blog-image"
          case ContentImageCollection.Review => "review-image"

      uploadedBinaryAssetRepository
        .saveAsset(
          UploadedBinaryAsset(
            assetId = assetId,
            ownerUserId = Some(ownerUserId),
            assetCategory = assetCategory,
            originalFileName = originalFileName,
            fileExtension = normalizedExtension,
            mimeType = mimeType,
            fileSize = fileBytes.length.toLong,
            binaryContent = fileBytes,
            createdAt = currentInstant
          )
        )
        .map { storedAsset =>
          StoredContentImageFile(
            publicUrl = UploadedBinaryAssetUrl.build(storedAsset.assetId, storedAsset.originalFileName, storedAsset.fileExtension),
            originalFileName = storedAsset.originalFileName,
            storagePath = s"db:${storedAsset.assetId}"
          )
        }
    }

object DatabaseContentImageStorage:
  def create[F[_]: Sync: Clock](uploadedBinaryAssetRepository: UploadedBinaryAssetRepository[F]): DatabaseContentImageStorage[F] =
    new DatabaseContentImageStorage[F](uploadedBinaryAssetRepository)

final class DatabaseTourGroupChatAttachmentStorage[F[_]: Sync: Clock] private (
    uploadedBinaryAssetRepository: UploadedBinaryAssetRepository[F]
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
      val normalizedExtension = fileExtension.toLowerCase
      val assetId = s"attachment-${UUID.randomUUID().toString.replace("-", "").take(20)}"
      val assetCategory =
        collection match
          case TourGroupChatAttachmentCollection.Image => "tour-group-image"
          case TourGroupChatAttachmentCollection.File  => "tour-group-file"

      uploadedBinaryAssetRepository
        .saveAsset(
          UploadedBinaryAsset(
            assetId = assetId,
            ownerUserId = Some(ownerUserId),
            assetCategory = assetCategory,
            originalFileName = originalFileName,
            fileExtension = normalizedExtension,
            mimeType = mimeType,
            fileSize = fileBytes.length.toLong,
            binaryContent = fileBytes,
            createdAt = currentInstant
          )
        )
        .map { storedAsset =>
          StoredTourGroupChatAttachmentFile(
            publicUrl = UploadedBinaryAssetUrl.build(storedAsset.assetId, storedAsset.originalFileName, storedAsset.fileExtension),
            originalFileName = storedAsset.originalFileName,
            mimeType = storedAsset.mimeType,
            fileSize = storedAsset.fileSize,
            storagePath = s"db:${storedAsset.assetId}"
          )
        }
    }

object DatabaseTourGroupChatAttachmentStorage:
  def create[F[_]: Sync: Clock](uploadedBinaryAssetRepository: UploadedBinaryAssetRepository[F]): DatabaseTourGroupChatAttachmentStorage[F] =
    new DatabaseTourGroupChatAttachmentStorage[F](uploadedBinaryAssetRepository)
