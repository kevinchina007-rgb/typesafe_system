package com.typesafe.travel.tourgroup.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}

def createTourGroup(
    groupId: TourGroupId,
    organizerUserId: UserId,
    title: String,
    description: String,
    destination: String,
    startDate: LocalDate,
    endDate: LocalDate,
    capacity: Int,
    createdAt: Instant
): Either[TourGroupError, TourGroup] =
  for
    _ <- Either.cond(capacity > 0, (), TourGroupError.GroupCapacityWasInvalid(capacity))
    _ <- Either.cond(startDate.isBefore(endDate), (), TourGroupError.GroupDateRangeWasInvalid(startDate, endDate))
    normalizedTitle <- normalizeRequiredText("tour-group-title", title)
    normalizedDescription <- normalizeRequiredText("tour-group-description", description)
    normalizedDestination <- normalizeRequiredText("tour-group-destination", destination)
  yield TourGroup(groupId, organizerUserId, normalizedTitle, normalizedDescription, normalizedDestination, startDate, endDate, capacity, TourGroupStatus.Open, createdAt)


def createOrganizerMembership(
    membershipId: TourGroupMembershipId,
    groupId: TourGroupId,
    organizerUserId: UserId,
    joinedAt: Instant
): TourGroupMembership =
  TourGroupMembership(membershipId, groupId, organizerUserId, joinedAt, TourGroupMembershipStatus.Active)


def createMemberMembership(
    membershipId: TourGroupMembershipId,
    groupId: TourGroupId,
    userId: UserId,
    joinedAt: Instant
): TourGroupMembership =
  TourGroupMembership(membershipId, groupId, userId, joinedAt, TourGroupMembershipStatus.Active)


def createGroupPlanItem(
    planItemId: GroupPlanItemId,
    groupId: TourGroupId,
    itemType: GroupPlanItemType,
    title: String,
    description: String,
    scheduledAt: Instant,
    endsAt: Option[Instant],
    sequenceNo: Int
): Either[TourGroupError, GroupPlanItem] =
  for
    normalizedTitle <- normalizeRequiredText("group-plan-item-title", title)
    normalizedDescription <- normalizeRequiredText("group-plan-item-description", description)
    _ <- Either.cond(sequenceNo > 0, (), TourGroupError.PlanItemSequenceWasInvalid(sequenceNo))
    _ <- Either.cond(endsAt.forall(_.isAfter(scheduledAt)), (), TourGroupError.PlanItemTimeWindowWasInvalid(planItemId))
  yield GroupPlanItem(planItemId, groupId, itemType, normalizedTitle, normalizedDescription, scheduledAt, endsAt, sequenceNo, GroupPlanItemStatus.Open)


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
): Either[TourGroupError, GroupPlanOption] =
  for
    normalizedResourceId <- normalizeRequiredText("group-plan-option-resource-id", resourceId)
    normalizedLabel <- normalizeRequiredText("group-plan-option-label", label)
    normalizedDescription <- normalizeRequiredText("group-plan-option-description", description)
    _ <- Either.cond(defaultQuantity > 0, (), TourGroupError.SelectionQuantityWasInvalid(defaultQuantity))
  yield GroupPlanOption(
    optionId,
    planItemId,
    resourceType,
    normalizedResourceId,
    resourceVariantCode.map(_.trim).filter(_.nonEmpty),
    resourceContext.map(_.trim).filter(_.nonEmpty),
    normalizedLabel,
    normalizedDescription,
    defaultQuantity,
    GroupPlanOptionStatus.Active
  )


def createGroupPlanSelection(
    selectionId: GroupPlanSelectionId,
    groupId: TourGroupId,
    planItemId: GroupPlanItemId,
    optionId: GroupPlanOptionId,
    membershipId: TourGroupMembershipId,
    quantity: Int,
    createdAt: Instant
): Either[TourGroupError, GroupPlanSelection] =
  Either.cond(quantity > 0, (), TourGroupError.SelectionQuantityWasInvalid(quantity)).map { _ =>
    GroupPlanSelection(selectionId, groupId, planItemId, optionId, membershipId, quantity, GroupPlanSelectionStatus.Draft, createdAt, None, None, None)
  }


private def normalizeRequiredText(fieldName: String, value: String): Either[TourGroupError, String] =
  val normalized = value.trim
  Either.cond(normalized.nonEmpty, normalized, TourGroupError.RequiredFieldWasEmpty(fieldName))
