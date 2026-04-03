package com.typesafe.travel.api.dto

import com.typesafe.travel.api.application.*
import com.typesafe.travel.shared.kernel.TourGroupMessageAttachmentId
import com.typesafe.travel.tourgroup.domain.*

import java.time.Instant

final case class CreateTourGroupRequestDto(
    organizerUserId: String,
    title: String,
    description: String,
    destination: String,
    startDate: String,
    endDate: String,
    capacity: Int
)

final case class JoinTourGroupRequestDto(userId: String)
final case class AddMembershipTravelerRequestDto(userId: String, travelerId: String)
final case class CreateGroupPlanItemRequestDto(
    organizerUserId: String,
    itemType: String,
    title: String,
    description: String,
    scheduledAt: String,
    endsAt: Option[String],
    sequenceNo: Int
)
final case class CreateGroupPlanOptionRequestDto(
    organizerUserId: String,
    resourceType: String,
    resourceId: String,
    resourceVariantCode: Option[String],
    resourceContext: Option[String],
    label: String,
    description: String,
    defaultQuantity: Int
)
final case class CreateGroupPlanSelectionRequestDto(
    userId: String,
    optionId: String,
    quantity: Int,
    travelerIds: List[String]
)
final case class SubmitGroupPlanSelectionRequestDto(userId: String)
final case class ReviewGroupPlanSelectionRequestDto(organizerUserId: String, reviewNote: Option[String])
final case class RejectGroupPlanSelectionRequestDto(organizerUserId: String, reviewNote: String)
final case class PayGroupPlanSelectionRequestDto(userId: String, paymentMethod: String)
final case class BatchReviewGroupPlanSelectionsRequestDto(organizerUserId: String, selectionIds: List[String], reviewNote: Option[String])
final case class BatchRejectGroupPlanSelectionsRequestDto(organizerUserId: String, selectionIds: List[String], reviewNote: String)
final case class BatchPayGroupPlanSelectionsRequestDto(userId: String, selectionIds: List[String], paymentMethod: String)

final case class UpdateTourGroupChatSettingsRequestDto(allowMemberDirectChat: Boolean)
final case class CreateDirectConversationRequestDto(targetUserId: String)
final case class SendTourGroupMessageRequestDto(
    messageType: Option[String],
    content: String,
    replyToMessageId: Option[String],
    attachments: List[TourGroupUploadedAttachmentResponseDto]
)
final case class EditTourGroupMessageRequestDto(content: String)
final case class ReactTourGroupMessageRequestDto(reactionType: String)
final case class UpdateConversationMuteRequestDto(muted: Boolean)
final case class UpdateConversationArchiveRequestDto(archived: Boolean)

final case class TourGroupSummaryResponseDto(
    groupId: String,
    organizerUserId: String,
    title: String,
    description: String,
    destination: String,
    startDate: String,
    endDate: String,
    capacity: Int,
    usedCapacity: Int,
    isFull: Boolean,
    memberCount: Int,
    activeTravelerCount: Int,
    pendingSelectionCount: Int,
    confirmedSelectionCount: Int,
    convertedOrderCount: Int,
    status: String,
    createdAt: String
)

final case class TourGroupMembershipResponseDto(membershipId: String, userId: String, status: String, joinedAt: String)
final case class TourGroupMembershipTravelerResponseDto(membershipTravelerId: String, membershipId: String, travelerId: String, status: String, joinedAt: String)
final case class GroupPlanItemResponseDto(planItemId: String, itemType: String, title: String, description: String, scheduledAt: String, endsAt: Option[String], sequenceNo: Int, status: String)
final case class GroupPlanOptionResponseDto(optionId: String, planItemId: String, resourceType: String, resourceId: String, resourceVariantCode: Option[String], resourceContext: Option[String], label: String, description: String, defaultQuantity: Int, status: String)
final case class GroupPlanSelectionResponseDto(selectionId: String, groupId: String, planItemId: String, optionId: String, membershipId: String, quantity: Int, travelerIds: List[String], status: String, createdAt: String, confirmedAt: Option[String], reviewedByOrganizerUserId: Option[String], reviewNote: Option[String])
final case class GroupSelectionOrderLinkResponseDto(selectionId: String, orderId: String, createdAt: String)
final case class GroupSelectionOrderProjectionResponseDto(selectionId: String, orderId: String, orderStatus: String, paymentStatus: String, supplierReviewStatus: String, refundStatus: Option[String], bookingSummaryLabel: String)

