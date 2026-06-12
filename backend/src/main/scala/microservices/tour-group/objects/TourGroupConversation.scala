// TourGroupConversation 定义团体游模块的数据模型。

package com.typesafe.travel.tourgroup.domain

import com.typesafe.travel.shared.kernel.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.Instant
import TourGroupSourceJsonCodecs.given

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

  given sourceEncoder: Encoder[TourGroupConversationType] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TourGroupConversationType] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[TourGroupConversationStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TourGroupConversationStatus] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[TourGroupConversationParticipantRole] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TourGroupConversationParticipantRole] = Decoder.decodeString.map(fromText)

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

  given sourceEncoder: Encoder[TourGroupConversationParticipantStatus] = Encoder.encodeString.contramap(_.toString)
  given sourceDecoder: Decoder[TourGroupConversationParticipantStatus] = Decoder.decodeString.map(fromText)

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

object TourGroupConversation:
  given sourceEncoder: Encoder[TourGroupConversation] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupConversation] = deriveDecoder

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

object TourGroupConversationParticipant:
  given sourceEncoder: Encoder[TourGroupConversationParticipant] = deriveEncoder
  given sourceDecoder: Decoder[TourGroupConversationParticipant] = deriveDecoder

