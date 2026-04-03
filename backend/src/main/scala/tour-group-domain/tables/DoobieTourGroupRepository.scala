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
import java.util.UUID

final class DoobieTourGroupRepository[F[_]: Async](transactor: Transactor[F]) extends TourGroupRepository[F]:
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
        TourGroup(groupId, UserId(organizerUserId), title, description, destination, startDate, endDate, capacity, TourGroupStatus.valueOf(status), createdAt)
      }
    )

  override def listGroups: F[List[TourGroup]] =
    sql"""
      select group_id, organizer_user_id, title, description, destination, start_date, end_date, capacity, status, created_at
      from tour_groups
      order by created_at desc, group_id
    """.query[(String, String, String, String, String, LocalDate, LocalDate, Int, String, Instant)].to[List].transact(transactor).map(
      _.map { case (groupId, organizerUserId, title, description, destination, startDate, endDate, capacity, status, createdAt) =>
        TourGroup(TourGroupId(groupId), UserId(organizerUserId), title, description, destination, startDate, endDate, capacity, TourGroupStatus.valueOf(status), createdAt)
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
        TourGroupMembership(membershipId, TourGroupId(groupId), UserId(userId), joinedAt, TourGroupMembershipStatus.valueOf(status))
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
        TourGroupMembership(TourGroupMembershipId(membershipId), groupId, UserId(userId), joinedAt, TourGroupMembershipStatus.valueOf(status))
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
        TourGroupMembership(TourGroupMembershipId(membershipId), groupId, userId, joinedAt, TourGroupMembershipStatus.valueOf(status))
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
        TourGroupMembershipTraveler(TourGroupMembershipTravelerId(membershipTravelerId), membershipId, TravelerId(travelerId), joinedAt, TourGroupMembershipTravelerStatus.valueOf(status))
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
        TourGroupMembershipTraveler(TourGroupMembershipTravelerId(membershipTravelerId), TourGroupMembershipId(membershipId), TravelerId(travelerId), joinedAt, TourGroupMembershipTravelerStatus.valueOf(status))
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
        GroupPlanItem(planItemId, TourGroupId(groupId), GroupPlanItemType.valueOf(itemType), title, description, scheduledAt, endsAt, sequenceNo, GroupPlanItemStatus.valueOf(status))
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
        GroupPlanItem(GroupPlanItemId(planItemId), groupId, GroupPlanItemType.valueOf(itemType), title, description, scheduledAt, endsAt, sequenceNo, GroupPlanItemStatus.valueOf(status))
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
        GroupPlanOption(optionId, GroupPlanItemId(planItemId), GroupPlanOptionResourceType.valueOf(resourceType), resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, GroupPlanOptionStatus.valueOf(status))
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
        GroupPlanOption(GroupPlanOptionId(optionId), planItemId, GroupPlanOptionResourceType.valueOf(resourceType), resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, GroupPlanOptionStatus.valueOf(status))
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
        GroupPlanOption(GroupPlanOptionId(optionId), GroupPlanItemId(planItemId), GroupPlanOptionResourceType.valueOf(resourceType), resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity, GroupPlanOptionStatus.valueOf(status))
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
        GroupPlanSelection(selectionId, TourGroupId(groupId), GroupPlanItemId(planItemId), GroupPlanOptionId(optionId), TourGroupMembershipId(membershipId), quantity, GroupPlanSelectionStatus.valueOf(status), createdAt, confirmedAt, reviewedByOrganizerUserId.map(UserId.apply), reviewNote)
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
        GroupPlanSelection(GroupPlanSelectionId(selectionId), groupId, GroupPlanItemId(planItemId), GroupPlanOptionId(optionId), TourGroupMembershipId(membershipId), quantity, GroupPlanSelectionStatus.valueOf(status), createdAt, confirmedAt, reviewedByOrganizerUserId.map(UserId.apply), reviewNote)
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

  override def saveChatSettings(settings: TourGroupChatSettings): F[TourGroupChatSettings] =
    sql"""
      insert into tour_group_chat_settings (
        group_id,
        allow_member_direct_chat,
        updated_at,
        updated_by_user_id
      ) values (
        ${settings.groupId.value},
        ${settings.allowMemberDirectChat},
        ${settings.updatedAt},
        ${settings.updatedByUserId.value}
      )
      on conflict (group_id) do update set
        allow_member_direct_chat = excluded.allow_member_direct_chat,
        updated_at = excluded.updated_at,
        updated_by_user_id = excluded.updated_by_user_id
    """.update.run.transact(transactor).as(settings)

  override def findChatSettingsByGroupId(groupId: TourGroupId): F[Option[TourGroupChatSettings]] =
    sql"""
      select allow_member_direct_chat, updated_at, updated_by_user_id
      from tour_group_chat_settings
      where group_id = ${groupId.value}
    """.query[(Boolean, Instant, String)].option.transact(transactor).map(
      _.map { case (allowMemberDirectChat, updatedAt, updatedByUserId) =>
        TourGroupChatSettings(groupId, allowMemberDirectChat, updatedAt, UserId(updatedByUserId))
      }
    )

  override def saveConversation(conversation: TourGroupConversation): F[TourGroupConversation] =
    sql"""
      insert into tour_group_conversations (
        conversation_id,
        group_id,
        conversation_type,
        status,
        direct_member_a_user_id,
        direct_member_b_user_id,
        created_at,
        updated_at
      ) values (
        ${conversation.conversationId.value},
        ${conversation.groupId.value},
        ${conversation.conversationType.toString},
        ${conversation.status.toString},
        ${conversation.directMemberAUserId.map(_.value)},
        ${conversation.directMemberBUserId.map(_.value)},
        ${conversation.createdAt},
        ${conversation.updatedAt}
      )
      on conflict (conversation_id) do update set
        group_id = excluded.group_id,
        conversation_type = excluded.conversation_type,
        status = excluded.status,
        direct_member_a_user_id = excluded.direct_member_a_user_id,
        direct_member_b_user_id = excluded.direct_member_b_user_id,
        created_at = excluded.created_at,
        updated_at = excluded.updated_at
    """.update.run.transact(transactor).as(conversation)

  override def findConversationById(conversationId: TourGroupConversationId): F[Option[TourGroupConversation]] =
    sql"""
      select group_id, conversation_type, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
      from tour_group_conversations
      where conversation_id = ${conversationId.value}
    """.query[(String, String, String, Option[String], Option[String], Instant, Instant)].option.transact(transactor).map(
      _.map { case (groupId, conversationType, status, directMemberAUserId, directMemberBUserId, createdAt, updatedAt) =>
        TourGroupConversation(
          conversationId = conversationId,
          groupId = TourGroupId(groupId),
          conversationType = TourGroupConversationType.valueOf(conversationType),
          status = TourGroupConversationStatus.valueOf(status),
          directMemberAUserId = directMemberAUserId.map(UserId.apply),
          directMemberBUserId = directMemberBUserId.map(UserId.apply),
          createdAt = createdAt,
          updatedAt = updatedAt
        )
      }
    )

  override def findPublicConversationByGroupId(groupId: TourGroupId): F[Option[TourGroupConversation]] =
    sql"""
      select conversation_id, status, created_at, updated_at
      from tour_group_conversations
      where group_id = ${groupId.value}
        and conversation_type = ${TourGroupConversationType.GroupPublic.toString}
      fetch first 1 row only
    """.query[(String, String, Instant, Instant)].option.transact(transactor).map(
      _.map { case (conversationId, status, createdAt, updatedAt) =>
        TourGroupConversation(TourGroupConversationId(conversationId), groupId, TourGroupConversationType.GroupPublic, TourGroupConversationStatus.valueOf(status), None, None, createdAt, updatedAt)
      }
    )

  override def findDirectConversationByGroupIdAndUsers(groupId: TourGroupId, userA: UserId, userB: UserId): F[Option[TourGroupConversation]] =
    val (leftUserId, rightUserId) =
      if userA.value <= userB.value then (userA, userB) else (userB, userA)
    sql"""
      select conversation_id, status, created_at, updated_at
      from tour_group_conversations
      where group_id = ${groupId.value}
        and conversation_type = ${TourGroupConversationType.Direct.toString}
        and direct_member_a_user_id = ${leftUserId.value}
        and direct_member_b_user_id = ${rightUserId.value}
      fetch first 1 row only
    """.query[(String, String, Instant, Instant)].option.transact(transactor).map(
      _.map { case (conversationId, status, createdAt, updatedAt) =>
        TourGroupConversation(TourGroupConversationId(conversationId), groupId, TourGroupConversationType.Direct, TourGroupConversationStatus.valueOf(status), Some(leftUserId), Some(rightUserId), createdAt, updatedAt)
      }
    )

  override def findDirectConversationsByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[List[TourGroupConversation]] =
    sql"""
      select conversation_id, status, direct_member_a_user_id, direct_member_b_user_id, created_at, updated_at
      from tour_group_conversations
      where group_id = ${groupId.value}
        and conversation_type = ${TourGroupConversationType.Direct.toString}
        and (direct_member_a_user_id = ${userId.value} or direct_member_b_user_id = ${userId.value})
      order by updated_at desc, conversation_id
    """.query[(String, String, Option[String], Option[String], Instant, Instant)].to[List].transact(transactor).map(
      _.map { case (conversationId, status, directMemberAUserId, directMemberBUserId, createdAt, updatedAt) =>
        TourGroupConversation(
          conversationId = TourGroupConversationId(conversationId),
          groupId = groupId,
          conversationType = TourGroupConversationType.Direct,
          status = TourGroupConversationStatus.valueOf(status),
          directMemberAUserId = directMemberAUserId.map(UserId.apply),
          directMemberBUserId = directMemberBUserId.map(UserId.apply),
          createdAt = createdAt,
          updatedAt = updatedAt
        )
      }
    )

  override def findAccessibleConversationsByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[List[TourGroupConversation]] =
    sql"""
      select c.conversation_id, c.conversation_type, c.status, c.direct_member_a_user_id, c.direct_member_b_user_id, c.created_at, c.updated_at
      from tour_group_conversations c
      inner join tour_group_conversation_participants p on p.conversation_id = c.conversation_id
      where c.group_id = ${groupId.value}
        and p.user_id = ${userId.value}
        and p.status = ${TourGroupConversationParticipantStatus.Active.toString}
      order by c.updated_at desc, c.conversation_id
    """.query[(String, String, String, Option[String], Option[String], Instant, Instant)].to[List].transact(transactor).map(
      _.map { case (conversationId, conversationType, status, directMemberAUserId, directMemberBUserId, createdAt, updatedAt) =>
        TourGroupConversation(
          conversationId = TourGroupConversationId(conversationId),
          groupId = groupId,
          conversationType = TourGroupConversationType.valueOf(conversationType),
          status = TourGroupConversationStatus.valueOf(status),
          directMemberAUserId = directMemberAUserId.map(UserId.apply),
          directMemberBUserId = directMemberBUserId.map(UserId.apply),
          createdAt = createdAt,
          updatedAt = updatedAt
        )
      }
    )

  override def saveConversationParticipant(participant: TourGroupConversationParticipant): F[TourGroupConversationParticipant] =
    sql"""
      insert into tour_group_conversation_participants (
        participant_id,
        conversation_id,
        user_id,
        role,
        joined_at,
        status,
        last_read_at,
        last_read_message_id,
        muted_at,
        archived_at
      ) values (
        ${participant.participantId.value},
        ${participant.conversationId.value},
        ${participant.userId.value},
        ${participant.role.toString},
        ${participant.joinedAt},
        ${participant.status.toString},
        ${participant.lastReadAt},
        ${participant.lastReadMessageId.map(_.value)},
        ${participant.mutedAt},
        ${participant.archivedAt}
      )
      on conflict (participant_id) do update set
        conversation_id = excluded.conversation_id,
        user_id = excluded.user_id,
        role = excluded.role,
        joined_at = excluded.joined_at,
        status = excluded.status,
        last_read_at = excluded.last_read_at,
        last_read_message_id = excluded.last_read_message_id,
        muted_at = excluded.muted_at,
        archived_at = excluded.archived_at
    """.update.run.transact(transactor).as(participant)

  override def findConversationParticipant(conversationId: TourGroupConversationId, userId: UserId): F[Option[TourGroupConversationParticipant]] =
    sql"""
      select participant_id, role, joined_at, status, last_read_at, last_read_message_id, muted_at, archived_at
      from tour_group_conversation_participants
      where conversation_id = ${conversationId.value}
        and user_id = ${userId.value}
      fetch first 1 row only
    """.query[(String, String, Instant, String, Option[Instant], Option[String], Option[Instant], Option[Instant])].option.transact(transactor).map(
      _.map { case (participantId, role, joinedAt, status, lastReadAt, lastReadMessageId, mutedAt, archivedAt) =>
        TourGroupConversationParticipant(
          participantId = TourGroupConversationParticipantId(participantId),
          conversationId = conversationId,
          userId = userId,
          role = TourGroupConversationParticipantRole.valueOf(role),
          joinedAt = joinedAt,
          status = TourGroupConversationParticipantStatus.valueOf(status),
          lastReadAt = lastReadAt,
          lastReadMessageId = lastReadMessageId.map(TourGroupMessageId.apply),
          mutedAt = mutedAt,
          archivedAt = archivedAt
        )
      }
    )

  override def findParticipantsByConversationId(conversationId: TourGroupConversationId): F[List[TourGroupConversationParticipant]] =
    sql"""
      select participant_id, user_id, role, joined_at, status, last_read_at, last_read_message_id, muted_at, archived_at
      from tour_group_conversation_participants
      where conversation_id = ${conversationId.value}
      order by joined_at, participant_id
    """.query[(String, String, String, Instant, String, Option[Instant], Option[String], Option[Instant], Option[Instant])].to[List].transact(transactor).map(
      _.map { case (participantId, userId, role, joinedAt, status, lastReadAt, lastReadMessageId, mutedAt, archivedAt) =>
        TourGroupConversationParticipant(
          participantId = TourGroupConversationParticipantId(participantId),
          conversationId = conversationId,
          userId = UserId(userId),
          role = TourGroupConversationParticipantRole.valueOf(role),
          joinedAt = joinedAt,
          status = TourGroupConversationParticipantStatus.valueOf(status),
          lastReadAt = lastReadAt,
          lastReadMessageId = lastReadMessageId.map(TourGroupMessageId.apply),
          mutedAt = mutedAt,
          archivedAt = archivedAt
        )
      }
    )

  override def saveMessage(message: TourGroupMessage): F[TourGroupMessage] =
    sql"""
      insert into tour_group_messages (
        message_id,
        conversation_id,
        sender_user_id,
        message_type,
        content,
        reply_to_message_id,
        forwarded_from_message_id,
        status,
        created_at,
        updated_at,
        deleted_at,
        recalled_at
      ) values (
        ${message.messageId.value},
        ${message.conversationId.value},
        ${message.senderUserId.value},
        ${message.messageType.toString},
        ${message.content},
        ${message.replyToMessageId.map(_.value)},
        ${message.forwardedFromMessageId.map(_.value)},
        ${message.status.toString},
        ${message.createdAt},
        ${message.updatedAt},
        ${message.deletedAt},
        ${message.recalledAt}
      )
      on conflict (message_id) do update set
        conversation_id = excluded.conversation_id,
        sender_user_id = excluded.sender_user_id,
        message_type = excluded.message_type,
        content = excluded.content,
        reply_to_message_id = excluded.reply_to_message_id,
        forwarded_from_message_id = excluded.forwarded_from_message_id,
        status = excluded.status,
        created_at = excluded.created_at,
        updated_at = excluded.updated_at,
        deleted_at = excluded.deleted_at,
        recalled_at = excluded.recalled_at
    """.update.run.transact(transactor).as(message)

  override def findMessageById(messageId: TourGroupMessageId): F[Option[TourGroupMessage]] =
    sql"""
      select conversation_id, sender_user_id, message_type, content, reply_to_message_id, forwarded_from_message_id, status, created_at, updated_at, deleted_at, recalled_at
      from tour_group_messages
      where message_id = ${messageId.value}
    """.query[(String, String, String, String, Option[String], Option[String], String, Instant, Instant, Option[Instant], Option[Instant])].option.transact(transactor).map(
      _.map { case (conversationId, senderUserId, messageType, content, replyToMessageId, forwardedFromMessageId, status, createdAt, updatedAt, deletedAt, recalledAt) =>
        TourGroupMessage(
          messageId = messageId,
          conversationId = TourGroupConversationId(conversationId),
          senderUserId = UserId(senderUserId),
          messageType = TourGroupMessageType.valueOf(messageType),
          content = content,
          replyToMessageId = replyToMessageId.map(TourGroupMessageId.apply),
          forwardedFromMessageId = forwardedFromMessageId.map(TourGroupMessageId.apply),
          status = TourGroupMessageStatus.valueOf(status),
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt,
          recalledAt = recalledAt
        )
      }
    )

  override def findMessagesByConversationId(conversationId: TourGroupConversationId): F[List[TourGroupMessage]] =
    sql"""
      select message_id, sender_user_id, message_type, content, reply_to_message_id, forwarded_from_message_id, status, created_at, updated_at, deleted_at, recalled_at
      from tour_group_messages
      where conversation_id = ${conversationId.value}
      order by created_at, message_id
    """.query[(String, String, String, String, Option[String], Option[String], String, Instant, Instant, Option[Instant], Option[Instant])].to[List].transact(transactor).map(
      _.map { case (messageId, senderUserId, messageType, content, replyToMessageId, forwardedFromMessageId, status, createdAt, updatedAt, deletedAt, recalledAt) =>
        TourGroupMessage(
          messageId = TourGroupMessageId(messageId),
          conversationId = conversationId,
          senderUserId = UserId(senderUserId),
          messageType = TourGroupMessageType.valueOf(messageType),
          content = content,
          replyToMessageId = replyToMessageId.map(TourGroupMessageId.apply),
          forwardedFromMessageId = forwardedFromMessageId.map(TourGroupMessageId.apply),
          status = TourGroupMessageStatus.valueOf(status),
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt,
          recalledAt = recalledAt
        )
      }
    )

  override def saveMessageAttachment(attachment: TourGroupMessageAttachment): F[TourGroupMessageAttachment] =
    sql"""
      insert into tour_group_message_attachments (
        attachment_id, message_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
      ) values (
        ${attachment.attachmentId.value}, ${attachment.messageId.value}, ${attachment.attachmentType.toString}, ${attachment.publicUrl}, ${attachment.storagePath},
        ${attachment.originalFileName}, ${attachment.mimeType}, ${attachment.fileSize}, ${attachment.sortOrder}, ${attachment.createdAt}
      )
      on conflict (attachment_id) do update set
        message_id = excluded.message_id,
        attachment_type = excluded.attachment_type,
        public_url = excluded.public_url,
        storage_path = excluded.storage_path,
        original_file_name = excluded.original_file_name,
        mime_type = excluded.mime_type,
        file_size = excluded.file_size,
        sort_order = excluded.sort_order,
        created_at = excluded.created_at
    """.update.run.transact(transactor).as(attachment)

  override def findAttachmentsByMessageId(messageId: TourGroupMessageId): F[List[TourGroupMessageAttachment]] =
    sql"""
      select attachment_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
      from tour_group_message_attachments
      where message_id = ${messageId.value}
      order by sort_order, attachment_id
    """.query[(String, String, String, String, String, String, Long, Int, Instant)].to[List].transact(transactor).map(
      _.map { case (attachmentId, attachmentType, publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt) =>
        TourGroupMessageAttachment(TourGroupMessageAttachmentId(attachmentId), messageId, TourGroupMessageAttachmentType.valueOf(attachmentType), publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt)
      }
    )

  override def findAttachmentsByMessageIds(messageIds: List[TourGroupMessageId]): F[Map[TourGroupMessageId, List[TourGroupMessageAttachment]]] =
    if messageIds.isEmpty then Async[F].pure(Map.empty)
    else
      (fr"""
        select message_id, attachment_id, attachment_type, public_url, storage_path, original_file_name, mime_type, file_size, sort_order, created_at
        from tour_group_message_attachments
        where """ ++ Fragments.in(fr"message_id", NonEmptyList.fromListUnsafe(messageIds.map(_.value))))
        .query[(String, String, String, String, String, String, String, Long, Int, Instant)]
        .to[List]
        .transact(transactor)
        .map(
          _.map { case (messageId, attachmentId, attachmentType, publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt) =>
            TourGroupMessageId(messageId) -> TourGroupMessageAttachment(TourGroupMessageAttachmentId(attachmentId), TourGroupMessageId(messageId), TourGroupMessageAttachmentType.valueOf(attachmentType), publicUrl, storagePath, originalFileName, mimeType, fileSize, sortOrder, createdAt)
          }.groupBy(_._1).view.mapValues(_.map(_._2).sortBy(_.sortOrder)).toMap
        )

  override def saveMessageReaction(reaction: TourGroupMessageReaction): F[TourGroupMessageReaction] =
    sql"""
      insert into tour_group_message_reactions (
        reaction_id, message_id, user_id, reaction_type, created_at
      ) values (
        ${reaction.reactionId.value}, ${reaction.messageId.value}, ${reaction.userId.value}, ${reaction.reactionType}, ${reaction.createdAt}
      )
      on conflict (message_id, user_id, reaction_type) do update set
        created_at = excluded.created_at
    """.update.run.transact(transactor).as(reaction)

  override def deleteMessageReaction(messageId: TourGroupMessageId, userId: UserId, reactionType: String): F[Unit] =
    sql"""
      delete from tour_group_message_reactions
      where message_id = ${messageId.value}
        and user_id = ${userId.value}
        and reaction_type = ${reactionType}
    """.update.run.transact(transactor).void

  override def findReactionsByMessageIds(messageIds: List[TourGroupMessageId]): F[Map[TourGroupMessageId, List[TourGroupMessageReaction]]] =
    if messageIds.isEmpty then Async[F].pure(Map.empty)
    else
      (fr"""
        select message_id, reaction_id, user_id, reaction_type, created_at
        from tour_group_message_reactions
        where """ ++ Fragments.in(fr"message_id", NonEmptyList.fromListUnsafe(messageIds.map(_.value))))
        .query[(String, String, String, String, Instant)]
        .to[List]
        .transact(transactor)
        .map(
          _.map { case (messageId, reactionId, userId, reactionType, createdAt) =>
            TourGroupMessageId(messageId) -> TourGroupMessageReaction(TourGroupMessageReactionId(reactionId), TourGroupMessageId(messageId), UserId(userId), reactionType, createdAt)
          }.groupBy(_._1).view.mapValues(_.map(_._2).sortBy(_.createdAt.toEpochMilli)).toMap
        )

  override def searchMessagesByGroupIdAndUserId(groupId: TourGroupId, userId: UserId, query: String): F[List[TourGroupMessage]] =
    sql"""
      select m.message_id, m.conversation_id, m.sender_user_id, m.message_type, m.content, m.reply_to_message_id, m.forwarded_from_message_id, m.status, m.created_at, m.updated_at, m.deleted_at, m.recalled_at
      from tour_group_messages m
      inner join tour_group_conversations c on c.conversation_id = m.conversation_id
      inner join tour_group_conversation_participants p on p.conversation_id = c.conversation_id
      where c.group_id = ${groupId.value}
        and p.user_id = ${userId.value}
        and p.status = ${TourGroupConversationParticipantStatus.Active.toString}
        and lower(m.content) like ${s"%${query.trim.toLowerCase}%"}
      order by m.created_at desc, m.message_id desc
    """.query[(String, String, String, String, String, Option[String], Option[String], String, Instant, Instant, Option[Instant], Option[Instant])].to[List].transact(transactor).map(
      _.map { case (messageId, conversationId, senderUserId, messageType, content, replyToMessageId, forwardedFromMessageId, status, createdAt, updatedAt, deletedAt, recalledAt) =>
        TourGroupMessage(TourGroupMessageId(messageId), TourGroupConversationId(conversationId), UserId(senderUserId), TourGroupMessageType.valueOf(messageType), content, replyToMessageId.map(TourGroupMessageId.apply), forwardedFromMessageId.map(TourGroupMessageId.apply), TourGroupMessageStatus.valueOf(status), createdAt, updatedAt, deletedAt, recalledAt)
      }
    )

  override def findGroupDetails(groupId: TourGroupId): F[Option[TourGroupDetails]] =
    findGroupById(groupId).flatMap {
      case None => Async[F].pure(None)
      case Some(group) =>
        for
          memberships <- findMembershipsByGroupId(groupId)
          membershipTravelers <- findMembershipTravelersByGroupId(groupId)
          planItems <- findPlanItemsByGroupId(groupId)
          planOptions <- findPlanOptionsByGroupId(groupId)
          selections <- findSelectionsByGroupId(groupId)
          selectionTravelers <- findSelectionTravelersByGroupId(groupId)
          links <- findSelectionOrderLinksByGroupId(groupId)
        yield Some(TourGroupDetails(group, memberships.toVector, membershipTravelers.toVector, planItems.toVector, planOptions.toVector, selections.toVector, selectionTravelers.toVector, links.toVector))
    }

  private def nextId[A](prefix: String, build: String => A): F[A] =
    Sync[F].delay(build(s"$prefix-${UUID.randomUUID().toString.take(12)}"))

  private def upsertGroup(group: TourGroup): ConnectionIO[Int] =
    sql"""
      update tour_groups
      set organizer_user_id = ${group.organizerUserId.value},
          title = ${group.title},
          description = ${group.description},
          destination = ${group.destination},
          start_date = ${group.startDate},
          end_date = ${group.endDate},
          capacity = ${group.capacity},
          status = ${group.status.toString},
          created_at = ${group.createdAt}
      where group_id = ${group.groupId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into tour_groups (
            group_id,
            organizer_user_id,
            title,
            description,
            destination,
            start_date,
            end_date,
            capacity,
            status,
            created_at
          ) values (
            ${group.groupId.value},
            ${group.organizerUserId.value},
            ${group.title},
            ${group.description},
            ${group.destination},
            ${group.startDate},
            ${group.endDate},
            ${group.capacity},
            ${group.status.toString},
            ${group.createdAt}
          )
        """.update.run
    }

  private def upsertMembership(membership: TourGroupMembership): ConnectionIO[Int] =
    sql"""
      update tour_group_memberships
      set group_id = ${membership.groupId.value},
          user_id = ${membership.userId.value},
          joined_at = ${membership.joinedAt},
          status = ${membership.status.toString}
      where membership_id = ${membership.membershipId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into tour_group_memberships (
            membership_id,
            group_id,
            user_id,
            joined_at,
            status
          ) values (
            ${membership.membershipId.value},
            ${membership.groupId.value},
            ${membership.userId.value},
            ${membership.joinedAt},
            ${membership.status.toString}
          )
        """.update.run
    }

  private def upsertMembershipTraveler(membershipTraveler: TourGroupMembershipTraveler): ConnectionIO[Int] =
    sql"""
      update tour_group_membership_travelers
      set membership_id = ${membershipTraveler.membershipId.value},
          traveler_id = ${membershipTraveler.travelerId.value},
          joined_at = ${membershipTraveler.joinedAt},
          status = ${membershipTraveler.status.toString}
      where membership_traveler_id = ${membershipTraveler.membershipTravelerId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into tour_group_membership_travelers (
            membership_traveler_id,
            membership_id,
            traveler_id,
            joined_at,
            status
          ) values (
            ${membershipTraveler.membershipTravelerId.value},
            ${membershipTraveler.membershipId.value},
            ${membershipTraveler.travelerId.value},
            ${membershipTraveler.joinedAt},
            ${membershipTraveler.status.toString}
          )
        """.update.run
    }

  private def upsertPlanItem(planItem: GroupPlanItem): ConnectionIO[Int] =
    sql"""
      update group_plan_items
      set group_id = ${planItem.groupId.value},
          item_type = ${planItem.itemType.toString},
          title = ${planItem.title},
          description = ${planItem.description},
          scheduled_at = ${planItem.scheduledAt},
          ends_at = ${planItem.endsAt},
          sequence_no = ${planItem.sequenceNo},
          status = ${planItem.status.toString}
      where plan_item_id = ${planItem.planItemId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into group_plan_items (
            plan_item_id,
            group_id,
            item_type,
            title,
            description,
            scheduled_at,
            ends_at,
            sequence_no,
            status
          ) values (
            ${planItem.planItemId.value},
            ${planItem.groupId.value},
            ${planItem.itemType.toString},
            ${planItem.title},
            ${planItem.description},
            ${planItem.scheduledAt},
            ${planItem.endsAt},
            ${planItem.sequenceNo},
            ${planItem.status.toString}
          )
        """.update.run
    }

  private def upsertPlanOption(planOption: GroupPlanOption): ConnectionIO[Int] =
    sql"""
      update group_plan_options
      set plan_item_id = ${planOption.planItemId.value},
          resource_type = ${planOption.resourceType.toString},
          resource_id = ${planOption.resourceId},
          resource_variant_code = ${planOption.resourceVariantCode},
          resource_context = ${planOption.resourceContext},
          label = ${planOption.label},
          description = ${planOption.description},
          default_quantity = ${planOption.defaultQuantity},
          status = ${planOption.status.toString}
      where option_id = ${planOption.optionId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into group_plan_options (
            option_id,
            plan_item_id,
            resource_type,
            resource_id,
            resource_variant_code,
            resource_context,
            label,
            description,
            default_quantity,
            status
          ) values (
            ${planOption.optionId.value},
            ${planOption.planItemId.value},
            ${planOption.resourceType.toString},
            ${planOption.resourceId},
            ${planOption.resourceVariantCode},
            ${planOption.resourceContext},
            ${planOption.label},
            ${planOption.description},
            ${planOption.defaultQuantity},
            ${planOption.status.toString}
          )
        """.update.run
    }

  private def upsertSelection(selection: GroupPlanSelection): ConnectionIO[Int] =
    sql"""
      update group_plan_selections
      set group_id = ${selection.groupId.value},
          plan_item_id = ${selection.planItemId.value},
          option_id = ${selection.optionId.value},
          membership_id = ${selection.membershipId.value},
          quantity = ${selection.quantity},
          status = ${selection.status.toString},
          created_at = ${selection.createdAt},
          confirmed_at = ${selection.confirmedAt},
          reviewed_by_organizer_user_id = ${selection.reviewedByOrganizerUserId.map(_.value)},
          review_note = ${selection.reviewNote}
      where selection_id = ${selection.selectionId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into group_plan_selections (
            selection_id,
            group_id,
            plan_item_id,
            option_id,
            membership_id,
            quantity,
            status,
            created_at,
            confirmed_at,
            reviewed_by_organizer_user_id,
            review_note
          ) values (
            ${selection.selectionId.value},
            ${selection.groupId.value},
            ${selection.planItemId.value},
            ${selection.optionId.value},
            ${selection.membershipId.value},
            ${selection.quantity},
            ${selection.status.toString},
            ${selection.createdAt},
            ${selection.confirmedAt},
            ${selection.reviewedByOrganizerUserId.map(_.value)},
            ${selection.reviewNote}
          )
        """.update.run
    }

  private def upsertSelectionTraveler(selectionTraveler: GroupPlanSelectionTraveler): ConnectionIO[Int] =
    sql"""
      update group_plan_selection_travelers
      set selection_id = ${selectionTraveler.selectionId.value},
          traveler_id = ${selectionTraveler.travelerId.value}
      where selection_traveler_id = ${selectionTraveler.selectionTravelerId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into group_plan_selection_travelers (
            selection_traveler_id,
            selection_id,
            traveler_id
          ) values (
            ${selectionTraveler.selectionTravelerId.value},
            ${selectionTraveler.selectionId.value},
            ${selectionTraveler.travelerId.value}
          )
        """.update.run
    }

  private def upsertSelectionOrderLink(link: GroupSelectionOrderLink): ConnectionIO[Int] =
    sql"""
      update group_selection_order_links
      set selection_id = ${link.selectionId.value},
          order_id = ${link.orderId.value},
          created_at = ${link.createdAt}
      where link_id = ${link.linkId.value}
    """.update.run.flatMap { updatedRows =>
      if updatedRows > 0 then updatedRows.pure[ConnectionIO]
      else
        sql"""
          insert into group_selection_order_links (
            link_id,
            selection_id,
            order_id,
            created_at
          ) values (
            ${link.linkId.value},
            ${link.selectionId.value},
            ${link.orderId.value},
            ${link.createdAt}
          )
        """.update.run
    }

object DoobieTourGroupRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieTourGroupRepository[F] =
    new DoobieTourGroupRepository[F](transactor)
