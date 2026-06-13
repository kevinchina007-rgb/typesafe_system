// TourGroupPlannerPlainSql 封装团体游模块的公共入口，只保留 Planner 对外调用。
package com.typesafe.travel.tourgroup.domain

import cats.effect.IO
import com.typesafe.travel.attraction.api.BookAttractionItemPlanner
import com.typesafe.travel.attraction.domain.BookAttractionItemPlannerRequest
import com.typesafe.travel.flight.api.BookFlightPlanner
import com.typesafe.travel.flight.objects.BookFlightPlannerRequest
import com.typesafe.travel.hotel.api.BookHotelPlanner
import com.typesafe.travel.hotel.objects.BookHotelPlannerRequest
import com.typesafe.travel.order.domain.CreateOrderPlannerRequest
import com.typesafe.travel.persistence.order.OrderPlannerPlainSql
import com.typesafe.travel.persistence.PlainSqlSupport
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.train.domain.BookTrainItemPlanner
import com.typesafe.travel.train.domain.BookTrainItemPlannerRequest
import io.circe.syntax.*

import java.sql.Connection
import java.time.Instant

object TourGroupPlannerPlainSql:
  def create(connection: Connection, input: CreateTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
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

  def list(connection: Connection): IO[TourGroupListPlannerResponse] =
    IO.blocking(TourGroupListPlannerResponse(TourGroupPlannerPlainSqlSupport.listSummaries(connection)))

  def get(connection: Connection, input: TourGroupByIdPlannerRequest): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking(TourGroupPlannerPlainSqlSupport.details(connection, input.groupId))

  def join(connection: Connection, input: JoinTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
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

  def leave(connection: Connection, input: LeaveTourGroupPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      val actingUserId = UserId(input.userId)
      if tourGroup.organizerUserId == actingUserId then
        throw TourGroupError.OrganizerMustTransferBeforeLeaving(tourGroup.groupId, actingUserId)
      val membership = TourGroupPlannerPlainSqlSupport.activeMembership(connection, input.groupId, input.userId)
      TourGroupPlannerPlainSqlSupport.markMembershipAndTravelersRemoved(connection, membership.membershipId)
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def addMembershipTraveler(connection: Connection, input: AddMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
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

  def removeMembershipTraveler(connection: Connection, input: RemoveMembershipTravelerPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
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

  def kickMember(connection: Connection, input: KickTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
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

  def blacklistMember(connection: Connection, input: BlacklistTourGroupMemberPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
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

  def transferOrganizer(connection: Connection, input: TransferTourGroupLeaderPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
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

  def createPlanItem(connection: Connection, input: CreateTourGroupPlanItemPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      TourGroupPlannerPlainSqlSupport.ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val planItem = TourGroupPlannerPlainSqlSupport.createGroupPlanItem(
        GroupPlanItemId(TourGroupPlannerPlainSqlSupport.nextId("plan-item")),
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
        statement.setTimestamp(6, java.sql.Timestamp.from(planItem.scheduledAt))
        statement.setTimestamp(7, planItem.endsAt.map(java.sql.Timestamp.from).orNull)
        statement.setInt(8, planItem.sequenceNo)
        statement.setString(9, planItem.status.toString)
        statement.executeUpdate()
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def createPlanOption(connection: Connection, input: CreateTourGroupPlanOptionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val tourGroup = TourGroupPlannerPlainSqlSupport.loadGroup(connection, input.groupId)
      TourGroupPlannerPlainSqlSupport.ensureTourGroupOrganizer(tourGroup, UserId(input.organizerUserId)).fold(throw _, identity)
      val planItem = TourGroupPlannerPlainSqlSupport.planItemById(connection, input.planItemId)
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
      val planOption = TourGroupPlannerPlainSqlSupport.createGroupPlanOption(
        GroupPlanOptionId(TourGroupPlannerPlainSqlSupport.nextId("plan-option")),
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
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def createSelection(connection: Connection, input: CreateTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    IO.blocking {
      val activeMembershipRow = TourGroupPlannerPlainSqlSupport.activeMembership(connection, input.groupId, input.userId)
      val membershipTravelerIds =
        TourGroupPlannerPlainSqlSupport.membershipTravelers(connection, input.groupId)
          .collect {
            case row if row.membershipId == activeMembershipRow.membershipId.value && row.status == "Active" =>
              TravelerId(row.travelerId)
          }
          .toSet
      val planItem = TourGroupPlannerPlainSqlSupport.planItems(connection, input.groupId).find(_.planItemId.value == input.planItemId).getOrElse {
        throw TourGroupError.PlanItemWasNotFound(GroupPlanItemId(input.planItemId))
      }
      val planOption = TourGroupPlannerPlainSqlSupport.planOptions(connection, input.groupId).find(_.optionId.value == input.optionId).getOrElse {
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
      val selection = TourGroupPlannerPlainSqlSupport.createGroupPlanSelection(
        GroupPlanSelectionId(TourGroupPlannerPlainSqlSupport.nextId("selection")),
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
        statement.setTimestamp(8, java.sql.Timestamp.from(selection.createdAt))
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
          statement.setString(1, TourGroupPlannerPlainSqlSupport.nextId(s"selection-traveler-${index + 1}"))
          statement.setString(2, selection.selectionId.value)
          statement.setString(3, travelerId.value)
          statement.executeUpdate()
        }
      }
      TourGroupPlannerPlainSqlSupport.details(connection, input.groupId)
    }

  def submitSelection(connection: Connection, input: SubmitTourGroupSelectionPlannerRequest, now: Instant): IO[TourGroupDetailsPlannerResponse] =
    TourGroupPlannerPlainSqlSupport.submitSelection(connection, input, now)
