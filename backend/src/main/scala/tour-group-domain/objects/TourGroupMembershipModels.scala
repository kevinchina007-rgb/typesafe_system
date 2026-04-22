package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TourGroupMembershipStatus:
  case Pending, Active, Left, Removed

object TourGroupMembershipStatus:
  val all: Vector[TourGroupMembershipStatus] =
    Vector(
      TourGroupMembershipStatus.Pending,
      TourGroupMembershipStatus.Active,
      TourGroupMembershipStatus.Left,
      TourGroupMembershipStatus.Removed
    )

  def fromText(value: String): TourGroupMembershipStatus =
    value.trim match
      case "Pending" => TourGroupMembershipStatus.Pending
      case "Active"  => TourGroupMembershipStatus.Active
      case "Left"    => TourGroupMembershipStatus.Left
      case "Removed" => TourGroupMembershipStatus.Removed
      case other     => throw new IllegalArgumentException(s"Unknown tour group membership status: $other")

enum TourGroupMembershipTravelerStatus:
  case Active, Removed

object TourGroupMembershipTravelerStatus:
  val all: Vector[TourGroupMembershipTravelerStatus] =
    Vector(TourGroupMembershipTravelerStatus.Active, TourGroupMembershipTravelerStatus.Removed)

  def fromText(value: String): TourGroupMembershipTravelerStatus =
    value.trim match
      case "Active"  => TourGroupMembershipTravelerStatus.Active
      case "Removed" => TourGroupMembershipTravelerStatus.Removed
      case other     => throw new IllegalArgumentException(s"Unknown tour group traveler status: $other")

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

