package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*

import java.time.Instant

enum TourGroupConversationType:
  case GroupPublic, Direct

enum TourGroupConversationStatus:
  case Active, Archived, Closed

enum TourGroupConversationParticipantRole:
  case Organizer, Member

enum TourGroupConversationParticipantStatus:
  case Active, Left

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

