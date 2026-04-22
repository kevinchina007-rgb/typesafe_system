package com.typesafe.travel.persistence.tourgroup

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import cats.data.NonEmptyList
import doobie.*
import doobie.implicits.*

import java.time.{Instant, LocalDate}

trait DoobieTourGroupRepositoryCoreSupport[F[_]: Async]:
  self: DoobieTourGroupRepository[F] =>
  override def nextGroupId: F[TourGroupId] = nextId("group", TourGroupId.apply)
  override def nextMembershipId: F[TourGroupMembershipId] = nextId("membership", TourGroupMembershipId.apply)
  override def nextMembershipTravelerId: F[TourGroupMembershipTravelerId] = nextId("membership-traveler", TourGroupMembershipTravelerId.apply)
  override def nextPlanItemId: F[GroupPlanItemId] = nextId("plan-item", GroupPlanItemId.apply)
  override def nextPlanOptionId: F[GroupPlanOptionId] = nextId("plan-option", GroupPlanOptionId.apply)
  override def nextSelectionId: F[GroupPlanSelectionId] = nextId("selection", GroupPlanSelectionId.apply)
  override def nextSelectionTravelerId: F[GroupPlanSelectionTravelerId] = nextId("selection-traveler", GroupPlanSelectionTravelerId.apply)
  override def nextSelectionOrderLinkId: F[GroupSelectionOrderLinkId] = nextId("selection-link", GroupSelectionOrderLinkId.apply)
  override def nextConversationId: F[TourGroupConversationId] = nextId("conversation", TourGroupConversationId.apply)
  override def nextConversationParticipantId: F[TourGroupConversationParticipantId] = nextId("conversation-participant", TourGroupConversationParticipantId.apply)
  override def nextMessageId: F[TourGroupMessageId] = nextId("message", TourGroupMessageId.apply)
  override def nextMessageAttachmentId: F[TourGroupMessageAttachmentId] = nextId("message-attachment", TourGroupMessageAttachmentId.apply)
  override def nextMessageReactionId: F[TourGroupMessageReactionId] = nextId("message-reaction", TourGroupMessageReactionId.apply)

  override def saveGroup(group: TourGroup): F[TourGroup] =
    upsertGroup(group).transact(transactor).as(group)

  override def findGroupById(groupId: TourGroupId): F[Option[TourGroup]] =
    sql"""
      select organizer_user_id, title, description, destination, start_date, end_date, capacity, status, created_at
      from tour_groups
      where group_id = ${groupId.value}
    """.query[(String, String, String, String, LocalDate, LocalDate, Int, String, Instant)].option.transact(transactor).map(
      _.map { case (organizerUserId, title, description, destination, startDate, endDate, capacity, status, createdAt) =>
      TourGroup(groupId, UserId(organizerUserId), title, description, destination, startDate, endDate, capacity, TourGroupStatus.fromText(status), createdAt)
      }
    )

  override def listGroups: F[List[TourGroup]] =
    sql"""
      select group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity, status, created_at
      from tour_groups
      order by created_at desc, group_id
    """.query[(String, String, String, String, String, LocalDate, LocalDate, Int, String, Instant)].to[List].transact(transactor).map(
      _.map { case (groupId, organizerUserId, title, description, destination, startDate, endDate, capacity, status, createdAt) =>
      TourGroup(TourGroupId(groupId), UserId(organizerUserId), title, description, destination, startDate, endDate, capacity, TourGroupStatus.fromText(status), createdAt)
      }
    )

  override def saveMembership(membership: TourGroupMembership): F[TourGroupMembership] =
    upsertMembership(membership).transact(transactor).as(membership)

  override def findMembershipById(membershipId: TourGroupMembershipId): F[Option[TourGroupMembership]] =
    sql"""
      select group_id, user_id, joined_at, status
      from tour_group_memberships
      where membership_id = ${membershipId.value}
    """.query[(String, String, Instant, String)].option.transact(transactor).map(
      _.map { case (groupId, userId, joinedAt, status) =>
      TourGroupMembership(membershipId, TourGroupId(groupId), UserId(userId), joinedAt, TourGroupMembershipStatus.fromText(status))
      }
    )

  override def findMembershipsByGroupId(groupId: TourGroupId): F[List[TourGroupMembership]] =
    sql"""
      select membership_id, user_id, joined_at, status
      from tour_group_memberships
      where group_id = ${groupId.value}
      order by joined_at, membership_id
    """.query[(String, String, Instant, String)].to[List].transact(transactor).map(
      _.map { case (membershipId, userId, joinedAt, status) =>
      TourGroupMembership(TourGroupMembershipId(membershipId), groupId, UserId(userId), joinedAt, TourGroupMembershipStatus.fromText(status))
      }
    )

  override def findActiveMembershipByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[Option[TourGroupMembership]] =
    sql"""
      select membership_id, joined_at, status
      from tour_group_memberships
      where group_id = ${groupId.value}
        and user_id = ${userId.value}
        and status = ${TourGroupMembershipStatus.Active.toString}
      order by joined_at
      fetch first 1 row only
    """.query[(String, Instant, String)].option.transact(transactor).map(
      _.map { case (membershipId, joinedAt, status) =>
      TourGroupMembership(TourGroupMembershipId(membershipId), groupId, userId, joinedAt, TourGroupMembershipStatus.fromText(status))
      }
    )

  override def saveMembershipTraveler(membershipTraveler: TourGroupMembershipTraveler): F[TourGroupMembershipTraveler] =
    upsertMembershipTraveler(membershipTraveler).transact(transactor).as(membershipTraveler)

  override def findMembershipTravelersByMembershipId(membershipId: TourGroupMembershipId): F[List[TourGroupMembershipTraveler]] =
    sql"""
      select membership_traveler_id, traveler_id, joined_at, status
      from tour_group_membership_travelers
      where membership_id = ${membershipId.value}
      order by joined_at, membership_traveler_id
    """.query[(String, String, Instant, String)].to[List].transact(transactor).map(
      _.map { case (membershipTravelerId, travelerId, joinedAt, status) =>
      TourGroupMembershipTraveler(TourGroupMembershipTravelerId(membershipTravelerId), membershipId, TravelerId(travelerId), joinedAt, TourGroupMembershipTravelerStatus.fromText(status))
      }
    )

  override def findMembershipTravelersByGroupId(groupId: TourGroupId): F[List[TourGroupMembershipTraveler]] =
    sql"""
      select mt.membership_traveler_id, mt.membership_id, mt.traveler_id, mt.joined_at, mt.status
      from tour_group_membership_travelers mt
      inner join tour_group_memberships m on m.membership_id = mt.membership_id
      where m.group_id = ${groupId.value}
      order by mt.joined_at, mt.membership_traveler_id
    """.query[(String, String, String, Instant, String)].to[List].transact(transactor).map(
      _.map { case (membershipTravelerId, membershipId, travelerId, joinedAt, status) =>
      TourGroupMembershipTraveler(TourGroupMembershipTravelerId(membershipTravelerId), TourGroupMembershipId(membershipId), TravelerId(travelerId), joinedAt, TourGroupMembershipTravelerStatus.fromText(status))
      }
    )

  override def savePlanItem(planItem: GroupPlanItem): F[GroupPlanItem] =
    upsertPlanItem(planItem).transact(transactor).as(planItem)

  override def findPlanItemById(planItemId: GroupPlanItemId): F[Option[GroupPlanItem]] =
    sql"""
      select group_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status
      from group_plan_items
      where plan_item_id = ${planItemId.value}
    """.query[(String, String, String, String, Instant, Option[Instant], Int, String)].option.transact(transactor).map(
      _.map { case (groupId, itemType, title, description, scheduledAt, endsAt, sequenceNo, status) =>
      GroupPlanItem(planItemId, TourGroupId(groupId), GroupPlanItemType.fromText(itemType), title, description, scheduledAt, endsAt, sequenceNo, GroupPlanItemStatus.fromText(status))
      }
    )

  override def findPlanItemsByGroupId(groupId: TourGroupId): F[List[GroupPlanItem]] =
    sql"""
      select plan_item_id, item_type, title, description, scheduled_at, ends_at, sequence_no, status
      from group_plan_items
      where group_id = ${groupId.value}
      order by sequence_no, plan_item_id
    """.query[(String, String, String, String, Instant, Option[Instant], Int, String)].to[List].transact(transactor).map(
      _.map { case (planItemId, itemType, title, description, scheduledAt, endsAt, sequenceNo, status) =>
      GroupPlanItem(GroupPlanItemId(planItemId), groupId, GroupPlanItemType.fromText(itemType), title, description, scheduledAt, endsAt, sequenceNo, GroupPlanItemStatus.fromText(status))
      }
    )

  override def savePlanOption(planOption: GroupPlanOption): F[GroupPlanOption] =
    upsertPlanOption(planOption).transact(transactor).as(planOption)

  override def findPlanOptionById(optionId: GroupPlanOptionId): F[Option[GroupPlanOption]] =
    sql"""
      select plan_item_id, resource_type, resource_id, resource_variant_code, resource_context, label, description, default_quantity, status
      from group_plan_options
      where option_id = ${optionId.value}
    """.query[(String, String, String, Option[String], Option[String], String, String, Int, String)].option.transact(transactor).map(
      _.map { case (planItemId, resourceType, resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, status) =>
      GroupPlanOption(optionId, GroupPlanItemId(planItemId), GroupPlanOptionResourceType.fromText(resourceType), resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, GroupPlanOptionStatus.fromText(status))
      }
    )

  override def findPlanOptionsByPlanItemId(planItemId: GroupPlanItemId): F[List[GroupPlanOption]] =
    sql"""
      select option_id, resource_type, resource_id, resource_variant_code, resource_context, label, description, default_quantity, status
      from group_plan_options
      where plan_item_id = ${planItemId.value}
      order by label, option_id
    """.query[(String, String, String, Option[String], Option[String], String, String, Int, String)].to[List].transact(transactor).map(
      _.map { case (optionId, resourceType, resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, status) =>
      GroupPlanOption(GroupPlanOptionId(optionId), planItemId, GroupPlanOptionResourceType.fromText(resourceType), resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, GroupPlanOptionStatus.fromText(status))
      }
    )

  override def findPlanOptionsByGroupId(groupId: TourGroupId): F[List[GroupPlanOption]] =
    sql"""
      select o.option_id, o.plan_item_id, o.resource_type, o.resource_id, o.resource_variant_code, o.resource_context, o.label, o.description, o.default_quantity, o.status
      from group_plan_options o
      inner join group_plan_items i on i.plan_item_id = o.plan_item_id
      where i.group_id = ${groupId.value}
      order by i.sequence_no, o.label, o.option_id
    """.query[(String, String, String, String, Option[String], Option[String], String, String, Int, String)].to[List].transact(transactor).map(
      _.map { case (optionId, planItemId, resourceType, resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, status) =>
      GroupPlanOption(GroupPlanOptionId(optionId), GroupPlanItemId(planItemId), GroupPlanOptionResourceType.fromText(resourceType), resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, GroupPlanOptionStatus.fromText(status))
      }
    )

  override def saveSelection(selection: GroupPlanSelection): F[GroupPlanSelection] =
    upsertSelection(selection).transact(transactor).as(selection)

  override def findSelectionById(selectionId: GroupPlanSelectionId): F[Option[GroupPlanSelection]] =
    sql"""
      select group_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
      from group_plan_selections
      where selection_id = ${selectionId.value}
    """.query[(String, String, String, String, Int, String, Instant, Option[Instant], Option[String], Option[String])].option.transact(transactor).map(
      _.map { case (groupId, planItemId, optionId, membershipId, quantity, status, createdAt, confirmedAt, reviewedByOrganizerUserId, reviewNote) =>
      GroupPlanSelection(selectionId, TourGroupId(groupId), GroupPlanItemId(planItemId), GroupPlanOptionId(optionId), TourGroupMembershipId(membershipId), quantity, GroupPlanSelectionStatus.fromText(status), createdAt, confirmedAt, reviewedByOrganizerUserId.map(UserId.apply), reviewNote)
      }
    )

  override def findSelectionsByGroupId(groupId: TourGroupId): F[List[GroupPlanSelection]] =
    sql"""
      select selection_id, plan_item_id, option_id, membership_id, quantity, status, created_at, confirmed_at, reviewed_by_organizer_user_id, review_note
      from group_plan_selections
      where group_id = ${groupId.value}
      order by created_at, selection_id
    """.query[(String, String, String, String, Int, String, Instant, Option[Instant], Option[String], Option[String])].to[List].transact(transactor).map(
      _.map { case (selectionId, planItemId, optionId, membershipId, quantity, status, createdAt, confirmedAt, reviewedByOrganizerUserId, reviewNote) =>
      GroupPlanSelection(GroupPlanSelectionId(selectionId), groupId, GroupPlanItemId(planItemId), GroupPlanOptionId(optionId), TourGroupMembershipId(membershipId), quantity, GroupPlanSelectionStatus.fromText(status), createdAt, confirmedAt, reviewedByOrganizerUserId.map(UserId.apply), reviewNote)
      }
    )

  override def saveSelectionTraveler(selectionTraveler: GroupPlanSelectionTraveler): F[GroupPlanSelectionTraveler] =
    upsertSelectionTraveler(selectionTraveler).transact(transactor).as(selectionTraveler)

  override def findSelectionTravelersBySelectionId(selectionId: GroupPlanSelectionId): F[List[GroupPlanSelectionTraveler]] =
    sql"""
      select selection_traveler_id, traveler_id
      from group_plan_selection_travelers
      where selection_id = ${selectionId.value}
      order by traveler_id, selection_traveler_id
    """.query[(String, String)].to[List].transact(transactor).map(
      _.map { case (selectionTravelerId, travelerId) =>
        GroupPlanSelectionTraveler(GroupPlanSelectionTravelerId(selectionTravelerId), selectionId, TravelerId(travelerId))
      }
    )

  override def findSelectionTravelersByGroupId(groupId: TourGroupId): F[List[GroupPlanSelectionTraveler]] =
    sql"""
      select st.selection_traveler_id, st.selection_id, st.traveler_id
      from group_plan_selection_travelers st
      inner join group_plan_selections s on s.selection_id = st.selection_id
      where s.group_id = ${groupId.value}
      order by st.traveler_id, st.selection_traveler_id
    """.query[(String, String, String)].to[List].transact(transactor).map(
      _.map { case (selectionTravelerId, selectionId, travelerId) =>
        GroupPlanSelectionTraveler(GroupPlanSelectionTravelerId(selectionTravelerId), GroupPlanSelectionId(selectionId), TravelerId(travelerId))
      }
    )

  override def saveSelectionOrderLink(link: GroupSelectionOrderLink): F[GroupSelectionOrderLink] =
    upsertSelectionOrderLink(link).transact(transactor).as(link)

  override def findSelectionOrderLinkBySelectionId(selectionId: GroupPlanSelectionId): F[Option[GroupSelectionOrderLink]] =
    sql"""
      select link_id, order_id, created_at
      from group_selection_order_links
      where selection_id = ${selectionId.value}
    """.query[(String, String, Instant)].option.transact(transactor).map(
      _.map { case (linkId, orderId, createdAt) =>
        GroupSelectionOrderLink(GroupSelectionOrderLinkId(linkId), selectionId, OrderId(orderId), createdAt)
      }
    )

  override def findSelectionOrderLinksByGroupId(groupId: TourGroupId): F[List[GroupSelectionOrderLink]] =
    sql"""
      select l.link_id, l.selection_id, l.order_id, l.created_at
      from group_selection_order_links l
      inner join group_plan_selections s on s.selection_id = l.selection_id
      where s.group_id = ${groupId.value}
      order by l.created_at, l.link_id
    """.query[(String, String, String, Instant)].to[List].transact(transactor).map(
      _.map { case (linkId, selectionId, orderId, createdAt) =>
        GroupSelectionOrderLink(GroupSelectionOrderLinkId(linkId), GroupPlanSelectionId(selectionId), OrderId(orderId), createdAt)
      }
    )
