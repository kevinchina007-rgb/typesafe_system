package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum GroupPlanItemType:
  case Flight, Hotel, Train, Attraction

object GroupPlanItemType:
  val all: Vector[GroupPlanItemType] =
    Vector(GroupPlanItemType.Flight, GroupPlanItemType.Hotel, GroupPlanItemType.Train, GroupPlanItemType.Attraction)

  def fromText(value: String): GroupPlanItemType =
    value.trim match
      case "Flight"     => GroupPlanItemType.Flight
      case "Hotel"      => GroupPlanItemType.Hotel
      case "Train"      => GroupPlanItemType.Train
      case "Attraction" => GroupPlanItemType.Attraction
      case other        => throw new IllegalArgumentException(s"Unknown group plan item type: $other")

enum GroupPlanItemStatus:
  case Draft, Open, Closed

object GroupPlanItemStatus:
  val all: Vector[GroupPlanItemStatus] =
    Vector(GroupPlanItemStatus.Draft, GroupPlanItemStatus.Open, GroupPlanItemStatus.Closed)

  def fromText(value: String): GroupPlanItemStatus =
    value.trim match
      case "Draft"  => GroupPlanItemStatus.Draft
      case "Open"   => GroupPlanItemStatus.Open
      case "Closed" => GroupPlanItemStatus.Closed
      case other    => throw new IllegalArgumentException(s"Unknown group plan item status: $other")

enum GroupPlanOptionStatus:
  case Active, Inactive

object GroupPlanOptionStatus:
  val all: Vector[GroupPlanOptionStatus] =
    Vector(GroupPlanOptionStatus.Active, GroupPlanOptionStatus.Inactive)

  def fromText(value: String): GroupPlanOptionStatus =
    value.trim match
      case "Active"   => GroupPlanOptionStatus.Active
      case "Inactive" => GroupPlanOptionStatus.Inactive
      case other      => throw new IllegalArgumentException(s"Unknown group plan option status: $other")

enum GroupPlanOptionResourceType:
  case Flight, HotelRoomType, TrainJourneySeat, AttractionTicketType

object GroupPlanOptionResourceType:
  val all: Vector[GroupPlanOptionResourceType] =
    Vector(
      GroupPlanOptionResourceType.Flight,
      GroupPlanOptionResourceType.HotelRoomType,
      GroupPlanOptionResourceType.TrainJourneySeat,
      GroupPlanOptionResourceType.AttractionTicketType
    )

  def fromText(value: String): GroupPlanOptionResourceType =
    value.trim match
      case "Flight"             => GroupPlanOptionResourceType.Flight
      case "HotelRoomType"      => GroupPlanOptionResourceType.HotelRoomType
      case "TrainJourneySeat"   => GroupPlanOptionResourceType.TrainJourneySeat
      case "AttractionTicketType" => GroupPlanOptionResourceType.AttractionTicketType
      case other                => throw new IllegalArgumentException(s"Unknown group plan option resource type: $other")

enum GroupPlanSelectionStatus:
  case Draft, Submitted, OrganizerConfirmed, Rejected, ConvertedToOrder, Cancelled

object GroupPlanSelectionStatus:
  val all: Vector[GroupPlanSelectionStatus] =
    Vector(
      GroupPlanSelectionStatus.Draft,
      GroupPlanSelectionStatus.Submitted,
      GroupPlanSelectionStatus.OrganizerConfirmed,
      GroupPlanSelectionStatus.Rejected,
      GroupPlanSelectionStatus.ConvertedToOrder,
      GroupPlanSelectionStatus.Cancelled
    )

  def fromText(value: String): GroupPlanSelectionStatus =
    value.trim match
      case "Draft"              => GroupPlanSelectionStatus.Draft
      case "Submitted"          => GroupPlanSelectionStatus.Submitted
      case "OrganizerConfirmed" => GroupPlanSelectionStatus.OrganizerConfirmed
      case "Rejected"           => GroupPlanSelectionStatus.Rejected
      case "ConvertedToOrder"   => GroupPlanSelectionStatus.ConvertedToOrder
      case "Cancelled"          => GroupPlanSelectionStatus.Cancelled
      case other                => throw new IllegalArgumentException(s"Unknown group plan selection status: $other")

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
    normalizedTitle <- normalizeRequiredGroupPlanText("group-plan-item-title", title)
    normalizedDescription <- normalizeRequiredGroupPlanText("group-plan-item-description", description)
    _ <- Either.cond(sequenceNo > 0, (), TourGroupError.PlanItemSequenceWasInvalid(sequenceNo))
    _ <- Either.cond(endsAt.forall(_.isAfter(scheduledAt)), (), TourGroupError.PlanItemTimeWindowWasInvalid(planItemId))
  yield GroupPlanItem(
    planItemId,
    groupId,
    itemType,
    normalizedTitle,
    normalizedDescription,
    scheduledAt,
    endsAt,
    sequenceNo,
    GroupPlanItemStatus.Open
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
    normalizedResourceId <- normalizeRequiredGroupPlanText("group-plan-option-resource-id", resourceId)
    normalizedLabel <- normalizeRequiredGroupPlanText("group-plan-option-label", label)
    normalizedDescription <- normalizeRequiredGroupPlanText("group-plan-option-description", description)
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
      None
    )
  }

private def normalizeRequiredGroupPlanText(fieldName: String, value: String): Either[TourGroupError, String] =
  val normalized = value.trim
  Either.cond(normalized.nonEmpty, normalized, TourGroupError.RequiredFieldWasEmpty(fieldName))

