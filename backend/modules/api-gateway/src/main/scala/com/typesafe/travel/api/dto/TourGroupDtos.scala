package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.TourGroupDetailsView
import com.typesafe.travel.order.domain.Order
import com.typesafe.travel.tourgroup.domain.*

final case class CreateTourGroupRequestDto(
    organizerUserId: String,
    title: String,
    description: String,
    destination: String,
    startDate: String,
    endDate: String,
    capacity: Int
)

final case class JoinTourGroupRequestDto(
    userId: String
)

final case class AddMembershipTravelerRequestDto(
    userId: String,
    travelerId: String
)

final case class CreateGroupPlanItemRequestDto(
    organizerUserId: String,
    itemType: String,
    title: String,
    description: String,
    scheduledAt: String,
    endsAt: Option[String],
    sequenceNo: Int
)

final case class CreateGroupPlanOptionRequestDto(
    organizerUserId: String,
    resourceType: String,
    resourceId: String,
    resourceVariantCode: Option[String],
    resourceContext: Option[String],
    label: String,
    description: String,
    defaultQuantity: Int
)

final case class CreateGroupPlanSelectionRequestDto(
    userId: String,
    optionId: String,
    quantity: Int,
    travelerIds: List[String]
)

final case class SubmitGroupPlanSelectionRequestDto(
    userId: String
)

final case class ReviewGroupPlanSelectionRequestDto(
    organizerUserId: String,
    reviewNote: Option[String]
)

final case class RejectGroupPlanSelectionRequestDto(
    organizerUserId: String,
    reviewNote: String
)

final case class PayGroupPlanSelectionRequestDto(
    userId: String,
    paymentMethod: String
)

final case class TourGroupSummaryResponseDto(
    groupId: String,
    organizerUserId: String,
    title: String,
    description: String,
    destination: String,
    startDate: String,
    endDate: String,
    capacity: Int,
    usedCapacity: Int,
    isFull: Boolean,
    status: String,
    createdAt: String
)

final case class TourGroupMembershipResponseDto(
    membershipId: String,
    userId: String,
    status: String,
    joinedAt: String
)

final case class TourGroupMembershipTravelerResponseDto(
    membershipTravelerId: String,
    membershipId: String,
    travelerId: String,
    status: String,
    joinedAt: String
)

final case class GroupPlanItemResponseDto(
    planItemId: String,
    itemType: String,
    title: String,
    description: String,
    scheduledAt: String,
    endsAt: Option[String],
    sequenceNo: Int,
    status: String
)

final case class GroupPlanOptionResponseDto(
    optionId: String,
    planItemId: String,
    resourceType: String,
    resourceId: String,
    resourceVariantCode: Option[String],
    resourceContext: Option[String],
    label: String,
    description: String,
    defaultQuantity: Int,
    status: String
)

final case class GroupPlanSelectionResponseDto(
    selectionId: String,
    groupId: String,
    planItemId: String,
    optionId: String,
    membershipId: String,
    quantity: Int,
    travelerIds: List[String],
    status: String,
    createdAt: String,
    confirmedAt: Option[String],
    reviewedByOrganizerUserId: Option[String],
    reviewNote: Option[String]
)

final case class GroupSelectionOrderLinkResponseDto(
    selectionId: String,
    orderId: String,
    createdAt: String
)

final case class TourGroupDetailsResponseDto(
    group: TourGroupSummaryResponseDto,
    memberships: List[TourGroupMembershipResponseDto],
    membershipTravelers: List[TourGroupMembershipTravelerResponseDto],
    planItems: List[GroupPlanItemResponseDto],
    planOptions: List[GroupPlanOptionResponseDto],
    selections: List[GroupPlanSelectionResponseDto],
    selectionOrderLinks: List[GroupSelectionOrderLinkResponseDto],
    bookings: List[OrderResponseDto]
)

final case class TourGroupListResponseDto(
    groups: List[TourGroupSummaryResponseDto]
)

object TourGroupSummaryResponseDto:
  def fromView(view: TourGroupDetailsView): TourGroupSummaryResponseDto =
    val group = view.details.group
    TourGroupSummaryResponseDto(
      groupId = group.groupId.value,
      organizerUserId = group.organizerUserId.value,
      title = group.title,
      description = group.description,
      destination = group.destination,
      startDate = group.startDate.toString,
      endDate = group.endDate.toString,
      capacity = group.capacity,
      usedCapacity = view.details.usedCapacity,
      isFull = view.details.isFull,
      status = group.status.toString,
      createdAt = group.createdAt.toString
    )

