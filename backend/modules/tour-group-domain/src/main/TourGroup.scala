package com.typesafe.travel.tourgroup.domain

import cats.syntax.all.*
import com.typesafe.travel.shared.kernel.*

import java.time.{Instant, LocalDate}

enum TourGroupStatus:
  case Draft, Open, Closed, Cancelled

enum TourGroupMembershipStatus:
  case Pending, Active, Left, Removed

enum TourGroupMembershipTravelerStatus:
  case Active, Removed

enum GroupPlanItemType:
  case Flight, Hotel, Train, Attraction

enum GroupPlanItemStatus:
  case Draft, Open, Closed

enum GroupPlanOptionStatus:
  case Active, Inactive

enum GroupPlanOptionResourceType:
  case Flight, HotelRoomType, TrainJourneySeat, AttractionTicketType

enum GroupPlanSelectionStatus:
  case Draft, Submitted, OrganizerConfirmed, Rejected, ConvertedToOrder, Cancelled

enum TourGroupError(val message: String) extends DomainError:
  case GroupCapacityWasInvalid(capacity: Int)
      extends TourGroupError(s"Tour group capacity '$capacity' must be greater than zero")
  case GroupDateRangeWasInvalid(startDate: LocalDate, endDate: LocalDate)
      extends TourGroupError(s"Tour group start date '$startDate' must be before end date '$endDate'")
  case GroupWasNotFound(groupId: TourGroupId)
      extends TourGroupError(s"Tour group '${groupId.value}' was not found")
  case MembershipWasNotFound(membershipId: TourGroupMembershipId)
      extends TourGroupError(s"Tour group membership '${membershipId.value}' was not found")
  case ActiveMembershipAlreadyExists(groupId: TourGroupId, userId: UserId)
      extends TourGroupError(s"User '${userId.value}' already has an active membership in group '${groupId.value}'")
  case PlanItemWasNotFound(planItemId: GroupPlanItemId)
      extends TourGroupError(s"Group plan item '${planItemId.value}' was not found")
  case PlanOptionWasNotFound(optionId: GroupPlanOptionId)
      extends TourGroupError(s"Group plan option '${optionId.value}' was not found")
  case SelectionWasNotFound(selectionId: GroupPlanSelectionId)
      extends TourGroupError(s"Group selection '${selectionId.value}' was not found")
  case OrganizerScopeDidNotMatch(groupId: TourGroupId, actingUserId: UserId)
      extends TourGroupError(s"User '${actingUserId.value}' is not organizer of group '${groupId.value}'")
  case MembershipScopeDidNotMatch(membershipId: TourGroupMembershipId, actingUserId: UserId)
      extends TourGroupError(s"User '${actingUserId.value}' does not own membership '${membershipId.value}'")
  case MembershipTravelerDidNotBelongToUser(travelerId: TravelerId, actingUserId: UserId)
      extends TourGroupError(s"Traveler '${travelerId.value}' does not belong to user '${actingUserId.value}'")
  case MembershipTravelerAlreadyExists(membershipId: TourGroupMembershipId, travelerId: TravelerId)
      extends TourGroupError(s"Traveler '${travelerId.value}' is already part of membership '${membershipId.value}'")
  case GroupCapacityWasExceeded(groupId: TourGroupId, capacity: Int, requestedUsedCapacity: Int)
      extends TourGroupError(s"Tour group '${groupId.value}' capacity $capacity would be exceeded by used capacity $requestedUsedCapacity")
  case GroupWasNotOpen(groupId: TourGroupId, status: TourGroupStatus)
      extends TourGroupError(s"Tour group '${groupId.value}' is not open in status $status")
  case PlanOptionDidNotBelongToPlanItem(optionId: GroupPlanOptionId, planItemId: GroupPlanItemId)
      extends TourGroupError(s"Plan option '${optionId.value}' does not belong to plan item '${planItemId.value}'")
  case PlanOptionResourceTypeDidNotMatchPlanItem(planItemId: GroupPlanItemId, itemType: GroupPlanItemType, resourceType: GroupPlanOptionResourceType)
      extends TourGroupError(s"Plan item '${planItemId.value}' of type $itemType cannot use resource type $resourceType")
  case SelectionTravelerWasEmpty(planItemId: GroupPlanItemId)
      extends TourGroupError(s"Plan item '${planItemId.value}' requires at least one traveler")
  case SelectionTravelerWasNotInMembership(selectionId: GroupPlanSelectionId, travelerId: TravelerId)
      extends TourGroupError(s"Traveler '${travelerId.value}' is not part of selection '${selectionId.value}' membership")
  case SelectionQuantityWasInvalid(quantity: Int)
      extends TourGroupError(s"Selection quantity '$quantity' must be greater than zero")
  case SelectionWasNotSubmittable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError(s"Selection '${selectionId.value}' cannot be submitted from status $status")
  case SelectionWasNotConfirmable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError(s"Selection '${selectionId.value}' cannot be confirmed from status $status")
  case SelectionWasNotRejectable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError(s"Selection '${selectionId.value}' cannot be rejected from status $status")
  case SelectionWasNotPayable(selectionId: GroupPlanSelectionId, status: GroupPlanSelectionStatus)
      extends TourGroupError(s"Selection '${selectionId.value}' cannot be paid from status $status")
  case SelectionReviewNoteWasEmpty(selectionId: GroupPlanSelectionId)
      extends TourGroupError(s"Selection '${selectionId.value}' reject review note must not be empty")
  case SelectionQuantityDidNotMatchTravelerCount(selectionId: GroupPlanSelectionId, quantity: Int, travelerCount: Int)
      extends TourGroupError(s"Selection '${selectionId.value}' quantity $quantity must match traveler count $travelerCount")
  case PlanItemSequenceWasInvalid(sequenceNo: Int)
      extends TourGroupError(s"Group plan item sequence '$sequenceNo' must be greater than zero")
  case PlanItemTimeWindowWasInvalid(planItemId: GroupPlanItemId)
      extends TourGroupError(s"Plan item '${planItemId.value}' has an invalid time window")
  case SelectionWasAlreadyLinked(selectionId: GroupPlanSelectionId, orderId: OrderId)
      extends TourGroupError(s"Selection '${selectionId.value}' is already linked to order '${orderId.value}'")
  case RequiredFieldWasEmpty(fieldName: String)
      extends TourGroupError(s"Field '$fieldName' must not be empty")

