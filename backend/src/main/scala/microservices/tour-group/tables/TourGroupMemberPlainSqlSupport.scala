package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.decode

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.Instant
import java.util.UUID

final case class ParticipantState(
    participantId: String,
    userId: String,
    role: String,
    joinedAt: Instant,
    status: String,
    lastReadAt: Option[Instant],
    lastReadMessageId: Option[String],
    mutedAt: Option[Instant],
    archivedAt: Option[Instant]
)

final case class ConversationAccess(
    conversation: TourGroupConversation,
    participant: ParticipantState
)

final case class ChatUserProfile(userId: String, displayName: String, avatarUrl: Option[String])

object TourGroupMemberPlainSqlSupport:

  def readParticipant(row: ResultSet): ParticipantState =
    ParticipantState(
      participantId = row.getString("participant_id"),
      userId = row.getString("user_id"),
      role = row.getString("role"),
      joinedAt = row.getTimestamp("joined_at").toInstant,
      status = row.getString("status"),
      lastReadAt = Option(row.getTimestamp("last_read_at")).map(_.toInstant),
      lastReadMessageId = Option(row.getString("last_read_message_id")).filter(_.nonEmpty),
      mutedAt = Option(row.getTimestamp("muted_at")).map(_.toInstant),
      archivedAt = Option(row.getTimestamp("archived_at")).map(_.toInstant)
    )

  def loadUserProfile(connection: Connection, userId: String): ChatUserProfile =
    PlainSqlSupport.withStatement(connection, "select user_id, nickname, avatar_url from users where user_id = ?") { statement =>
      statement.setString(1, userId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          ChatUserProfile(
            userId = resultSet.getString("user_id"),
            displayName = resultSet.getString("nickname"),
            avatarUrl = Option(resultSet.getString("avatar_url")).filter(_.nonEmpty)
          )
        else ChatUserProfile(userId, userId, None)
      finally resultSet.close()
    }

  def listActiveMemberDisplayNames(connection: Connection, groupId: String): List[String] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select u.nickname
        from tour_group_memberships m
        join users u on u.user_id = m.user_id
        where m.group_id = ? and m.status = 'Active'
        order by m.joined_at, m.membership_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(_.getString("nickname"))
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

  def details(connection: Connection, groupId: String): TourGroupDetailsPlannerResponse =
    val group = groupSummary(connection, groupId)
    TourGroupDetailsPlannerResponse(
      group = group,
      memberships = memberships(connection, groupId),
      membershipTravelers = membershipTravelers(connection, groupId),
      planItems = planItems(connection, groupId),
      planOptions = planOptions(connection, groupId),
      selections = selections(connection, groupId),
      selectionOrderLinks = selectionOrderLinks(connection, groupId),
      blacklists = blacklists(connection, groupId)
    )

  def groupSummary(connection: Connection, groupId: String): TourGroupSummaryPlannerResponse =
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

  def planItems(connection: Connection, groupId: String): List[GroupPlanItem] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select plan_item_id, group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status
        from group_plan_items
        where group_id = ?
        order by sequence_no, plan_item_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(readPlanItem)
    }

  def planOptions(connection: Connection, groupId: String): List[GroupPlanOption] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select o.option_id, o.plan_item_id, o.resource_type, o.resource_id, o.resource_variant_code, o.resource_context, o.label, o.description, o.default_quantity, o.status
        from group_plan_options o
        inner join group_plan_items i on i.plan_item_id = o.plan_item_id
        where i.group_id = ?
        order by i.sequence_no, o.option_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(readPlanOption)
    }

  def selections(connection: Connection, groupId: String): List[GroupPlanSelection] =
    val travelerIdsBySelectionId = selectionTravelerIds(connection, groupId)
    PlainSqlSupport.withStatement(
      connection,
      """
        select selection_id, group_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
        from group_plan_selections
        where group_id = ?
        order by created_at, selection_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        readSelection(row, travelerIdsBySelectionId.getOrElse(row.getString("selection_id"), Vector.empty))
      }
    }

  def selectionOrderLinks(connection: Connection, groupId: String): List[GroupSelectionOrderLink] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select l.link_id, l.selection_id, l.order_id, l.created_at
        from group_selection_order_links l
        inner join group_plan_selections s on s.selection_id = l.selection_id
        where s.group_id = ?
        order by l.created_at, l.link_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement)(readSelectionOrderLink)
    }

  def existingSelectionOrderId(connection: Connection, selectionId: String): IO[Option[String]] =
    IO.blocking {
      PlainSqlSupport.withStatement(
        connection,
        """
          select order_id
          from group_selection_order_links
          where selection_id = ?
          fetch first 1 row only
        """
      ) { statement =>
        statement.setString(1, selectionId)
        val resultSet = statement.executeQuery()
        try if resultSet.next() then Some(resultSet.getString("order_id")) else None
        finally resultSet.close()
      }
    }

  def insertSelectionOrderLink(connection: Connection, selectionId: String, orderId: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        insert into group_selection_order_links(link_id, selection_id, order_id, created_at)
        values (?, ?, ?, ?)
      """
    ) { statement =>
      statement.setString(1, nextId("selection-order-link"))
      statement.setString(2, selectionId)
      statement.setString(3, orderId)
      statement.setTimestamp(4, Timestamp.from(now))
      statement.executeUpdate()
    }

  def updateSelectionAsConvertedToOrder(connection: Connection, selectionId: String, now: Instant): Unit =
    PlainSqlSupport.withStatement(
      connection,
      """
        update group_plan_selections
        set status = ?, confirmed_at = ?, reviewed_by_organizer_user_id = ?, review_note = ?
        where selection_id = ?
      """
    ) { statement =>
      statement.setString(1, GroupPlanSelectionStatus.ConvertedToOrder.toString)
      statement.setTimestamp(2, Timestamp.from(now))
      statement.setString(3, null)
      statement.setString(4, null)
      statement.setString(5, selectionId)
      statement.executeUpdate()
    }

  def selectionTravelerIds(connection: Connection, groupId: String): Map[String, Vector[TravelerId]] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select t.selection_id, t.traveler_id
        from group_plan_selection_travelers t
        inner join group_plan_selections s on s.selection_id = t.selection_id
        where s.group_id = ?
        order by t.selection_traveler_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        row.getString("selection_id") -> TravelerId(row.getString("traveler_id"))
      }.groupMap(_._1)(_._2).map { case (selectionId, travelerIds) => selectionId -> travelerIds.toVector }
    }

  def selectionById(connection: Connection, selectionId: String): GroupPlanSelection =
    PlainSqlSupport.withStatement(
      connection,
      """
        select selection_id, group_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
        from group_plan_selections
        where selection_id = ?
      """
    ) { statement =>
      statement.setString(1, selectionId)
      val resultSet = statement.executeQuery()
      try
        if resultSet.next() then
          val groupId = resultSet.getString("group_id")
          val travelerIdsBySelectionId = selectionTravelerIds(connection, groupId)
          readSelection(resultSet, travelerIdsBySelectionId.getOrElse(selectionId, Vector.empty))
        else
          throw TourGroupError.SelectionWasNotFound(GroupPlanSelectionId(selectionId))
      finally resultSet.close()
    }

  def planItemById(connection: Connection, planItemId: String): GroupPlanItem =
    PlainSqlSupport.withStatement(
      connection,
      """
        select plan_item_id, group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status
        from group_plan_items
        where plan_item_id = ?
      """
    ) { statement =>
      statement.setString(1, planItemId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then readPlanItem(resultSet) else throw TourGroupError.PlanItemWasNotFound(GroupPlanItemId(planItemId))
      finally resultSet.close()
    }

  def readPlanItem(row: ResultSet): GroupPlanItem =
    GroupPlanItem(
      planItemId = GroupPlanItemId(row.getString("plan_item_id")),
      groupId = TourGroupId(row.getString("group_id")),
      itemType = GroupPlanItemType.fromText(row.getString("item_type")),
      title = row.getString("title"),
      description = row.getString("description"),
      scheduledAt = row.getTimestamp("scheduled_at").toInstant,
      endsAt = Option(row.getTimestamp("ends_at")).map(_.toInstant),
      sequenceNo = row.getInt("sequence_no"),
      status = GroupPlanItemStatus.fromText(row.getString("status"))
    )

  def readPlanOption(row: ResultSet): GroupPlanOption =
    GroupPlanOption(
      optionId = GroupPlanOptionId(row.getString("option_id")),
      planItemId = GroupPlanItemId(row.getString("plan_item_id")),
      resourceType = GroupPlanOptionResourceType.fromText(row.getString("resource_type")),
      resourceId = row.getString("resource_id"),
      resourceVariantCode = Option(row.getString("resource_variant_code")).filter(_.nonEmpty),
      resourceContext = Option(row.getString("resource_context")).filter(_.nonEmpty),
      label = row.getString("label"),
      description = row.getString("description"),
      defaultQuantity = row.getInt("default_quantity"),
      status = GroupPlanOptionStatus.fromText(row.getString("status"))
    )

  def readSelection(row: ResultSet, travelerIds: Vector[TravelerId]): GroupPlanSelection =
    GroupPlanSelection(
      selectionId = GroupPlanSelectionId(row.getString("selection_id")),
      groupId = TourGroupId(row.getString("group_id")),
      planItemId = GroupPlanItemId(row.getString("plan_item_id")),
      optionId = GroupPlanOptionId(row.getString("option_id")),
      membershipId = TourGroupMembershipId(row.getString("membership_id")),
      quantity = row.getInt("quantity"),
      status = GroupPlanSelectionStatus.fromText(row.getString("status")),
      createdAt = row.getTimestamp("created_at").toInstant,
      confirmedAt = Option(row.getTimestamp("confirmed_at")).map(_.toInstant),
      reviewedByOrganizerUserId = Option(row.getString("reviewed_by_organizer_user_id")).filter(_.nonEmpty).map(UserId.apply),
      reviewNote = Option(row.getString("review_note")).filter(_.nonEmpty),
      travelerIds = travelerIds
    )

  def readSelectionOrderLink(row: ResultSet): GroupSelectionOrderLink =
    GroupSelectionOrderLink(
      linkId = GroupSelectionOrderLinkId(row.getString("link_id")),
      selectionId = GroupPlanSelectionId(row.getString("selection_id")),
      orderId = OrderId(row.getString("order_id")),
      createdAt = row.getTimestamp("created_at").toInstant
    )

  def readSummary(row: ResultSet): TourGroupSummaryPlannerResponse =
    val capacity = row.getInt("capacity")
    val activeTravelerCount = row.getInt("active_traveler_count")
    val tags =
      Option(row.getString("tags_json"))
        .map(value => decode[List[String]](value).fold(_ => Nil, identity))
        .getOrElse(Nil)
    TourGroupSummaryPlannerResponse(
      groupId = row.getString("group_id"),
      organizerUserId = row.getString("organizer_user_id"),
      title = row.getString("title"),
      description = row.getString("description"),
      destination = row.getString("destination"),
      startDate = row.getDate("start_date").toLocalDate.toString,
      endDate = row.getDate("end_date").toLocalDate.toString,
      capacity = capacity,
      usedCapacity = activeTravelerCount,
      isFull = activeTravelerCount >= capacity,
      coverImageUrl = Option(row.getString("cover_image_url")).filter(_.nonEmpty),
      tags = tags,
      memberCount = row.getInt("member_count"),
      activeTravelerCount = activeTravelerCount,
      pendingSelectionCount = row.getInt("pending_selection_count"),
      confirmedSelectionCount = row.getInt("confirmed_selection_count"),
      convertedOrderCount = row.getInt("converted_order_count"),
      status = row.getString("status"),
      createdAt = row.getTimestamp("created_at").toInstant.toString
    )

  def memberships(connection: Connection, groupId: String): List[TourGroupMembershipPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select membership_id, user_id, joined_at, status from tour_group_memberships where group_id = ? order by joined_at, membership_id") { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        val userId = row.getString("user_id")
        TourGroupMembershipPlannerResponse(row.getString("membership_id"), userId, userId, row.getString("status"), row.getTimestamp("joined_at").toInstant.toString)
      }
    }

  def membershipTravelers(connection: Connection, groupId: String): List[TourGroupMembershipTravelerPlannerResponse] =
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
        TourGroupMembershipTravelerPlannerResponse(row.getString("membership_traveler_id"), row.getString("membership_id"), row.getString("traveler_id"), row.getString("status"), row.getTimestamp("joined_at").toInstant.toString)
      }
    }

  def blacklists(connection: Connection, groupId: String): List[TourGroupBlacklistPlannerResponse] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select blacklist_id, group_id, user_id, blacklisted_by_user_id, reason, created_at
        from tour_group_blacklists
        where group_id = ?
        order by created_at desc, blacklist_id desc
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        TourGroupBlacklistPlannerResponse(
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
    }
    membershipId

  def activeMembershipId(connection: Connection, groupId: String, userId: String): String =
    PlainSqlSupport.withStatement(connection, "select membership_id from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString("membership_id") else throw new IllegalArgumentException("Active tour group membership was not found")
      finally resultSet.close()
    }

  def markMembershipAndTravelersRemoved(connection: Connection, membershipId: TourGroupMembershipId): Unit =
    PlainSqlSupport.withStatement(connection, "update tour_group_memberships set status = ? where membership_id = ?") { statement =>
      statement.setString(1, "Removed")
      statement.setString(2, membershipId.value)
      statement.executeUpdate()
    }
    PlainSqlSupport.withStatement(connection, "update tour_group_membership_travelers set status = ? where membership_id = ? and status = 'Active'") { statement =>
      statement.setString(1, "Removed")
      statement.setString(2, membershipId.value)
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
      "select 1 from tour_group_blacklists where group_id = ? and user_id = ? fetch first 1 row only"
    ) { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try resultSet.next()
      finally resultSet.close()
    }

  def nextId(prefix: String): String =
    s"$prefix-${UUID.randomUUID().toString.take(12)}"
