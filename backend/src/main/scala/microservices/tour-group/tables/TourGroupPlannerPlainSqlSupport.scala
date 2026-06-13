package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.attraction.api.BookAttractionItemPlanner
import com.typesafe.travel.attraction.domain.BookAttractionItemPlannerRequest
import com.typesafe.travel.flight.api.BookFlightPlanner
import com.typesafe.travel.flight.objects.BookFlightPlannerRequest
import com.typesafe.travel.hotel.api.BookHotelPlanner
import com.typesafe.travel.hotel.objects.BookHotelPlannerRequest
import com.typesafe.travel.order.domain.CreateOrderPlannerRequest
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.BookTrainItemPlanner
import com.typesafe.travel.train.domain.BookTrainItemPlannerRequest
import io.circe.parser.decode
import io.circe.syntax.*

import java.sql.{Connection, ResultSet, Timestamp}
import java.time.{Instant, LocalDate}
import java.util.UUID

object TourGroupPlannerPlainSqlSupport:
  def listSummaries(connection: Connection): List[TourGroupSummaryPlannerResponse] =
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
      "insert into group_selection_order_links(link_id, selection_id, order_id, created_at) values (?, ?, ?, ?)"
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
        set status = 'ConvertedToOrder', confirmed_at = coalesce(confirmed_at, ?)
        where selection_id = ?
      """
    ) { statement =>
      statement.setTimestamp(1, Timestamp.from(now))
      statement.setString(2, selectionId)
      statement.executeUpdate()
    }

  def createOrderForSelection(
      connection: Connection,
      userId: String,
      selection: GroupPlanSelection,
      planItem: GroupPlanItem,
      planOption: GroupPlanOption,
      now: Instant
  ): IO[String] =
    planItem.itemType match
      case GroupPlanItemType.Flight =>
        val (departureAirport, arrivalAirport, departureDate) = parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Flight selection is missing resource context")))
        BookFlightPlanner
          .plan(
          BookFlightPlannerRequest(
            userId = userId,
            flightId = planOption.resourceId,
            travelerIds = selection.travelerIds.map(_.value).toList,
            cabinClass = planOption.resourceVariantCode.getOrElse("Economy")
          ),
          connection
        )
          .map(_.orderId)
      case GroupPlanItemType.Hotel =>
        val (checkInDate, checkOutDate) = parseStayContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Hotel selection is missing stay context")))
        BookHotelPlanner
          .plan(
          BookHotelPlannerRequest(
            userId = userId,
            roomTypeId = planOption.resourceId,
            guestTravelerIds = selection.travelerIds.map(_.value).toList,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            roomCount = selection.quantity
          ),
          connection
        )
          .map(_.orderId)
      case GroupPlanItemType.Train =>
        val (fromStationCode, toStationCode, date) = parseTripContext(planOption.resourceContext.getOrElse(throw new IllegalArgumentException("Train selection is missing route context")))
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookTrainItemPlanner
            .plan(
              BookTrainItemPlannerRequest(
                userId = userId,
                orderId = order.orderId,
                trainId = planOption.resourceId,
                travelerIds = selection.travelerIds.map(_.value).toList,
                fromStationCode = fromStationCode,
                toStationCode = toStationCode,
                seatClass = planOption.resourceVariantCode.getOrElse("SecondClass"),
                seatPreference = None
              ),
              connection
            )
        yield order.orderId
      case GroupPlanItemType.Attraction =>
        val useDate = planItem.scheduledAt.toString.take(10)
        val attractionId = planOption.resourceContext.map(_.trim).filter(_.nonEmpty).getOrElse {
          throw new IllegalArgumentException("Attraction selection is missing attraction context")
        }
        val orderCurrency = "CNY"
        for
          order <- OrderPlannerPlainSql.create(connection, CreateOrderPlannerRequest(userId, orderCurrency), now)
          _ <- BookAttractionItemPlanner
            .plan(
              BookAttractionItemPlannerRequest(
                userId = userId,
                orderId = order.orderId,
                attractionId = attractionId,
                ticketTypeId = planOption.resourceId,
                sessionId = planOption.resourceVariantCode,
                travelerIds = selection.travelerIds.map(_.value).toList,
                useDate = useDate
              ),
              connection
            )
        yield order.orderId

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

  def parseTripContext(resourceContext: String): (String, String, String) =
    decode[Array[String]](resourceContext) match
      case Right(Array(left, right, date)) => (left, right, normalizeDateOnly(date))
      case _ => throw new IllegalArgumentException(s"Invalid trip context: $resourceContext")

  def parseStayContext(resourceContext: String): (String, String) =
    decode[Array[String]](resourceContext) match
      case Right(Array(checkInDate, checkOutDate)) => (normalizeDateOnly(checkInDate), normalizeDateOnly(checkOutDate))
      case _ => throw new IllegalArgumentException(s"Invalid stay context: $resourceContext")

  def normalizeDateOnly(value: String): String =
    LocalDate.parse(value).toString

  def selectionTravelerIds(connection: Connection, groupId: String): Map[String, Vector[TravelerId]] =
    PlainSqlSupport.withStatement(
      connection,
      """
        select s.selection_id, st.traveler_id
        from group_plan_selections s
        left join group_plan_selection_travelers st on st.selection_id = s.selection_id
        where s.group_id = ?
        order by s.created_at, s.selection_id, st.selection_traveler_id
      """
    ) { statement =>
      statement.setString(1, groupId)
      PlainSqlSupport.queryList(statement) { row =>
        (row.getString("selection_id"), Option(row.getString("traveler_id")).filter(_.nonEmpty).map(TravelerId.apply))
      }.foldLeft(Map.empty[String, Vector[TravelerId]]) {
        case (acc, (selectionId, Some(travelerId))) => acc.updated(selectionId, acc.getOrElse(selectionId, Vector.empty) :+ travelerId)
        case (acc, _) => acc
      }
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
          val selection = readSelection(resultSet, selectionTravelerIds(connection, resultSet.getString("group_id")).getOrElse(selectionId, Vector.empty))
          selection
        else throw TourGroupError.SelectionWasNotFound(GroupPlanSelectionId(selectionId))
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

  def createGroupPlanItem(
      planItemId: GroupPlanItemId,
      groupId: TourGroupId,
      itemType: GroupPlanItemType,
      title: String,
      description: String,
      scheduledAt: Instant,
      endsAt: Option[Instant],
      sequenceNo: Int
  ): Either[Throwable, GroupPlanItem] =
    Right(
      GroupPlanItem(
        planItemId = planItemId,
        groupId = groupId,
        itemType = itemType,
        title = title,
        description = description,
        scheduledAt = scheduledAt,
        endsAt = endsAt,
        sequenceNo = sequenceNo,
        status = GroupPlanItemStatus.Draft
      )
    )

  def createGroupPlanOption(
      optionId: GroupPlanOptionId,
      planItemId: GroupPlanItemId,
      resourceType: GroupPlanOptionResourceType,
      resourceId: String,
      resourceVariantCode: Option[String],
      resourceContext: Option[String],
      label: String,
      description: String,
      defaultQuantity: Int
  ): Either[Throwable, GroupPlanOption] =
    Right(
      GroupPlanOption(
        optionId = optionId,
        planItemId = planItemId,
        resourceType = resourceType,
        resourceId = resourceId,
        resourceVariantCode = resourceVariantCode,
        resourceContext = resourceContext,
        label = label,
        description = description,
        defaultQuantity = defaultQuantity,
        status = GroupPlanOptionStatus.Active
      )
    )

  def createGroupPlanSelection(
      selectionId: GroupPlanSelectionId,
      groupId: TourGroupId,
      planItemId: GroupPlanItemId,
      optionId: GroupPlanOptionId,
      membershipId: TourGroupMembershipId,
      quantity: Int,
      travelerIds: Vector[TravelerId],
      now: Instant
  ): Either[Throwable, GroupPlanSelection] =
    Right(
      GroupPlanSelection(
        selectionId = selectionId,
        groupId = groupId,
        planItemId = planItemId,
        optionId = optionId,
        membershipId = membershipId,
        quantity = quantity,
        travelerIds = travelerIds,
        status = GroupPlanSelectionStatus.Submitted,
        createdAt = now,
        confirmedAt = None,
        reviewedByOrganizerUserId = None,
        reviewNote = None
      )
    )

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
      travelerIds = travelerIds,
      status = GroupPlanSelectionStatus.fromText(row.getString("status")),
      createdAt = row.getTimestamp("created_at").toInstant,
      confirmedAt = Option(row.getTimestamp("confirmed_at")).map(_.toInstant),
      reviewedByOrganizerUserId = Option(row.getString("reviewed_by_organizer_user_id")).filter(_.nonEmpty).map(UserId.apply),
      reviewNote = Option(row.getString("review_note")).filter(_.nonEmpty)
    )

  def readSelectionOrderLink(row: ResultSet): GroupSelectionOrderLink =
    GroupSelectionOrderLink(
      linkId = GroupSelectionOrderLinkId(row.getString("link_id")),
      selectionId = GroupPlanSelectionId(row.getString("selection_id")),
      orderId = OrderId(row.getString("order_id")),
      createdAt = row.getTimestamp("created_at").toInstant
    )

  def readSummary(row: ResultSet): TourGroupSummaryPlannerResponse =
    val activeTravelerCount = row.getInt("active_traveler_count")
    TourGroupSummaryPlannerResponse(
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

  def memberships(connection: Connection, groupId: String): List[TourGroupMembershipPlannerResponse] =
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
        TourGroupMembershipPlannerResponse(
          membershipId = row.getString("membership_id"),
          userId = row.getString("user_id"),
          userDisplayName = row.getString("user_id"),
          status = row.getString("status"),
          joinedAt = row.getTimestamp("joined_at").toInstant.toString
        )
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
        TourGroupMembershipTravelerPlannerResponse(
          membershipTravelerId = row.getString("membership_traveler_id"),
          membershipId = row.getString("membership_id"),
          travelerId = row.getString("traveler_id"),
          joinedAt = row.getTimestamp("joined_at").toInstant.toString,
          status = row.getString("status")
        )
      }
    }

  def blacklists(connection: Connection, groupId: String): List[TourGroupBlacklistPlannerResponse] =
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
