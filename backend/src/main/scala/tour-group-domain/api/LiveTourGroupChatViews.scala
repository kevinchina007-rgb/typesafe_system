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


trait LiveTourGroupChatViews[F[_]: MonadThrow]:
  self: LiveTourGroupApplicationService[F] =>
  protected def buildMessageViews(
      conversation: TourGroupConversation,
      messages: List[TourGroupMessage],
      participant: TourGroupConversationParticipant,
      actingUserId: UserId,
      settings: TourGroupChatSettings
  ): F[List[TourGroupMessageView]] =
    for
      groupedAttachments <- tourGroupRepository.findAttachmentsByMessageIds(messages.map(_.messageId))
      groupedReactions <- tourGroupRepository.findReactionsByMessageIds(messages.map(_.messageId))
      replyPreviewMap <- messages.flatMap(_.replyToMessageId).distinct.traverse { replyId =>
        tourGroupRepository.findMessageById(replyId).map(message => replyId -> message.map(_.content.take(120)).getOrElse(""))
      }.map(_.toMap)
      views <- messages.traverse(message =>
        buildSingleMessageView(
          conversation,
          message,
          participant,
          actingUserId,
          settings.updatedByUserId,
          groupedAttachments.getOrElse(message.messageId, Nil),
          groupedReactions.getOrElse(message.messageId, Nil),
          replyPreviewMap
        )
      )
    yield views

  protected def buildConversationSummary(
      conversation: TourGroupConversation,
      actingUserId: UserId
  ): F[TourGroupConversationSummaryView] =
    for
      participants <- tourGroupRepository.findParticipantsByConversationId(conversation.conversationId)
      participant <- tourGroupRepository.findConversationParticipant(conversation.conversationId, actingUserId)
        .flatMap(_.liftTo[F](TourGroupError.ConversationAccessWasDenied(conversation.conversationId, actingUserId)))
      messages <- tourGroupRepository.findMessagesByConversationId(conversation.conversationId)
      details <- loadGroupDetails(conversation.groupId)
      settings <- getOrCreateChatSettings(details.group, latestConversationTimestamp(conversation))
      counterpartUserId = participants.find(participant => participant.userId != actingUserId).map(_.userId)
      counterpartInfo <- counterpartUserId.traverse(findUserDisplayInfo)
      (counterpartDisplayName, counterpartAvatarUrl) = counterpartInfo match
        case Some((displayName, avatarUrl)) => (Some(displayName), avatarUrl)
        case None                           => (None, None)
      lastMessage = messages.lastOption
      lastMessageAttachments <-
        lastMessage match
          case Some(message) => tourGroupRepository.findAttachmentsByMessageId(message.messageId)
          case None          => MonadThrow[F].pure(Nil)
      unreadCount = messages.count(message =>
        message.senderUserId != actingUserId &&
          participant.lastReadAt.forall(lastReadAt => message.createdAt.isAfter(lastReadAt))
      )
      participantUserIds = participants.filter(_.status == TourGroupConversationParticipantStatus.Active).map(_.userId)
      participantDisplayNames <- participantUserIds.distinct.traverse(findUserDisplayInfo).map(_.map(_._1))
      participantsSummary = participantDisplayNames.mkString(", ")
      conversationTitle =
        conversation.conversationType match
          case TourGroupConversationType.GroupPublic => "Group chat"
          case TourGroupConversationType.Direct      => counterpartDisplayName.getOrElse(counterpartUserId.map(_.value).getOrElse("Direct chat"))
    yield TourGroupConversationSummaryView(
      conversationId = conversation.conversationId,
      conversationType = conversation.conversationType,
      status = conversation.status,
      counterpartUserId = counterpartUserId,
      counterpartDisplayName = counterpartDisplayName,
      counterpartAvatarUrl = counterpartAvatarUrl,
      conversationTitle = conversationTitle,
      participantsSummary = participantsSummary,
      lastMessagePreview = lastMessage.map(message => renderMessagePreview(message, lastMessageAttachments)),
      lastMessageAt = lastMessage.map(_.createdAt),
      unreadCount = unreadCount,
      isMuted = participant.mutedAt.nonEmpty,
      isArchived = participant.archivedAt.nonEmpty,
      canSendMessage = canSendInConversation(conversation, settings, details.group.organizerUserId, actingUserId, participant.userId)
    )

  protected def buildSingleMessageView(
      conversation: TourGroupConversation,
      message: TourGroupMessage,
      participant: TourGroupConversationParticipant,
      actingUserId: UserId,
      organizerUserId: UserId,
      attachments: List[TourGroupMessageAttachment],
      reactions: List[TourGroupMessageReaction],
      replyPreviewMap: Map[TourGroupMessageId, String]
  ): F[TourGroupMessageView] =
    findUserDisplayInfo(message.senderUserId).map { case (displayName, avatarUrl) =>
      val visibleContent =
        message.status match
          case TourGroupMessageStatus.Recalled => "[Message recalled]"
          case TourGroupMessageStatus.Deleted  => "[Message deleted]"
          case _                               => message.content
      TourGroupMessageView(
        messageId = message.messageId,
        conversationId = conversation.conversationId,
        messageType = message.messageType,
        senderUserId = message.senderUserId,
        senderDisplayName = displayName,
        senderAvatarUrl = avatarUrl,
        content = visibleContent,
        replyToMessageId = message.replyToMessageId,
        replyToPreview = message.replyToMessageId.flatMap(replyPreviewMap.get),
        status = message.status,
        createdAt = message.createdAt,
        updatedAt = if message.updatedAt == Instant.EPOCH then message.createdAt else message.updatedAt,
        attachments = attachments.sortBy(_.sortOrder).map(attachment =>
          TourGroupMessageAttachmentView(
            attachmentId = attachment.attachmentId,
            attachmentType = attachment.attachmentType,
            publicUrl = attachment.publicUrl,
            originalFileName = attachment.originalFileName,
            mimeType = attachment.mimeType,
            fileSize = attachment.fileSize
          )
        ),
        reactions = reactions.groupBy(_.reactionType).toList.sortBy(_._1).map { case (reactionType, groupedReactions) =>
          TourGroupMessageReactionView(
            reactionType = reactionType,
            count = groupedReactions.size,
            reactedByCurrentUser = groupedReactions.exists(_.userId == actingUserId)
          )
        },
        canEdit = message.senderUserId == actingUserId && (message.status == TourGroupMessageStatus.Visible || message.status == TourGroupMessageStatus.Edited),
        canDelete = message.senderUserId == actingUserId && message.status != TourGroupMessageStatus.Recalled,
        canRecall = message.senderUserId == actingUserId && message.status != TourGroupMessageStatus.Deleted,
        canReact = participant.status == TourGroupConversationParticipantStatus.Active && conversation.status == TourGroupConversationStatus.Active,
        isMine = message.senderUserId == actingUserId
      )
    }

  protected def renderMessagePreview(
      message: TourGroupMessage,
      attachments: List[TourGroupMessageAttachment]
  ): String =
    message.status match
      case TourGroupMessageStatus.Recalled => "[Message recalled]"
      case TourGroupMessageStatus.Deleted  => "[Message deleted]"
      case _ =>
        val normalizedContent = message.content.trim
        if normalizedContent.nonEmpty then
          normalizedContent.take(120)
        else
          attachments.sortBy(_.sortOrder).headOption match
            case Some(attachment) if attachment.attachmentType == TourGroupMessageAttachmentType.Image =>
              s"[Image] ${attachment.originalFileName}".take(120)
            case Some(attachment) =>
              s"[File] ${attachment.originalFileName}".take(120)
            case None =>
              message.messageType match
                case TourGroupMessageType.Image => "[Image]"
                case TourGroupMessageType.File  => "[File]"
                case TourGroupMessageType.Mixed => "[Attachment]"
                case TourGroupMessageType.Text  => ""

  protected def findUserDisplayInfo(userId: UserId): F[(String, Option[String])] =
    userRepository.findByUserId(userId).map {
      case Some(user) => user.userDisplayName.value -> user.avatarUrl.map(_.value)
      case None       => userId.value -> Option.empty[String]
    }