final case class TourGroup(
    groupId: TourGroupId,
    organizerUserId: UserId,
    title: String,
    description: String,
    destination: String,
    startDate: LocalDate,
    endDate: LocalDate,
    capacity: Int,
    status: TourGroupStatus,
    createdAt: Instant
):
  def ensureOrganizer(actingUserId: UserId): Either[TourGroupError, TourGroup] =
    Either.cond(organizerUserId == actingUserId, this, TourGroupError.OrganizerScopeDidNotMatch(groupId, actingUserId))

  def ensureOpen: Either[TourGroupError, TourGroup] =
    Either.cond(status == TourGroupStatus.Open, this, TourGroupError.GroupWasNotOpen(groupId, status))

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

final case class TourGroupMembership(
    membershipId: TourGroupMembershipId,
    groupId: TourGroupId,
    userId: UserId,
    joinedAt: Instant,
    status: TourGroupMembershipStatus
):
  def ensureOwner(actingUserId: UserId): Either[TourGroupError, TourGroupMembership] =
    Either.cond(userId == actingUserId, this, TourGroupError.MembershipScopeDidNotMatch(membershipId, actingUserId))

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

final case class TourGroupMembershipTraveler(
    membershipTravelerId: TourGroupMembershipTravelerId,
    membershipId: TourGroupMembershipId,
    travelerId: TravelerId,
    joinedAt: Instant,
    status: TourGroupMembershipTravelerStatus
)

final case class GroupPlanItem(
    planItemId: GroupPlanItemId,
    groupId: TourGroupId,
    itemType: GroupPlanItemType,
    title: String,
    description: String,
    scheduledAt: Instant,
    endsAt: Option[Instant],
    sequenceNo: Int,
    status: GroupPlanItemStatus
)

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