final case class TourGroupDetailsResponseDto(
    group: TourGroupSummaryResponseDto,
    memberships: List[TourGroupMembershipResponseDto],
    membershipTravelers: List[TourGroupMembershipTravelerResponseDto],
    planItems: List[GroupPlanItemResponseDto],
    planOptions: List[GroupPlanOptionResponseDto],
    selections: List[GroupPlanSelectionResponseDto],
    selectionOrderLinks: List[GroupSelectionOrderLinkResponseDto],
    selectionOrderProjections: List[GroupSelectionOrderProjectionResponseDto],
    bookings: List[OrderResponseDto]
)

final case class TourGroupListResponseDto(groups: List[TourGroupSummaryResponseDto])
final case class TourGroupBatchPayResponseDto(group: TourGroupDetailsResponseDto, orders: List[OrderResponseDto])

final case class TourGroupChatSettingsResponseDto(
    groupId: String,
    allowMemberDirectChat: Boolean,
    updatedAt: String,
    updatedByUserId: String,
    canUpdate: Boolean
)

final case class TourGroupConversationSummaryResponseDto(
    conversationId: String,
    conversationType: String,
    status: String,
    counterpartUserId: Option[String],
    counterpartDisplayName: Option[String],
    counterpartAvatarUrl: Option[String],
    conversationTitle: String,
    participantsSummary: String,
    lastMessagePreview: Option[String],
    lastMessageAt: Option[String],
    unreadCount: Int,
    isMuted: Boolean,
    isArchived: Boolean,
    canSendMessage: Boolean
)

final case class TourGroupConversationListResponseDto(
    conversations: List[TourGroupConversationSummaryResponseDto],
    groupChatConversationId: Option[String]
)

final case class TourGroupMessageAttachmentResponseDto(
    attachmentId: String,
    attachmentType: String,
    publicUrl: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long
)

final case class TourGroupUploadedAttachmentResponseDto(
    attachmentId: String,
    attachmentType: String,
    publicUrl: String,
    storagePath: String,
    originalFileName: String,
    mimeType: String,
    fileSize: Long,
    sortOrder: Int,
    createdAt: String
)

final case class TourGroupMessageReactionResponseDto(
    reactionType: String,
    count: Int,
    reactedByCurrentUser: Boolean
)

final case class TourGroupMessageResponseDto(
    messageId: String,
    conversationId: String,
    messageType: String,
    senderUserId: String,
    senderDisplayName: String,
    senderAvatarUrl: Option[String],
    content: String,
    replyToMessageId: Option[String],
    replyToPreview: Option[String],
    status: String,
    createdAt: String,
    updatedAt: String,
    attachments: List[TourGroupMessageAttachmentResponseDto],
    reactions: List[TourGroupMessageReactionResponseDto],
    canEdit: Boolean,
    canDelete: Boolean,
    canRecall: Boolean,
    canReact: Boolean,
    isMine: Boolean
)

final case class TourGroupMessageListResponseDto(messages: List[TourGroupMessageResponseDto])
final case class TourGroupMessageSearchResultResponseDto(
    conversationId: String,
    conversationTitle: String,
    message: TourGroupMessageResponseDto
)
final case class TourGroupMessageSearchResponseDto(results: List[TourGroupMessageSearchResultResponseDto])

object TourGroupSummaryResponseDto:
  def fromView(view: TourGroupDetailsView): TourGroupSummaryResponseDto =
    val group = view.details.group
    TourGroupSummaryResponseDto(
      groupId = group.groupId.value,
      organizerUserId = group.organizerUserId.value,
      title = group.title,
      description = group.description,
      destination = group.destination,
      startDate = group.startDate.toString,
      endDate = group.endDate.toString,
      capacity = group.capacity,
      usedCapacity = view.details.usedCapacity,
      isFull = view.details.isFull,
      memberCount = view.details.memberships.count(_.status == TourGroupMembershipStatus.Active),
      activeTravelerCount = view.details.membershipTravelers.count(_.status == TourGroupMembershipTravelerStatus.Active),
      pendingSelectionCount = view.details.selections.count(_.status == GroupPlanSelectionStatus.Submitted),
      confirmedSelectionCount = view.details.selections.count(_.status == GroupPlanSelectionStatus.OrganizerConfirmed),
      convertedOrderCount = view.details.selectionOrderLinks.size,
      status = group.status.toString,
      createdAt = group.createdAt.toString
    )

