package com.typesafe.travel.api.memory

import cats.effect.kernel.Sync
import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap

final class InMemoryTourGroupRepository[F[_]: Sync] private (
    groupState: TrieMap[TourGroupId, TourGroup],
    membershipState: TrieMap[TourGroupMembershipId, TourGroupMembership],
    membershipTravelerState: TrieMap[TourGroupMembershipTravelerId, TourGroupMembershipTraveler],
    planItemState: TrieMap[GroupPlanItemId, GroupPlanItem],
    planOptionState: TrieMap[GroupPlanOptionId, GroupPlanOption],
    selectionState: TrieMap[GroupPlanSelectionId, GroupPlanSelection],
    selectionTravelerState: TrieMap[GroupPlanSelectionTravelerId, GroupPlanSelectionTraveler],
    selectionOrderLinkState: TrieMap[GroupSelectionOrderLinkId, GroupSelectionOrderLink],
    sequence: AtomicLong
) extends TourGroupRepository[F]:

  override def nextGroupId: F[TourGroupId] = nextId("group", TourGroupId.apply)
  override def nextMembershipId: F[TourGroupMembershipId] = nextId("membership", TourGroupMembershipId.apply)
  override def nextMembershipTravelerId: F[TourGroupMembershipTravelerId] = nextId("membership-traveler", TourGroupMembershipTravelerId.apply)
  override def nextPlanItemId: F[GroupPlanItemId] = nextId("plan-item", GroupPlanItemId.apply)
  override def nextPlanOptionId: F[GroupPlanOptionId] = nextId("plan-option", GroupPlanOptionId.apply)
  override def nextSelectionId: F[GroupPlanSelectionId] = nextId("selection", GroupPlanSelectionId.apply)
  override def nextSelectionTravelerId: F[GroupPlanSelectionTravelerId] = nextId("selection-traveler", GroupPlanSelectionTravelerId.apply)
  override def nextSelectionOrderLinkId: F[GroupSelectionOrderLinkId] = nextId("selection-link", GroupSelectionOrderLinkId.apply)

  override def saveGroup(group: TourGroup): F[TourGroup] = save(groupState, group.groupId, group)
  override def findGroupById(groupId: TourGroupId): F[Option[TourGroup]] = get(groupState, groupId)
  override def listGroups: F[List[TourGroup]] = Sync[F].delay(groupState.values.toList.sortBy(_.createdAt.toEpochMilli))

  override def saveMembership(membership: TourGroupMembership): F[TourGroupMembership] = save(membershipState, membership.membershipId, membership)
  override def findMembershipById(membershipId: TourGroupMembershipId): F[Option[TourGroupMembership]] = get(membershipState, membershipId)
  override def findMembershipsByGroupId(groupId: TourGroupId): F[List[TourGroupMembership]] =
    Sync[F].delay(membershipState.values.filter(_.groupId == groupId).toList.sortBy(_.joinedAt.toEpochMilli))
  override def findActiveMembershipByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[Option[TourGroupMembership]] =
    Sync[F].delay(membershipState.values.find(m => m.groupId == groupId && m.userId == userId && m.status == TourGroupMembershipStatus.Active))

  override def saveMembershipTraveler(membershipTraveler: TourGroupMembershipTraveler): F[TourGroupMembershipTraveler] =
    save(membershipTravelerState, membershipTraveler.membershipTravelerId, membershipTraveler)
  override def findMembershipTravelersByMembershipId(membershipId: TourGroupMembershipId): F[List[TourGroupMembershipTraveler]] =
    Sync[F].delay(membershipTravelerState.values.filter(_.membershipId == membershipId).toList.sortBy(_.joinedAt.toEpochMilli))
  override def findMembershipTravelersByGroupId(groupId: TourGroupId): F[List[TourGroupMembershipTraveler]] =
    findMembershipsByGroupId(groupId).flatMap { memberships =>
      val membershipIds = memberships.map(_.membershipId).toSet
      Sync[F].delay(membershipTravelerState.values.filter(mt => membershipIds.contains(mt.membershipId)).toList.sortBy(_.joinedAt.toEpochMilli))
    }

  override def savePlanItem(planItem: GroupPlanItem): F[GroupPlanItem] = save(planItemState, planItem.planItemId, planItem)
  override def findPlanItemById(planItemId: GroupPlanItemId): F[Option[GroupPlanItem]] = get(planItemState, planItemId)
  override def findPlanItemsByGroupId(groupId: TourGroupId): F[List[GroupPlanItem]] =
    Sync[F].delay(planItemState.values.filter(_.groupId == groupId).toList.sortBy(_.sequenceNo))

  override def savePlanOption(planOption: GroupPlanOption): F[GroupPlanOption] = save(planOptionState, planOption.optionId, planOption)
  override def findPlanOptionById(optionId: GroupPlanOptionId): F[Option[GroupPlanOption]] = get(planOptionState, optionId)
  override def findPlanOptionsByPlanItemId(planItemId: GroupPlanItemId): F[List[GroupPlanOption]] =
    Sync[F].delay(planOptionState.values.filter(_.planItemId == planItemId).toList.sortBy(_.label))
  override def findPlanOptionsByGroupId(groupId: TourGroupId): F[List[GroupPlanOption]] =
    findPlanItemsByGroupId(groupId).flatMap { planItems =>
      val planItemIds = planItems.map(_.planItemId).toSet
      Sync[F].delay(planOptionState.values.filter(option => planItemIds.contains(option.planItemId)).toList.sortBy(_.label))
    }

  override def saveSelection(selection: GroupPlanSelection): F[GroupPlanSelection] = save(selectionState, selection.selectionId, selection)
  override def findSelectionById(selectionId: GroupPlanSelectionId): F[Option[GroupPlanSelection]] = get(selectionState, selectionId)
  override def findSelectionsByGroupId(groupId: TourGroupId): F[List[GroupPlanSelection]] =
    Sync[F].delay(selectionState.values.filter(_.groupId == groupId).toList.sortBy(_.createdAt.toEpochMilli))

  override def saveSelectionTraveler(selectionTraveler: GroupPlanSelectionTraveler): F[GroupPlanSelectionTraveler] =
    save(selectionTravelerState, selectionTraveler.selectionTravelerId, selectionTraveler)
  override def findSelectionTravelersBySelectionId(selectionId: GroupPlanSelectionId): F[List[GroupPlanSelectionTraveler]] =
    Sync[F].delay(selectionTravelerState.values.filter(_.selectionId == selectionId).toList.sortBy(_.travelerId.value))
  override def findSelectionTravelersByGroupId(groupId: TourGroupId): F[List[GroupPlanSelectionTraveler]] =
    findSelectionsByGroupId(groupId).flatMap { selections =>
      val selectionIds = selections.map(_.selectionId).toSet
      Sync[F].delay(selectionTravelerState.values.filter(st => selectionIds.contains(st.selectionId)).toList.sortBy(_.travelerId.value))
    }

  override def saveSelectionOrderLink(link: GroupSelectionOrderLink): F[GroupSelectionOrderLink] = save(selectionOrderLinkState, link.linkId, link)
  override def findSelectionOrderLinkBySelectionId(selectionId: GroupPlanSelectionId): F[Option[GroupSelectionOrderLink]] =
    Sync[F].delay(selectionOrderLinkState.values.find(_.selectionId == selectionId))
  override def findSelectionOrderLinksByGroupId(groupId: TourGroupId): F[List[GroupSelectionOrderLink]] =
    findSelectionsByGroupId(groupId).flatMap { selections =>
      val selectionIds = selections.map(_.selectionId).toSet
      Sync[F].delay(selectionOrderLinkState.values.filter(link => selectionIds.contains(link.selectionId)).toList.sortBy(_.createdAt.toEpochMilli))
    }

  override def findGroupDetails(groupId: TourGroupId): F[Option[TourGroupDetails]] =
    findGroupById(groupId).flatMap {
      case None => Sync[F].pure(None)
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
    Sync[F].delay(build(s"$prefix-${sequence.incrementAndGet()}"))

  private def save[K, V](state: TrieMap[K, V], key: K, value: V): F[V] =
    Sync[F].delay {
      state.put(key, value)
      value
    }

  private def get[K, V](state: TrieMap[K, V], key: K): F[Option[V]] =
    Sync[F].delay(state.get(key))

object InMemoryTourGroupRepository:
  def create[F[_]: Sync]: InMemoryTourGroupRepository[F] =
    new InMemoryTourGroupRepository[F](
      TrieMap.empty,
      TrieMap.empty,
      TrieMap.empty,
      TrieMap.empty,
      TrieMap.empty,
      TrieMap.empty,
      TrieMap.empty,
      TrieMap.empty,
      AtomicLong(1000)
    )
