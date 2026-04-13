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
      on conflict (asset_id) do update set
        owner_user_id = excluded.owner_user_id,
        asset_category = excluded.asset_category,
        original_file_name = excluded.original_file_name,
        file_extension = excluded.file_extension,
        mime_type = excluded.mime_type,
        file_size = excluded.file_size,
        binary_content = excluded.binary_content,
        created_at = excluded.created_at
    """.update.run.transact(transactor).as(asset)

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

