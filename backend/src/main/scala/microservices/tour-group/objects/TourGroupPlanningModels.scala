// TourGroupPlanningModels 定义团体游模块的数据模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant
import TourGroupSourceJsonCodecs.given

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

  given sourceEncoder: Encoder[GroupPlanItemType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[GroupPlanItemType] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[GroupPlanItemStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[GroupPlanItemStatus] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[GroupPlanOptionStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[GroupPlanOptionStatus] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[GroupPlanOptionResourceType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[GroupPlanOptionResourceType] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[GroupPlanSelectionStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[GroupPlanSelectionStatus] = Decoder.decodeString.map(fromText)

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

object GroupPlanItem:
  given sourceEncoder: Encoder[GroupPlanItem] = deriveEncoder
  given sourceDecoder: Decoder[GroupPlanItem] = deriveDecoder

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

object GroupPlanOption:
  given sourceEncoder: Encoder[GroupPlanOption] = deriveEncoder
  given sourceDecoder: Decoder[GroupPlanOption] = deriveDecoder

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
    reviewNote: Option[String],
    travelerIds: Vector[TravelerId]
)

object GroupPlanSelection:
  given sourceEncoder: Encoder[GroupPlanSelection] = deriveEncoder
  given sourceDecoder: Decoder[GroupPlanSelection] = deriveDecoder

final case class GroupPlanSelectionTraveler(
    selectionTravelerId: GroupPlanSelectionTravelerId,
    selectionId: GroupPlanSelectionId,
    travelerId: TravelerId
)

object GroupPlanSelectionTraveler:
  given sourceEncoder: Encoder[GroupPlanSelectionTraveler] = deriveEncoder
  given sourceDecoder: Decoder[GroupPlanSelectionTraveler] = deriveDecoder

final case class GroupSelectionOrderLink(
    linkId: GroupSelectionOrderLinkId,
    selectionId: GroupPlanSelectionId,
    orderId: OrderId,
    createdAt: Instant
)

object GroupSelectionOrderLink:
  given sourceEncoder: Encoder[GroupSelectionOrderLink] = deriveEncoder
  given sourceDecoder: Decoder[GroupSelectionOrderLink] = deriveDecoder
