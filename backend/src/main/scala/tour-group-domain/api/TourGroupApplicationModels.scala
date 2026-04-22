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

final case class TourGroupDetailsView(
    details: TourGroupDetails,
    linkedOrders: Map[GroupPlanSelectionId, Order],
    memberDisplayNames: Map[UserId, String]
)

final case class GroupSelectionOrderProjectionView(
    selectionId: GroupPlanSelectionId,
    orderId: OrderId,
    orderStatus: String,
    paymentStatus: String,
    supplierReviewStatus: String,
    refundStatus: Option[String],
    bookingSummaryLabel: String
)

final case class TourGroupChatSettingsView(
    groupId: TourGroupId,
    allowMemberDirectChat: Boolean,
    updatedAt: Instant,
    updatedByUserId: UserId,
    canUpdate: Boolean
)

final case class TourGroupConversationSummaryView(
    conversationId: TourGroupConversationId,
    conversationType: TourGroupConversationType,
    status: TourGroupConversationStatus,
    counterpartUserId: Option[UserId],
    counterpartDisplayName: Option[String],
    counterpartAvatarUrl: Option[String],
    conversationTitle: String,
    participantsSummary: String,
    lastMessagePreview: Option[String],
    lastMessageAt: Option[Instant],
    unreadCount: Int,
    isMuted: Boolean,
    isArchived: Boolean,
    canSendMessage: Boolean
)

final case class TourGroupConversationListView(
    conversations: List[TourGroupConversationSummaryView],
    groupChatConversationId: Option[TourGroupConversationId]
)

final case class TourGroupMessageAttachmentView(
    attachmentId: TourGroupMessageAttachmentId,
    attachmentType: TourGroupMessageAttachmentType,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)

final case class TourGroupMessageReactionView(
    reactionType: String,
    count: Int,
    reactedByCurrentUser: Boolean
)

final case class TourGroupMessageView(
    messageId: TourGroupMessageId,
    conversationId: TourGroupConversationId,
    messageType: TourGroupMessageType,
    senderUserId: UserId,
    senderDisplayName: String,
    senderAvatarUrl: Option[String],
    content: String,
    replyToMessageId: Option[TourGroupMessageId],
    replyToPreview: Option[String],
    status: TourGroupMessageStatus,
    createdAt: Instant,
    updatedAt: Instant,
    attachments: List[TourGroupMessageAttachmentView],
    reactions: List[TourGroupMessageReactionView],
    canEdit: Boolean,
    canDelete: Boolean,
    canRecall: Boolean,
    canReact: Boolean,
    isMine: Boolean
)

final case class TourGroupUploadedAttachmentView(
    attachmentId: TourGroupMessageAttachmentId,
    attachmentType: TourGroupMessageAttachmentType,
    publicUrl: String,
    storagePath: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long,
    sortOrder: Int,
    createdAt: Instant
)

final case class TourGroupMessageSearchResultView(
    conversationId: TourGroupConversationId,
    conversationTitle: String,
    message: TourGroupMessageView
)

final case class TourGroupConversationDetailView(
    summary: TourGroupConversationSummaryView,
    messages: List[TourGroupMessageView]
)

sealed trait TourGroupApplicationError extends DomainError

object TourGroupApplicationError:
  final case class ResourceContextWasInvalid(optionId: GroupPlanOptionId, resourceType: GroupPlanOptionResourceType)
      extends TourGroupApplicationError:
    override val message: String =
      s"Option '${optionId.value}' has invalid resource context for resource type $resourceType"

  final case class MembershipTravelerWasNotFound(travelerId: TravelerId) extends TourGroupApplicationError:
    override val message: String =
      s"Traveler '${travelerId.value}' was not found"

  final case class MembershipTravelerWasNotOwnedByUser(travelerId: TravelerId, userId: UserId)
      extends TourGroupApplicationError:
    override val message: String =
      s"Traveler '${travelerId.value}' is not owned by user '${userId.value}'"

  final case class SelectionOrderCreationFailed(selectionId: GroupPlanSelectionId) extends TourGroupApplicationError:
    override val message: String =
      s"Selection '${selectionId.value}' could not be converted into an order"

