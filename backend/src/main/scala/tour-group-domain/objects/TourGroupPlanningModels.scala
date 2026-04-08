package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

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

