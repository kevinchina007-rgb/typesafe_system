package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import java.time.{Instant, LocalDate}

def ensureTourGroupOrganizer(tourGroup: TourGroup, actingUserId: UserId): Either[TourGroupError, TourGroup] =
  Either.cond(tourGroup.organizerUserId == actingUserId, tourGroup, TourGroupError.OrganizerScopeDidNotMatch(tourGroup.groupId, actingUserId))

def ensureTourGroupOpen(tourGroup: TourGroup): Either[TourGroupError, TourGroup] =
  Either.cond(tourGroup.status == TourGroupStatus.Open, tourGroup, TourGroupError.GroupWasNotOpen(tourGroup.groupId, tourGroup.status))

def tourGroupUsedCapacity(details: TourGroupDetails): Int =
  details.membershipTravelers.count(_.status == TourGroupMembershipTravelerStatus.Active)

def tourGroupIsFull(details: TourGroupDetails): Boolean =
  tourGroupUsedCapacity(details) >= details.group.capacity

def tourGroupMembershipById(details: TourGroupDetails, membershipId: TourGroupMembershipId): Either[TourGroupError, TourGroupMembership] =
  details.memberships.find(_.membershipId == membershipId).toRight(TourGroupError.MembershipWasNotFound(membershipId))

def tourGroupPlanItemById(details: TourGroupDetails, planItemId: GroupPlanItemId): Either[TourGroupError, GroupPlanItem] =
  details.planItems.find(_.planItemId == planItemId).toRight(TourGroupError.PlanItemWasNotFound(planItemId))

def tourGroupPlanOptionById(details: TourGroupDetails, optionId: GroupPlanOptionId): Either[TourGroupError, GroupPlanOption] =
  details.planOptions.find(_.optionId == optionId).toRight(TourGroupError.PlanOptionWasNotFound(optionId))

def tourGroupSelectionById(details: TourGroupDetails, selectionId: GroupPlanSelectionId): Either[TourGroupError, GroupPlanSelection] =
  details.selections.find(_.selectionId == selectionId).toRight(TourGroupError.SelectionWasNotFound(selectionId))

def tourGroupMembershipTravelersFor(details: TourGroupDetails, membershipId: TourGroupMembershipId): Vector[TourGroupMembershipTraveler] =
  details.membershipTravelers.filter(_.membershipId == membershipId)

def tourGroupSelectionTravelersFor(details: TourGroupDetails, selectionId: GroupPlanSelectionId): Vector[GroupPlanSelectionTraveler] =
  details.selectionTravelers.filter(_.selectionId == selectionId)

def ensureTourGroupMembershipOwner(membership: TourGroupMembership, actingUserId: UserId): Either[TourGroupError, TourGroupMembership] =
  Either.cond(membership.userId == actingUserId, membership, TourGroupError.MembershipScopeDidNotMatch(membership.membershipId, actingUserId))

def submitGroupPlanSelection(selection: GroupPlanSelection): Either[TourGroupError, GroupPlanSelection] =
  Either.cond(selection.status == GroupPlanSelectionStatus.Draft, selection.copy(status = GroupPlanSelectionStatus.Submitted), TourGroupError.SelectionWasNotSubmittable(selection.selectionId, selection.status))

def confirmGroupPlanSelection(selection: GroupPlanSelection, organizerUserId: UserId, confirmedAt: Instant, reviewNote: Option[String]): Either[TourGroupError, GroupPlanSelection] =
  Either.cond(
    selection.status == GroupPlanSelectionStatus.Submitted,
    selection.copy(
      status = GroupPlanSelectionStatus.OrganizerConfirmed,
      confirmedAt = Some(confirmedAt),
      reviewedByOrganizerUserId = Some(organizerUserId),
      reviewNote = reviewNote.map(_.trim).filter(_.nonEmpty)
    ),
    TourGroupError.SelectionWasNotConfirmable(selection.selectionId, selection.status)
  )

def rejectGroupPlanSelection(selection: GroupPlanSelection, organizerUserId: UserId, rejectedAt: Instant, reviewNote: String): Either[TourGroupError, GroupPlanSelection] =
  val normalizedReviewNote = reviewNote.trim
  if normalizedReviewNote.isEmpty then Left(TourGroupError.SelectionReviewNoteWasEmpty(selection.selectionId))
  else
    Either.cond(
      selection.status == GroupPlanSelectionStatus.Submitted,
      selection.copy(status = GroupPlanSelectionStatus.Rejected, confirmedAt = Some(rejectedAt), reviewedByOrganizerUserId = Some(organizerUserId), reviewNote = Some(normalizedReviewNote)),
      TourGroupError.SelectionWasNotRejectable(selection.selectionId, selection.status)
    )

def markGroupPlanSelectionConvertedToOrder(selection: GroupPlanSelection): Either[TourGroupError, GroupPlanSelection] =
  Either.cond(selection.status == GroupPlanSelectionStatus.OrganizerConfirmed, selection.copy(status = GroupPlanSelectionStatus.ConvertedToOrder), TourGroupError.SelectionWasNotPayable(selection.selectionId, selection.status))

