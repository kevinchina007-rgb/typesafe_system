package com.typesafe.travel.advertising.domain

import cats.effect.IO
import io.circe.Json

import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.Instant
import java.util.Base64
import java.util.UUID

object AdvertisementPlainSqlSupport:
  val selectAdvertisementsSql: String =
    """
      select advertisement_id, owner_manager_id, owner_type, owner_display_name, advertisement_kind,
             target_resource_type, target_resource_id, resource_summary_title, landing_target,
             placement, audience, title, subtitle, description, image_url, cta_label,
             review_status, delivery_status, priority, slot_index, creative_json, creative_width, creative_height,
             start_at, end_at, rejection_note, created_at, updated_at
      from advertisements
    """

  val insertAdvertisementSql: String =
    """
      insert into advertisements(
        advertisement_id, owner_manager_id, owner_type, owner_display_name, advertisement_kind,
        target_resource_type, target_resource_id, resource_summary_title, landing_target,
        placement, audience, title, subtitle, description, image_url, cta_label,
        review_status, delivery_status, priority, slot_index, creative_json, creative_width, creative_height,
        start_at, end_at, rejection_note, created_at, updated_at
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  val updateAdvertisementSql: String =
    """
      update advertisements
      set target_resource_type = ?, target_resource_id = ?, resource_summary_title = ?, landing_target = ?,
          placement = ?, audience = ?, title = ?, subtitle = ?, description = ?, image_url = ?, cta_label = ?,
          review_status = ?, delivery_status = ?, priority = ?, slot_index = ?, advertisement_kind = ?,
          creative_json = ?, creative_width = ?, creative_height = ?, start_at = ?, end_at = ?,
          rejection_note = ?, updated_at = ?
      where advertisement_id = ? and owner_manager_id = ? and owner_type = ?
    """

  val updateOwnerStatusSql: String =
    """
      update advertisements
      set review_status = ?, delivery_status = ?, slot_index = ?, updated_at = ?
      where advertisement_id = ? and owner_manager_id = ? and owner_type = ?
    """

  val updateReviewStatusSql: String =
    """
      update advertisements
      set review_status = ?, delivery_status = ?, slot_index = ?, rejection_note = ?, updated_at = ?
      where advertisement_id = ?
    """

  val clearConflictingSlotSql: String =
    """
      update advertisements
      set slot_index = null, updated_at = ?
      where placement = ? and slot_index = ? and advertisement_id <> ?
    """

  val listReviewsSql: String =
    """
      select review_id, advertisement_id, reviewer_manager_id, decision, review_note, reviewed_at
      from advertisement_reviews
      where advertisement_id = ?
      order by reviewed_at desc
    """

  val insertReviewSql: String =
    """
      insert into advertisement_reviews(review_id, advertisement_id, reviewer_manager_id, decision, review_note, reviewed_at)
      values (?, ?, ?, ?, ?, ?)
    """

  val insertUploadedBinaryAssetSql: String =
    """
      insert into uploaded_binary_assets(
        asset_id, owner_user_id, asset_category, original_file_name,
        file_extension, mime_type, file_size, binary_content, created_at
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  val selectDeliverySettingsSql: String =
    """
      select placement, rotation_interval_seconds, play_order, start_at, end_at, updated_by_manager_id, updated_at
      from advertisement_delivery_settings
      where placement = ?
    """

  val upsertDeliverySettingsSql: String =
    """
      insert into advertisement_delivery_settings(
        placement, rotation_interval_seconds, play_order, start_at, end_at, updated_by_manager_id, updated_at
      ) values (?, ?, ?, ?, ?, ?, ?)
      on conflict (placement) do update
      set rotation_interval_seconds = excluded.rotation_interval_seconds,
          play_order = excluded.play_order,
          start_at = excluded.start_at,
          end_at = excluded.end_at,
          updated_by_manager_id = excluded.updated_by_manager_id,
          updated_at = excluded.updated_at
    """

  def listSql(request: ListAdvertisementsRequest): (String, List[String]) =
    val filters = List.newBuilder[String]
    val parameters = List.newBuilder[String]

    request.placement.map(_.trim).filter(_.nonEmpty).foreach { value =>
      filters += "placement = ?"
      parameters += AdvertisementPlacement.fromText(value).toString
    }
    request.reviewStatus.map(_.trim).filter(_.nonEmpty).foreach { value =>
      filters += "review_status = ?"
      parameters += AdvertisementReviewStatus.fromText(value).toString
    }
    request.reviewStatuses.getOrElse(Nil).map(_.trim).filter(_.nonEmpty).map(AdvertisementReviewStatus.fromText).distinct match
      case Nil => ()
      case values =>
        filters += values.map(_ => "?").mkString("review_status in (", ", ", ")")
        values.foreach(value => parameters += value.toString)
    request.ownerManagerId.map(_.trim).filter(_.nonEmpty).foreach { value =>
      filters += "owner_manager_id = ?"
      parameters += value
    }
    request.ownerType.map(_.trim).filter(_.nonEmpty).foreach { value =>
      filters += "owner_type = ?"
      parameters += AdvertisementOwnerType.fromText(value).toString
    }

    val whereSql = filters.result() match
      case Nil => ""
      case values => values.mkString(" where ", " and ", "")

    (selectAdvertisementsSql + whereSql + " order by priority desc, updated_at desc", parameters.result())

  def bindInsert(statement: PreparedStatement, advertisementId: String, request: CreateAdvertisementRequest, createdAt: Instant): Unit =
    val ownerType = AdvertisementOwnerType.fromText(request.ownerType).toString
    val targetResourceType = AdvertisementTargetResourceType.fromText(request.targetResourceType).toString
    val placement = AdvertisementPlacement.fromText(request.placement).toString
    val resourceSummaryTitle = request.resourceSummaryTitle.map(_.trim).filter(_.nonEmpty).getOrElse(request.targetResourceId)
    val landingTarget = request.landingTarget.map(_.trim).filter(_.nonEmpty).getOrElse(defaultLandingTarget(targetResourceType, request.targetResourceId))

    statement.setString(1, advertisementId)
    statement.setString(2, request.ownerManagerId)
    statement.setString(3, ownerType)
    statement.setString(4, request.ownerDisplayName)
    statement.setString(5, normalizeAdvertisementKind(request.advertisementKind))
    statement.setString(6, targetResourceType)
    statement.setString(7, request.targetResourceId)
    statement.setString(8, resourceSummaryTitle)
    statement.setString(9, landingTarget)
    statement.setString(10, placement)
    statement.setString(11, AdvertisementAudience.BookingUser.toString)
    statement.setString(12, request.title)
    statement.setString(13, request.subtitle)
    statement.setString(14, request.description)
    statement.setString(15, request.imageUrl.map(_.trim).filter(_.nonEmpty).orNull)
    statement.setString(16, request.ctaLabel)
    statement.setString(17, AdvertisementReviewStatus.Draft.toString)
    statement.setString(18, AdvertisementDeliveryStatus.Scheduled.toString)
    statement.setInt(19, request.priority)
    statement.setObject(20, null)
    statement.setString(21, request.creativeJson.map(_.trim).filter(_.nonEmpty).orNull)
    statement.setInt(22, request.creativeWidth.getOrElse(960))
    statement.setInt(23, request.creativeHeight.getOrElse(240))
    statement.setTimestamp(24, Timestamp.from(Instant.parse(request.startAt)))
    statement.setTimestamp(25, Timestamp.from(Instant.parse(request.endAt)))
    statement.setString(26, null)
    statement.setTimestamp(27, Timestamp.from(createdAt))
    statement.setTimestamp(28, Timestamp.from(createdAt))

  def bindUpdate(statement: PreparedStatement, request: UpdateAdvertisementRequest, updatedAt: Instant): Unit =
    val ownerType = AdvertisementOwnerType.fromText(request.ownerType).toString
    val targetResourceType = AdvertisementTargetResourceType.fromText(request.targetResourceType).toString
    val resourceSummaryTitle = request.resourceSummaryTitle.map(_.trim).filter(_.nonEmpty).getOrElse(request.targetResourceId)
    val landingTarget = request.landingTarget.map(_.trim).filter(_.nonEmpty).getOrElse(defaultLandingTarget(targetResourceType, request.targetResourceId))

    statement.setString(1, targetResourceType)
    statement.setString(2, request.targetResourceId)
    statement.setString(3, resourceSummaryTitle)
    statement.setString(4, landingTarget)
    statement.setString(5, AdvertisementPlacement.fromText(request.placement).toString)
    statement.setString(6, AdvertisementAudience.BookingUser.toString)
    statement.setString(7, request.title)
    statement.setString(8, request.subtitle)
    statement.setString(9, request.description)
    statement.setString(10, request.imageUrl.map(_.trim).filter(_.nonEmpty).orNull)
    statement.setString(11, request.ctaLabel)
    statement.setString(12, AdvertisementReviewStatus.Draft.toString)
    statement.setString(13, AdvertisementDeliveryStatus.Scheduled.toString)
    statement.setInt(14, request.priority)
    statement.setObject(15, null)
    statement.setString(16, normalizeAdvertisementKind(request.advertisementKind))
    statement.setString(17, request.creativeJson.map(_.trim).filter(_.nonEmpty).orNull)
    statement.setInt(18, request.creativeWidth.getOrElse(960))
    statement.setInt(19, request.creativeHeight.getOrElse(240))
    statement.setTimestamp(20, Timestamp.from(Instant.parse(request.startAt)))
    statement.setTimestamp(21, Timestamp.from(Instant.parse(request.endAt)))
    statement.setString(22, null)
    statement.setTimestamp(23, Timestamp.from(updatedAt))
    statement.setString(24, request.advertisementId)
    statement.setString(25, request.ownerManagerId)
    statement.setString(26, ownerType)

  def updateOwnerStatus(
      connection: Connection,
      request: AdvertisementOwnerActionRequest,
      reviewStatus: AdvertisementReviewStatus,
      deliveryStatus: AdvertisementDeliveryStatus,
      updatedAt: Instant
  ): IO[AdvertisementResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(updateOwnerStatusSql)
      try
        statement.setString(1, reviewStatus.toString)
        statement.setString(2, deliveryStatus.toString)
        statement.setObject(3, null)
        statement.setTimestamp(4, Timestamp.from(updatedAt))
        statement.setString(5, request.advertisementId)
        statement.setString(6, request.ownerManagerId)
        statement.setString(7, AdvertisementOwnerType.fromText(request.ownerType).toString)
        val updatedRows = statement.executeUpdate()
        if updatedRows == 0 then throw AdvertisementError.AdvertisementWasNotFound(AdvertisementId(request.advertisementId))
        readById(connection, request.advertisementId)
      finally statement.close()
    }

  def review(
      connection: Connection,
      request: AdvertisementReviewDecisionRequest,
      decision: AdvertisementReviewDecision,
      reviewStatus: AdvertisementReviewStatus,
      deliveryStatus: AdvertisementDeliveryStatus,
      updatedAt: Instant
  ): IO[AdvertisementResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(updateReviewStatusSql)
      try
        statement.setString(1, reviewStatus.toString)
        statement.setString(2, deliveryStatus.toString)
        statement.setObject(3, null)
        statement.setString(4, request.reviewNote.map(_.trim).filter(_.nonEmpty).orNull)
        statement.setTimestamp(5, Timestamp.from(updatedAt))
        statement.setString(6, request.advertisementId)
        val updatedRows = statement.executeUpdate()
        if updatedRows == 0 then throw AdvertisementError.AdvertisementWasNotFound(AdvertisementId(request.advertisementId))
      finally statement.close()

      insertReview(
        connection = connection,
        advertisementId = request.advertisementId,
        reviewerManagerId = request.reviewerManagerId,
        decision = decision,
        reviewNote = request.reviewNote.map(_.trim).filter(_.nonEmpty),
        reviewedAt = updatedAt
      )
      readById(connection, request.advertisementId)
    }

  def insertReview(
      connection: Connection,
      advertisementId: String,
      reviewerManagerId: String,
      decision: AdvertisementReviewDecision,
      reviewNote: Option[String],
      reviewedAt: Instant
  ): Unit =
    val statement = connection.prepareStatement(insertReviewSql)
    try
      statement.setString(1, s"advertisement-review-${UUID.randomUUID().toString.take(12)}")
      statement.setString(2, advertisementId)
      statement.setString(3, reviewerManagerId)
      statement.setString(4, decision.toString)
      statement.setString(5, reviewNote.orNull)
      statement.setTimestamp(6, Timestamp.from(reviewedAt))
      statement.executeUpdate()
    finally statement.close()

  def readById(connection: Connection, advertisementId: String): AdvertisementResponse =
    val statement = connection.prepareStatement(selectAdvertisementsSql + " where advertisement_id = ?")
    try
      statement.setString(1, advertisementId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then readAdvertisementResponse(resultSet, listReviews(connection, advertisementId))
        else throw new IllegalStateException(s"Advertisement '$advertisementId' was not found")
      finally resultSet.close()
    finally statement.close()

  def readDeliverySettingsByPlacement(connection: Connection, placement: String): Option[AdvertisementDeliverySettingsResponse] =
    val statement = connection.prepareStatement(selectDeliverySettingsSql)
    try
      statement.setString(1, placement)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then Some(readDeliverySettings(resultSet)) else None
      finally resultSet.close()
    finally statement.close()

  def readDeliverySettings(resultSet: ResultSet): AdvertisementDeliverySettingsResponse =
    AdvertisementDeliverySettingsResponse(
      placement = resultSet.getString("placement"),
      rotationIntervalSeconds = resultSet.getInt("rotation_interval_seconds"),
      playOrder = resultSet.getString("play_order"),
      startAt = Option(resultSet.getTimestamp("start_at")).map(_.toInstant.toString),
      endAt = Option(resultSet.getTimestamp("end_at")).map(_.toInstant.toString),
      updatedByManagerId = Option(resultSet.getString("updated_by_manager_id")),
      updatedAt = resultSet.getTimestamp("updated_at").toInstant.toString
    )

  def normalizeActiveSlots(connection: Connection, placement: String, updatedAt: Instant): Unit =
    val statement = connection.prepareStatement(
      """
        select advertisement_id
        from advertisements
        where placement = ? and review_status = ? and delivery_status = ? and slot_index is not null
        order by slot_index asc, updated_at asc
      """
    )
    try
      statement.setString(1, placement)
      statement.setString(2, AdvertisementReviewStatus.Approved.toString)
      statement.setString(3, AdvertisementDeliveryStatus.Active.toString)
      val resultSet = statement.executeQuery()
      try
        val ids = List.newBuilder[String]
        while resultSet.next() do ids += resultSet.getString("advertisement_id")
        ids.result().take(4).zipWithIndex.foreach { case (advertisementId, index) =>
          val updateStatement = connection.prepareStatement("update advertisements set slot_index = ?, updated_at = ? where advertisement_id = ?")
          try
            updateStatement.setInt(1, index + 1)
            updateStatement.setTimestamp(2, Timestamp.from(updatedAt))
            updateStatement.setString(3, advertisementId)
            updateStatement.executeUpdate()
          finally updateStatement.close()
        }
      finally resultSet.close()
    finally statement.close()

  def listReviews(connection: Connection, advertisementId: String): List[AdvertisementReviewResponse] =
    val statement = connection.prepareStatement(listReviewsSql)
    try
      statement.setString(1, advertisementId)
      val resultSet = statement.executeQuery()
      try
        val rows = List.newBuilder[AdvertisementReviewResponse]
        while resultSet.next() do rows += readAdvertisementReviewResponse(resultSet)
        rows.result()
      finally resultSet.close()
    finally statement.close()

  def readAdvertisementResponse(resultSet: ResultSet, reviews: List[AdvertisementReviewResponse]): AdvertisementResponse =
    AdvertisementResponse(
      advertisementId = resultSet.getString("advertisement_id"),
      ownerManagerId = resultSet.getString("owner_manager_id"),
      ownerType = resultSet.getString("owner_type"),
      ownerDisplayName = resultSet.getString("owner_display_name"),
      advertisementKind = resultSet.getString("advertisement_kind"),
      targetResourceType = resultSet.getString("target_resource_type"),
      targetResourceId = resultSet.getString("target_resource_id"),
      resourceSummaryTitle = resultSet.getString("resource_summary_title"),
      landingTarget = resultSet.getString("landing_target"),
      placement = resultSet.getString("placement"),
      audience = resultSet.getString("audience"),
      title = resultSet.getString("title"),
      subtitle = resultSet.getString("subtitle"),
      description = resultSet.getString("description"),
      imageUrl = Option(resultSet.getString("image_url")),
      ctaLabel = resultSet.getString("cta_label"),
      reviewStatus = resultSet.getString("review_status"),
      deliveryStatus = resultSet.getString("delivery_status"),
      priority = resultSet.getInt("priority"),
      slotIndex = Option(resultSet.getObject("slot_index")).map(_.asInstanceOf[Number].intValue()),
      creativeJson = Option(resultSet.getString("creative_json")),
      creativeWidth = resultSet.getInt("creative_width"),
      creativeHeight = resultSet.getInt("creative_height"),
      startAt = resultSet.getTimestamp("start_at").toInstant.toString,
      endAt = resultSet.getTimestamp("end_at").toInstant.toString,
      rejectionNote = Option(resultSet.getString("rejection_note")),
      createdAt = resultSet.getTimestamp("created_at").toInstant.toString,
      updatedAt = resultSet.getTimestamp("updated_at").toInstant.toString,
      reviews = reviews
    )

  def readAdvertisementReviewResponse(resultSet: ResultSet): AdvertisementReviewResponse =
    AdvertisementReviewResponse(
      reviewId = resultSet.getString("review_id"),
      reviewerManagerId = resultSet.getString("reviewer_manager_id"),
      decision = resultSet.getString("decision"),
      reviewNote = Option(resultSet.getString("review_note")),
      reviewedAt = resultSet.getTimestamp("reviewed_at").toInstant.toString
    )

  def shouldIncludeAdvertisement(advertisement: AdvertisementResponse, request: ListAdvertisementsRequest): Boolean =
    request.deliverableOnly.getOrElse(false) match
      case false => true
      case true =>
        advertisement.reviewStatus == AdvertisementReviewStatus.Approved.toString &&
          advertisement.deliveryStatus == AdvertisementDeliveryStatus.Active.toString &&
          advertisement.slotIndex.nonEmpty

  def bindStrings(statement: PreparedStatement, values: List[String]): Unit =
    values.zipWithIndex.foreach { case (value, index) =>
      statement.setString(index + 1, value)
    }

  def defaultLandingTarget(targetResourceType: String, targetResourceId: String): String =
    targetResourceType match
      case "hotel" => s"/hotels/$targetResourceId"
      case "flight" => s"/flights/$targetResourceId"
      case "train" => s"/trains/$targetResourceId"
      case "attraction" => s"/attractions/$targetResourceId"
      case _ => s"/resources/$targetResourceType/$targetResourceId"

  def normalizeAdvertisementKind(value: Option[String]): String =
    value.map(_.trim).filter(_.nonEmpty).getOrElse("Generic")

  def normalizePlayOrder(value: String): String =
    val normalized = value.trim.toLowerCase
    if normalized == "manual" then "Manual" else "Auto"

  def extensionFromFileName(fileName: String): String =
    val index = fileName.lastIndexOf('.')
    if index < 0 || index == fileName.length - 1 then "bin" else fileName.substring(index + 1).toLowerCase

  def uploadImage(connection: Connection, request: UploadAdvertisementImageRequest, createdAt: Instant): UploadAdvertisementImageResponse =
    val normalizedFileName = request.originalFileName.trim
    val fileExtension = extensionFromFileName(normalizedFileName)
    val binaryContent = Base64.getDecoder.decode(request.fileContentBase64)
    val assetId = s"advertising-${UUID.randomUUID().toString.replace("-", "").take(20)}"
    val statement = connection.prepareStatement(insertUploadedBinaryAssetSql)
    try
      statement.setString(1, assetId)
      statement.setString(2, null)
      statement.setString(3, "advertisement-image")
      statement.setString(4, normalizedFileName)
      statement.setString(5, fileExtension)
      statement.setString(6, request.mimeType)
      statement.setLong(7, binaryContent.length.toLong)
      statement.setBytes(8, binaryContent)
      statement.setTimestamp(9, Timestamp.from(createdAt))
      statement.executeUpdate()
    finally statement.close()
    UploadAdvertisementImageResponse(
      assetId = assetId,
      publicUrl = s"/uploads/assets/$assetId/${java.net.URLEncoder.encode(normalizedFileName, java.nio.charset.StandardCharsets.UTF_8)}",
      originalFileName = normalizedFileName,
      mimeType = request.mimeType,
      fileSize = binaryContent.length.toLong
    )
