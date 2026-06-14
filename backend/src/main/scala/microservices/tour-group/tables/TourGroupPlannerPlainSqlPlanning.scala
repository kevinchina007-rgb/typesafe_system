// 这个文件承接 tour-group 后端“计划/成员管理”相关的数据库编排步骤。
// 这里的职责是把多个单表动作串起来，完成创建团体、加入/退出、黑名单、转让等跨步骤流程。
// 它是后端内部实现文件，不是给前端镜像用的 DTO 或 planner 入口。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.syntax.*

import java.sql.Connection
import java.time.Instant

object TourGroupPlannerPlainSqlPlanning:
  def create(connection: Connection, input: CreateTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      val groupId = TourGroupPlannerPlainSqlSupport.nextId("group")
      val membershipId = TourGroupPlannerPlainSqlSupport.nextId("membership")
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_groups(group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity, cover_image_url, tags_json, status, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, groupId)
        statement.setString(2, input.organizerUserId)
        statement.setString(3, input.title.trim)
        statement.setString(4, input.description.trim)
        statement.setString(5, input.destination.trim)
        statement.setDate(6, java.sql.Date.valueOf(java.time.LocalDate.parse(input.startDate)))
        statement.setDate(7, java.sql.Date.valueOf(java.time.LocalDate.parse(input.endDate)))
        statement.setInt(8, input.capacity)
        statement.setString(9, input.coverImageUrl.map(_.trim).filter(_.nonEmpty).orNull)
        statement.setString(10, input.tags.map(_.trim).filter(_.nonEmpty).distinct.asJson.noSpaces)
        statement.setString(11, "Open")
        statement.setTimestamp(12, java.sql.Timestamp.from(now))
        statement.executeUpdate()
      }
      TourGroupPlannerPlainSqlSupport.insertMembership(connection, membershipId, groupId, input.organizerUserId, now)
      TourGroupPlannerPlainSqlSupport.details(connection, groupId)
    }

  def list(connection: Connection): IO[TourGroupListResponse] =
    IO.blocking(TourGroupListResponse(TourGroupPlannerPlainSqlSupport.listSummaries(connection)))

  def get(connection: Connection, input: GetTourGroupDetailsPlannerRequest): IO[TourGroupDetailsResponse] =
    IO.blocking(TourGroupPlannerPlainSqlSupport.details(connection, input.groupId))

  def join(connection: Connection, input: JoinTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      if TourGroupPlannerPlainSqlSupport.isBlacklisted(connection, input.groupId, input.userId) then
        throw TourGroupError.UserWasBlacklistedFromGroup(TourGroupId(input.groupId), UserId(input.userId))
      val existingMembershipId =
        PlainSqlSupport.withStatement(connection, "select membership_id from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
          statement.setString(1, input.groupId)
          statement.setString(2, input.userId)
          val resultSet = statement.executeQuery()
          try if resultSet.next() then Some(resultSet.getString("membership_id")) else None
          finally resultSet.close()
        }
      existingMembershipId.getOrElse(TourGroupPlannerPlainSqlSupport.insertMembership(connection, TourGroupPlannerPlainSqlSupport.nextId("membership"), input.groupId, input.userId, now))
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def leave(connection: Connection, input: LeaveTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      val actingUserId = UserId(input.userId)
      if tourGroup.organizerUserId == actingUserId then
        throw TourGroupError.OrganizerMustTransferBeforeLeaving(tourGroup.groupId, actingUserId)
      val membership = TourGroupPlannerPlainSqlSupport.activeMembership(connection, input.groupId, input.userId)
      TourGroupPlannerPlainSqlSupport.markMembershipAndTravelersRemoved(connection, membership.membershipId)
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def addMembershipTraveler(connection: Connection, input: AddMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      val membershipId = TourGroupPlannerPlainSqlSupport.activeMembershipId(connection, input.groupId, input.userId)
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_group_membership_travelers(membership_traveler_id, membership_id, traveler_id, joined_at, status) values (?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, TourGroupPlannerPlainSqlSupport.nextId("membership-traveler"))
        statement.setString(2, membershipId)
        statement.setString(3, input.travelerId)
        statement.setTimestamp(4, java.sql.Timestamp.from(now))
        statement.setString(5, "Active")
        statement.executeUpdate()
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def removeMembershipTraveler(connection: Connection, input: RemoveMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      val membershipId = TourGroupPlannerPlainSqlSupport.activeMembershipId(connection, input.groupId, input.userId)
      PlainSqlSupport.withStatement(
        connection,
        """
          update tour_group_membership_travelers
          set status = ?
          where membership_id = ? and traveler_id = ? and status = 'Active'
        """
      ) { statement =>
        statement.setString(1, "Removed")
        statement.setString(2, membershipId)
        statement.setString(3, input.travelerId)
        val updatedRows = statement.executeUpdate()
        if updatedRows == 0 then
          throw TourGroupError.MembershipTravelerDidNotBelongToUser(TravelerId(input.travelerId), UserId(input.userId))
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def kickMember(connection: Connection, input: KickTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      TourGroupPlannerPlainSqlSupport.ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val targetUserId = UserId(input.targetUserId)
      if tourGroup.organizerUserId == targetUserId then
        throw TourGroupError.OrganizerCannotBeKicked(tourGroup.groupId, targetUserId)
      val membership = TourGroupPlannerPlainSqlSupport.activeMembership(connection, input.groupId, input.targetUserId)
      TourGroupPlannerPlainSqlSupport.markMembershipAndTravelersRemoved(connection, membership.membershipId)
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def blacklistMember(connection: Connection, input: BlacklistTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      TourGroupPlannerPlainSqlSupport.ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val targetUserId = UserId(input.targetUserId)
      if tourGroup.organizerUserId == targetUserId then
        throw TourGroupError.OrganizerCannotBeBlacklisted(tourGroup.groupId, targetUserId)
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into tour_group_blacklists(blacklist_id, group_id, user_id, blacklisted_by_user_id, reason, created_at)
          values (?, ?, ?, ?, ?, ?)
          on conflict (group_id, user_id) do update set
            blacklisted_by_user_id = excluded.blacklisted_by_user_id,
            reason = excluded.reason,
            created_at = excluded.created_at
        """
      ) { statement =>
        statement.setString(1, TourGroupPlannerPlainSqlSupport.nextId("blacklist"))
        statement.setString(2, input.groupId)
        statement.setString(3, input.targetUserId)
        statement.setString(4, input.organizerUserId)
        statement.setString(5, "blacklisted by organizer")
        statement.setTimestamp(6, java.sql.Timestamp.from(now))
        statement.executeUpdate()
      }
      TourGroupPlannerPlainSqlSupport.markMembershipIfExistsRemoved(connection, input.groupId, input.targetUserId)
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def transferOrganizer(connection: Connection, input: TransferTourGroupLeaderPlannerRequest, now: Instant): IO[TourGroupDetailsResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      TourGroupPlannerPlainSqlSupport.ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val targetUserId = UserId(input.targetUserId)
      if tourGroup.organizerUserId == targetUserId then
        throw TourGroupError.OrganizerTransferTargetWasInvalid(tourGroup.groupId, targetUserId)
      if TourGroupPlannerPlainSqlSupport.isBlacklisted(connection, input.groupId, input.targetUserId) then
        throw TourGroupError.UserWasBlacklistedFromGroup(tourGroup.groupId, targetUserId)
      TourGroupPlannerPlainSqlSupport.activeMembership(connection, input.groupId, input.targetUserId)
      PlainSqlSupport.withStatement(connection, "update tour_groups set organizer_user_id = ? where group_id = ?") { statement =>
        statement.setString(1, input.targetUserId)
        statement.setString(2, input.groupId)
        statement.executeUpdate()
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }
