package com.typesafe.travel.api.application

import cats.MonadThrow
import cats.syntax.all.*
import com.typesafe.travel.identity.domain.UserRepository
import com.typesafe.travel.order.domain.{Order, OrderError, OrderService, PaymentMethod, PaymentStatus, RefundStatus, SupplierReviewStatus}
import com.typesafe.travel.shared.kernel.*
import com.typesafe.travel.tourgroup.domain.*
import com.typesafe.travel.train.domain.{TrainSeatClass, TrainStationCode}
import com.typesafe.travel.traveler.domain.TravelerProfileRepository
import java.time.{Instant, LocalDate, ZoneOffset}


trait LiveTourGroupChatSupport[F[_]: MonadThrow]:
  self: LiveTourGroupApplicationService[F] =>
  protected def getOrCreateChatSettings(group: TourGroup, currentTime: Instant): F[TourGroupChatSettings] =
    tourGroupRepository.findChatSettingsByGroupId(group.groupId).flatMap {
      case Some(settings) => settings.pure[F]
      case None => ensureDefaultChatSettings(group, currentTime)
    }

  protected def ensureDefaultChatSettings(group: TourGroup, currentTime: Instant): F[TourGroupChatSettings] =
    tourGroupRepository.findChatSettingsByGroupId(group.groupId).flatMap {
      case Some(settings) => settings.pure[F]
      case None =>
        val defaultSettings = TourGroupChatSettings(
          groupId = group.groupId,
          allowMemberDirectChat = false,
          updatedAt = currentTime,
          updatedByUserId = group.organizerUserId
        )
        tourGroupRepository.saveChatSettings(defaultSettings)
    }

  protected def ensurePublicConversation(groupId: TourGroupId, currentTime: Instant): F[TourGroupConversation] =
    for
      details <- loadGroupDetails(groupId)
      conversation <- tourGroupRepository.findPublicConversationByGroupId(groupId).flatMap {
        case Some(existingConversation) => existingConversation.pure[F]
        case None =>
          for
            conversationId <- tourGroupRepository.nextConversationId
            newConversation = TourGroupConversation(
              conversationId = conversationId,
              groupId = groupId,
              conversationType = TourGroupConversationType.GroupPublic,
              directMemberAUserId = None,
              directMemberBUserId = None,
              createdAt = currentTime
            )
            _ <- tourGroupRepository.saveConversation(newConversation)
          yield newConversation
      }
      _ <- details.memberships.filter(_.status == TourGroupMembershipStatus.Active).toList.traverse_(membership =>
        ensureConversationParticipant(conversation.conversationId, membership.userId, details.group.organizerUserId, membership.joinedAt)
      )
    yield conversation

  protected def ensureConversationParticipant(
      conversationId: TourGroupConversationId,
      userId: UserId,
      organizerUserId: UserId,
      joinedAt: Instant
  ): F[TourGroupConversationParticipant] =
    tourGroupRepository.findConversationParticipant(conversationId, userId).flatMap {
      case Some(existingParticipant) => existingParticipant.pure[F]
      case None =>
        for
          participantId <- tourGroupRepository.nextConversationParticipantId
          participant = TourGroupConversationParticipant(
            participantId = participantId,
            conversationId = conversationId,
            userId = userId,
            role = if userId == organizerUserId then TourGroupConversationParticipantRole.Organizer else TourGroupConversationParticipantRole.Member,
            joinedAt = joinedAt,
            status = TourGroupConversationParticipantStatus.Active
          )
          _ <- tourGroupRepository.saveConversationParticipant(participant)
        yield participant
    }

  protected def ensureActiveGroupMember(details: TourGroupDetails, userId: UserId): Either[TourGroupError, TourGroupMembership] =
    details.memberships.find(membership => membership.userId == userId && membership.status == TourGroupMembershipStatus.Active)
      .toRight(TourGroupError.GroupMemberWasNotFound(details.group.groupId, userId))

  protected def ensureDirectTarget(details: TourGroupDetails, actingUserId: UserId, targetUserId: UserId): Either[TourGroupError, Unit] =
    if actingUserId == targetUserId then Left(TourGroupError.DirectConversationTargetWasInvalid(targetUserId))
    else ensureActiveGroupMember(details, targetUserId).map(_ => ())

  protected def ensureDirectConversationAllowed(
      group: TourGroup,
      settings: TourGroupChatSettings,
      actingUserId: UserId,
      targetUserId: UserId
  ): Either[TourGroupError, Unit] =
    val organizerUserId = group.organizerUserId
    val isOrganizerPair = actingUserId == organizerUserId || targetUserId == organizerUserId
    Either.cond(isOrganizerPair || settings.allowMemberDirectChat, (), TourGroupError.DirectConversationWasNotAllowed(group.groupId, actingUserId, targetUserId))

  protected def createDirectConversation(
      conversationId: TourGroupConversationId,
      groupId: TourGroupId,
      leftUserId: UserId,
      rightUserId: UserId,
      createdAt: Instant
  ): TourGroupConversation =
    val (memberAUserId, memberBUserId) =
      if leftUserId.value <= rightUserId.value then (leftUserId, rightUserId) else (rightUserId, leftUserId)
    TourGroupConversation(
      conversationId = conversationId,
      groupId = groupId,
      conversationType = TourGroupConversationType.Direct,
      directMemberAUserId = Some(memberAUserId),
      directMemberBUserId = Some(memberBUserId),
      createdAt = createdAt
    )

  protected def loadConversation(conversationId: TourGroupConversationId): F[TourGroupConversation] =
    tourGroupRepository.findConversationById(conversationId).flatMap(_.liftTo[F](TourGroupError.ConversationWasNotFound(conversationId)))

  protected def ensureConversationParticipantAccess(conversationId: TourGroupConversationId, actingUserId: UserId): F[Unit] =
    tourGroupRepository.findConversationParticipant(conversationId, actingUserId).flatMap {
      case Some(participant) if participant.status == TourGroupConversationParticipantStatus.Active => ().pure[F]
      case _ => TourGroupError.ConversationAccessWasDenied(conversationId, actingUserId).raiseError[F, Unit]
    }

  protected def ensureConversationParticipantAccess(
      conversation: TourGroupConversation,
      details: TourGroupDetails,
      settings: TourGroupChatSettings,
      actingUserId: UserId
  ): F[TourGroupConversationParticipant] =
    for
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      participant <- tourGroupRepository.findConversationParticipant(conversation.conversationId, actingUserId)
        .flatMap(_.liftTo[F](TourGroupError.ConversationAccessWasDenied(conversation.conversationId, actingUserId)))
      _ <- if participant.status == TourGroupConversationParticipantStatus.Active then ().pure[F]
      else TourGroupError.ConversationAccessWasDenied(conversation.conversationId, actingUserId).raiseError[F, Unit]
      _ <- if canSendInConversation(conversation, settings, details.group.organizerUserId, actingUserId, participant.userId) || conversation.conversationType == TourGroupConversationType.GroupPublic then ().pure[F]
      else ().pure[F]
    yield participant

  protected def normalizeMessageContent(content: String): Either[TourGroupError, String] =
    Option(content).map(_.trim).filter(_.nonEmpty).toRight(TourGroupError.MessageContentWasEmpty())

  protected def normalizeMessagePayload(
      content: String,
      attachmentRefs: List[TourGroupUploadedAttachmentView]
  ): Either[TourGroupError, String] =
    val trimmed = Option(content).map(_.trim).getOrElse("")
    if trimmed.nonEmpty || attachmentRefs.nonEmpty then Right(trimmed) else Left(TourGroupError.MessageContentWasEmpty())

  protected def inferFinalMessageType(
      requestedType: TourGroupMessageType,
      content: String,
      attachmentRefs: List[TourGroupUploadedAttachmentView]
  ): Either[TourGroupError, TourGroupMessageType] =
    val hasText = content.trim.nonEmpty
    val hasImage = attachmentRefs.exists(_.attachmentType == TourGroupMessageAttachmentType.Image)
    val hasFile = attachmentRefs.exists(_.attachmentType == TourGroupMessageAttachmentType.File)
    if hasText && (hasImage || hasFile) then Right(TourGroupMessageType.Mixed)
    else if hasImage && !hasFile && !hasText then Right(TourGroupMessageType.Image)
    else if hasFile && !hasImage && !hasText then Right(TourGroupMessageType.File)
    else if hasText then Right(TourGroupMessageType.Text)
    else Right(requestedType)

  protected def normalizeSearchQuery(query: String): Either[TourGroupError, String] =
    Option(query).map(_.trim.toLowerCase).filter(_.nonEmpty).toRight(TourGroupError.MessageContentWasEmpty())

  protected def normalizeReactionType(reactionType: String): Either[TourGroupError, String] =
    Option(reactionType).map(_.trim).filter(_.nonEmpty).toRight(TourGroupError.MessageReactionTypeWasInvalid(reactionType))

  protected def normalizeAttachmentFileName(fileName: String): Either[TourGroupError, String] =
    Option(fileName).map(_.trim).filter(_.nonEmpty).toRight(TourGroupError.MessageAttachmentUploadWasNotAllowed("fileName"))

  protected def inferAttachmentType(mimeType: String, fileName: String): Either[TourGroupError, TourGroupMessageAttachmentType] =
    val normalizedMimeType = Option(mimeType).getOrElse("").trim.toLowerCase
    if normalizedMimeType.startsWith("image/") then Right(TourGroupMessageAttachmentType.Image)
    else if extractFileExtension(fileName).nonEmpty then Right(TourGroupMessageAttachmentType.File)
    else Left(TourGroupError.MessageAttachmentUploadWasNotAllowed(fileName))

  protected def validateAttachmentSize(
      attachmentType: TourGroupMessageAttachmentType,
      fileSize: Long
  ): Either[TourGroupError, Unit] =
    val maxBytes =
      attachmentType match
        case TourGroupMessageAttachmentType.Image => 8L * 1024 * 1024
        case TourGroupMessageAttachmentType.File  => 20L * 1024 * 1024
    Either.cond(fileSize > 0 && fileSize <= maxBytes, (), TourGroupError.MessageAttachmentUploadWasNotAllowed(s"size:$fileSize"))

  protected def validateAttachmentMimeType(
      attachmentType: TourGroupMessageAttachmentType,
      mimeType: String,
      fileName: String
  ): Either[TourGroupError, Unit] =
    val normalizedMimeType = Option(mimeType).getOrElse("").trim.toLowerCase
    val allowed =
      attachmentType match
        case TourGroupMessageAttachmentType.Image =>
          normalizedMimeType.startsWith("image/")
        case TourGroupMessageAttachmentType.File =>
          Set(
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip"
          ).contains(normalizedMimeType) || extractFileExtension(fileName).nonEmpty
    Either.cond(allowed, (), TourGroupError.MessageAttachmentUploadWasNotAllowed(fileName))

  protected def toAttachmentCollection(
      attachmentType: TourGroupMessageAttachmentType
  ): TourGroupChatAttachmentCollection =
    attachmentType match
      case TourGroupMessageAttachmentType.Image => TourGroupChatAttachmentCollection.Image
      case TourGroupMessageAttachmentType.File  => TourGroupChatAttachmentCollection.File

  protected def extractFileExtension(fileName: String): String =
    fileName.split('.').toList.lastOption.filter(_.nonEmpty).getOrElse("bin")

  protected def touchConversation(conversation: TourGroupConversation, updatedAt: Instant): F[TourGroupConversation] =
    tourGroupRepository.saveConversation(conversation.copy(updatedAt = updatedAt))

  protected def latestConversationTimestamp(conversation: TourGroupConversation): Instant =
    if conversation.updatedAt == Instant.EPOCH then conversation.createdAt else conversation.updatedAt

  protected def latestMessageTimestamp(message: TourGroupMessage): Instant =
    if message.updatedAt == Instant.EPOCH then message.createdAt else message.updatedAt

  protected def markParticipantRead(
      participant: TourGroupConversationParticipant,
      lastReadMessageId: Option[TourGroupMessageId],
      currentTime: Instant
  ): F[TourGroupConversationParticipant] =
    tourGroupRepository.saveConversationParticipant(
      participant.copy(
        lastReadAt = Some(currentTime),
        lastReadMessageId = lastReadMessageId.orElse(participant.lastReadMessageId)
      )
    )

  protected def ensureReplyTarget(
      conversationId: TourGroupConversationId,
      replyToMessageId: TourGroupMessageId
  ): F[Unit] =
    tourGroupRepository.findMessageById(replyToMessageId).flatMap {
      case Some(message) if message.conversationId == conversationId => ().pure[F]
      case _ => TourGroupError.MessageWasNotFound(replyToMessageId).raiseError[F, Unit]
    }

  protected def loadMessageContext(
      messageId: TourGroupMessageId,
      actingUserId: UserId
  ): F[(TourGroupConversation, TourGroupDetails, TourGroupConversationParticipant, TourGroupMessage)] =
    for
      message <- tourGroupRepository.findMessageById(messageId).flatMap(_.liftTo[F](TourGroupError.MessageWasNotFound(messageId)))
      conversation <- loadConversation(message.conversationId)
      details <- loadGroupDetails(conversation.groupId)
      settings <- getOrCreateChatSettings(details.group, latestMessageTimestamp(message))
      participant <- ensureConversationParticipantAccess(conversation, details, settings, actingUserId)
    yield (conversation, details, participant, message)

  protected def mutateConversationMessage(
      messageId: TourGroupMessageId,
      actingUserId: UserId,
      currentTime: Instant
  )(
      updater: TourGroupMessage => Either[TourGroupError, TourGroupMessage]
  ): F[List[TourGroupMessageView]] =
    for
      context <- loadMessageContext(messageId, actingUserId)
      (conversation, details, participant, message) = context
      updatedMessage <- updater(message).liftTo[F]
      _ <- tourGroupRepository.saveMessage(updatedMessage)
      _ <- touchConversation(conversation, currentTime)
      _ <- markParticipantRead(participant, Some(updatedMessage.messageId), currentTime)
      settings <- getOrCreateChatSettings(details.group, currentTime)
      messages <- tourGroupRepository.findMessagesByConversationId(conversation.conversationId)
      messageViews <- buildMessageViews(conversation, messages, participant, actingUserId, settings)
    yield messageViews

  protected def canSendInConversation(
      conversation: TourGroupConversation,
      settings: TourGroupChatSettings,
      organizerUserId: UserId,
      actingUserId: UserId,
      participantUserId: UserId
  ): Boolean =
    conversation.conversationType match
      case TourGroupConversationType.GroupPublic =>
        conversation.status == TourGroupConversationStatus.Active
      case TourGroupConversationType.Direct =>
        val memberMemberConversation =
          conversation.directMemberAUserId.exists(_ != organizerUserId) &&
            conversation.directMemberBUserId.exists(_ != organizerUserId)
        conversation.status == TourGroupConversationStatus.Active &&
        (!memberMemberConversation || settings.allowMemberDirectChat) &&
        actingUserId == participantUserId

