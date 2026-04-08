package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TourGroupMembershipStatus:
  case Pending, Active, Left, Removed

enum TourGroupMembershipTravelerStatus:
  case Active, Removed

final case class TourGroupMembership(
    membershipId: TourGroupMembershipId,
    groupId: TourGroupId,
    userId: UserId,
    joinedAt: Instant,
    status: TourGroupMembershipStatus
):
  def ensureOwner(actingUserId: UserId): Either[TourGroupError, TourGroupMembership] =
    Either.cond(userId == actingUserId, this, TourGroupError.MembershipScopeDidNotMatch(membershipId, actingUserId))

final case class TourGroupMembershipTraveler(
    membershipTravelerId: TourGroupMembershipTravelerId,
    membershipId: TourGroupMembershipId,
    travelerId: TravelerId,
    joinedAt: Instant,
    status: TourGroupMembershipTravelerStatus
)

