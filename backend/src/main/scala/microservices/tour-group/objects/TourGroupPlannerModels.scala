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
    capacity: Int
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

final case class AddMembershipTravelerPlannerRequest(groupId: String, userId: String, travelerId: String)
object AddMembershipTravelerPlannerRequest:
  given sourceEncoder: Encoder[AddMembershipTravelerPlannerRequest] = deriveEncoder
  given sourceDecoder: Decoder[AddMembershipTravelerPlannerRequest] = deriveDecoder

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

final case class TourGroupDetailsPlannerResponse(
    group: TourGroupSummaryPlannerResponse,
    memberships: List[TourGroupMembershipPlannerResponse],
    membershipTravelers: List[TourGroupMembershipTravelerPlannerResponse]
)
object TourGroupDetailsPlannerResponse:
  given sourceEncoder: Encoder[TourGroupDetailsPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupDetailsPlannerResponse] = deriveDecoder

final case class TourGroupListPlannerResponse(groups: List[TourGroupSummaryPlannerResponse])
object TourGroupListPlannerResponse:
  given sourceEncoder: Encoder[TourGroupListPlannerResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupListPlannerResponse] = deriveDecoder