object TourGroupDetailsResponseDto:
  def fromView(view: TourGroupDetailsView): TourGroupDetailsResponseDto =
    TourGroupDetailsResponseDto(
      group = TourGroupSummaryResponseDto.fromView(view),
      memberships = view.details.memberships.toList.map(membership =>
        TourGroupMembershipResponseDto(
          membershipId = membership.membershipId.value,
          userId = membership.userId.value,
          status = membership.status.toString,
          joinedAt = membership.joinedAt.toString
        )
      ),
      membershipTravelers = view.details.membershipTravelers.toList.map(membershipTraveler =>
        TourGroupMembershipTravelerResponseDto(
          membershipTravelerId = membershipTraveler.membershipTravelerId.value,
          membershipId = membershipTraveler.membershipId.value,
          travelerId = membershipTraveler.travelerId.value,
          status = membershipTraveler.status.toString,
          joinedAt = membershipTraveler.joinedAt.toString
        )
      ),
      planItems = view.details.planItems.toList.sortBy(_.sequenceNo).map(planItem =>
        GroupPlanItemResponseDto(
          planItemId = planItem.planItemId.value,
          itemType = planItem.itemType.toString,
          title = planItem.title,
          description = planItem.description,
          scheduledAt = planItem.scheduledAt.toString,
          endsAt = planItem.endsAt.map(_.toString),
          sequenceNo = planItem.sequenceNo,
          status = planItem.status.toString
        )
      ),
      planOptions = view.details.planOptions.toList.map(planOption =>
        GroupPlanOptionResponseDto(
          optionId = planOption.optionId.value,
          planItemId = planOption.planItemId.value,
          resourceType = planOption.resourceType.toString,
          resourceId = planOption.resourceId,
          resourceVariantCode = planOption.resourceVariantCode,
          resourceContext = planOption.resourceContext,
          label = planOption.label,
          description = planOption.description,
          defaultQuantity = planOption.defaultQuantity,
          status = planOption.status.toString
        )
      ),
      selections = view.details.selections.toList.map { selection =>
        GroupPlanSelectionResponseDto(
          selectionId = selection.selectionId.value,
          groupId = selection.groupId.value,
          planItemId = selection.planItemId.value,
          optionId = selection.optionId.value,
          membershipId = selection.membershipId.value,
          quantity = selection.quantity,
          travelerIds = view.details.selectionTravelersFor(selection.selectionId).map(_.travelerId.value).toList,
          status = selection.status.toString,
          createdAt = selection.createdAt.toString,
          confirmedAt = selection.confirmedAt.map(_.toString),
          reviewedByOrganizerUserId = selection.reviewedByOrganizerUserId.map(_.value),
          reviewNote = selection.reviewNote
        )
      },
      selectionOrderLinks = view.details.selectionOrderLinks.toList.map(link =>
        GroupSelectionOrderLinkResponseDto(
          selectionId = link.selectionId.value,
          orderId = link.orderId.value,
          createdAt = link.createdAt.toString
        )
      ),
      bookings = view.linkedOrders.values.toList.toList.sortBy(_.createdAt.toEpochMilli).reverse.map(OrderResponseDto.fromDomain(_))
    )

object TourGroupDtoMappers:
  def toPlanItemType(value: String): GroupPlanItemType =
    value.trim.toLowerCase match
      case "flight"     => GroupPlanItemType.Flight
      case "hotel"      => GroupPlanItemType.Hotel
      case "train"      => GroupPlanItemType.Train
      case "attraction" => GroupPlanItemType.Attraction
      case _            => GroupPlanItemType.Attraction

  def toResourceType(value: String): GroupPlanOptionResourceType =
    value.trim.toLowerCase match
      case "flight"                => GroupPlanOptionResourceType.Flight
      case "hotelroomtype" | "hotel_room_type" | "hotel" => GroupPlanOptionResourceType.HotelRoomType
      case "trainjourneyseat" | "train_journey_seat" | "train" => GroupPlanOptionResourceType.TrainJourneySeat
      case "attractiontickettype" | "attraction_ticket_type" | "attraction" => GroupPlanOptionResourceType.AttractionTicketType
      case _ => GroupPlanOptionResourceType.AttractionTicketType