final case class GroupPlanOption(
    optionId: GroupPlanOptionId,
    planItemId: GroupPlanItemId,
    resourceType: GroupPlanOptionResourceType,
    resourceId: String,
    resourceVariantCode: Option[String],
    resourceContext: Option[String],
    label: String,
    description: String,
    defaultQuantity: Int,
    status: GroupPlanOptionStatus
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

final case class GroupPlanSelection(
    selectionId: GroupPlanSelectionId,
    groupId: TourGroupId,
    planItemId: GroupPlanItemId,
    optionId: GroupPlanOptionId,
    membershipId: TourGroupMembershipId,
    quantity: Int,
    status: GroupPlanSelectionStatus,
    createdAt: Instant,
    confirmedAt: Option[Instant],
    reviewedByOrganizerUserId: Option[UserId],
    reviewNote: Option[String]
):
  def submit: Either[TourGroupError, GroupPlanSelection] =
    Either.cond(status == GroupPlanSelectionStatus.Draft, copy(status = GroupPlanSelectionStatus.Submitted), TourGroupError.SelectionWasNotSubmittable(selectionId, status))

  def confirm(organizerUserId: UserId, confirmedAt: Instant, reviewNote: Option[String]): Either[TourGroupError, GroupPlanSelection] =
    Either.cond(
      status == GroupPlanSelectionStatus.Submitted,
      copy(
        status = GroupPlanSelectionStatus.OrganizerConfirmed,
        confirmedAt = Some(confirmedAt),
        reviewedByOrganizerUserId = Some(organizerUserId),
        reviewNote = reviewNote.map(_.trim).filter(_.nonEmpty)
      ),
      TourGroupError.SelectionWasNotConfirmable(selectionId, status)
    )

  def reject(organizerUserId: UserId, rejectedAt: Instant, reviewNote: String): Either[TourGroupError, GroupPlanSelection] =
    val normalizedReviewNote = reviewNote.trim
    if normalizedReviewNote.isEmpty then Left(TourGroupError.SelectionReviewNoteWasEmpty(selectionId))
    else
      Either.cond(
        status == GroupPlanSelectionStatus.Submitted,
        copy(
          status = GroupPlanSelectionStatus.Rejected,
          confirmedAt = Some(rejectedAt),
          reviewedByOrganizerUserId = Some(organizerUserId),
          reviewNote = Some(normalizedReviewNote)
        ),
        TourGroupError.SelectionWasNotRejectable(selectionId, status)
      )

  def markConvertedToOrder: Either[TourGroupError, GroupPlanSelection] =
    Either.cond(
      status == GroupPlanSelectionStatus.OrganizerConfirmed,
      copy(status = GroupPlanSelectionStatus.ConvertedToOrder),
      TourGroupError.SelectionWasNotPayable(selectionId, status)
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

final case class GroupPlanSelectionTraveler(
    selectionTravelerId: GroupPlanSelectionTravelerId,
    selectionId: GroupPlanSelectionId,
    travelerId: TravelerId
)

final case class GroupSelectionOrderLink(
    linkId: GroupSelectionOrderLinkId,
    selectionId: GroupPlanSelectionId,
    orderId: OrderId,
    createdAt: Instant
)

final case class TourGroupDetails(
    group: TourGroup,
    memberships: Vector[TourGroupMembership],
    membershipTravelers: Vector[TourGroupMembershipTraveler],
    planItems: Vector[GroupPlanItem],
    planOptions: Vector[GroupPlanOption],
    selections: Vector[GroupPlanSelection],
    selectionTravelers: Vector[GroupPlanSelectionTraveler],
    selectionOrderLinks: Vector[GroupSelectionOrderLink]
):
  lazy val usedCapacity: Int =
    membershipTravelers.count(_.status == TourGroupMembershipTravelerStatus.Active)

  lazy val isFull: Boolean =
    usedCapacity >= group.capacity

  def membershipById(membershipId: TourGroupMembershipId): Either[TourGroupError, TourGroupMembership] =
    memberships.find(_.membershipId == membershipId).toRight(TourGroupError.MembershipWasNotFound(membershipId))

  def planItemById(planItemId: GroupPlanItemId): Either[TourGroupError, GroupPlanItem] =
    planItems.find(_.planItemId == planItemId).toRight(TourGroupError.PlanItemWasNotFound(planItemId))

  def planOptionById(optionId: GroupPlanOptionId): Either[TourGroupError, GroupPlanOption] =
    planOptions.find(_.optionId == optionId).toRight(TourGroupError.PlanOptionWasNotFound(optionId))

  def selectionById(selectionId: GroupPlanSelectionId): Either[TourGroupError, GroupPlanSelection] =
    selections.find(_.selectionId == selectionId).toRight(TourGroupError.SelectionWasNotFound(selectionId))

  def membershipTravelersFor(membershipId: TourGroupMembershipId): Vector[TourGroupMembershipTraveler] =
    membershipTravelers.filter(_.membershipId == membershipId)

  def selectionTravelersFor(selectionId: GroupPlanSelectionId): Vector[GroupPlanSelectionTraveler] =
    selectionTravelers.filter(_.selectionId == selectionId)

private def normalizeRequiredText(fieldName: String, value: String): Either[TourGroupError, String] =
  val normalized = value.trim
  Either.cond(normalized.nonEmpty, normalized, TourGroupError.RequiredFieldWasEmpty(fieldName))
