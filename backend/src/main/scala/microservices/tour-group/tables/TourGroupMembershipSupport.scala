// 这个文件只服务 tour-group 后端的团体详情聚合查询。
// 它把 membership、traveler、plan、selection、blacklist 等多张表拼装成详情视图，方便 planner 一次性返回完整页面数据。
// 前端不应直接镜像这里的 SQL 组装逻辑，只需要消费同名 response DTO。
package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.decode

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

object TourGroupMembershipSupport:
  def listSummaries(connection: Connection): List[TourGroupSummaryResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select g.group_id, g.organizer_user_id, g.title, g.description, g.destination, g.start_date, g.end_date, g.capacity, g.cover_image_url, g.tags_json, g.status, g.created_at,
          (select count(*) from tour_group_memberships m where m.group_id = g.group_id and m.status = 'Active') as member_count,
          (select count(*) from tour_group_membership_travelers mt inner join tour_group_memberships m on m.membership_id = mt.membership_id where m.group_id = g.group_id and mt.status = 'Active') as active_traveler_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'Submitted') as pending_selection_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'OrganizerConfirmed') as confirmed_selection_count,
          (select count(*) from group_selection_order_links l inner join group_plan_selections s on s.selection_id = l.selection_id where s.group_id = g.group_id) as converted_order_count
        from tour_groups g
        order by g.created_at desc, g.group_id
      """
    ) { statement =>
      PlainSqlSupport.queryList(statement)(readSummary)
    }

  def details(connection: Connection, groupId: String): TourGroupDetailsResponse =
    val group = groupSummary(connection, groupId)
    TourGroupDetailsResponse(
      group = group,
      memberships = memberships(connection, groupId),
      membershipTravelers = membershipTravelers(connection, groupId),
      planItems = TourGroupSelectionSupport.planItems(connection, groupId),
      planOptions = TourGroupSelectionSupport.planOptions(connection, groupId),
      selections = TourGroupSelectionSupport.selections(connection, groupId),
      selectionOrderLinks = TourGroupSelectionSupport.selectionOrderLinks(connection, groupId),
      blacklists = blacklists(connection, groupId)
    )

  def groupSummary(connection: Connection, groupId: String): TourGroupSummaryResponse =
    PlainSqlSupport.withStatement(
      connection,
      """
          select g.group_id, g.organizer_user_id, g.title, g.description, g.destination, g.start_date, g.end_date, g.capacity, g.cover_image_url, g.tags_json, g.status, g.created_at,
          (select count(*) from tour_group_memberships m where m.group_id = g.group_id and m.status = 'Active') as member_count,
          (select count(*) from tour_group_membership_travelers mt inner join tour_group_memberships m on m.membership_id = mt.membership_id where m.group_id = g.group_id and mt.status = 'Active') as active_traveler_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'Submitted') as pending_selection_count,
          (select count(*) from group_plan_selections s where s.group_id = g.group_id and s.status = 'OrganizerConfirmed') as confirmed_selection_count,
          (select count(*) from group_selection_order_links l inner join group_plan_selections s on s.selection_id = l.selection_id where s.group_id = g.group_id) as converted_order_count
        from tour_groups g
        where g.group_id = ?
      """
    ) { statement =>
      statement.setString(1, groupId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readSummary(resultSet) else throw new IllegalArgumentException(s"Tour group '$groupId' was not found")
      finally resultSet.close()
    }

  def loadGroup(connection: Connection, groupId: String): TourGroup =
    PlainSqlSupport.withStatement(
      connection,
      """
        select group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity, cover_image_url, tags_json, status, created_at
        from tour_groups
        where group_id = ?
      """
    ) { statement =>
      statement.setString(1, groupId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readGroup(resultSet) else throw TourGroupError.GroupWasNotFound(TourGroupId(groupId))
      finally resultSet.close()
    }

  def activeMembership(connection: Connection, groupId: String, userId: String): TourGroupMembership =
    PlainSqlSupport.withStatement(
      connection,
      """
        select membership_id, group_id, user_id, joined_at, status
        from tour_group_memberships
        where group_id = ? and user_id = ? and status = 'Active'
        fetch first 1 row only
      """
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readMembership(resultSet) else throw TourGroupError.GroupMemberWasNotFound(TourGroupId(groupId), UserId(userId))
      finally resultSet.close()
    }

  def readGroup(row: ResultSet): TourGroup =
    val tags =
      Option(row.getString("tags_json"))
        .map(value => decode[List[String]](value).fold(_ => Nil, identity))
        .getOrElse(Nil)
    TourGroup(
      groupId = TourGroupId(row.getString("group_id")),
      organizerUserId = UserId(row.getString("organizer_user_id")),
      title = row.getString("title"),
      description = row.getString("description"),
      destination = row.getString("destination"),
      startDate = row.getDate("start_date").toLocalDate,
      endDate = row.getDate("end_date").toLocalDate,
      capacity = row.getInt("capacity"),
      coverImageUrl = Option(row.getString("cover_image_url")).filter(_.nonEmpty),
      tags = tags.toVector,
      status = TourGroupStatus.fromText(row.getString("status")),
      createdAt = row.getTimestamp("created_at").toInstant
    )

  def readMembership(row: ResultSet): TourGroupMembership =
    TourGroupMembership(
      membershipId = TourGroupMembershipId(row.getString("membership_id")),
      groupId = TourGroupId(row.getString("group_id")),
      userId = UserId(row.getString("user_id")),
      joinedAt = row.getTimestamp("joined_at").toInstant,
      status = TourGroupMembershipStatus.fromText(row.getString("status"))
    )

  def readSummary(row: ResultSet): TourGroupSummaryResponse =
    val activeTravelerCount = row.getInt("active_traveler_count")
    TourGroupSummaryResponse(
      groupId = row.getString("group_id"),
      organizerUserId = row.getString("organizer_user_id"),
      title = row.getString("title"),
      description = row.getString("description"),
      destination = row.getString("destination"),
      startDate = row.getDate("start_date").toLocalDate.toString,
      endDate = row.getDate("end_date").toLocalDate.toString,
      capacity = row.getInt("capacity"),
      usedCapacity = activeTravelerCount,
      isFull = activeTravelerCount >= row.getInt("capacity"),
      coverImageUrl = Option(row.getString("cover_image_url")).filter(_.nonEmpty),
      tags = Option(row.getString("tags_json")).map(value => decode[List[String]](value).fold(_ => Nil, identity)).getOrElse(Nil),
      memberCount = row.getInt("member_count"),
      activeTravelerCount = activeTravelerCount,
      pendingSelectionCount = row.getInt("pending_selection_count"),
      confirmedSelectionCount = row.getInt("confirmed_selection_count"),
      convertedOrderCount = row.getInt("converted_order_count"),
      status = row.getString("status"),
      createdAt = row.getTimestamp("created_at").toInstant.toString
    )

  def memberships(connection: Connection, groupId: String): List[TourGroupMembershipResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select membership_id, group_id, user_id, joined_at, status
        from tour_group_memberships
        where group_id = ?
        order by joined_at, membership_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupMembershipResponse(
          membershipId = row.getString("membership_id"),
          userId = row.getString("user_id"),
          userDisplayName = row.getString("user_id"),
          status = row.getString("status"),
          joinedAt = row.getTimestamp("joined_at").toInstant.toString
        )
      }
    }

  def membershipTravelers(connection: Connection, groupId: String): List[TourGroupMembershipTravelerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select mt.membership_traveler_id, mt.membership_id, mt.traveler_id, mt.joined_at, mt.status
        from tour_group_membership_travelers mt
        inner join tour_group_memberships m on m.membership_id = mt.membership_id
        where m.group_id = ?
        order by mt.joined_at, mt.membership_traveler_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupMembershipTravelerResponse(
          membershipTravelerId = row.getString("membership_traveler_id"),
          membershipId = row.getString("membership_id"),
          travelerId = row.getString("traveler_id"),
          joinedAt = row.getTimestamp("joined_at").toInstant.toString,
          status = row.getString("status")
        )
      }
    }

  def blacklists(connection: Connection, groupId: String): List[TourGroupBlacklistResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select blacklist_id, group_id, user_id, blacklisted_by_user_id, reason, created_at
        from tour_group_blacklists
        where group_id = ?
        order by created_at desc, blacklist_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupBlacklistResponse(
          blacklistId = row.getString("blacklist_id"),
          groupId = row.getString("group_id"),
          userId = row.getString("user_id"),
          blacklistedByUserId = row.getString("blacklisted_by_user_id"),
          reason = row.getString("reason"),
          createdAt = row.getTimestamp("created_at").toInstant.toString
        )
      }
    }

  def insertMembership(connection: Connection, membershipId: String, groupId: String, userId: String, joinedAt: Instant): String =
    PlainSqlSupport.withStatement(connection, "insert into tour_group_memberships(membership_id, group_id, user_id, joined_at, status) values (?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, membershipId)
      statement.setString(2, groupId)
      statement.setString(3, userId)
      statement.setTimestamp(4, Timestamp.from(joinedAt))
      statement.setString(5, "Active")
      statement.executeUpdate()
      membershipId
    }

  def activeMembershipId(connection: Connection, groupId: String, userId: String): String =
    activeMembership(connection, groupId, userId).membershipId.value

  def markMembershipAndTravelersRemoved(connection: Connection, membershipId: TourGroupMembershipId): Unit =
    PlainSqlSupport.withStatement(connection, "update tour_group_memberships set status = 'Removed' where membership_id = ?") { statement =>
      statement.setString(1, membershipId.value)
      statement.executeUpdate()
    }
    PlainSqlSupport.withStatement(connection, "update tour_group_membership_travelers set status = 'Removed' where membership_id = ? and status = 'Active'") { statement =>
      statement.setString(1, membershipId.value)
      statement.executeUpdate()
    }

  def markMembershipIfExistsRemoved(connection: Connection, groupId: String, userId: String): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        select membership_id
        from tour_group_memberships
        where group_id = ? and user_id = ? and status = 'Active'
        fetch first 1 row only
      """
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          markMembershipAndTravelersRemoved(connection, TourGroupMembershipId(resultSet.getString("membership_id")))
      finally resultSet.close()
    }

  def isBlacklisted(connection: Connection, groupId: String, userId: String): Boolean =
    PlainSqlSupport.withStatement(
      connection,
      """
        select 1
        from tour_group_blacklists
        where group_id = ? and user_id = ?
        fetch first 1 row only
      """
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try resultSet.next()
      finally resultSet.close()
    }

  def nextId(prefix: String): String =
    s"$prefix-${UUID.randomUUID().toString.replace("-", "").take(12)}"

  def ensureTourGroupOrganizer(tourGroup: TourGroup, userId: UserId): Either[Throwable, Unit] =
    if tourGroup.organizerUserId == userId then Right(()) else Left(TourGroupError.OrganizerScopeDidNotMatch(tourGroup.groupId, userId))