def createTourGroup(
    groupId: TourGroupId,
    organizerUserId: UserId,
    title: String,
    description: String,
    destination: String,
    startDate: LocalDate,
    endDate: LocalDate,
    capacity: Int,
    coverImageUrl: Option[String],
    tags: Vector[String],
    createdAt: Instant
): Either[TourGroupError, TourGroup] =
  for
    _ <- Either.cond(capacity > 0, (), TourGroupError.GroupCapacityWasInvalid(capacity))
    _ <- Either.cond(startDate.isBefore(endDate), (), TourGroupError.GroupDateRangeWasInvalid(startDate, endDate))
    normalizedTitle <- normalizeRequiredTourGroupText("tour-group-title", title)
    normalizedDescription <- normalizeRequiredTourGroupText("tour-group-description", description)
    normalizedDestination <- normalizeRequiredTourGroupText("tour-group-destination", destination)
  yield TourGroup(
    groupId,
    organizerUserId,
    normalizedTitle,
    normalizedDescription,
    normalizedDestination,
    startDate,
    endDate,
    capacity,
    coverImageUrl.map(_.trim).filter(_.nonEmpty),
    tags.map(_.trim).filter(_.nonEmpty).distinct,
    TourGroupStatus.Open,
    createdAt
  )

def createOrganizerMembership(membershipId: TourGroupMembershipId, groupId: TourGroupId, organizerUserId: UserId, joinedAt: Instant): TourGroupMembership =
  TourGroupMembership(membershipId, groupId, organizerUserId, joinedAt, TourGroupMembershipStatus.Active)

def createMemberMembership(membershipId: TourGroupMembershipId, groupId: TourGroupId, userId: UserId, joinedAt: Instant): TourGroupMembership =
  TourGroupMembership(membershipId, groupId, userId, joinedAt, TourGroupMembershipStatus.Active)

def createGroupPlanItem(planItemId: GroupPlanItemId, groupId: TourGroupId, itemType: GroupPlanItemType, title: String, description: String, scheduledAt: Instant, endsAt: Option[Instant], sequenceNo: Int): Either[TourGroupError, GroupPlanItem] =
  for
    normalizedTitle <- normalizeRequiredGroupPlanText("group-plan-item-title", title)
    normalizedDescription <- normalizeRequiredGroupPlanText("group-plan-item-description", description)
    _ <- Either.cond(sequenceNo > 0, (), TourGroupError.PlanItemSequenceWasInvalid(sequenceNo))
    _ <- Either.cond(endsAt.forall(_.isAfter(scheduledAt)), (), TourGroupError.PlanItemTimeWindowWasInvalid(planItemId))
  yield GroupPlanItem(planItemId, groupId, itemType, normalizedTitle, normalizedDescription, scheduledAt, endsAt, sequenceNo, GroupPlanItemStatus.Open)

def createGroupPlanOption(optionId: GroupPlanOptionId, planItemId: GroupPlanItemId, resourceType: GroupPlanOptionResourceType, resourceId: String, resourceVariantCode: Option[String], resourceContext: Option[String], label: String, description: String, defaultQuantity: Int): Either[TourGroupError, GroupPlanOption] =
  for
    normalizedResourceId <- normalizeRequiredGroupPlanText("group-plan-option-resource-id", resourceId)
    normalizedLabel <- normalizeRequiredGroupPlanText("group-plan-option-label", label)
    normalizedDescription <- normalizeRequiredGroupPlanText("group-plan-option-description", description)
    _ <- Either.cond(defaultQuantity > 0, (), TourGroupError.SelectionQuantityWasInvalid(defaultQuantity))
  yield GroupPlanOption(optionId, planItemId, resourceType, normalizedResourceId, resourceVariantCode.map(_.trim).filter(_.nonEmpty), resourceContext.map(_.trim).filter(_.nonEmpty), normalizedLabel, normalizedDescription, defaultQuantity, GroupPlanOptionStatus.Active)

def createGroupPlanSelection(
    selectionId: GroupPlanSelectionId,
    groupId: TourGroupId,
    planItemId: GroupPlanItemId,
    optionId: GroupPlanOptionId,
    membershipId: TourGroupMembershipId,
    quantity: Int,
    travelerIds: Vector[TravelerId],
    createdAt: Instant
): Either[TourGroupError, GroupPlanSelection] =
  Either.cond(quantity > 0, (), TourGroupError.SelectionQuantityWasInvalid(quantity)).map { _ =>
    GroupPlanSelection(
      selectionId,
      groupId,
      planItemId,
      optionId,
      membershipId,
      quantity,
      GroupPlanSelectionStatus.Draft,
      createdAt,
      None,
      None,
      None,
      travelerIds
    )
  }

private def normalizeRequiredTourGroupText(fieldName: String, value: String): Either[TourGroupError, String] =
  val normalized = value.trim
  Either.cond(normalized.nonEmpty, normalized, TourGroupError.RequiredFieldWasEmpty(fieldName))

private def normalizeRequiredGroupPlanText(fieldName: String, value: String): Either[TourGroupError, String] =
  val normalized = value.trim
  Either.cond(normalized.nonEmpty, normalized, TourGroupError.RequiredFieldWasEmpty(fieldName))
