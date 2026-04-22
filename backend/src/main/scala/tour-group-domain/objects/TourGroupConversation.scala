package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TourGroupConversationType:
  case GroupPublic, Direct

object TourGroupConversationType:
  val all: Vector[TourGroupConversationType] =
    Vector(TourGroupConversationType.GroupPublic, TourGroupConversationType.Direct)

  def fromText(value: String): TourGroupConversationType =
    value.trim match
      case "GroupPublic" => TourGroupConversationType.GroupPublic
      case "Direct"      => TourGroupConversationType.Direct
      case other         => throw new IllegalArgumentException(s"Unknown tour group conversation type: $other")

enum TourGroupConversationStatus:
  case Active, Archived, Closed

object TourGroupConversationStatus:
  val all: Vector[TourGroupConversationStatus] =
    Vector(TourGroupConversationStatus.Active, TourGroupConversationStatus.Archived, TourGroupConversationStatus.Closed)

  def fromText(value: String): TourGroupConversationStatus =
    value.trim match
      case "Active"   => TourGroupConversationStatus.Active
      case "Archived" => TourGroupConversationStatus.Archived
      case "Closed"   => TourGroupConversationStatus.Closed
      case other      => throw new IllegalArgumentException(s"Unknown tour group conversation status: $other")

enum TourGroupConversationParticipantRole:
  case Organizer, Member

object TourGroupConversationParticipantRole:
  val all: Vector[TourGroupConversationParticipantRole] =
    Vector(TourGroupConversationParticipantRole.Organizer, TourGroupConversationParticipantRole.Member)

  def fromText(value: String): TourGroupConversationParticipantRole =
    value.trim match
      case "Organizer" => TourGroupConversationParticipantRole.Organizer
      case "Member"    => TourGroupConversationParticipantRole.Member
      case other       => throw new IllegalArgumentException(s"Unknown tour group participant role: $other")

enum TourGroupConversationParticipantStatus:
  case Active, Left

object TourGroupConversationParticipantStatus:
  val all: Vector[TourGroupConversationParticipantStatus] =
    Vector(TourGroupConversationParticipantStatus.Active, TourGroupConversationParticipantStatus.Left)

  def fromText(value: String): TourGroupConversationParticipantStatus =
    value.trim match
      case "Active" => TourGroupConversationParticipantStatus.Active
      case "Left"   => TourGroupConversationParticipantStatus.Left
      case other    => throw new IllegalArgumentException(s"Unknown tour group participant status: $other")

final case class TourGroupConversation(
    conversationId: TourGroupConversationId,
    groupId: TourGroupId,
    conversationType: TourGroupConversationType,
    status: TourGroupConversationStatus = TourGroupConversationStatus.Active,
    directMemberAUserId: Option[UserId] = None,
    directMemberBUserId: Option[UserId] = None,
    createdAt: Instant,
    updatedAt: Instant = Instant.EPOCH
)

final case class TourGroupConversationParticipant(
    participantId: TourGroupConversationParticipantId,
    conversationId: TourGroupConversationId,
    userId: UserId,
    role: TourGroupConversationParticipantRole,
    joinedAt: Instant,
    status: TourGroupConversationParticipantStatus = TourGroupConversationParticipantStatus.Active,
    lastReadAt: Option[Instant] = None,
    lastReadMessageId: Option[TourGroupMessageId] = None,
    mutedAt: Option[Instant] = None,
    archivedAt: Option[Instant] = None
)

