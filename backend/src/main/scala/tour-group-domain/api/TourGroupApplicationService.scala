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
    linkedOrders: Map[GroupPlanSelectionId, Order]
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

enum TourGroupApplicationError(val message: String) extends DomainError:
  case ResourceContextWasInvalid(optionId: GroupPlanOptionId, resourceType: GroupPlanOptionResourceType)
      extends TourGroupApplicationError(
        s"Option '${optionId.value}' has invalid resource context for resource type $resourceType"
      )
  case MembershipTravelerWasNotFound(travelerId: TravelerId)
      extends TourGroupApplicationError(s"Traveler '${travelerId.value}' was not found")
  case MembershipTravelerWasNotOwnedByUser(travelerId: TravelerId, userId: UserId)
      extends TourGroupApplicationError(s"Traveler '${travelerId.value}' is not owned by user '${userId.value}'")
  case SelectionOrderCreationFailed(selectionId: GroupPlanSelectionId)
      extends TourGroupApplicationError(s"Selection '${selectionId.value}' could not be converted into an order")

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

final class LiveTourGroupApplicationService[F[_]: MonadThrow](
    tourGroupRepository: TourGroupRepository[F],
    userRepository: UserRepository[F],
    travelerProfileRepository: TravelerProfileRepository[F],
    orderService: OrderService[F],
    orderLifecycleApplicationService: OrderLifecycleApplicationService[F],
    orderRepository: com.typesafe.travel.order.domain.OrderRepository[F],
    flightBookingApplicationService: FlightBookingApplicationService[F],
    hotelBookingApplicationService: HotelBookingApplicationService[F],
    trainBookingApplicationService: TrainBookingApplicationService[F],
    attractionBookingApplicationService: AttractionBookingApplicationService[F],
    chatAttachmentStorage: TourGroupChatAttachmentStorage[F]
) extends TourGroupApplicationService[F]:

  override def createGroup(
      organizerUserId: UserId,
      title: String,
      description: String,
      destination: String,
      startDate: LocalDate,
      endDate: LocalDate,
      capacity: Int,
      createdAt: Instant
  ): F[TourGroupDetailsView] =
    for
      groupId <- tourGroupRepository.nextGroupId
      membershipId <- tourGroupRepository.nextMembershipId
      group <- createTourGroup(groupId, organizerUserId, title, description, destination, startDate, endDate, capacity, createdAt).liftTo[F]
      organizerMembership = createOrganizerMembership(membershipId, groupId, organizerUserId, createdAt)
      _ <- tourGroupRepository.saveGroup(group)
      _ <- tourGroupRepository.saveMembership(organizerMembership)
      _ <- ensureDefaultChatSettings(group, createdAt)
      publicConversation <- ensurePublicConversation(groupId, createdAt)
      _ <- ensureConversationParticipant(publicConversation.conversationId, organizerUserId, group.organizerUserId, createdAt)
      detailsView <- getGroupDetails(groupId)
    yield detailsView

  override def listGroups: F[List[TourGroupDetailsView]] =
    for
      groups <- tourGroupRepository.listGroups
      details <- groups.sortBy(_.createdAt.toEpochMilli).reverse.traverse(group => getGroupDetails(group.groupId))
    yield details

  override def getGroupDetails(groupId: TourGroupId): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      linkedOrders <- loadLinkedOrders(details.selectionOrderLinks)
    yield TourGroupDetailsView(details, linkedOrders)

  override def joinGroup(groupId: TourGroupId, userId: UserId, joinedAt: Instant): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- details.group.ensureOpen.liftTo[F]
      _ <- details.memberships.find(m => m.userId == userId && m.status == TourGroupMembershipStatus.Active) match
        case Some(_) => TourGroupError.ActiveMembershipAlreadyExists(groupId, userId).raiseError[F, Unit]
        case None    => ().pure[F]
      membershipId <- tourGroupRepository.nextMembershipId
      membership = createMemberMembership(membershipId, groupId, userId, joinedAt)
      _ <- tourGroupRepository.saveMembership(membership)
      publicConversation <- ensurePublicConversation(groupId, joinedAt)
      _ <- ensureConversationParticipant(publicConversation.conversationId, userId, details.group.organizerUserId, joinedAt)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def addMembershipTraveler(groupId: TourGroupId, userId: UserId, travelerId: TravelerId, joinedAt: Instant): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      membership <- details.memberships.find(m => m.userId == userId && m.status == TourGroupMembershipStatus.Active).liftTo[F](TourGroupError.MembershipWasNotFound(TourGroupMembershipId(s"membership-$userId")))
      _ <- ensureTravelerBelongsToUser(userId, travelerId)
      _ <- if details.membershipTravelers.exists(mt => mt.membershipId == membership.membershipId && mt.travelerId == travelerId && mt.status == TourGroupMembershipTravelerStatus.Active) then
        TourGroupError.MembershipTravelerAlreadyExists(membership.membershipId, travelerId).raiseError[F, Unit]
      else ().pure[F]
      projectedUsedCapacity = details.usedCapacity + 1
      _ <- if projectedUsedCapacity > details.group.capacity then
        TourGroupError.GroupCapacityWasExceeded(groupId, details.group.capacity, projectedUsedCapacity).raiseError[F, Unit]
      else ().pure[F]
      membershipTravelerId <- tourGroupRepository.nextMembershipTravelerId
      membershipTraveler = TourGroupMembershipTraveler(membershipTravelerId, membership.membershipId, travelerId, joinedAt, TourGroupMembershipTravelerStatus.Active)
      _ <- tourGroupRepository.saveMembershipTraveler(membershipTraveler)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def createPlanItem(
      groupId: TourGroupId,
      organizerUserId: UserId,
      itemType: GroupPlanItemType,
      title: String,
      description: String,
      scheduledAt: Instant,
      endsAt: Option[Instant],
      sequenceNo: Int
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- details.group.ensureOrganizer(organizerUserId).liftTo[F]
      planItemId <- tourGroupRepository.nextPlanItemId
      planItem <- createGroupPlanItem(planItemId, groupId, itemType, title, description, scheduledAt, endsAt, sequenceNo).liftTo[F]
      _ <- tourGroupRepository.savePlanItem(planItem)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def createPlanOption(
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
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      _ <- details.group.ensureOrganizer(organizerUserId).liftTo[F]
      planItem <- details.planItemById(planItemId).liftTo[F]
      _ <- ensureOptionResourceTypeMatchesPlanItem(planItem, resourceType).liftTo[F]
      optionId <- tourGroupRepository.nextPlanOptionId
      option <- createGroupPlanOption(optionId, planItemId, resourceType, resourceId, resourceVariantCode, resourceContext, label, description, defaultQuantity).liftTo[F]
      _ <- tourGroupRepository.savePlanOption(option)
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def createSelection(
      groupId: TourGroupId,
      actingUserId: UserId,
      planItemId: GroupPlanItemId,
      optionId: GroupPlanOptionId,
      quantity: Int,
      travelerIds: List[TravelerId],
      createdAt: Instant
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetails(groupId)
      membership <- details.memberships.find(m => m.userId == actingUserId && m.status == TourGroupMembershipStatus.Active).liftTo[F](TourGroupError.MembershipWasNotFound(TourGroupMembershipId(s"membership-$actingUserId")))
      planItem <- details.planItemById(planItemId).liftTo[F]
      option <- details.planOptionById(optionId).liftTo[F]
      _ <- Either.cond(option.planItemId == planItemId, (), TourGroupError.PlanOptionDidNotBelongToPlanItem(option.optionId, planItemId)).liftTo[F]
      _ <- ensureSelectionTravelers(details, membership.membershipId, travelerIds)
      _ <- ensureSelectionQuantity(planItem.itemType, quantity, travelerIds.size, GroupPlanSelectionId("pending-selection")).liftTo[F]
      selectionId <- tourGroupRepository.nextSelectionId
      selection <- createGroupPlanSelection(selectionId, groupId, planItemId, optionId, membership.membershipId, quantity, createdAt).liftTo[F]
      _ <- tourGroupRepository.saveSelection(selection)
      _ <- travelerIds.distinct.traverse_ { travelerId =>
        tourGroupRepository.nextSelectionTravelerId.flatMap { selectionTravelerId =>
          tourGroupRepository.saveSelectionTraveler(GroupPlanSelectionTraveler(selectionTravelerId, selection.selectionId, travelerId))
        }
      }
      refreshed <- getGroupDetails(groupId)
    yield refreshed

  override def submitSelection(selectionId: GroupPlanSelectionId, actingUserId: UserId): F[TourGroupDetailsView] =
    mutateSelection(selectionId) { (details, selection) =>
      for
        membership <- details.membershipById(selection.membershipId)
        _ <- membership.ensureOwner(actingUserId)
        selectionTravelers = details.selectionTravelersFor(selection.selectionId)
        _ <- if selectionTravelers.nonEmpty then Right(()) else Left(TourGroupError.SelectionTravelerWasEmpty(selection.planItemId))
        updatedSelection <- selection.submit
      yield updatedSelection
    }

  override def confirmSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: Option[String], confirmedAt: Instant): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetailsBySelection(selectionId)
      selection <- details.selectionById(selectionId).liftTo[F]
      _ <- details.group.ensureOrganizer(organizerUserId).liftTo[F]
      updatedSelection <- selection.confirm(organizerUserId, confirmedAt, reviewNote).liftTo[F]
      _ <- tourGroupRepository.saveSelection(updatedSelection)
      _ <- ensureSelectionOrderLinked(details, updatedSelection, confirmedAt)
      refreshed <- getGroupDetails(details.group.groupId)
    yield refreshed

  override def rejectSelection(selectionId: GroupPlanSelectionId, organizerUserId: UserId, reviewNote: String, rejectedAt: Instant): F[TourGroupDetailsView] =
    mutateSelection(selectionId) { (details, selection) =>
      for
        _ <- details.group.ensureOrganizer(organizerUserId)
        updatedSelection <- selection.reject(organizerUserId, rejectedAt, reviewNote)
      yield updatedSelection
    }

  override def paySelection(selectionId: GroupPlanSelectionId, actingUserId: UserId, paymentMethod: PaymentMethod, paidAt: Instant): F[(TourGroupDetailsView, Order)] =
    paySelectionsInternal(List(selectionId), actingUserId, paymentMethod, paidAt).map { case (detailsView, paidOrders) =>
      detailsView -> paidOrders.head
    }

  override def batchConfirmSelections(
      selectionIds: List[GroupPlanSelectionId],
      organizerUserId: UserId,
      reviewNote: Option[String],
      confirmedAt: Instant
  ): F[TourGroupDetailsView] =
    batchUpdateSelections(selectionIds) { (details, selection) =>
      for
        _ <- details.group.ensureOrganizer(organizerUserId)
        updatedSelection <- selection.confirm(organizerUserId, confirmedAt, reviewNote)
      yield updatedSelection
    }.flatMap { details =>
      selectionIds.distinct.traverse_(selectionId =>
        details.selectionById(selectionId).liftTo[F].flatMap(selection => ensureSelectionOrderLinked(details, selection, confirmedAt))
      ) *> getGroupDetails(details.group.groupId)
    }

  override def batchRejectSelections(
      selectionIds: List[GroupPlanSelectionId],
      organizerUserId: UserId,
      reviewNote: String,
      rejectedAt: Instant
  ): F[TourGroupDetailsView] =
    batchUpdateSelections(selectionIds) { (details, selection) =>
      for
        _ <- details.group.ensureOrganizer(organizerUserId)
        updatedSelection <- selection.reject(organizerUserId, rejectedAt, reviewNote)
      yield updatedSelection
    }.flatMap(details => getGroupDetails(details.group.groupId))

  override def batchPaySelections(
      selectionIds: List[GroupPlanSelectionId],
      actingUserId: UserId,
      paymentMethod: PaymentMethod,
      paidAt: Instant
  ): F[(TourGroupDetailsView, List[Order])] =
    paySelectionsInternal(selectionIds, actingUserId, paymentMethod, paidAt)

  private def paySelectionsInternal(
      selectionIds: List[GroupPlanSelectionId],
      actingUserId: UserId,
      paymentMethod: PaymentMethod,
      paidAt: Instant
  ): F[(TourGroupDetailsView, List[Order])] =
    for
      normalizedSelectionIds <- normalizeSelectionIds(selectionIds).liftTo[F]
      anchorDetails <- loadGroupDetailsBySelection(normalizedSelectionIds.head)
      validatedSelections <- normalizedSelectionIds.traverse(selectionId => loadPayableSelection(anchorDetails, selectionId, actingUserId))
      paidOrders <- validatedSelections.traverse { case (selection, membership, planItem, option, travelerIds) =>
        for
          order <- getOrCreateSelectionOrder(anchorDetails, selection, membership, planItem, option, travelerIds, paidAt)
          paidOrder <- orderLifecycleApplicationService.payOrder(order.orderId, paymentMethod, paymentSucceeded = true, currentTime = paidAt)
          convertedSelection <- selection.markConvertedToOrder.liftTo[F]
          _ <- tourGroupRepository.saveSelection(convertedSelection)
        yield paidOrder
      }
      refreshed <- getGroupDetails(anchorDetails.group.groupId)
    yield refreshed -> paidOrders

  override def listGroupBookings(groupId: TourGroupId): F[List[Order]] =
    loadGroupDetails(groupId).flatMap(details => loadLinkedOrders(details.selectionOrderLinks).map(_.values.toList.sortBy(_.createdAt.toEpochMilli).reverse))

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
      storagePath = stored.absolutePath.toString,
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

  private def mutateSelection(
      selectionId: GroupPlanSelectionId
  )(
      updater: (TourGroupDetails, GroupPlanSelection) => Either[TourGroupError, GroupPlanSelection]
  ): F[TourGroupDetailsView] =
    for
      details <- loadGroupDetailsBySelection(selectionId)
      selection <- details.selectionById(selectionId).liftTo[F]
      updatedSelection <- updater(details, selection).liftTo[F]
      _ <- tourGroupRepository.saveSelection(updatedSelection)
      refreshed <- getGroupDetails(details.group.groupId)
    yield refreshed

  private def batchUpdateSelections(
      selectionIds: List[GroupPlanSelectionId]
  )(
      updater: (TourGroupDetails, GroupPlanSelection) => Either[TourGroupError, GroupPlanSelection]
  ): F[TourGroupDetails] =
    for
      normalizedSelectionIds <- normalizeSelectionIds(selectionIds).liftTo[F]
      anchorDetails <- loadGroupDetailsBySelection(normalizedSelectionIds.head)
      _ <- normalizedSelectionIds.traverse_ { selectionId =>
        for
          details <- loadGroupDetailsBySelection(selectionId)
          _ <- if details.group.groupId == anchorDetails.group.groupId then ().pure[F]
          else TourGroupError.SelectionWasNotFound(selectionId).raiseError[F, Unit]
          selection <- details.selectionById(selectionId).liftTo[F]
          updatedSelection <- updater(anchorDetails, selection).liftTo[F]
          _ <- tourGroupRepository.saveSelection(updatedSelection)
        yield ()
      }
      refreshedDetails <- loadGroupDetails(anchorDetails.group.groupId)
    yield refreshedDetails

  private def loadGroupDetails(groupId: TourGroupId): F[TourGroupDetails] =
    tourGroupRepository.findGroupDetails(groupId).flatMap(_.liftTo[F](TourGroupError.GroupWasNotFound(groupId)))

  private def loadGroupDetailsBySelection(selectionId: GroupPlanSelectionId): F[TourGroupDetails] =
    tourGroupRepository
      .findSelectionById(selectionId)
      .flatMap(_.liftTo[F](TourGroupError.SelectionWasNotFound(selectionId)))
      .flatMap(selection => loadGroupDetails(selection.groupId))

  private def loadLinkedOrders(links: Vector[GroupSelectionOrderLink]): F[Map[GroupPlanSelectionId, Order]] =
    links.toList.traverse { link =>
      orderRepository.findOrderById(link.orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(link.orderId))).map(order => link.selectionId -> order)
    }.map(_.toMap)

  private def ensureTravelerBelongsToUser(userId: UserId, travelerId: TravelerId): F[Unit] =
    travelerProfileRepository.findTravelerProfileById(travelerId).flatMap {
      case None => TourGroupApplicationError.MembershipTravelerWasNotFound(travelerId).raiseError[F, Unit]
      case Some(travelerProfile) if travelerProfile.ownerUserId == userId => ().pure[F]
      case Some(_) => TourGroupApplicationError.MembershipTravelerWasNotOwnedByUser(travelerId, userId).raiseError[F, Unit]
    }

  private def ensureSelectionTravelers(
      details: TourGroupDetails,
      membershipId: TourGroupMembershipId,
      travelerIds: List[TravelerId]
  ): F[Unit] =
    if travelerIds.distinct.isEmpty then TourGroupError.SelectionTravelerWasEmpty(GroupPlanItemId("plan-item")).raiseError[F, Unit]
    else
      travelerIds.distinct.traverse_ { travelerId =>
        if details.membershipTravelers.exists(mt => mt.membershipId == membershipId && mt.travelerId == travelerId && mt.status == TourGroupMembershipTravelerStatus.Active) then
          ().pure[F]
        else
          TourGroupError.SelectionTravelerWasNotInMembership(GroupPlanSelectionId("selection"), travelerId).raiseError[F, Unit]
      }

  private def ensureSelectionQuantity(
      itemType: GroupPlanItemType,
      quantity: Int,
      travelerCount: Int,
      selectionId: GroupPlanSelectionId
  ): Either[TourGroupError, Unit] =
    if quantity <= 0 then Left(TourGroupError.SelectionQuantityWasInvalid(quantity))
    else
      itemType match
        case GroupPlanItemType.Flight | GroupPlanItemType.Train | GroupPlanItemType.Attraction =>
          Either.cond(quantity == travelerCount, (), TourGroupError.SelectionQuantityDidNotMatchTravelerCount(selectionId, quantity, travelerCount))
        case GroupPlanItemType.Hotel =>
          Right(())

  private def ensureOptionResourceTypeMatchesPlanItem(
      planItem: GroupPlanItem,
      resourceType: GroupPlanOptionResourceType
  ): Either[TourGroupError, Unit] =
    val compatible =
      (planItem.itemType, resourceType) match
        case (GroupPlanItemType.Flight, GroupPlanOptionResourceType.Flight)                 => true
        case (GroupPlanItemType.Hotel, GroupPlanOptionResourceType.HotelRoomType)           => true
        case (GroupPlanItemType.Train, GroupPlanOptionResourceType.TrainJourneySeat)        => true
        case (GroupPlanItemType.Attraction, GroupPlanOptionResourceType.AttractionTicketType) => true
        case _                                                                              => false
    Either.cond(compatible, (), TourGroupError.PlanOptionResourceTypeDidNotMatchPlanItem(planItem.planItemId, planItem.itemType, resourceType))

  private def ensureSelectionOrderLinked(
      details: TourGroupDetails,
      selection: GroupPlanSelection,
      currentTime: Instant
  ): F[Unit] =
    tourGroupRepository.findSelectionOrderLinkBySelectionId(selection.selectionId).flatMap {
      case Some(_) =>
        ().pure[F]
      case None =>
        for
          membership <- details.membershipById(selection.membershipId).liftTo[F]
          planItem <- details.planItemById(selection.planItemId).liftTo[F]
          option <- details.planOptionById(selection.optionId).liftTo[F]
          travelerIds = details.selectionTravelersFor(selection.selectionId).map(_.travelerId).toList
          _ <- ensureSelectionTravelers(details, membership.membershipId, travelerIds)
          _ <- ensureSelectionQuantity(planItem.itemType, selection.quantity, travelerIds.size, selection.selectionId).liftTo[F]
          order <- createOrderFromSelection(
            actingUserId = membership.userId,
            planItem = planItem,
            option = option,
            travelerIds = travelerIds,
            quantity = selection.quantity,
            currentTime = currentTime
          )
          linkId <- tourGroupRepository.nextSelectionOrderLinkId
          _ <- tourGroupRepository.saveSelectionOrderLink(GroupSelectionOrderLink(linkId, selection.selectionId, order.orderId, currentTime))
        yield ()
    }

  private def getOrCreateSelectionOrder(
      details: TourGroupDetails,
      selection: GroupPlanSelection,
      membership: TourGroupMembership,
      planItem: GroupPlanItem,
      option: GroupPlanOption,
      travelerIds: List[TravelerId],
      currentTime: Instant
  ): F[Order] =
    tourGroupRepository.findSelectionOrderLinkBySelectionId(selection.selectionId).flatMap {
      case Some(link) =>
        orderRepository.findOrderById(link.orderId).flatMap(_.liftTo[F](OrderError.OrderWasNotFound(link.orderId)))
      case None =>
        for
          order <- createOrderFromSelection(membership.userId, planItem, option, travelerIds, selection.quantity, currentTime)
          linkId <- tourGroupRepository.nextSelectionOrderLinkId
          _ <- tourGroupRepository.saveSelectionOrderLink(GroupSelectionOrderLink(linkId, selection.selectionId, order.orderId, currentTime))
        yield order
    }

  private def loadPayableSelection(
      details: TourGroupDetails,
      selectionId: GroupPlanSelectionId,
      actingUserId: UserId
  ): F[(GroupPlanSelection, TourGroupMembership, GroupPlanItem, GroupPlanOption, List[TravelerId])] =
    for
      selection <- details.selectionById(selectionId).liftTo[F]
      membership <- details.membershipById(selection.membershipId).liftTo[F]
      _ <- membership.ensureOwner(actingUserId).liftTo[F]
      _ <- if selection.status == GroupPlanSelectionStatus.OrganizerConfirmed then ().pure[F]
      else TourGroupError.SelectionWasNotPayable(selection.selectionId, selection.status).raiseError[F, Unit]
      planItem <- details.planItemById(selection.planItemId).liftTo[F]
      option <- details.planOptionById(selection.optionId).liftTo[F]
      travelerIds = details.selectionTravelersFor(selection.selectionId).map(_.travelerId).toList
      _ <- ensureSelectionTravelers(details, membership.membershipId, travelerIds)
      _ <- ensureSelectionQuantity(planItem.itemType, selection.quantity, travelerIds.size, selection.selectionId).liftTo[F]
    yield (selection, membership, planItem, option, travelerIds)

  private def normalizeSelectionIds(selectionIds: List[GroupPlanSelectionId]): Either[TourGroupError, List[GroupPlanSelectionId]] =
    Either.cond(selectionIds.distinct.nonEmpty, selectionIds.distinct, TourGroupError.SelectionWasNotFound(GroupPlanSelectionId("selection")))

  private def createOrderFromSelection(
      actingUserId: UserId,
      planItem: GroupPlanItem,
      option: GroupPlanOption,
      travelerIds: List[TravelerId],
      quantity: Int,
      currentTime: Instant
  ): F[Order] =
    option.resourceType match
      case GroupPlanOptionResourceType.Flight =>
        for
          cabinClassText <- option.resourceVariantCode.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          cabinClass <- CabinClass.create(cabinClassText).liftTo[F]
          order <- flightBookingApplicationService.createFlightOrder(
            actingUserId = actingUserId,
            flightId = FlightId(option.resourceId),
            travelerIds = travelerIds,
            cabinClass = cabinClass
          )
        yield order
      case GroupPlanOptionResourceType.HotelRoomType =>
        for
          checkOutAt <- planItem.endsAt.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          roomCount <- RoomCount.create(quantity).liftTo[F]
          order <- hotelBookingApplicationService.createHotelOrder(
            actingUserId = actingUserId,
            roomTypeId = RoomTypeId(option.resourceId),
            guestTravelerIds = travelerIds,
            checkInDate = planItem.scheduledAt.atZone(ZoneOffset.UTC).toLocalDate,
            checkOutDate = checkOutAt.atZone(ZoneOffset.UTC).toLocalDate,
            roomCount = roomCount
          )
        yield order
      case GroupPlanOptionResourceType.TrainJourneySeat =>
        for
          seatClassText <- option.resourceVariantCode.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          trainContext <- option.resourceContext.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          stationParts <- parseResourceContext(trainContext, option.optionId, option.resourceType)
          seatClass <- TrainSeatClass.create(seatClassText).liftTo[F]
          trainJourney <- trainBookingApplicationService.getTrainDetails(TrainId(option.resourceId))
          quote <- trainJourney.quote(
            fromStationCode = TrainStationCode.unsafe(stationParts._1),
            toStationCode = TrainStationCode.unsafe(stationParts._2),
            seatClass = seatClass,
            currentTime = currentTime
          ).liftTo[F]
          draftOrder <- orderService.createDraftOrder(actingUserId, quote.unitPrice.currency, currentTime)
          orderWithItem <- trainBookingApplicationService.addTrainItemToOrder(
            actingUserId = actingUserId,
            orderId = draftOrder.orderId,
            trainId = TrainId(option.resourceId),
            travelerIds = travelerIds,
            fromStationCode = TrainStationCode.unsafe(stationParts._1),
            toStationCode = TrainStationCode.unsafe(stationParts._2),
            seatClass = seatClass
          )
        yield orderWithItem
      case GroupPlanOptionResourceType.AttractionTicketType =>
        for
          attractionIdText <- option.resourceContext.liftTo[F](TourGroupApplicationError.ResourceContextWasInvalid(option.optionId, option.resourceType))
          attraction <- attractionBookingApplicationService.getAttractionDetails(AttractionId(attractionIdText))
          ticketType <- attraction.findTicketType(TicketTypeId(option.resourceId)).liftTo[F]
          draftOrder <- orderService.createDraftOrder(actingUserId, ticketType.unitPrice.currency, currentTime)
          orderWithItem <- attractionBookingApplicationService.addAttractionItemToOrder(
            actingUserId = actingUserId,
            orderId = draftOrder.orderId,
            attractionId = AttractionId(attractionIdText),
            ticketTypeId = TicketTypeId(option.resourceId),
            travelerIds = travelerIds,
            useDate = planItem.scheduledAt.atZone(ZoneOffset.UTC).toLocalDate,
            now = currentTime
          )
        yield orderWithItem

  private def parseResourceContext(
      resourceContext: String,
      optionId: GroupPlanOptionId,
      resourceType: GroupPlanOptionResourceType
  ): F[(String, String)] =
    resourceContext.split("\\|", 2).map(_.trim).toList match
      case from :: to :: Nil if from.nonEmpty && to.nonEmpty => (from, to).pure[F]
      case _ => TourGroupApplicationError.ResourceContextWasInvalid(optionId, resourceType).raiseError[F, (String, String)]

  private def getOrCreateChatSettings(group: TourGroup, currentTime: Instant): F[TourGroupChatSettings] =
    tourGroupRepository.findChatSettingsByGroupId(group.groupId).flatMap {
      case Some(settings) => settings.pure[F]
      case None => ensureDefaultChatSettings(group, currentTime)
    }

  private def ensureDefaultChatSettings(group: TourGroup, currentTime: Instant): F[TourGroupChatSettings] =
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

  private def ensurePublicConversation(groupId: TourGroupId, currentTime: Instant): F[TourGroupConversation] =
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

  private def ensureConversationParticipant(
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

  private def ensureActiveGroupMember(details: TourGroupDetails, userId: UserId): Either[TourGroupError, TourGroupMembership] =
    details.memberships.find(membership => membership.userId == userId && membership.status == TourGroupMembershipStatus.Active)
      .toRight(TourGroupError.GroupMemberWasNotFound(details.group.groupId, userId))

  private def ensureDirectTarget(details: TourGroupDetails, actingUserId: UserId, targetUserId: UserId): Either[TourGroupError, Unit] =
    if actingUserId == targetUserId then Left(TourGroupError.DirectConversationTargetWasInvalid(targetUserId))
    else ensureActiveGroupMember(details, targetUserId).map(_ => ())

  private def ensureDirectConversationAllowed(
      group: TourGroup,
      settings: TourGroupChatSettings,
      actingUserId: UserId,
      targetUserId: UserId
  ): Either[TourGroupError, Unit] =
    val organizerUserId = group.organizerUserId
    val isOrganizerPair = actingUserId == organizerUserId || targetUserId == organizerUserId
    Either.cond(isOrganizerPair || settings.allowMemberDirectChat, (), TourGroupError.DirectConversationWasNotAllowed(group.groupId, actingUserId, targetUserId))

  private def createDirectConversation(
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

  private def loadConversation(conversationId: TourGroupConversationId): F[TourGroupConversation] =
    tourGroupRepository.findConversationById(conversationId).flatMap(_.liftTo[F](TourGroupError.ConversationWasNotFound(conversationId)))

  private def ensureConversationParticipantAccess(conversationId: TourGroupConversationId, actingUserId: UserId): F[Unit] =
    tourGroupRepository.findConversationParticipant(conversationId, actingUserId).flatMap {
      case Some(participant) if participant.status == TourGroupConversationParticipantStatus.Active => ().pure[F]
      case _ => TourGroupError.ConversationAccessWasDenied(conversationId, actingUserId).raiseError[F, Unit]
    }

  private def ensureConversationParticipantAccess(
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

  private def normalizeMessageContent(content: String): Either[TourGroupError, String] =
    Option(content).map(_.trim).filter(_.nonEmpty).toRight(TourGroupError.MessageContentWasEmpty())

  private def normalizeMessagePayload(
      content: String,
      attachmentRefs: List[TourGroupUploadedAttachmentView]
  ): Either[TourGroupError, String] =
    val trimmed = Option(content).map(_.trim).getOrElse("")
    if trimmed.nonEmpty || attachmentRefs.nonEmpty then Right(trimmed) else Left(TourGroupError.MessageContentWasEmpty())

  private def inferFinalMessageType(
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

  private def normalizeSearchQuery(query: String): Either[TourGroupError, String] =
    Option(query).map(_.trim.toLowerCase).filter(_.nonEmpty).toRight(TourGroupError.MessageContentWasEmpty())

  private def normalizeReactionType(reactionType: String): Either[TourGroupError, String] =
    Option(reactionType).map(_.trim).filter(_.nonEmpty).toRight(TourGroupError.MessageReactionTypeWasInvalid(reactionType))

  private def normalizeAttachmentFileName(fileName: String): Either[TourGroupError, String] =
    Option(fileName).map(_.trim).filter(_.nonEmpty).toRight(TourGroupError.MessageAttachmentUploadWasNotAllowed("fileName"))

  private def inferAttachmentType(mimeType: String, fileName: String): Either[TourGroupError, TourGroupMessageAttachmentType] =
    val normalizedMimeType = Option(mimeType).getOrElse("").trim.toLowerCase
    if normalizedMimeType.startsWith("image/") then Right(TourGroupMessageAttachmentType.Image)
    else if extractFileExtension(fileName).nonEmpty then Right(TourGroupMessageAttachmentType.File)
    else Left(TourGroupError.MessageAttachmentUploadWasNotAllowed(fileName))

  private def validateAttachmentSize(
      attachmentType: TourGroupMessageAttachmentType,
      fileSize: Long
  ): Either[TourGroupError, Unit] =
    val maxBytes =
      attachmentType match
        case TourGroupMessageAttachmentType.Image => 8L * 1024 * 1024
        case TourGroupMessageAttachmentType.File  => 20L * 1024 * 1024
    Either.cond(fileSize > 0 && fileSize <= maxBytes, (), TourGroupError.MessageAttachmentUploadWasNotAllowed(s"size:$fileSize"))

  private def validateAttachmentMimeType(
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

  private def toAttachmentCollection(
      attachmentType: TourGroupMessageAttachmentType
  ): TourGroupChatAttachmentCollection =
    attachmentType match
      case TourGroupMessageAttachmentType.Image => TourGroupChatAttachmentCollection.Image
      case TourGroupMessageAttachmentType.File  => TourGroupChatAttachmentCollection.File

  private def extractFileExtension(fileName: String): String =
    fileName.split('.').toList.lastOption.filter(_.nonEmpty).getOrElse("bin")

  private def touchConversation(conversation: TourGroupConversation, updatedAt: Instant): F[TourGroupConversation] =
    tourGroupRepository.saveConversation(conversation.copy(updatedAt = updatedAt))

  private def latestConversationTimestamp(conversation: TourGroupConversation): Instant =
    if conversation.updatedAt == Instant.EPOCH then conversation.createdAt else conversation.updatedAt

  private def latestMessageTimestamp(message: TourGroupMessage): Instant =
    if message.updatedAt == Instant.EPOCH then message.createdAt else message.updatedAt

  private def markParticipantRead(
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

  private def ensureReplyTarget(
      conversationId: TourGroupConversationId,
      replyToMessageId: TourGroupMessageId
  ): F[Unit] =
    tourGroupRepository.findMessageById(replyToMessageId).flatMap {
      case Some(message) if message.conversationId == conversationId => ().pure[F]
      case _ => TourGroupError.MessageWasNotFound(replyToMessageId).raiseError[F, Unit]
    }

  private def loadMessageContext(
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

  private def mutateConversationMessage(
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

  private def canSendInConversation(
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

  private def buildMessageViews(
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

  private def buildConversationSummary(
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
      participantsSummary = participantUserIds.map(_.value).mkString(", ")
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

  private def buildSingleMessageView(
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

  private def renderMessagePreview(
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

  private def findUserDisplayInfo(userId: UserId): F[(String, Option[String])] =
    userRepository.findByUserId(userId).map {
      case Some(user) => user.userDisplayName.value -> user.avatarUrl.map(_.value)
      case None       => userId.value -> Option.empty[String]
    }

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

