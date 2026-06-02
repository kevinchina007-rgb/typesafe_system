package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class CreateTourGroupPlannerRequest(
    organizerUserId: String,
    title: String,
    description: String,
    destination: String,
    startDate: String,
    endDate: String,
    capacity: Int,
    coverImageUrl: Option[String] = None,
    tags: List[String] = Nil
)
object CreateTourGroupPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupPlannerRequest] = deriveDecoder

final case class ListTourGroupsPlannerRequest()
object ListTourGroupsPlannerRequest:
  given sourceEncoder: Encoder[ListTourGroupsPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[ListTourGroupsPlannerRequest] = deriveDecoder

final case class TourGroupByIdPlannerRequest(groupId: String)
object TourGroupByIdPlannerRequest:
  given sourceEncoder: Encoder[TourGroupByIdPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupByIdPlannerRequest] = deriveDecoder

final case class JoinTourGroupPlannerRequest(groupId: String, userId: String)
object JoinTourGroupPlannerRequest:
  given sourceEncoder: Encoder[JoinTourGroupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[JoinTourGroupPlannerRequest] = deriveDecoder

final case class LeaveTourGroupPlannerRequest(groupId: String, userId: String)
object LeaveTourGroupPlannerRequest:
  given sourceEncoder: Encoder[LeaveTourGroupPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[LeaveTourGroupPlannerRequest] = deriveDecoder

final case class AddMembershipTravelerPlannerRequest(groupId: String, userId: String, travelerId: String)
object AddMembershipTravelerPlannerRequest:
  given sourceEncoder: Encoder[AddMembershipTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[AddMembershipTravelerPlannerRequest] = deriveDecoder

final case class KickTourGroupMemberPlannerRequest(groupId: String, organizerUserId: String, targetUserId: String)
object KickTourGroupMemberPlannerRequest:
  given sourceEncoder: Encoder[KickTourGroupMemberPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[KickTourGroupMemberPlannerRequest] = deriveDecoder

final case class BlacklistTourGroupMemberPlannerRequest(groupId: String, organizerUserId: String, targetUserId: String)
object BlacklistTourGroupMemberPlannerRequest:
  given sourceEncoder: Encoder[BlacklistTourGroupMemberPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[BlacklistTourGroupMemberPlannerRequest] = deriveDecoder

final case class TransferTourGroupLeaderPlannerRequest(groupId: String, organizerUserId: String, targetUserId: String)
object TransferTourGroupLeaderPlannerRequest:
  given sourceEncoder: Encoder[TransferTourGroupLeaderPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[TransferTourGroupLeaderPlannerRequest] = deriveDecoder

final case class CreateTourGroupPlanItemPlannerRequest(
    groupId: String,
    organizerUserId: String,
    itemType: String,
    title: String,
    description: String,
    scheduledAt: String,
    endsAt: Option[String] = None,
    sequenceNo: Int
)
object CreateTourGroupPlanItemPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupPlanItemPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupPlanItemPlannerRequest] = deriveDecoder

final case class CreateTourGroupPlanOptionPlannerRequest(
    groupId: String,
    organizerUserId: String,
    planItemId: String,
    resourceType: String,
    resourceId: String,
    resourceVariantCode: Option[String] = None,
    resourceContext: Option[String] = None,
    label: String,
    description: String,
    defaultQuantity: Int
)
object CreateTourGroupPlanOptionPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupPlanOptionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupPlanOptionPlannerRequest] = deriveDecoder

final case class CreateTourGroupSelectionPlannerRequest(
    groupId: String,
    userId: String,
    planItemId: String,
    optionId: String,
    quantity: Int,
    travelerIds: List[String]
)
object CreateTourGroupSelectionPlannerRequest:
  given sourceEncoder: Encoder[CreateTourGroupSelectionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[CreateTourGroupSelectionPlannerRequest] = deriveDecoder

final case class SubmitTourGroupSelectionPlannerRequest(userId: String, selectionId: String)
object SubmitTourGroupSelectionPlannerRequest:
  given sourceEncoder: Encoder[SubmitTourGroupSelectionPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[SubmitTourGroupSelectionPlannerRequest] = deriveDecoder

final case class TourGroupSummaryPlannerResponse(
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
    coverImageUrl: Option[String],
    tags: List[String],
    memberCount: Int,
    activeTravelerCount: Int,
    pendingSelectionCount: Int,
    confirmedSelectionCount: Int,
    convertedOrderCount: Int,
    status: String,
    createdAt: String
)
object TourGroupSummaryPlannerResponse:
  given sourceEncoder: Encoder[TourGroupSummaryPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupSummaryPlannerResponse] = deriveDecoder

final case class TourGroupMembershipPlannerResponse(membershipId: String, userId: String, userDisplayName: String, status: String, joinedAt: String)
object TourGroupMembershipPlannerResponse:
  given sourceEncoder: Encoder[TourGroupMembershipPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMembershipPlannerResponse] = deriveDecoder

final case class TourGroupMembershipTravelerPlannerResponse(membershipTravelerId: String, membershipId: String, travelerId: String, status: String, joinedAt: String)
object TourGroupMembershipTravelerPlannerResponse:
  given sourceEncoder: Encoder[TourGroupMembershipTravelerPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMembershipTravelerPlannerResponse] = deriveDecoder

final case class TourGroupBlacklistPlannerResponse(
    blacklistId: String,
    groupId: String,
    userId: String,
    blacklistedByUserId: String,
    reason: String,
    createdAt: String
)
object TourGroupBlacklistPlannerResponse:
  given sourceEncoder: Encoder[TourGroupBlacklistPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupBlacklistPlannerResponse] = deriveDecoder

final case class TourGroupDetailsPlannerResponse(
    group: TourGroupSummaryPlannerResponse,
    memberships: List[TourGroupMembershipPlannerResponse],
    membershipTravelers: List[TourGroupMembershipTravelerPlannerResponse],
    planItems: List[GroupPlanItem],
    planOptions: List[GroupPlanOption],
    selections: List[GroupPlanSelection],
    selectionOrderLinks: List[GroupSelectionOrderLink],
    blacklists: List[TourGroupBlacklistPlannerResponse]
)
object TourGroupDetailsPlannerResponse:
  given sourceEncoder: Encoder[TourGroupDetailsPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupDetailsPlannerResponse] = deriveDecoder

final case class TourGroupListPlannerResponse(groups: List[TourGroupSummaryPlannerResponse])
object TourGroupListPlannerResponse:
  given sourceEncoder: Encoder[TourGroupListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupListPlannerResponse] = deriveDecoder
