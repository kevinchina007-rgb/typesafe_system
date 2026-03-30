package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

trait TourGroupRepository[F[_]]:
  def nextGroupId: F[TourGroupId]
  def nextMembershipId: F[TourGroupMembershipId]
  def nextMembershipTravelerId: F[TourGroupMembershipTravelerId]
  def nextPlanItemId: F[GroupPlanItemId]
  def nextPlanOptionId: F[GroupPlanOptionId]
  def nextSelectionId: F[GroupPlanSelectionId]
  def nextSelectionTravelerId: F[GroupPlanSelectionTravelerId]
  def nextSelectionOrderLinkId: F[GroupSelectionOrderLinkId]

  def saveGroup(group: TourGroup): F[TourGroup]
  def findGroupById(groupId: TourGroupId): F[Option[TourGroup]]
  def listGroups: F[List[TourGroup]]

  def saveMembership(membership: TourGroupMembership): F[TourGroupMembership]
  def findMembershipById(membershipId: TourGroupMembershipId): F[Option[TourGroupMembership]]
  def findMembershipsByGroupId(groupId: TourGroupId): F[List[TourGroupMembership]]
  def findActiveMembershipByGroupIdAndUserId(groupId: TourGroupId, userId: UserId): F[Option[TourGroupMembership]]

  def saveMembershipTraveler(membershipTraveler: TourGroupMembershipTraveler): F[TourGroupMembershipTraveler]
  def findMembershipTravelersByMembershipId(membershipId: TourGroupMembershipId): F[List[TourGroupMembershipTraveler]]
  def findMembershipTravelersByGroupId(groupId: TourGroupId): F[List[TourGroupMembershipTraveler]]

  def savePlanItem(planItem: GroupPlanItem): F[GroupPlanItem]
  def findPlanItemById(planItemId: GroupPlanItemId): F[Option[GroupPlanItem]]
  def findPlanItemsByGroupId(groupId: TourGroupId): F[List[GroupPlanItem]]

  def savePlanOption(planOption: GroupPlanOption): F[GroupPlanOption]
  def findPlanOptionById(optionId: GroupPlanOptionId): F[Option[GroupPlanOption]]
  def findPlanOptionsByPlanItemId(planItemId: GroupPlanItemId): F[List[GroupPlanOption]]
  def findPlanOptionsByGroupId(groupId: TourGroupId): F[List[GroupPlanOption]]

  def saveSelection(selection: GroupPlanSelection): F[GroupPlanSelection]
  def findSelectionById(selectionId: GroupPlanSelectionId): F[Option[GroupPlanSelection]]
  def findSelectionsByGroupId(groupId: TourGroupId): F[List[GroupPlanSelection]]

  def saveSelectionTraveler(selectionTraveler: GroupPlanSelectionTraveler): F[GroupPlanSelectionTraveler]
  def findSelectionTravelersBySelectionId(selectionId: GroupPlanSelectionId): F[List[GroupPlanSelectionTraveler]]
  def findSelectionTravelersByGroupId(groupId: TourGroupId): F[List[GroupPlanSelectionTraveler]]

  def saveSelectionOrderLink(link: GroupSelectionOrderLink): F[GroupSelectionOrderLink]
  def findSelectionOrderLinkBySelectionId(selectionId: GroupPlanSelectionId): F[Option[GroupSelectionOrderLink]]
  def findSelectionOrderLinksByGroupId(groupId: TourGroupId): F[List[GroupSelectionOrderLink]]

  def findGroupDetails(groupId: TourGroupId): F[Option[TourGroupDetails]]
