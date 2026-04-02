package com.typesafe.travel.persistence.tourgroup

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all.*
import com.typesafe.travel.persistence.codecs.DatabaseCodecs.given
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
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
    (
      sql"""
        merge into tour_group_memberships key(membership_id)
        values(
          ${membership.membershipId.value},
          ${membership.groupId.value},
          ${membership.userId.value},
          ${membership.joinedAt},
          ${membership.status.toString}
        )
      """.update.run
    ).transact(transactor).as(membership)

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
    (
      sql"""
        merge into tour_group_membership_travelers key(membership_traveler_id)
        values(
          ${membershipTraveler.membershipTravelerId.value},
          ${membershipTraveler.membershipId.value},
          ${membershipTraveler.travelerId.value},
          ${membershipTraveler.joinedAt},
          ${membershipTraveler.status.toString}
        )
      """.update.run
    ).transact(transactor).as(membershipTraveler)

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
    (
      sql"""
        merge into group_plan_items key(plan_item_id)
        values(
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
    ).transact(transactor).as(planItem)

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
    (
      sql"""
        merge into group_plan_options key(option_id)
        values(
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
    ).transact(transactor).as(planOption)

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
    (
      sql"""
        merge into group_plan_selections key(selection_id)
        values(
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
    ).transact(transactor).as(selection)

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
    (
      sql"""
        merge into group_plan_selection_travelers key(selection_traveler_id)
        values(
          ${selectionTraveler.selectionTravelerId.value},
          ${selectionTraveler.selectionId.value},
          ${selectionTraveler.travelerId.value}
        )
      """.update.run
    ).transact(transactor).as(selectionTraveler)

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
    (
      sql"""
        merge into group_selection_order_links key(link_id)
        values(
          ${link.linkId.value},
          ${link.selectionId.value},
          ${link.orderId.value},
          ${link.createdAt}
        )
      """.update.run
    ).transact(transactor).as(link)

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
      merge into tour_groups key(group_id)
      values(
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

object DoobieTourGroupRepository:
  def apply[F[_]: Async](transactor: Transactor[F]): DoobieTourGroupRepository[F] =
    new DoobieTourGroupRepository[F](transactor)
