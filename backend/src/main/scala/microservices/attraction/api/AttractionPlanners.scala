package com.typesafe.travel.attraction.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan
import com.typesafe.travel.persistence.attraction.AttractionPlannerPlainSql

import java.sql.Connection
import java.time.Instant
import java.sql.Timestamp
import java.util.{Base64, UUID}

object AttractionSuggestionsPlanner extends ConnectionApiPlan[AttractionSuggestionRequest, AttractionSuggestionListPlannerResponse]:
  override val name: String = "AttractionSuggestionsPlanner"
  override def plan(input: AttractionSuggestionRequest, connection: Connection): IO[AttractionSuggestionListPlannerResponse] =
    AttractionPlannerPlainSql.suggestions(connection, input)

object ListAttractionsPlanner extends ConnectionApiPlan[ListAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListAttractionsPlanner"
  override def plan(input: ListAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.list(connection, input)

object GetAttractionDetailsPlanner extends ConnectionApiPlan[GetAttractionDetailsPlannerRequest, Attraction]:
  override val name: String = "GetAttractionDetailsPlanner"
  override def plan(input: GetAttractionDetailsPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.details(connection, input)

object ListManagedAttractionsPlanner extends ConnectionApiPlan[ListManagedAttractionsPlannerRequest, AttractionListPlannerResponse]:
  override val name: String = "ListManagedAttractionsPlanner"
  override def plan(input: ListManagedAttractionsPlannerRequest, connection: Connection): IO[AttractionListPlannerResponse] =
    AttractionPlannerPlainSql.listManaged(connection, input)

object CreateAttractionPlanner extends ConnectionApiPlan[CreateAttractionPlannerRequest, Attraction]:
  override val name: String = "CreateAttractionPlanner"
  override def plan(input: CreateAttractionPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.create(connection, input, Instant.now())

object UploadAttractionImagePlanner extends ConnectionApiPlan[UploadAttractionImagePlannerRequest, UploadAttractionImagePlannerResponse]:
  override val name: String = "UploadAttractionImagePlanner"
  override def plan(input: UploadAttractionImagePlannerRequest, connection: Connection): IO[UploadAttractionImagePlannerResponse] =
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
        statement.setTimestamp(9, Timestamp.from(Instant.now()))
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

object CreateAttractionTicketTypePlanner extends ConnectionApiPlan[CreateAttractionTicketTypePlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketTypePlanner"
  override def plan(input: CreateAttractionTicketTypePlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketType(connection, input, Instant.now())

object CreateAttractionTicketSessionPlanner extends ConnectionApiPlan[CreateAttractionTicketSessionPlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketSessionPlanner"
  override def plan(input: CreateAttractionTicketSessionPlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketSession(connection, input, Instant.now())

object CreateAttractionTicketRulePlanner extends ConnectionApiPlan[CreateAttractionTicketRulePlannerRequest, Attraction]:
  override val name: String = "CreateAttractionTicketRulePlanner"
  override def plan(input: CreateAttractionTicketRulePlannerRequest, connection: Connection): IO[Attraction] =
    AttractionPlannerPlainSql.createTicketRule(connection, input, Instant.now())
