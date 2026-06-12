// UploadHotelRoomTypeImagePlanner 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.Base64
import java.util.UUID

object UploadHotelRoomTypeImagePlanner extends ConnectionApiPlan[UploadHotelRoomTypeImagePlannerRequest, UploadHotelRoomTypeImagePlannerResponse]:
  override val name: String = "UploadHotelRoomTypeImagePlanner"
  override def plan(input: UploadHotelRoomTypeImagePlannerRequest, connection: Connection): IO[UploadHotelRoomTypeImagePlannerResponse] =
    IO.blocking {
      val normalizedFileName = input.originalFileName.trim
      val fileContent = Base64.getDecoder.decode(input.fileContentBase64)
      val assetId = s"hotel-room-${UUID.randomUUID().toString.replace("-", "").take(20)}"
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
        statement.setString(3, "hotel-room-image")
        statement.setString(4, normalizedFileName)
        statement.setString(5, normalizedFileName.split("\\.").lastOption.getOrElse("bin"))
        statement.setString(6, input.mimeType)
        statement.setLong(7, fileContent.length.toLong)
        statement.setBytes(8, fileContent)
        statement.setTimestamp(9, Timestamp.from(Instant.now()))
        statement.executeUpdate()
      finally statement.close()
      UploadHotelRoomTypeImagePlannerResponse(
        assetId = assetId,
        publicUrl = s"/uploads/assets/$assetId/${java.net.URLEncoder.encode(normalizedFileName, java.nio.charset.StandardCharsets.UTF_8)}",
        originalFileName = normalizedFileName,
        mimeType = input.mimeType,
        fileSize = fileContent.length.toLong
      )
    }
