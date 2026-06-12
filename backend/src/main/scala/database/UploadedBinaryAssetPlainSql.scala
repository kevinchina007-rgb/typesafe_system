// UploadedBinaryAssetPlainSql 封装对应的数据库查询和更新。

package com.typesafe.travel.persistence

import cats.effect.IO
import com.typesafe.travel.shared.kernel.UserId

import java.sql.{Connection, DriverManager, ResultSet}
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

trait UploadedBinaryAssetReader:
  def findAssetByAssetId(assetId: String): IO[Option[UploadedBinaryAsset]]

final class UploadedBinaryAssetPlainSql(databaseConfig: DatabaseConfig) extends UploadedBinaryAssetReader:
  override def findAssetByAssetId(assetId: String): IO[Option[UploadedBinaryAsset]] =
    IO.blocking {
      Class.forName(databaseConfig.jdbcDriverClassName)
      val connection = DriverManager.getConnection(databaseConfig.jdbcUrl, databaseConfig.jdbcUser, databaseConfig.jdbcPassword)
      try UploadedBinaryAssetPlainSql.findAssetByAssetId(connection, assetId)
      finally connection.close()
    }

object UploadedBinaryAssetPlainSql:
  def apply(databaseConfig: DatabaseConfig): UploadedBinaryAssetPlainSql =
    new UploadedBinaryAssetPlainSql(databaseConfig)

  private val findByAssetIdSql: String =
    """
      select
        asset_id,
        owner_user_id,
        asset_category,
        original_file_name,
        file_extension,
        mime_type,
        file_size,
        binary_content,
        created_at
      from uploaded_binary_assets
      where asset_id = ?
    """

  def findAssetByAssetId(connection: Connection, assetId: String): Option[UploadedBinaryAsset] =
    val statement = connection.prepareStatement(findByAssetIdSql)
    try
      statement.setString(1, assetId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then Some(readUploadedBinaryAsset(resultSet))
        else None
      finally resultSet.close()
    finally statement.close()

  private def readUploadedBinaryAsset(resultSet: ResultSet): UploadedBinaryAsset =
    val ownerUserIdValue = resultSet.getString("owner_user_id")
    UploadedBinaryAsset(
      assetId = resultSet.getString("asset_id"),
      ownerUserId = Option(ownerUserIdValue).filter(_.nonEmpty).map(UserId.apply),
      assetCategory = resultSet.getString("asset_category"),
      originalFileName = resultSet.getString("original_file_name"),
      fileExtension = resultSet.getString("file_extension"),
      mimeType = resultSet.getString("mime_type"),
      fileSize = resultSet.getLong("file_size"),
      binaryContent = resultSet.getBytes("binary_content"),
      createdAt = resultSet.getTimestamp("created_at").toInstant
    )
