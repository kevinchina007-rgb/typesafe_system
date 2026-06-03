package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.attraction.api.BookAttractionItemPlanner
import com.typesafe.travel.attraction.domain.BookAttractionItemPlannerRequest
import com.typesafe.travel.flight.api.BookFlightPlanner
import com.typesafe.travel.flight.objects.BookFlightPlannerRequest
import com.typesafe.travel.hotel.api.BookHotelPlanner
import com.typesafe.travel.hotel.objects.BookHotelPlannerRequest
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql
import com.typesafe.travel.order.domain.CreateOrderPlannerRequest
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.BookTrainItemPlanner
import com.typesafe.travel.train.domain.BookTrainItemPlannerRequest
import io.circe.parser.decode
import io.circe.syntax.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Instant, LocalDate}
import java.util.UUID

object TourGroupPlannerPlainSql:
  def create(connection: Connection, input: CreateTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val groupId = nextId("group")
      val membershipId = nextId("membership")
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_groups(group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity, cover_image_url, tags_json, status, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, groupId)
        statement.setString(2, input.organizerUserId)
        statement.setString(3, input.title.trim)
        statement.setString(4, input.description.trim)
        statement.setString(5, input.destination.trim)
        statement.setDate(6, java.sql.Date.valueOf(LocalDate.parse(input.startDate)))
        statement.setDate(7, java.sql.Date.valueOf(LocalDate.parse(input.endDate)))
        statement.setInt(8, input.capacity)
        statement.setString(9, input.coverImageUrl.map(_.trim).filter(_.nonEmpty).orNull)
        statement.setString(10, input.tags.map(_.trim).filter(_.nonEmpty).distinct.asJson.noSpaces)
        statement.setString(11, "Open")
        statement.setTimestamp(12, Timestamp.from(now))
        statement.executeUpdate()
      }
      insertMembership(connection, membershipId, groupId, input.organizerUserId, now)
      details(connection, groupId)
    }

  def list(connection: Connection): IO[TourGroupListPlannerResponse] =
    IO.blocking {
      TourGroupListPlannerResponse(
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
      )
    }

  def get(connection: Connection, input: TourGroupByIdPlannerRequest): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking(details(connection, input.groupId))

  def join(connection: Connection, input: JoinTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      if isBlacklisted(connection, input.groupId, input.userId) then
        throw TourGroupError.UserWasBlacklistedFromGroup(TourGroupId(input.groupId), UserId(input.userId))
      val existingMembershipId =
        PlainSqlSupport.withStatement(connection, "select membership_id from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
          statement.setString(1, input.groupId)
          statement.setString(2, input.userId)
          val resultSet = statement.executeQuery()
          try if resultSet.next() then Some(resultSet.getString("membership_id")) else None
          finally resultSet.close()
        }
      existingMembershipId.getOrElse(insertMembership(connection, nextId("membership"), input.groupId, input.userId, now))
      details(connection, input.groupId)
    }

  def leave(connection: Connection, input: LeaveTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = loadGroup(connection, input.groupId)
      val actingUserId = UserId(input.userId)
      if tourGroup.organizerUserId == actingUserId then
        throw TourGroupError.OrganizerMustTransferBeforeLeaving(tourGroup.groupId, actingUserId)
      val membership = activeMembership(connection, input.groupId, input.userId)
      markMembershipAndTravelersRemoved(connection, membership.membershipId)
      details(connection, input.groupId)
    }

  def addMembershipTraveler(connection: Connection, input: AddMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val membershipId = activeMembershipId(connection, input.groupId, input.userId)
      PlainSqlSupport.withStatement(
        connection,
        "insert into tour_group_membership_travelers(membership_traveler_id, membership_id, traveler_id, joined_at, status) values (?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, nextId("membership-traveler"))
        statement.setString(2, membershipId)
        statement.setString(3, input.travelerId)
        statement.setTimestamp(4, Timestamp.from(now))
        statement.setString(5, "Active")
        statement.executeUpdate()
      }
      details(connection, input.groupId)
    }

  def kickMember(connection: Connection, input: KickTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = loadGroup(connection, input.groupId)
      ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val targetUserId = UserId(input.targetUserId)
      if tourGroup.organizerUserId == targetUserId then
        throw TourGroupError.OrganizerCannotBeKicked(tourGroup.groupId, targetUserId)
      val membership = activeMembership(connection, input.groupId, input.targetUserId)
      markMembershipAndTravelersRemoved(connection, membership.membershipId)
      details(connection, input.groupId)
    }

  def blacklistMember(connection: Connection, input: BlacklistTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = loadGroup(connection, input.groupId)
      ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
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
        statement.setString(1, nextId("blacklist"))
        statement.setString(2, input.groupId)
        statement.setString(3, input.targetUserId)
        statement.setString(4, input.organizerUserId)
        statement.setString(5, "blacklisted by organizer")
        statement.setTimestamp(6, Timestamp.from(now))
        statement.executeUpdate()
      }
      markMembershipIfExistsRemoved(connection, input.groupId, input.targetUserId)
      details(connection, input.groupId)
    }

  def transferOrganizer(connection: Connection, input: TransferTourGroupLeaderPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = loadGroup(connection, input.groupId)
      ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val targetUserId = UserId(input.targetUserId)
      if tourGroup.organizerUserId == targetUserId then
        throw TourGroupError.OrganizerTransferTargetWasInvalid(tourGroup.groupId, targetUserId)
      if isBlacklisted(connection, input.groupId, input.targetUserId) then
        throw TourGroupError.UserWasBlacklistedFromGroup(tourGroup.groupId, targetUserId)
      activeMembership(connection, input.groupId, input.targetUserId)
      PlainSqlSupport.withStatement(connection, "update tour_groups set organizer_user_id = ? where group_id = ?") { statement =>
        statement.setString(1, input.targetUserId)
        statement.setString(2, input.groupId)
        statement.executeUpdate()
      }
      details(connection, input.groupId)
    }

  def createPlanItem(connection: Connection, input: CreateTourGroupPlanItemPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = loadGroup(connection, input.groupId)
      ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val planItem = createGroupPlanItem(
        GroupPlanItemId(nextId("plan-item")),
        tourGroup.groupId,
        GroupPlanItemType.fromText(input.itemType),
        input.title,
        input.description,
        Instant.parse(input.scheduledAt),
        input.endsAt.map(Instant.parse),
        input.sequenceNo
      ).fold(throw _, identity)
      PlainSqlSupport.withStatement(
        connection,
        "insert into group_plan_items(plan_item_id, group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, planItem.planItemId.value)
        statement.setString(2, planItem.groupId.value)
        statement.setString(3, planItem.itemType.toString)
        statement.setString(4, planItem.title)
        statement.setString(5, planItem.description)
        statement.setTimestamp(6, Timestamp.from(planItem.scheduledAt))
        statement.setTimestamp(7, planItem.endsAt.map(Timestamp.from).orNull)
        statement.setInt(8, planItem.sequenceNo)
        statement.setString(9, planItem.status.toString)
        statement.executeUpdate()
      }
      details(connection, input.groupId)
    }

  def createPlanOption(connection: Connection, input: CreateTourGroupPlanOptionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = loadGroup(connection, input.groupId)
      ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val planItem = planItemById(connection, input.planItemId)
      if planItem.groupId.value != input.groupId then
        throw TourGroupError.PlanItemWasNotFound(planItem.planItemId)
      val resourceType = GroupPlanOptionResourceType.fromText(input.resourceType)
      val compatible = planItem.itemType match
        case GroupPlanItemType.Flight     => resourceType == GroupPlanOptionResourceType.Flight
        case GroupPlanItemType.Hotel      => resourceType == GroupPlanOptionResourceType.HotelRoomType
        case GroupPlanItemType.Train      => resourceType == GroupPlanOptionResourceType.TrainJourneySeat
        case GroupPlanItemType.Attraction => resourceType == GroupPlanOptionResourceType.AttractionTicketType
      if !compatible then
        throw TourGroupError.PlanOptionResourceTypeDidNotMatchPlanItem(planItem.planItemId, planItem.itemType, resourceType)
      val planOption = createGroupPlanOption(
        GroupPlanOptionId(nextId("plan-option")),
        planItem.planItemId,
        resourceType,
        input.resourceId,
        input.resourceVariantCode,
        input.resourceContext,
        input.label,
        input.description,
        input.defaultQuantity
      ).fold(throw _, identity)
      PlainSqlSupport.withStatement(
        connection,
        "insert into group_plan_options(option_id, plan_item_id, resource_type, resource_id, resource_variant_code, resource_context, label, description, default_quantity, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      ) { statement =>
        statement.setString(1, planOption.optionId.value)
        statement.setString(2, planOption.planItemId.value)
        statement.setString(3, planOption.resourceType.toString)
        statement.setString(4, planOption.resourceId)
        statement.setString(5, planOption.resourceVariantCode.orNull)
        statement.setString(6, planOption.resourceContext.orNull)
        statement.setString(7, planOption.label)
        statement.setString(8, planOption.description)
        statement.setInt(9, planOption.defaultQuantity)
        statement.setString(10, planOption.status.toString)
        statement.executeUpdate()
      }
      details(connection, input.groupId)
    }

  def createSelection(connection: Connection, input: CreateTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val activeMembershipRow = activeMembership(connection, input.groupId, input.userId)
      val membershipTravelerIds =
        membershipTravelers(connection, input.groupId)
          .collect {
            case row if row.membershipId == activeMembershipRow.membershipId.value && row.status == "Active" =>
              TravelerId(row.travelerId)
          }
          .toSet
      val planItem = planItems(connection, input.groupId).find(_.planItemId.value == input.planItemId).getOrElse {
        throw TourGroupError.PlanItemWasNotFound(GroupPlanItemId(input.planItemId))
      }
      val planOption = planOptions(connection, input.groupId).find(_.optionId.value == input.optionId).getOrElse {
        throw TourGroupError.PlanOptionWasNotFound(GroupPlanOptionId(input.optionId))
      }
      if planOption.planItemId != planItem.planItemId then
        throw TourGroupError.PlanOptionDidNotBelongToPlanItem(planOption.optionId, planItem.planItemId)
      val requestedTravelerIds = input.travelerIds.map(TravelerId.apply).distinct.toVector
      if requestedTravelerIds.isEmpty then
        throw TourGroupError.SelectionTravelerWasEmpty(planItem.planItemId)
      if requestedTravelerIds.size != input.quantity then
        throw TourGroupError.SelectionQuantityDidNotMatchTravelerCount(GroupPlanSelectionId("pending"), input.quantity, requestedTravelerIds.size)
      requestedTravelerIds.foreach { travelerId =>
        if !membershipTravelerIds.contains(travelerId) then
          throw TourGroupError.SelectionTravelerWasNotInMembership(GroupPlanSelectionId("pending"), travelerId)
      }
      val selection = createGroupPlanSelection(
        GroupPlanSelectionId(nextId("selection")),
        TourGroupId(input.groupId),
        planItem.planItemId,
        planOption.optionId,
        activeMembershipRow.membershipId,
        input.quantity,
        requestedTravelerIds,
        now
      ).fold(throw _, identity)
      PlainSqlSupport.withStatement(
        connection,
        """
          insert into group_plan_selections(
            selection_id, group_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
          ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
      ) { statement =>
        statement.setString(1, selection.selectionId.value)
        statement.setString(2, selection.groupId.value)
        statement.setString(3, selection.planItemId.value)
        statement.setString(4, selection.optionId.value)
        statement.setString(5, selection.membershipId.value)
        statement.setInt(6, selection.quantity)
        statement.setString(7, selection.status.toString)
        statement.setTimestamp(8, Timestamp.from(selection.createdAt))
        statement.setTimestamp(9, null)
        statement.setString(10, null)
        statement.setString(11, null)
        statement.executeUpdate()
      }
      requestedTravelerIds.zipWithIndex.foreach { case (travelerId, index) =>
        PlainSqlSupport.withStatement(
          connection,
          """
            insert into group_plan_selection_travelers(selection_traveler_id, selection_id, traveler_id)
            values (?, ?, ?)
          """
        ) { statement =>
          statement.setString(1, nextId(s"selection-traveler-${index + 1}"))
          statement.setString(2, selection.selectionId.value)
          statement.setString(3, travelerId.value)
          statement.executeUpdate()
        }
      }
      details(connection, input.groupId)
    }

  def submitSelection(connection: Connection, input: SubmitTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    val loadedContext = IO.blocking {
      val selection = selectionById(connection, input.selectionId)
      val activeMembershipRow = activeMembership(connection, selection.groupId.value, input.userId)
      if selection.membershipId != activeMembershipRow.membershipId then
        throw TourGroupError.MembershipScopeDidNotMatch(activeMembershipRow.membershipId, UserId(input.userId))
      val acceptedSelection = submitGroupPlanSelection(selection, now).fold(throw _, identity)
      val planItem = planItems(connection, selection.groupId.value).find(_.planItemId == selection.planItemId).getOrElse {
        throw TourGroupError.PlanItemWasNotFound(selection.planItemId)
      }
      val planOption = planOptions(connection, selection.groupId.value).find(_.optionId == selection.optionId).getOrElse {
        throw TourGroupError.PlanOptionWasNotFound(selection.optionId)
      }
      (acceptedSelection, planItem, planOption)
    }.flatMap { case (acceptedSelection, planItem, planOption) =>
      existingSelectionOrderId(connection, acceptedSelection.selectionId.value).flatMap {
        case Some(_) => IO.blocking(details(connection, acceptedSelection.groupId.value))
        case None =>
          createOrderForSelection(connection, input.userId, acceptedSelection, planItem, planOption, now).flatMap { orderId =>
            IO.blocking {
              insertSelectionOrderLink(connection, acceptedSelection.selectionId.value, orderId, now)
              updateSelectionAsConvertedToOrder(connection, acceptedSelection.selectionId.value, now)
              details(connection, acceptedSelection.groupId.value)
            }
          }
      }
    }
    loadedContext

  private def details(connection: Connection, groupId: String): TourGroupDetailsPlannerResponse =
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

  private def groupSummary(connection: Connection, groupId: String): TourGroupSummaryPlannerResponse =
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

  private def loadGroup(connection: Connection, groupId: String): TourGroup =
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

  private def activeMembership(connection: Connection, groupId: String, userId: String): TourGroupMembership =
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

  private def readGroup(row: ResultSet): TourGroup =
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

  private def readMembership(row: ResultSet): TourGroupMembership =
    TourGroupMembership(
      membershipId = TourGroupMembershipId(row.getString("membership_id")),
      groupId = TourGroupId(row.getString("group_id")),
      userId = UserId(row.getString("user_id")),
      joinedAt = row.getTimestamp("joined_at").toInstant,
      status = TourGroupMembershipStatus.fromText(row.getString("status"))
    )

  private def planItems(connection: Connection, groupId: String): List[GroupPlanItem] =
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

  private def planOptions(connection: Connection, groupId: String): List[GroupPlanOption] =
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

  private def selections(connection: Connection, groupId: String): List[GroupPlanSelection] =
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

  private def selectionOrderLinks(connection: Connection, groupId: String): List[GroupSelectionOrderLink] =
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

  private def existingSelectionOrderId(connection: Connection, selectionId: String): IO[Option[String]] =
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

  private def insertSelectionOrderLink(connection: Connection, selectionId: String, orderId: String, now: Instant): Unit =
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

  private def updateSelectionAsConvertedToOrder(connection: Connection, selectionId: String, now: Instant): Unit =
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

  private def createOrderForSelection(
      connection: Connection,
      userId: String,
      selection: GroupPlanSelection,
      planItem: GroupPlanItem,
      planOption: GroupPlanOption,
      now: Instant
  ): IO[String] =
    val travelerIds = selection.travelerIds.map(_.value).toList
    planOption.resourceType match
      case GroupPlanOptionResourceType.Flight =>
        val (departureAirport, arrivalAirport, departureDate) = parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Flight selection is missing resource context")))
        val cabinClass = planOption.resourceVariantCode.getOrElse(throw new IllegalArgumentException("Flight selection is missing cabin class"))
        BookFlightPlanner
          .plan(
            BookFlightPlannerRequest(
              userId = userId,
              flightId = planOption.resourceId,
              travelerIds = travelerIds,
              cabinClass = cabinClass
            ),
            connection
          )
          .map(_.orderId)
      case GroupPlanOptionResourceType.HotelRoomType =>
        val (checkInDate, checkOutDate) = parseStayContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Hotel selection is missing stay context")))
        BookHotelPlanner
          .plan(
            BookHotelPlannerRequest(
              userId = userId,
              roomTypeId = planOption.resourceId,
              guestTravelerIds = travelerIds,
              checkInDate = checkInDate,
              checkOutDate = checkOutDate,
              roomCount = selection.quantity
            ),
            connection
          )
          .map(_.orderId)
      case GroupPlanOptionResourceType.TrainJourneySeat =>
        val (fromStationCode, toStationCode, date) = parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Train selection is missing route context")))
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookTrainItemPlanner.plan(
            BookTrainItemPlannerRequest(
              userId = userId,
              orderId = order.orderId,
              trainId = planOption.resourceId,
              travelerIds = travelerIds,
              fromStationCode = fromStationCode,
              toStationCode = toStationCode,
              seatClass = planOption.resourceVariantCode.getOrElse(throw new IllegalArgumentException("Train selection is missing seat class")),
              seatPreference = None
            ),
            connection
          )
        yield order.orderId
      case GroupPlanOptionResourceType.AttractionTicketType =>
        val useDate = planItem.scheduledAt.toString.take(10)
        val attractionId = planOption.resourceContext.map(_.trim).filter(_.nonEmpty).getOrElse {
          throw new IllegalArgumentException("Attraction selection is missing attraction context")
        }
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookAttractionItemPlanner.plan(
            BookAttractionItemPlannerRequest(
              userId = userId,
              orderId = order.orderId,
              attractionId = attractionId,
              ticketTypeId = planOption.resourceId,
              sessionId = planOption.resourceVariantCode,
              travelerIds = travelerIds,
              useDate = useDate
            ),
            connection
          )
        yield order.orderId

  private def parseTripContext(resourceContext: String): (String, String, String) =
    resourceContext.split('|').map(_.trim) match
      case Array(left, right, date) => (left, right, normalizeDateOnly(date))
      case _ => throw new IllegalArgumentException(s"Invalid tour-group resource context: '$resourceContext'")

  private def parseStayContext(resourceContext: String): (String, String) =
    resourceContext.split('|').map(_.trim) match
      case Array(checkInDate, checkOutDate) => (normalizeDateOnly(checkInDate), normalizeDateOnly(checkOutDate))
      case _ => throw new IllegalArgumentException(s"Invalid tour-group hotel context: '$resourceContext'")

  private def normalizeDateOnly(value: String): String =
    value.trim.take(10)

  private def selectionTravelerIds(connection: Connection, groupId: String): Map[String, Vector[TravelerId]] =
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

  private def selectionById(connection: Connection, selectionId: String): GroupPlanSelection =
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

  private def planItemById(connection: Connection, planItemId: String): GroupPlanItem =
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

  private def readPlanItem(row: ResultSet): GroupPlanItem =
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

  private def readPlanOption(row: ResultSet): GroupPlanOption =
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

  private def readSelection(row: ResultSet, travelerIds: Vector[TravelerId]): GroupPlanSelection =
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

  private def readSelectionOrderLink(row: ResultSet): GroupSelectionOrderLink =
    GroupSelectionOrderLink(
      linkId = GroupSelectionOrderLinkId(row.getString("link_id")),
      selectionId = GroupPlanSelectionId(row.getString("selection_id")),
      orderId = OrderId(row.getString("order_id")),
      createdAt = row.getTimestamp("created_at").toInstant
    )

  private def readSummary(row: ResultSet): TourGroupSummaryPlannerResponse =
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

  private def memberships(connection: Connection, groupId: String): List[TourGroupMembershipPlannerResponse] =
    PlainSqlSupport.withStatement(connection, "select membership_id, user_id, joined_at, status from tour_group_memberships where group_id = ? order by joined_at, membership_id") { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        val userId = row.getString("user_id")
        TourGroupMembershipPlannerResponse(row.getString("membership_id"), userId, userId, row.getString("status"), row.getTimestamp("joined_at").toInstant.toString)
      }
    }

  private def membershipTravelers(connection: Connection, groupId: String): List[TourGroupMembershipTravelerPlannerResponse] =
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

  private def blacklists(connection: Connection, groupId: String): List[TourGroupBlacklistPlannerResponse] =
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

  private def insertMembership(connection: Connection, membershipId: String, groupId: String, userId: String, joinedAt: Instant): String =
    PlainSqlSupport.withStatement(connection, "insert into tour_group_memberships(membership_id, group_id, user_id, joined_at, status) values (?, ?, ?, ?, ?)") { statement =>
      statement.setString(1, membershipId)
      statement.setString(2, groupId)
      statement.setString(3, userId)
      statement.setTimestamp(4, Timestamp.from(joinedAt))
      statement.setString(5, "Active")
      statement.executeUpdate()
    }
    membershipId

  private def activeMembershipId(connection: Connection, groupId: String, userId: String): String =
    PlainSqlSupport.withStatement(connection, "select membership_id from tour_group_memberships where group_id = ? and user_id = ? and status = 'Active' fetch first 1 row only") { statement =>
      statement.setString(1, groupId)
      statement.setString(2, userId)
      val resultSet = statement.executeQuery()
      try if resultSet.next() then resultSet.getString("membership_id") else throw new IllegalArgumentException("Active tour group membership was not found")
      finally resultSet.close()
    }

  private def markMembershipAndTravelersRemoved(connection: Connection, membershipId: TourGroupMembershipId): Unit =
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

  private def markMembershipIfExistsRemoved(connection: Connection, groupId: String, userId: String): Unit =
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

  private def isBlacklisted(connection: Connection, groupId: String, userId: String): Boolean =
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

  private def nextId(prefix: String): String =
    s"$prefix-${UUID.randomUUID().toString.take(12)}"
