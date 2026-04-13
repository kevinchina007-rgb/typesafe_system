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


trait LiveTourGroupChatOperations[F[_]: MonadThrow]:
  self: LiveTourGroupApplicationService[F] =>
  override def getChatSettings(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[TourGroupChatSettingsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      settings <- getOrCreateChatSettings(details.group, currentTime)
    yield TourGroupChatSettingsView(
      groupId = groupId,
      allowMemberDirectChat = settings.allowMemberDirectChat,
      updatedAt = settings.updatedAt,
      updatedByUserId = settings.updatedByUserId,
      canUpdate = details.group.organizerUserId == actingUserId
    )

  override def updateChatSettings(groupId: TourGroupId, organizerUserId: UserId, allowMemberDirectChat: Boolean, updatedAt: Instant): F[TourGroupChatSettingsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- details.group.ensureOrganizer(organizerUserId).liftTo[F]
      settings = TourGroupChatSettings(groupId, allowMemberDirectChat, updatedAt, organizerUserId)
      _ <- tourGroupRepository.saveChatSettings(settings)
    yield TourGroupChatSettingsView(groupId, allowMemberDirectChat, updatedAt, organizerUserId, canUpdate = true)

  override def listGroupChatMessages(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[List[TourGroupMessageView]] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      conversation <- ensurePublicConversation(groupId, currentTime)
      messageViews <- getConversationMessages(conversation.conversationId, actingUserId)
    yield messageViews

  override def sendGroupChatMessage(groupId: TourGroupId, actingUserId: UserId, content: String, createdAt: Instant): F[List[TourGroupMessageView]] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      conversation <- ensurePublicConversation(groupId, createdAt)
      messageViews <- sendConversationMessage(
        conversationId = conversation.conversationId,
        actingUserId = actingUserId,
        messageType = TourGroupMessageType.Text,
        content = content,
        replyToMessageId = None,
        attachmentRefs = Nil,
        createdAt = createdAt
      )
    yield messageViews

  override def listDirectConversations(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[List[TourGroupConversationSummaryView]] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      _ <- getOrCreateChatSettings(details.group, currentTime)
      conversations <- tourGroupRepository.findDirectConversationsByGroupIdAndUserId(groupId, actingUserId)
      summaries <- conversations.traverse(conversation => buildConversationSummary(conversation, actingUserId))
    yield summaries.sortBy(_.lastMessageAt.map(_.toEpochMilli).getOrElse(0L)).reverse

  override def getOrCreateDirectConversation(groupId: TourGroupId, actingUserId: UserId, targetUserId: UserId, currentTime: Instant): F[TourGroupConversationSummaryView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      _ <- ensureDirectTarget(details, actingUserId, targetUserId).liftTo[F]
      settings <- getOrCreateChatSettings(details.group, currentTime)
      _ <- ensureDirectConversationAllowed(details.group, settings, actingUserId, targetUserId).liftTo[F]
      existingConversation <- tourGroupRepository.findDirectConversationByGroupIdAndUsers(groupId, actingUserId, targetUserId)
      conversation <- existingConversation match
        case Some(value) => value.pure[F]
        case None =>
          for
            conversationId <- tourGroupRepository.nextConversationId
            createdConversation = createDirectConversation(conversationId, groupId, actingUserId, targetUserId, currentTime)
            _ <- tourGroupRepository.saveConversation(createdConversation)
            _ <- ensureConversationParticipant(createdConversation.conversationId, actingUserId, details.group.organizerUserId, currentTime)
            _ <- ensureConversationParticipant(createdConversation.conversationId, targetUserId, details.group.organizerUserId, currentTime)
          yield createdConversation
      summary <- buildConversationSummary(conversation, actingUserId)
    yield summary

  override def listDirectConversationMessages(conversationId: TourGroupConversationId, actingUserId: UserId): F[List[TourGroupMessageView]] =
    getConversationMessages(conversationId, actingUserId)

  override def sendDirectConversationMessage(conversationId: TourGroupConversationId, actingUserId: UserId, content: String, createdAt: Instant): F[List[TourGroupMessageView]] =
    sendConversationMessage(
      conversationId = conversationId,
      actingUserId = actingUserId,
      messageType = TourGroupMessageType.Text,
      content = content,
      replyToMessageId = None,
      attachmentRefs = Nil,
      createdAt = createdAt
    )

  override def listConversations(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[TourGroupConversationListView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      _ <- getOrCreateChatSettings(details.group, currentTime)
      publicConversation <- ensurePublicConversation(groupId, currentTime)
      directConversations <- tourGroupRepository.findDirectConversationsByGroupIdAndUserId(groupId, actingUserId)
      summaries <- (publicConversation :: directConversations).distinctBy(_.conversationId).traverse(buildConversationSummary(_, actingUserId))
    yield TourGroupConversationListView(
      conversations = summaries.sortBy(_.lastMessageAt.map(_.toEpochMilli).getOrElse(0L)).reverse,
      groupChatConversationId = Some(publicConversation.conversationId)
    )

  override def getConversationMessages(conversationId: TourGroupConversationId, actingUserId: UserId): F[List[TourGroupMessageView]] =
    for
      conversation <- loadConversation(conversationId)
      details <- loadGroupDetails(conversation.groupId)
      settings <- getOrCreateChatSettings(details.group, latestConversationTimestamp(conversation))
      participant <- ensureConversationParticipantAccess(conversation, details, settings, actingUserId)
      messages <- tourGroupRepository.findMessagesByConversationId(conversationId)
      messageViews <- buildMessageViews(conversation, messages, participant, actingUserId, settings)
    yield messageViews

  override def uploadConversationAttachment(
      groupId: TourGroupId,
      actingUserId: UserId,
      fileName: String,
      mimeType: String,
      fileBytes: Array[Byte],
      createdAt: Instant
  ): F[TourGroupUploadedAttachmentView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      sanitizedFileName <- normalizeAttachmentFileName(fileName).liftTo[F]
      attachmentType <- inferAttachmentType(mimeType, sanitizedFileName).liftTo[F]
      _ <- validateAttachmentSize(attachmentType, fileBytes.length.toLong).liftTo[F]
      _ <- validateAttachmentMimeType(attachmentType, mimeType, sanitizedFileName).liftTo[F]
      attachmentId <- tourGroupRepository.nextMessageAttachmentId
      stored <- chatAttachmentStorage.storeAttachment(
        ownerUserId = actingUserId,
        collection = toAttachmentCollection(attachmentType),
        originalFileName = sanitizedFileName,
        fileExtension = extractFileExtension(sanitizedFileName),
        mimeType = mimeType,
        fileBytes = fileBytes
      )
    yield TourGroupUploadedAttachmentView(
      attachmentId = attachmentId,
      attachmentType = attachmentType,
      publicUrl = stored.publicUrl,
      storagePath = stored.storagePath,
      originalFileName = stored.originalFileName,
      mimeType = stored.mimeType,
      fileSize = stored.fileSize,
      sortOrder = 0,
      createdAt = createdAt
    )

  override def sendConversationMessage(
      conversationId: TourGroupConversationId,
      actingUserId: UserId,
      messageType: TourGroupMessageType,
      content: String,
      replyToMessageId: Option[TourGroupMessageId],
      attachmentRefs: List[TourGroupUploadedAttachmentView],
      createdAt: Instant
  ): F[List[TourGroupMessageView]] =
    for
      conversation <- loadConversation(conversationId)
      details <- loadGroupDetails(conversation.groupId)
      settings <- getOrCreateChatSettings(details.group, createdAt)
      participant <- ensureConversationParticipantAccess(conversation, details, settings, actingUserId)
      normalizedContent <- normalizeMessagePayload(content, attachmentRefs).liftTo[F]
      normalizedMessageType <- inferFinalMessageType(messageType, normalizedContent, attachmentRefs).liftTo[F]
      _ <- replyToMessageId.traverse(replyId => ensureReplyTarget(conversationId, replyId))
      messageId <- tourGroupRepository.nextMessageId
      savedMessage <- tourGroupRepository.saveMessage(
        TourGroupMessage(
          messageId = messageId,
          conversationId = conversation.conversationId,
          senderUserId = actingUserId,
          messageType = normalizedMessageType,
          content = normalizedContent,
          replyToMessageId = replyToMessageId,
          status = TourGroupMessageStatus.Visible,
          createdAt = createdAt,
          updatedAt = createdAt
        )
      )
      _ <- attachmentRefs.zipWithIndex.traverse_ { case (attachmentRef, index) =>
        tourGroupRepository.saveMessageAttachment(
          TourGroupMessageAttachment(
            attachmentId = attachmentRef.attachmentId,
            messageId = savedMessage.messageId,
            attachmentType = attachmentRef.attachmentType,
            publicUrl = attachmentRef.publicUrl,
            storagePath = attachmentRef.storagePath,
            originalFileName = attachmentRef.originalFileName,
            mimeType = attachmentRef.mimeType,
            fileSize = attachmentRef.fileSize,
            sortOrder = index,
            createdAt = attachmentRef.createdAt
          )
        ).void
      }
      _ <- touchConversation(conversation, createdAt)
      _ <- markParticipantRead(participant, Some(savedMessage.messageId), createdAt)
      messageViews <- getConversationMessages(conversationId, actingUserId)
    yield messageViews

  override def editMessage(messageId: TourGroupMessageId, actingUserId: UserId, content: String, updatedAt: Instant): F[List[TourGroupMessageView]] =
    mutateConversationMessage(messageId, actingUserId, updatedAt) { message =>
      for
        normalizedContent <- normalizeMessageContent(content)
        _ <- Either.cond(message.senderUserId == actingUserId, (), TourGroupError.MessageEditWasNotAllowed(messageId, message.status))
        _ <- Either.cond(message.status == TourGroupMessageStatus.Visible || message.status == TourGroupMessageStatus.Edited, (), TourGroupError.MessageEditWasNotAllowed(messageId, message.status))
      yield message.copy(content = normalizedContent, status = TourGroupMessageStatus.Edited, updatedAt = updatedAt)
    }

  override def deleteMessage(messageId: TourGroupMessageId, actingUserId: UserId, deletedAt: Instant): F[List[TourGroupMessageView]] =
    mutateConversationMessage(messageId, actingUserId, deletedAt) { message =>
      for
        _ <- Either.cond(message.senderUserId == actingUserId, (), TourGroupError.MessageDeleteWasNotAllowed(messageId, message.status))
        _ <- Either.cond(message.status != TourGroupMessageStatus.Recalled, (), TourGroupError.MessageDeleteWasNotAllowed(messageId, message.status))
      yield message.copy(content = "", status = TourGroupMessageStatus.Deleted, updatedAt = deletedAt, deletedAt = Some(deletedAt))
    }

  override def recallMessage(messageId: TourGroupMessageId, actingUserId: UserId, recalledAt: Instant): F[List[TourGroupMessageView]] =
    mutateConversationMessage(messageId, actingUserId, recalledAt) { message =>
      for
        _ <- Either.cond(message.senderUserId == actingUserId, (), TourGroupError.MessageRecallWasNotAllowed(messageId, message.status))
        _ <- Either.cond(message.status != TourGroupMessageStatus.Deleted, (), TourGroupError.MessageRecallWasNotAllowed(messageId, message.status))
      yield message.copy(content = "", status = TourGroupMessageStatus.Recalled, updatedAt = recalledAt, recalledAt = Some(recalledAt))
    }

  override def addReaction(messageId: TourGroupMessageId, actingUserId: UserId, reactionType: String, createdAt: Instant): F[List[TourGroupMessageView]] =
    for
      context <- loadMessageContext(messageId, actingUserId)
      (conversation, _, participant, message) = context
      normalizedReaction <- normalizeReactionType(reactionType).liftTo[F]
      reactionId <- tourGroupRepository.nextMessageReactionId
      _ <- tourGroupRepository.saveMessageReaction(
        TourGroupMessageReaction(
          reactionId = reactionId,
          messageId = message.messageId,
          userId = actingUserId,
          reactionType = normalizedReaction,
          createdAt = createdAt
        )
      )
      _ <- markParticipantRead(participant, Some(message.messageId), createdAt)
      messageViews <- getConversationMessages(conversation.conversationId, actingUserId)
    yield messageViews

  override def removeReaction(messageId: TourGroupMessageId, actingUserId: UserId, reactionType: String): F[List[TourGroupMessageView]] =
    for
      context <- loadMessageContext(messageId, actingUserId)
      (conversation, _, _, _) = context
      normalizedReaction <- normalizeReactionType(reactionType).liftTo[F]
      _ <- tourGroupRepository.deleteMessageReaction(messageId, actingUserId, normalizedReaction)
      messageViews <- getConversationMessages(conversation.conversationId, actingUserId)
    yield messageViews

  override def markConversationRead(conversationId: TourGroupConversationId, actingUserId: UserId, currentTime: Instant): F[TourGroupConversationSummaryView] =
    for
      conversation <- loadConversation(conversationId)
      details <- loadGroupDetails(conversation.groupId)
      settings <- getOrCreateChatSettings(details.group, currentTime)
      participant <- ensureConversationParticipantAccess(conversation, details, settings, actingUserId)
      messages <- tourGroupRepository.findMessagesByConversationId(conversationId)
      lastMessageId = messages.lastOption.map(_.messageId)
      _ <- markParticipantRead(participant, lastMessageId, currentTime)
      refreshed <- loadConversation(conversationId)
      summary <- buildConversationSummary(refreshed, actingUserId)
    yield summary

  override def updateConversationMuteState(conversationId: TourGroupConversationId, actingUserId: UserId, muted: Boolean, currentTime: Instant): F[TourGroupConversationSummaryView] =
    for
      conversation <- loadConversation(conversationId)
      details <- loadGroupDetails(conversation.groupId)
      settings <- getOrCreateChatSettings(details.group, currentTime)
      participant <- ensureConversationParticipantAccess(conversation, details, settings, actingUserId)
      _ <- tourGroupRepository.saveConversationParticipant(participant.copy(mutedAt = if muted then Some(currentTime) else None))
      summary <- buildConversationSummary(conversation, actingUserId)
    yield summary

  override def updateConversationArchiveState(conversationId: TourGroupConversationId, actingUserId: UserId, archived: Boolean, currentTime: Instant): F[TourGroupConversationSummaryView] =
    for
      conversation <- loadConversation(conversationId)
      details <- loadGroupDetails(conversation.groupId)
      settings <- getOrCreateChatSettings(details.group, currentTime)
      participant <- ensureConversationParticipantAccess(conversation, details, settings, actingUserId)
      _ <- tourGroupRepository.saveConversationParticipant(participant.copy(archivedAt = if archived then Some(currentTime) else None))
      summary <- buildConversationSummary(conversation, actingUserId)
    yield summary

  override def searchConversations(groupId: TourGroupId, actingUserId: UserId, query: String, currentTime: Instant): F[List[TourGroupConversationSummaryView]] =
    for
      conversationListView <- listConversations(groupId, actingUserId, currentTime)
      normalizedQuery <- normalizeSearchQuery(query).liftTo[F]
    yield conversationListView.conversations.filter { conversation =>
      val searchableText = List(
        conversation.conversationTitle,
        conversation.counterpartDisplayName.getOrElse(""),
        conversation.participantsSummary,
        conversation.conversationType.toString
      ).mkString(" ").toLowerCase
      searchableText.contains(normalizedQuery)
    }

  override def searchMessages(groupId: TourGroupId, actingUserId: UserId, query: String, currentTime: Instant): F[List[TourGroupMessageSearchResultView]] =
    for
      details <- loadGroupDetails(groupId)
      _ <- ensureActiveGroupMember(details, actingUserId).liftTo[F]
      normalizedQuery <- normalizeSearchQuery(query).liftTo[F]
      conversations <- tourGroupRepository.findAccessibleConversationsByGroupIdAndUserId(groupId, actingUserId)
      allowedConversationIds = conversations.map(_.conversationId).toSet
      messages <- tourGroupRepository.searchMessagesByGroupIdAndUserId(groupId, actingUserId, normalizedQuery)
      filteredMessages = messages.filter(message => allowedConversationIds.contains(message.conversationId))
      groupedAttachments <- tourGroupRepository.findAttachmentsByMessageIds(filteredMessages.map(_.messageId))
      groupedReactions <- tourGroupRepository.findReactionsByMessageIds(filteredMessages.map(_.messageId))
      conversationMap = conversations.map(conversation => conversation.conversationId -> conversation).toMap
      participantMap <- conversations.traverse(conversation =>
        tourGroupRepository.findConversationParticipant(conversation.conversationId, actingUserId).map(participant => conversation.conversationId -> participant)
      ).map(_.collect { case (conversationId, Some(participant)) => conversationId -> participant }.toMap)
      summaries <- conversations.traverse(conversation => buildConversationSummary(conversation, actingUserId).map(summary => conversation.conversationId -> summary)).map(_.toMap)
      views <- filteredMessages.traverse { message =>
        val conversation = conversationMap(message.conversationId)
        val participant = participantMap(message.conversationId)
        buildSingleMessageView(conversation, message, participant, actingUserId, details.group.organizerUserId, groupedAttachments.getOrElse(message.messageId, Nil), groupedReactions.getOrElse(message.messageId, Nil), Map.empty)
          .map(view => TourGroupMessageSearchResultView(message.conversationId, summaries(message.conversationId).conversationTitle, view))
      }
    yield views.sortBy(_.message.createdAt.toEpochMilli).reverse