object TourGroupDetailsResponseDto:
  def fromView(view: TourGroupDetailsView): TourGroupDetailsResponseDto =
    TourGroupDetailsResponseDto(
      group = TourGroupSummaryResponseDto.fromView(view),
      memberships = view.details.memberships.toList.map(membership =>
        TourGroupMembershipResponseDto(membership.membershipId.value, membership.userId.value, membership.status.toString, membership.joinedAt.toString)
      ),
      membershipTravelers = view.details.membershipTravelers.toList.map(membershipTraveler =>
        TourGroupMembershipTravelerResponseDto(membershipTraveler.membershipTravelerId.value, membershipTraveler.membershipId.value, membershipTraveler.travelerId.value, membershipTraveler.status.toString, membershipTraveler.joinedAt.toString)
      ),
      planItems = view.details.planItems.toList.sortBy(_.sequenceNo).map(planItem =>
        GroupPlanItemResponseDto(planItem.planItemId.value, planItem.itemType.toString, planItem.title, planItem.description, planItem.scheduledAt.toString, planItem.endsAt.map(_.toString), planItem.sequenceNo, planItem.status.toString)
      ),
      planOptions = view.details.planOptions.toList.map(planOption =>
        GroupPlanOptionResponseDto(planOption.optionId.value, planOption.planItemId.value, planOption.resourceType.toString, planOption.resourceId, planOption.resourceVariantCode, planOption.resourceContext, planOption.label, planOption.description, planOption.defaultQuantity, planOption.status.toString)
      ),
      selections = view.details.selections.toList.map { selection =>
        GroupPlanSelectionResponseDto(
          selection.selectionId.value,
          selection.groupId.value,
          selection.planItemId.value,
          selection.optionId.value,
          selection.membershipId.value,
          selection.quantity,
          view.details.selectionTravelersFor(selection.selectionId).map(_.travelerId.value).toList,
          selection.status.toString,
          selection.createdAt.toString,
          selection.confirmedAt.map(_.toString),
          selection.reviewedByOrganizerUserId.map(_.value),
          selection.reviewNote
        )
      },
      selectionOrderLinks = view.details.selectionOrderLinks.toList.map(link => GroupSelectionOrderLinkResponseDto(link.selectionId.value, link.orderId.value, link.createdAt.toString)),
      selectionOrderProjections = TourGroupDetailsView.buildSelectionOrderProjectionViews(view).map(GroupSelectionOrderProjectionResponseDto.fromView),
      bookings = view.linkedOrders.values.toList.sortBy(_.createdAt.toEpochMilli).reverse.map(order => OrderResponseDto.fromDomain(order))
    )

object GroupSelectionOrderProjectionResponseDto:
  def fromView(view: GroupSelectionOrderProjectionView): GroupSelectionOrderProjectionResponseDto =
    GroupSelectionOrderProjectionResponseDto(view.selectionId.value, view.orderId.value, view.orderStatus, view.paymentStatus, view.supplierReviewStatus, view.refundStatus, view.bookingSummaryLabel)

object TourGroupChatSettingsResponseDto:
  def fromView(view: TourGroupChatSettingsView): TourGroupChatSettingsResponseDto =
    TourGroupChatSettingsResponseDto(view.groupId.value, view.allowMemberDirectChat, view.updatedAt.toString, view.updatedByUserId.value, view.canUpdate)

object TourGroupConversationSummaryResponseDto:
  def fromView(view: TourGroupConversationSummaryView): TourGroupConversationSummaryResponseDto =
    TourGroupConversationSummaryResponseDto(
      conversationId = view.conversationId.value,
      conversationType = view.conversationType.toString,
      status = view.status.toString,
      counterpartUserId = view.counterpartUserId.map(_.value),
      counterpartDisplayName = view.counterpartDisplayName,
      counterpartAvatarUrl = view.counterpartAvatarUrl,
      conversationTitle = view.conversationTitle,
      participantsSummary = view.participantsSummary,
      lastMessagePreview = view.lastMessagePreview,
      lastMessageAt = view.lastMessageAt.map(_.toString),
      unreadCount = view.unreadCount,
      isMuted = view.isMuted,
      isArchived = view.isArchived,
      canSendMessage = view.canSendMessage
    )

object TourGroupConversationListResponseDto:
  def fromView(view: TourGroupConversationListView): TourGroupConversationListResponseDto =
    TourGroupConversationListResponseDto(
      conversations = view.conversations.map(TourGroupConversationSummaryResponseDto.fromView),
      groupChatConversationId = view.groupChatConversationId.map(_.value)
    )

object TourGroupMessageAttachmentResponseDto:
  def fromView(view: TourGroupMessageAttachmentView): TourGroupMessageAttachmentResponseDto =
    TourGroupMessageAttachmentResponseDto(view.attachmentId.value, view.attachmentType.toString, view.publicUrl, view.originalFileName, view.mimeType, view.fileSize)

