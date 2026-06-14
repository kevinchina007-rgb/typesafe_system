package com.typesafe.travel.tourgroup.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class TourGroupDetailsResponse(
    group: TourGroupSummaryResponse,
    memberships: List[TourGroupMembershipResponse],
    membershipTravelers: List[TourGroupMembershipTravelerResponse],
    planItems: List[GroupPlanItemResponse],
    planOptions: List[GroupPlanOptionResponse],
    selections: List[GroupPlanSelectionResponse],
    selectionOrderLinks: List[GroupSelectionOrderLinkResponse],
    blacklists: List[TourGroupBlacklistResponse],
    selectionOrderProjections: List[GroupSelectionOrderProjectionResponse] = Nil
)
object TourGroupDetailsResponse:
  given sourceEncoder: Encoder[TourGroupDetailsResponse] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupDetailsResponse] = deriveDecoder
