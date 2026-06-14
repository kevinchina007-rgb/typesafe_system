// UploadTourGroupCoverImagePlanner 是团体游模块的上传入口，负责请求校验、流程编排和结果返回。

package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.{Base64, UUID}

object UploadTourGroupCoverImagePlanner extends ConnectionApiPlan[UploadTourGroupCoverImagePlannerRequest, UploadTourGroupCoverImageResponse]:
  override val name: String = "UploadTourGroupCoverImagePlanner"
  override def plan(input: UploadTourGroupCoverImagePlannerRequest, connection: Connection): IO[UploadTourGroupCoverImageResponse] =
    IO.blocking {
      val normalizedFileName = input.originalFileName.trim
      val normalizedMimeType = input.mimeType.trim.toLowerCase
      require(input.ownerUserId.trim.nonEmpty, "ownerUserId is required")
      require(normalizedFileName.nonEmpty, "originalFileName is required")
      require(normalizedMimeType.startsWith("image/"), "only image files are supported")

      val fileContent = Base64.getDecoder.decode(input.fileContentBase64)
      require(fileContent.nonEmpty, "image file cannot be empty")
      require(fileContent.length <= 10 * 1024 * 1024, "image file cannot exceed 10 MB")

      val assetId = s"tour-group-cover-${UUID.randomUUID().toString.replace("-", "").take(20)}"
      val fileExtension = normalizedFileName.split("\\.").lastOption.filter(_ != normalizedFileName).getOrElse("bin")
      val statement = connection.prepareStatement(
        """
          insert into uploaded_binary_assets(
            asset_id, owner_user_id, asset_category, original_file_name, file_extension, mime_type, file_size, binary_content, created_at
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      )
      try
        statement.setString(1, assetId)
        statement.setString(2, input.ownerUserId.trim)
        statement.setString(3, "tour-group-cover")
        statement.setString(4, normalizedFileName)
        statement.setString(5, fileExtension)
        statement.setString(6, normalizedMimeType)
        statement.setLong(7, fileContent.length.toLong)
        statement.setBytes(8, fileContent)
        statement.setTimestamp(9, Timestamp.from(Instant.now()))
        statement.executeUpdate()
      finally statement.close()

      UploadTourGroupCoverImageResponse(
        assetId = assetId,
        publicUrl = s"/uploads/assets/$assetId/${java.net.URLEncoder.encode(normalizedFileName, java.nio.charset.StandardCharsets.UTF_8)}",
        originalFileName = normalizedFileName,
        mimeType = normalizedMimeType,
        fileSize = fileContent.length.toLong
      )
    }
