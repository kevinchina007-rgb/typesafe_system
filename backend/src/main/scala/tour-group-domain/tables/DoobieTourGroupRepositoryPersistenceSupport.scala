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

trait DoobieTourGroupRepositoryPersistenceSupport[F[_]: Async]:
  self: DoobieTourGroupRepository[F] =>
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

  protected def nextId[A](prefix: String, build: String => A): F[A] =
    Sync[F].delay(build(s"$prefix-${UUID.randomUUID().toString.take(12)}"))

  protected def upsertGroup(group: TourGroup): ConnectionIO[Int] =
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

  protected def upsertMembership(membership: TourGroupMembership): ConnectionIO[Int] =
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

  protected def upsertMembershipTraveler(membershipTraveler: TourGroupMembershipTraveler): ConnectionIO[Int] =
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

  protected def upsertPlanItem(planItem: GroupPlanItem): ConnectionIO[Int] =
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

  protected def upsertPlanOption(planOption: GroupPlanOption): ConnectionIO[Int] =
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

  protected def upsertSelection(selection: GroupPlanSelection): ConnectionIO[Int] =
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

  protected def upsertSelectionTraveler(selectionTraveler: GroupPlanSelectionTraveler): ConnectionIO[Int] =
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

  protected def upsertSelectionOrderLink(link: GroupSelectionOrderLink): ConnectionIO[Int] =
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
