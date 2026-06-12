// TourGroupStatusesSupport 定义团体游模块的状态解析辅助。

package com.typesafe.travel.tourgroup.domain

object TourGroupStatusesSupport:
  def parseTourGroupStatus(value: String): TourGroupStatus =
    value.trim match
      case "Draft" => TourGroupStatus.Draft
      case "Open" => TourGroupStatus.Open
      case "Closed" => TourGroupStatus.Closed
      case "Cancelled" => TourGroupStatus.Cancelled
      case other => throw new IllegalArgumentException(s"Unknown tour group status: $other")

  def parseTourGroupConversationType(value: String): TourGroupConversationType =
    value.trim match
      case "GroupPublic" => TourGroupConversationType.GroupPublic
      case "Direct" => TourGroupConversationType.Direct
      case other => throw new IllegalArgumentException(s"Unknown tour group conversation type: $other")

  def parseTourGroupConversationStatus(value: String): TourGroupConversationStatus =
    value.trim match
      case "Active" => TourGroupConversationStatus.Active
      case "Archived" => TourGroupConversationStatus.Archived
      case "Closed" => TourGroupConversationStatus.Closed
      case other => throw new IllegalArgumentException(s"Unknown tour group conversation status: $other")

  def parseTourGroupConversationParticipantRole(value: String): TourGroupConversationParticipantRole =
    value.trim match
      case "Organizer" => TourGroupConversationParticipantRole.Organizer
      case "Member" => TourGroupConversationParticipantRole.Member
      case other => throw new IllegalArgumentException(s"Unknown tour group participant role: $other")

  def parseTourGroupConversationParticipantStatus(value: String): TourGroupConversationParticipantStatus =
    value.trim match
      case "Active" => TourGroupConversationParticipantStatus.Active
      case "Left" => TourGroupConversationParticipantStatus.Left
      case other => throw new IllegalArgumentException(s"Unknown tour group participant status: $other")

  def parseTourGroupMessageStatus(value: String): TourGroupMessageStatus =
    value.trim match
      case "Visible" => TourGroupMessageStatus.Visible
      case "Edited" => TourGroupMessageStatus.Edited
      case "Deleted" => TourGroupMessageStatus.Deleted
      case "Recalled" => TourGroupMessageStatus.Recalled
      case other => throw new IllegalArgumentException(s"Unknown tour group message status: $other")

  def parseTourGroupMessageType(value: String): TourGroupMessageType =
    value.trim match
      case "Text" => TourGroupMessageType.Text
      case "Image" => TourGroupMessageType.Image
      case "File" => TourGroupMessageType.File
      case "Mixed" => TourGroupMessageType.Mixed
      case other => throw new IllegalArgumentException(s"Unknown tour group message type: $other")

  def parseTourGroupMessageAttachmentType(value: String): TourGroupMessageAttachmentType =
    value.trim match
      case "Image" => TourGroupMessageAttachmentType.Image
      case "File" => TourGroupMessageAttachmentType.File
      case other => throw new IllegalArgumentException(s"Unknown tour group message attachment type: $other")

  def parseTourGroupMembershipStatus(value: String): TourGroupMembershipStatus =
    value.trim match
      case "Pending" => TourGroupMembershipStatus.Pending
      case "Active" => TourGroupMembershipStatus.Active
      case "Left" => TourGroupMembershipStatus.Left
      case "Removed" => TourGroupMembershipStatus.Removed
      case other => throw new IllegalArgumentException(s"Unknown tour group membership status: $other")

  def parseTourGroupMembershipTravelerStatus(value: String): TourGroupMembershipTravelerStatus =
    value.trim match
      case "Active" => TourGroupMembershipTravelerStatus.Active
      case "Removed" => TourGroupMembershipTravelerStatus.Removed
      case other => throw new IllegalArgumentException(s"Unknown tour group traveler status: $other")

  def parseGroupPlanItemType(value: String): GroupPlanItemType =
    value.trim match
      case "Flight" => GroupPlanItemType.Flight
      case "Hotel" => GroupPlanItemType.Hotel
      case "Train" => GroupPlanItemType.Train
      case "Attraction" => GroupPlanItemType.Attraction
      case other => throw new IllegalArgumentException(s"Unknown group plan item type: $other")

  def parseGroupPlanItemStatus(value: String): GroupPlanItemStatus =
    value.trim match
      case "Draft" => GroupPlanItemStatus.Draft
      case "Open" => GroupPlanItemStatus.Open
      case "Closed" => GroupPlanItemStatus.Closed
      case other => throw new IllegalArgumentException(s"Unknown group plan item status: $other")

  def parseGroupPlanOptionStatus(value: String): GroupPlanOptionStatus =
    value.trim match
      case "Active" => GroupPlanOptionStatus.Active
      case "Inactive" => GroupPlanOptionStatus.Inactive
      case other => throw new IllegalArgumentException(s"Unknown group plan option status: $other")

  def parseGroupPlanOptionResourceType(value: String): GroupPlanOptionResourceType =
    value.trim match
      case "Flight" => GroupPlanOptionResourceType.Flight
      case "HotelRoomType" => GroupPlanOptionResourceType.HotelRoomType
      case "TrainJourneySeat" => GroupPlanOptionResourceType.TrainJourneySeat
      case "AttractionTicketType" => GroupPlanOptionResourceType.AttractionTicketType
      case other => throw new IllegalArgumentException(s"Unknown group plan option resource type: $other")

  def parseGroupPlanSelectionStatus(value: String): GroupPlanSelectionStatus =
    value.trim match
      case "Draft" => GroupPlanSelectionStatus.Draft
      case "Submitted" => GroupPlanSelectionStatus.Submitted
      case "OrganizerConfirmed" => GroupPlanSelectionStatus.OrganizerConfirmed
      case "Rejected" => GroupPlanSelectionStatus.Rejected
      case "ConvertedToOrder" => GroupPlanSelectionStatus.ConvertedToOrder
      case "Cancelled" => GroupPlanSelectionStatus.Cancelled
      case other => throw new IllegalArgumentException(s"Unknown group plan selection status: $other")
