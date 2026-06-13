// AdvertisementPlainSql 封装广告模块的对外入口，只保留公共 Planner 调用。
package com.typesafe.travel.advertising.domain

import cats.effect.IO

import java.sql.Connection
import java.time.Instant
import java.util.Base64
import java.util.UUID

object AdvertisementPlainSql:
  def list(connection: Connection, request: ListAdvertisementsRequest): IO[ListAdvertisementsResponse] =
    IO.blocking {
      val (sql, parameters) = AdvertisementPlainSqlSupport.listSql(request)
      val statement = connection.prepareStatement(sql)
      try
        AdvertisementPlainSqlSupport.bindStrings(statement, parameters)
        val resultSet = statement.executeQuery()
        try
          val rows = List.newBuilder[AdvertisementResponse]
          while resultSet.next() do
            val advertisement = AdvertisementPlainSqlSupport.readAdvertisementResponse(
              resultSet,
              AdvertisementPlainSqlSupport.listReviews(connection, resultSet.getString("advertisement_id"))
            )
            if AdvertisementPlainSqlSupport.shouldIncludeAdvertisement(advertisement, request) then rows += advertisement
          ListAdvertisementsResponse(rows.result())
        finally resultSet.close()
      finally statement.close()
    }

  def insert(connection: Connection, request: CreateAdvertisementRequest, createdAt: Instant): IO[AdvertisementResponse] =
    IO.blocking {
      val advertisementId = s"advertisement-${UUID.randomUUID().toString.take(12)}"
      val statement = connection.prepareStatement(AdvertisementPlainSqlSupport.insertAdvertisementSql)
      try
        AdvertisementPlainSqlSupport.bindInsert(statement, advertisementId, request, createdAt)
        statement.executeUpdate()
        AdvertisementPlainSqlSupport.readById(connection, advertisementId)
      finally statement.close()
    }

  def update(connection: Connection, request: UpdateAdvertisementRequest, updatedAt: Instant): IO[AdvertisementResponse] =
    IO.blocking {
      val statement = connection.prepareStatement(AdvertisementPlainSqlSupport.updateAdvertisementSql)
      try
        AdvertisementPlainSqlSupport.bindUpdate(statement, request, updatedAt)
        val updatedRows = statement.executeUpdate()
        if updatedRows == 0 then throw new IllegalStateException(s"Advertisement '${request.advertisementId}' was not found for owner '${request.ownerManagerId}'")
        AdvertisementPlainSqlSupport.readById(connection, request.advertisementId)
      finally statement.close()
    }

  def submitForReview(connection: Connection, request: AdvertisementOwnerActionRequest, updatedAt: Instant): IO[AdvertisementResponse] =
    AdvertisementPlainSqlSupport.updateOwnerStatus(
      connection = connection,
      request = request,
      reviewStatus = AdvertisementReviewStatus.PendingReview,
      deliveryStatus = AdvertisementDeliveryStatus.Scheduled,
      updatedAt = updatedAt
    )

  def pause(connection: Connection, request: AdvertisementOwnerActionRequest, updatedAt: Instant): IO[AdvertisementResponse] =
    AdvertisementPlainSqlSupport.updateOwnerStatus(
      connection = connection,
      request = request,
      reviewStatus = AdvertisementReviewStatus.Approved,
      deliveryStatus = AdvertisementDeliveryStatus.Paused,
      updatedAt = updatedAt
    )

  def approve(connection: Connection, request: AdvertisementReviewDecisionRequest, updatedAt: Instant): IO[AdvertisementResponse] =
    AdvertisementPlainSqlSupport.review(
      connection = connection,
      request = request,
      decision = AdvertisementReviewDecision.Approved,
      reviewStatus = AdvertisementReviewStatus.Approved,
      deliveryStatus = AdvertisementDeliveryStatus.Active,
      updatedAt = updatedAt
    )

  def reject(connection: Connection, request: AdvertisementReviewDecisionRequest, updatedAt: Instant): IO[AdvertisementResponse] =
    AdvertisementPlainSqlSupport.review(
      connection = connection,
      request = request,
      decision = AdvertisementReviewDecision.Rejected,
      reviewStatus = AdvertisementReviewStatus.Rejected,
      deliveryStatus = AdvertisementDeliveryStatus.Paused,
      updatedAt = updatedAt
    )

  def assignSlot(connection: Connection, request: AdvertisementSlotAssignmentRequest, updatedAt: Instant): IO[AdvertisementResponse] =
    IO.blocking {
      if request.slotIndex < 1 || request.slotIndex > 4 then
        throw AdvertisementError.AdvertisementSlotIndexWasInvalid(request.slotIndex)

      val current = AdvertisementPlainSqlSupport.readById(connection, request.advertisementId)
      if current.reviewStatus != AdvertisementReviewStatus.Approved.toString then
        throw AdvertisementError.AdvertisementWasNotApproved(AdvertisementId(request.advertisementId))

      val clearStatement = connection.prepareStatement(AdvertisementPlainSqlSupport.clearConflictingSlotSql)
      try
        clearStatement.setTimestamp(1, java.sql.Timestamp.from(updatedAt))
        clearStatement.setString(2, current.placement)
        clearStatement.setInt(3, request.slotIndex)
        clearStatement.setString(4, request.advertisementId)
        clearStatement.executeUpdate()
      finally clearStatement.close()

      val statement = connection.prepareStatement(
        "update advertisements set slot_index = ?, delivery_status = ?, updated_at = ? where advertisement_id = ?"
      )
      try
        statement.setInt(1, request.slotIndex)
        statement.setString(2, AdvertisementDeliveryStatus.Active.toString)
        statement.setTimestamp(3, java.sql.Timestamp.from(updatedAt))
        statement.setString(4, request.advertisementId)
        val updatedRows = statement.executeUpdate()
        if updatedRows == 0 then throw AdvertisementError.AdvertisementWasNotFound(AdvertisementId(request.advertisementId))
      finally statement.close()

      AdvertisementPlainSqlSupport.insertReview(
        connection = connection,
        advertisementId = request.advertisementId,
        reviewerManagerId = request.reviewerManagerId,
        decision = AdvertisementReviewDecision.Approved,
        reviewNote = Some(s"Assigned to slot ${request.slotIndex}"),
        reviewedAt = updatedAt
      )
      AdvertisementPlainSqlSupport.readById(connection, request.advertisementId)
    }

  def pauseDisplayBySiteAdmin(connection: Connection, request: AdvertisementReviewDecisionRequest, updatedAt: Instant): IO[AdvertisementResponse] =
    IO.blocking {
      val current = AdvertisementPlainSqlSupport.readById(connection, request.advertisementId)
      val statement = connection.prepareStatement(
        "update advertisements set delivery_status = ?, slot_index = null, updated_at = ? where advertisement_id = ?"
      )
      try
        statement.setString(1, AdvertisementDeliveryStatus.Paused.toString)
        statement.setTimestamp(2, java.sql.Timestamp.from(updatedAt))
        statement.setString(3, request.advertisementId)
        val updatedRows = statement.executeUpdate()
        if updatedRows == 0 then throw AdvertisementError.AdvertisementWasNotFound(AdvertisementId(request.advertisementId))
      finally statement.close()

      AdvertisementPlainSqlSupport.insertReview(
        connection = connection,
        advertisementId = request.advertisementId,
        reviewerManagerId = request.reviewerManagerId,
        decision = AdvertisementReviewDecision.Approved,
        reviewNote = request.reviewNote.map(_.trim).filter(_.nonEmpty).orElse(Some("Paused display")),
        reviewedAt = updatedAt
      )
      AdvertisementPlainSqlSupport.normalizeActiveSlots(connection, current.placement, updatedAt)
      AdvertisementPlainSqlSupport.readById(connection, request.advertisementId)
    }

  def findById(connection: Connection, advertisementId: String): IO[AdvertisementResponse] =
    IO.blocking(AdvertisementPlainSqlSupport.readById(connection, advertisementId))

  def getDeliverySettings(connection: Connection, request: GetAdvertisementDeliverySettingsRequest, now: Instant): IO[AdvertisementDeliverySettingsResponse] =
    IO.blocking {
      val placement = AdvertisementPlacement.fromText(request.placement).toString
      val statement = connection.prepareStatement(AdvertisementPlainSqlSupport.selectDeliverySettingsSql)
      try
        statement.setString(1, placement)
        val resultSet = statement.executeQuery()
        try
          if resultSet.next() then AdvertisementPlainSqlSupport.readDeliverySettings(resultSet)
          else AdvertisementDeliverySettingsResponse(
            placement = placement,
            rotationIntervalSeconds = 5,
            playOrder = "Manual",
            startAt = None,
            endAt = None,
            updatedByManagerId = None,
            updatedAt = now.toString
          )
        finally resultSet.close()
      finally statement.close()
    }

  def saveDeliverySettings(connection: Connection, request: SaveAdvertisementDeliverySettingsRequest, updatedAt: Instant): IO[AdvertisementDeliverySettingsResponse] =
    IO.blocking {
      val placement = AdvertisementPlacement.fromText(request.placement).toString
      val statement = connection.prepareStatement(AdvertisementPlainSqlSupport.upsertDeliverySettingsSql)
      try
        statement.setString(1, placement)
        statement.setInt(2, request.rotationIntervalSeconds.max(1))
        statement.setString(3, AdvertisementPlainSqlSupport.normalizePlayOrder(request.playOrder))
        statement.setTimestamp(4, request.startAt.map(Instant.parse).map(java.sql.Timestamp.from).orNull)
        statement.setTimestamp(5, request.endAt.map(Instant.parse).map(java.sql.Timestamp.from).orNull)
        statement.setString(6, request.updatedByManagerId)
        statement.setTimestamp(7, java.sql.Timestamp.from(updatedAt))
        statement.executeUpdate()
      finally statement.close()
      AdvertisementPlainSqlSupport.readDeliverySettingsByPlacement(connection, placement).getOrElse(
        throw new IllegalStateException(s"Delivery settings '$placement' were not saved")
      )
    }

  def uploadImage(connection: Connection, request: UploadAdvertisementImageRequest, createdAt: Instant): IO[UploadAdvertisementImageResponse] =
    IO.blocking {
      AdvertisementPlainSqlSupport.uploadImage(connection, request, createdAt)
    }
