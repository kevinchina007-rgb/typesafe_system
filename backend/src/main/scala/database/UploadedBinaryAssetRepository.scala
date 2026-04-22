package com.typesafe.travel.persistence

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.UserId
import doobie.*
import doobie.implicits.*

import java.time.Instant

final case class UploadedBinaryAsset(
    assetId: String,
    ownerUserId: Option[UserId],
    assetCategory: String,
    originalFileName: String,
    fileExtension: String,
    mimeType: String,
    fileSize: Long,
    binaryContent: Array[Byte],
    createdAt: Instant
)

trait UploadedBinaryAssetReader[F[_]]:
  def findAssetByAssetId(assetId: String): F[Option[UploadedBinaryAsset]]

trait UploadedBinaryAssetRepository[F[_]] extends UploadedBinaryAssetReader[F]:
  def saveAsset(asset: UploadedBinaryAsset): F[UploadedBinaryAsset]

final class DoobieUploadedBinaryAssetRepository[F[_]: Async](
    transactor: Transactor[F]
) extends UploadedBinaryAssetRepository[F]:

  override def saveAsset(asset: UploadedBinaryAsset): F[UploadedBinaryAsset] =
    val updateExisting =
      sql"""
        update uploaded_binary_assets
        set
          owner_user_id = ${asset.ownerUserId.map(_.value)},
          asset_category = ${asset.assetCategory},
          original_file_name = ${asset.originalFileName},
          file_extension = ${asset.fileExtension},
          mime_type = ${asset.mimeType},
          file_size = ${asset.fileSize},
          binary_content = ${asset.binaryContent},
          created_at = ${asset.createdAt}
        where asset_id = ${asset.assetId}
      """.update.run

    val insertNew =
      sql"""
        insert into uploaded_binary_assets (
          asset_id,
          owner_user_id,
          asset_category,
          original_file_name,
          file_extension,
          mime_type,
          file_size,
          binary_content,
          created_at
        ) values (
          ${asset.assetId},
          ${asset.ownerUserId.map(_.value)},
          ${asset.assetCategory},
          ${asset.originalFileName},
          ${asset.fileExtension},
          ${asset.mimeType},
          ${asset.fileSize},
          ${asset.binaryContent},
          ${asset.createdAt}
        )
      """.update.run

    updateExisting.transact(transactor).flatMap { updatedRowCount =>
      if updatedRowCount > 0 then Async[F].pure(asset)
      else insertNew.transact(transactor).as(asset)
    }

  override def findAssetByAssetId(assetId: String): F[Option[UploadedBinaryAsset]] =
    sql"""
      select
        owner_user_id,
        asset_category,
        original_file_name,
        file_extension,
        mime_type,
        file_size,
        binary_content,
        created_at
      from uploaded_binary_assets
      where asset_id = $assetId
    """.query[(Option[String], String, String, String, String, Long, Array[Byte], Instant)]
      .option
      .transact(transactor)
      .map(
        _.map { case (ownerUserId, assetCategory, originalFileName, fileExtension, mimeType, fileSize, binaryContent, createdAt) =>
          UploadedBinaryAsset(
            assetId = assetId,
            ownerUserId = ownerUserId.map(UserId.apply),
            assetCategory = assetCategory,
            originalFileName = originalFileName,
            fileExtension = fileExtension,
            mimeType = mimeType,
            fileSize = fileSize,
            binaryContent = binaryContent,
            createdAt = createdAt
          )
        }
      )

object DoobieUploadedBinaryAssetRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieUploadedBinaryAssetRepository[F] =
    new DoobieUploadedBinaryAssetRepository[F](transactor)

