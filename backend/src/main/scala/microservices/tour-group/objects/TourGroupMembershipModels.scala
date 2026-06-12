// TourGroupMembershipModels 定义团体游模块的数据模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant
import TourGroupSourceJsonCodecs.given

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

  given sourceEncoder: Encoder[TourGroupMembershipStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TourGroupMembershipStatus] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[TourGroupMembershipTravelerStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TourGroupMembershipTravelerStatus] = Decoder.decodeString.map(fromText)

final case class TourGroupMembership(
    membershipId: TourGroupMembershipId,
    groupId: TourGroupId,
    userId: UserId,
    joinedAt: Instant,
    status: TourGroupMembershipStatus
)

object TourGroupMembership:
  given sourceEncoder: Encoder[TourGroupMembership] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMembership] = deriveDecoder

final case class TourGroupMembershipTraveler(
    membershipTravelerId: TourGroupMembershipTravelerId,
    membershipId: TourGroupMembershipId,
    travelerId: TravelerId,
    joinedAt: Instant,
    status: TourGroupMembershipTravelerStatus
)

object TourGroupMembershipTraveler:
  given sourceEncoder: Encoder[TourGroupMembershipTraveler] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupMembershipTraveler] = deriveDecoder