trait TourGroupApplicationService[F[_]]:
  def createGroup(
      organizerUserId: UserId,
      title: String,
      description: String,
      destination: String,
      startDate: LocalDate,
      endDate: LocalDate,
      capacity: Int,
      createdAt: Instant
  ): F[TourGroupDetailsView]
  def listGroups: F[List[TourGroupDetailsView]]
  def getGroupDetails(groupId: TourGroupId): F[TourGroupDetailsView]
  def joinGroup(groupId: TourGroupId, userId: UserId, joinedAt: Instant): F[TourGroupDetailsView]
  def addMembershipTraveler(groupId: TourGroupId, userId: UserId, travelerId: TravelerId, joinedAt: Instant): F[TourGroupDetailsView]
  def createPlanItem(
      groupId: TourGroupId,
      organizerUserId: UserId,
      itemType: GroupPlanItemType,
      title: String,
      description: String,
      scheduledAt: Instant,
      endsAt: Option[Instant],
      sequenceNo: Int
  ): F[TourGroupDetailsView]
  def createPlanOption(
      groupId: TourGroupId,
      planItemId: GroupPlanItemId,
      organizerUserId: UserId,
      resourceType: GroupPlanOptionResourceType,
      resourceId: String,
      resourceVariantCode: Option[String],
      resourceContext: Option[String],
      label: String,
      description: String,
      defaultQuantity: Int
  ): F[TourGroupDetailsView]
  def createSelection(
      groupId: TourGroupId,
      actingUserId: UserId,
      planItemId: GroupPlanItemId,
      optionId: GroupPlanOptionId,
      quantity: Int,
      travelerIds: List[TravelerId],
      createdAt: Instant
  ): F[TourGroupDetailsView]
  def submitSelection(selectionId: GroupPlanSelectionId, actingUserId: UserId): F[TourGroupDetailsView]
  def confirmSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: Option[String], confirmedAt: Instant): F[TourGroupDetailsView]
  def rejectSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: String, rejectedAt: Instant): F[TourGroupDetailsView]
  def paySelection(selectionId: GroupPlanSelectionId, actingUserId: UserId, paymentMethod: PaymentMethod, paidAt: Instant): F[(TourGroupDetailsView, Order)]
  def batchConfirmSelections(
      selectionIds: List[GroupPlanSelectionId],
      organizerUserId: UserId,
      reviewNote: Option[String],
      confirmedAt: Instant
  ): F[TourGroupDetailsView]
  def batchRejectSelections(
      selectionIds: List[GroupPlanSelectionId],
      organizerUserId: UserId,
      reviewNote: String,
      rejectedAt: Instant
  ): F[TourGroupDetailsView]
  def batchPaySelections(
      selectionIds: List[GroupPlanSelectionId],
      actingUserId: UserId,
      paymentMethod: PaymentMethod,
      paidAt: Instant
  ): F[(TourGroupDetailsView, List[Order])]
  def listGroupBookings(groupId: TourGroupId): F[List[Order]]
  def getChatSettings(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[TourGroupChatSettingsView]
  def updateChatSettings(groupId: TourGroupId, organizerUserId: UserId, allowMemberDirectChat: Boolean, updatedAt: Instant): F[TourGroupChatSettingsView]
  def listGroupChatMessages(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[List[TourGroupMessageView]]
  def sendGroupChatMessage(groupId: TourGroupId, actingUserId: UserId, content: String, createdAt: Instant): F[List[TourGroupMessageView]]
  def listDirectConversations(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[List[TourGroupConversationSummaryView]]
  def getOrCreateDirectConversation(groupId: TourGroupId, actingUserId: UserId, targetUserId: UserId, currentTime: Instant): F[TourGroupConversationSummaryView]
  def listDirectConversationMessages(conversationId: TourGroupConversationId, actingUserId: UserId): F[List[TourGroupMessageView]]
  def sendDirectConversationMessage(conversationId: TourGroupConversationId, actingUserId: UserId, content: String, createdAt: Instant): F[List[TourGroupMessageView]]
  def listConversations(groupId: TourGroupId, actingUserId: UserId, currentTime: Instant): F[TourGroupConversationListView]
  def getConversationMessages(conversationId: TourGroupConversationId, actingUserId: UserId): F[List[TourGroupMessageView]]
  def uploadConversationAttachment(
      groupId: TourGroupId,
      actingUserId: UserId,
      fileName: String,
      mimeType: String,
      fileBytes: Array[Byte],
      createdAt: Instant
  ): F[TourGroupUploadedAttachmentView]
  def sendConversationMessage(
      conversationId: TourGroupConversationId,
      actingUserId: UserId,
      messageType: TourGroupMessageType,
      content: String,
      replyToMessageId: Option[TourGroupMessageId],
      attachmentRefs: List[TourGroupUploadedAttachmentView],
      createdAt: Instant
  ): F[List[TourGroupMessageView]]
  def editMessage(
      messageId: TourGroupMessageId,
      actingUserId: UserId,
      content: String,
      updatedAt: Instant
  ): F[List[TourGroupMessageView]]
  def deleteMessage(
      messageId: TourGroupMessageId,
      actingUserId: UserId,
      deletedAt: Instant
  ): F[List[TourGroupMessageView]]
  def recallMessage(
      messageId: TourGroupMessageId,
      actingUserId: UserId,
      recalledAt: Instant
  ): F[List[TourGroupMessageView]]
  def addReaction(
      messageId: TourGroupMessageId,
      actingUserId: UserId,
      reactionType: String,
      createdAt: Instant
  ): F[List[TourGroupMessageView]]
  def removeReaction(
      messageId: TourGroupMessageId,
      actingUserId: UserId,
      reactionType: String
  ): F[List[TourGroupMessageView]]
  def markConversationRead(
      conversationId: TourGroupConversationId,
      actingUserId: UserId,
      currentTime: Instant
  ): F[TourGroupConversationSummaryView]
  def updateConversationMuteState(
      conversationId: TourGroupConversationId,
      actingUserId: UserId,
      muted: Boolean,
      currentTime: Instant
  ): F[TourGroupConversationSummaryView]
  def updateConversationArchiveState(
      conversationId: TourGroupConversationId,
      actingUserId: UserId,
      archived: Boolean,
      currentTime: Instant
  ): F[TourGroupConversationSummaryView]
  def searchConversations(
      groupId: TourGroupId,
      actingUserId: UserId,
      query: String,
      currentTime: Instant
  ): F[List[TourGroupConversationSummaryView]]
  def searchMessages(
      groupId: TourGroupId,
      actingUserId: UserId,
      query: String,
      currentTime: Instant
  ): F[List[TourGroupMessageSearchResultView]]

object TourGroupDetailsView:
  def buildSelectionOrderProjectionViews(view: TourGroupDetailsView): List[GroupSelectionOrderProjectionView] =
    view.details.selectionOrderLinks.toList.flatMap { link =>
      view.linkedOrders.get(link.selectionId).map { order =>
        val orderItemStatuses = order.orderLineItems.map(_.supplierReviewStatus.toString).distinct
        val paymentStatuses = order.orderPayments.map(_.paymentStatus).distinct
        val refundStatuses = order.orderRefunds.map(_.refundStatus).distinct
        GroupSelectionOrderProjectionView(
          selectionId = link.selectionId,
          orderId = link.orderId,
          orderStatus = order.orderStatus.toString,
          paymentStatus =
            if paymentStatuses.contains(PaymentStatus.Captured) then "Paid"
            else if paymentStatuses.contains(PaymentStatus.Authorized) then "Authorized"
            else if paymentStatuses.contains(PaymentStatus.Failed) then "Failed"
            else "Pending",
          supplierReviewStatus =
            if orderItemStatuses.contains(SupplierReviewStatus.SupplierRejected.toString) then "SupplierRejected"
            else if orderItemStatuses.contains(SupplierReviewStatus.PendingSupplierConfirmation.toString) then "PendingSupplierConfirmation"
            else if orderItemStatuses.contains(SupplierReviewStatus.SupplierConfirmed.toString) then "SupplierConfirmed"
            else "NotSubmitted",
          refundStatus = refundStatuses.headOption.map(_.toString),
          bookingSummaryLabel = s"${order.orderType} ${order.totalBookedMoney.amount} ${order.orderCurrency}"
        )
      }
    }

