// AttractionImagePlanner 只负责 attraction 图片上传这一条后端链路，包括文件校验、二进制落库和返回上传结果，前端只镜像请求/响应对象，不镜像这一实现层。
package com.typesafe.travel.attraction.api

import com.typesafe.travel.attraction.domain.*

import cats.effect.IO

import java.sql.Connection
import java.time.Instant
import java.util.{Base64, UUID}

object AttractionImagePlanner:
  def uploadImage(connection: Connection, input: UploadAttractionImagePlannerRequest): IO[UploadAttractionImagePlannerResponse] =
    IO.blocking {
      val normalizedFileName = input.originalFileName.trim
      val normalizedMimeType = input.mimeType.trim.toLowerCase
      require(normalizedFileName.nonEmpty, "originalFileName is required")
      require(normalizedMimeType.startsWith("image/"), "only image files are supported")

      val fileContent = Base64.getDecoder.decode(input.fileContentBase64)
      require(fileContent.nonEmpty, "image file cannot be empty")
      require(fileContent.length <= 10 * 1024 * 1024, "image file cannot exceed 10 MB")

      val assetId = s"attraction-image-${UUID.randomUUID().toString.replace("-", "").take(20)}"
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
        statement.setString(2, null)
        statement.setString(3, "attraction-image")
        statement.setString(4, normalizedFileName)
        statement.setString(5, fileExtension)
        statement.setString(6, normalizedMimeType)
        statement.setLong(7, fileContent.length.toLong)
        statement.setBytes(8, fileContent)
        statement.setTimestamp(9, java.sql.Timestamp.from(Instant.now()))
        statement.executeUpdate()
      finally statement.close()

      UploadAttractionImagePlannerResponse(
        assetId = assetId,
        publicUrl = s"/uploads/assets/$assetId/${java.net.URLEncoder.encode(normalizedFileName, java.nio.charset.StandardCharsets.UTF_8)}",
        originalFileName = normalizedFileName,
        mimeType = normalizedMimeType,
        fileSize = fileContent.length.toLong
      )
    }