object TourGroupUploadedAttachmentResponseDto:
  def fromView(view: TourGroupUploadedAttachmentView): TourGroupUploadedAttachmentResponseDto =
    TourGroupUploadedAttachmentResponseDto(
      attachmentId = view.attachmentId.value,
      attachmentType = view.attachmentType.toString,
      publicUrl = view.publicUrl,
      storagePath = view.storagePath,
      originalFileName = view.originalFileName,
      mimeType = view.mimeType,
      fileSize = view.fileSize,
      sortOrder = view.sortOrder,
      createdAt = view.createdAt.toString
    )

  def toView(dto: TourGroupUploadedAttachmentResponseDto): TourGroupUploadedAttachmentView =
    TourGroupUploadedAttachmentView(
      attachmentId = TourGroupMessageAttachmentId(dto.attachmentId),
      attachmentType = TourGroupDtoMappers.toMessageAttachmentType(dto.attachmentType),
      publicUrl = dto.publicUrl,
      storagePath = dto.storagePath,
      originalFileName = dto.originalFileName,
      mimeType = dto.mimeType,
      fileSize = dto.fileSize,
      sortOrder = dto.sortOrder,
      createdAt = Instant.parse(dto.createdAt)
    )

object TourGroupMessageReactionResponseDto:
  def fromView(view: TourGroupMessageReactionView): TourGroupMessageReactionResponseDto =
    TourGroupMessageReactionResponseDto(view.reactionType, view.count, view.reactedByCurrentUser)

object TourGroupMessageResponseDto:
  def fromView(view: TourGroupMessageView): TourGroupMessageResponseDto =
    TourGroupMessageResponseDto(
      messageId = view.messageId.value,
      conversationId = view.conversationId.value,
      messageType = view.messageType.toString,
      senderUserId = view.senderUserId.value,
      senderDisplayName = view.senderDisplayName,
      senderAvatarUrl = view.senderAvatarUrl,
      content = view.content,
      replyToMessageId = view.replyToMessageId.map(_.value),
      replyToPreview = view.replyToPreview,
      status = view.status.toString,
      createdAt = view.createdAt.toString,
      updatedAt = view.updatedAt.toString,
      attachments = view.attachments.map(TourGroupMessageAttachmentResponseDto.fromView),
      reactions = view.reactions.map(TourGroupMessageReactionResponseDto.fromView),
      canEdit = view.canEdit,
      canDelete = view.canDelete,
      canRecall = view.canRecall,
      canReact = view.canReact,
      isMine = view.isMine
    )

object TourGroupMessageSearchResultResponseDto:
  def fromView(view: TourGroupMessageSearchResultView): TourGroupMessageSearchResultResponseDto =
    TourGroupMessageSearchResultResponseDto(view.conversationId.value, view.conversationTitle, TourGroupMessageResponseDto.fromView(view.message))

object TourGroupDtoMappers:
  def toPlanItemType(value: String): GroupPlanItemType =
    value.trim.toLowerCase match
      case "flight"     => GroupPlanItemType.Flight
      case "hotel"      => GroupPlanItemType.Hotel
      case "train"      => GroupPlanItemType.Train
      case "attraction" => GroupPlanItemType.Attraction
      case _            => GroupPlanItemType.Attraction

  def toResourceType(value: String): GroupPlanOptionResourceType =
    value.trim.toLowerCase match
      case "flight" => GroupPlanOptionResourceType.Flight
      case "hotelroomtype" | "hotel_room_type" | "hotel" => GroupPlanOptionResourceType.HotelRoomType
      case "trainjourneyseat" | "train_journey_seat" | "train" => GroupPlanOptionResourceType.TrainJourneySeat
      case "attractiontickettype" | "attraction_ticket_type" | "attraction" => GroupPlanOptionResourceType.AttractionTicketType
      case _ => GroupPlanOptionResourceType.AttractionTicketType

  def toMessageType(value: String): TourGroupMessageType =
    value.trim.toLowerCase match
      case "image" => TourGroupMessageType.Image
      case "file"  => TourGroupMessageType.File
      case "mixed" => TourGroupMessageType.Mixed
      case _       => TourGroupMessageType.Text

  def toMessageAttachmentType(value: String): TourGroupMessageAttachmentType =
    value.trim.toLowerCase match
      case "image" => TourGroupMessageAttachmentType.Image
      case _       => TourGroupMessageAttachmentType.File
