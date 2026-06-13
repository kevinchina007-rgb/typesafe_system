package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import io.circe.parser.decode

import java.sql.{Connection, ResultSet}
import java.time.{Instant, LocalDate}

object TourGroupSelectionSupport:
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
      statement.setString(1, TourGroupMembershipSupport.nextId("selection-order-link"))
      statement.setString(2, selectionId)
      statement.setString(3, orderId)
      statement.setTimestamp(4, java.sql.Timestamp.from(now))
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
      statement.setTimestamp(1, java.sql.Timestamp.from(now))
      statement.setString(2, selectionId)
      statement.executeUpdate()
    }

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
