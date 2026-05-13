package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.{Instant, LocalDate}
import TourGroupSourceJsonCodecs.given

// TourGroup 只负责“团体计划与成员关系”，不直接承担真实交易�?
// 真实支付和退款仍然通过 Order 主链完成�?
enum TourGroupStatus:
  case Draft, Open, Closed, Cancelled

object TourGroupStatus:
  val all: Vector[TourGroupStatus] =
    Vector(TourGroupStatus.Draft, TourGroupStatus.Open, TourGroupStatus.Closed, TourGroupStatus.Cancelled)

  def fromText(value: String): TourGroupStatus =
    value.trim match
      case "Draft"     => TourGroupStatus.Draft
      case "Open"      => TourGroupStatus.Open
      case "Closed"    => TourGroupStatus.Closed
      case "Cancelled" => TourGroupStatus.Cancelled
      case other       => throw new IllegalArgumentException(s"Unknown tour group status: $other")

  given sourceEncoder: Encoder[TourGroupStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TourGroupStatus] = Decoder.decodeString.map(fromText)

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
)

object TourGroup:
  given sourceEncoder: Encoder[TourGroup] = deriveEncoder
  given sourceDecoder: Decoder[TourGroup] = deriveDecoder

final case class TourGroupDetails(
    group: TourGroup,
    memberships: Vector[TourGroupMembership],
    membershipTravelers: Vector[TourGroupMembershipTraveler],
    planItems: Vector[GroupPlanItem],
    planOptions: Vector[GroupPlanOption],
    selections: Vector[GroupPlanSelection],
    selectionTravelers: Vector[GroupPlanSelectionTraveler],
    selectionOrderLinks: Vector[GroupSelectionOrderLink]
)

object TourGroupDetails:
  given sourceEncoder: Encoder[TourGroupDetails] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupDetails] = deriveDecoder

