package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.api.routes.ConnectionApiPlan

import java.sql.{Connection, Timestamp}
import java.time.Instant
import java.util.{Base64, UUID}

object CreateTourGroupPlanner extends ConnectionApiPlan[CreateTourGroupPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "CreateTourGroupPlanner"
  override def plan(input: CreateTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.create(connection, input, java.time.Instant.now())

object UploadTourGroupCoverImagePlanner extends ConnectionApiPlan[UploadTourGroupCoverImagePlannerRequest, UploadTourGroupCoverImagePlannerResponse]:
  override val name: String = "UploadTourGroupCoverImagePlanner"
  override def plan(input: UploadTourGroupCoverImagePlannerRequest, connection: Connection): IO[UploadTourGroupCoverImagePlannerResponse] =
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

      UploadTourGroupCoverImagePlannerResponse(
        assetId = assetId,
        publicUrl = s"/uploads/assets/$assetId/${java.net.URLEncoder.encode(normalizedFileName, java.nio.charset.StandardCharsets.UTF_8)}",
        originalFileName = normalizedFileName,
        mimeType = normalizedMimeType,
        fileSize = fileContent.length.toLong
      )
    }

object ListTourGroupsPlanner extends ConnectionApiPlan[ListTourGroupsPlannerRequest, TourGroupListPlannerResponse]:
  override val name: String = "ListTourGroupsPlanner"
  override def plan(input: ListTourGroupsPlannerRequest, connection: Connection): IO[TourGroupListPlannerResponse] =
    TourGroupPlannerPlainSql.list(connection)

object GetTourGroupDetailsPlanner extends ConnectionApiPlan[TourGroupByIdPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "GetTourGroupDetailsPlanner"
  override def plan(input: TourGroupByIdPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.get(connection, input)

object JoinTourGroupPlanner extends ConnectionApiPlan[JoinTourGroupPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "JoinTourGroupPlanner"
  override def plan(input: JoinTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.join(connection, input, java.time.Instant.now())

object LeaveTourGroupPlanner extends ConnectionApiPlan[LeaveTourGroupPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "LeaveTourGroupPlanner"
  override def plan(input: LeaveTourGroupPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.leave(connection, input, java.time.Instant.now())

object AddMembershipTravelerPlanner extends ConnectionApiPlan[AddMembershipTravelerPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "AddMembershipTravelerPlanner"
  override def plan(input: AddMembershipTravelerPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.addMembershipTraveler(connection, input, java.time.Instant.now())

object RemoveMembershipTravelerPlanner extends ConnectionApiPlan[RemoveMembershipTravelerPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "RemoveMembershipTravelerPlanner"
  override def plan(input: RemoveMembershipTravelerPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.removeMembershipTraveler(connection, input, java.time.Instant.now())

object KickTourGroupMemberPlanner extends ConnectionApiPlan[KickTourGroupMemberPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "KickTourGroupMemberPlanner"
  override def plan(input: KickTourGroupMemberPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.kickMember(connection, input, java.time.Instant.now())

object BlacklistTourGroupMemberPlanner extends ConnectionApiPlan[BlacklistTourGroupMemberPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "BlacklistTourGroupMemberPlanner"
  override def plan(input: BlacklistTourGroupMemberPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.blacklistMember(connection, input, java.time.Instant.now())

object TransferTourGroupLeaderPlanner extends ConnectionApiPlan[TransferTourGroupLeaderPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "TransferTourGroupLeaderPlanner"
  override def plan(input: TransferTourGroupLeaderPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.transferOrganizer(connection, input, java.time.Instant.now())

object CreateTourGroupPlanItemPlanner extends ConnectionApiPlan[CreateTourGroupPlanItemPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "CreateTourGroupPlanItemPlanner"
  override def plan(input: CreateTourGroupPlanItemPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.createPlanItem(connection, input, java.time.Instant.now())

object CreateTourGroupPlanOptionPlanner extends ConnectionApiPlan[CreateTourGroupPlanOptionPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "CreateTourGroupPlanOptionPlanner"
  override def plan(input: CreateTourGroupPlanOptionPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.createPlanOption(connection, input, java.time.Instant.now())

object CreateTourGroupSelectionPlanner extends ConnectionApiPlan[CreateTourGroupSelectionPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "CreateTourGroupSelectionPlanner"
  override def plan(input: CreateTourGroupSelectionPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.createSelection(connection, input, java.time.Instant.now())

object SubmitTourGroupSelectionPlanner extends ConnectionApiPlan[SubmitTourGroupSelectionPlannerRequest, TourGroupDetailsPlannerResponse]:
  override val name: String = "SubmitTourGroupSelectionPlanner"
  override def plan(input: SubmitTourGroupSelectionPlannerRequest, connection: Connection): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSql.submitSelection(connection, input, java.time.Instant.now())
