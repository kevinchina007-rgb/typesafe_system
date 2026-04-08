import type { OrderResponse } from './orders'

export type TourGroupSummaryResponse = {
  groupId: string
  organizerUserId: string
  title: string
  description: string
  destination: string
  startDate: string
  endDate: string
  capacity: number
  usedCapacity: number
  isFull: boolean
  memberCount: number
  activeTravelerCount: number
  pendingSelectionCount: number
  confirmedSelectionCount: number
  convertedOrderCount: number
  status: string
  createdAt: string
}

export type TourGroupMembershipResponse = {
  membershipId: string
  userId: string
  userDisplayName?: string
  status: string
  joinedAt: string
}

export type TourGroupMembershipTravelerResponse = {
  membershipTravelerId: string
  membershipId: string
  travelerId: string
  status: string
  joinedAt: string
}

export type GroupPlanItemResponse = {
  planItemId: string
  itemType: string
  title: string
  description: string
  scheduledAt: string
  endsAt: string | null
  sequenceNo: number
  status: string
}

export type GroupPlanOptionResponse = {
  optionId: string
  planItemId: string
  resourceType: string
  resourceId: string
  resourceVariantCode: string | null
  resourceContext: string | null
  label: string
  description: string
  defaultQuantity: number
  status: string
}

export type GroupPlanSelectionResponse = {
  selectionId: string
  groupId: string
  planItemId: string
  optionId: string
  membershipId: string
  quantity: number
  travelerIds: string[]
  status: string
  createdAt: string
  confirmedAt: string | null
  reviewedByOrganizerUserId: string | null
  reviewNote: string | null
}

export type GroupSelectionOrderLinkResponse = {
  selectionId: string
  orderId: string
  createdAt: string
}

export type GroupSelectionOrderProjectionResponse = {
  selectionId: string
  orderId: string
  orderStatus: string
  paymentStatus: string
  supplierReviewStatus: string
  refundStatus: string | null
  bookingSummaryLabel: string
}

export type TourGroupChatSettingsResponse = {
  groupId: string
  allowMemberDirectChat: boolean
  updatedAt: string
  updatedByUserId: string
  canUpdate: boolean
}

export type TourGroupConversationSummaryResponse = {
  conversationId: string
  conversationType: string
  status: string
  counterpartUserId: string | null
  counterpartDisplayName: string | null
  counterpartAvatarUrl: string | null
  conversationTitle: string
  participantsSummary: string
  lastMessagePreview: string | null
  lastMessageAt: string | null
  unreadCount: number
  isMuted: boolean
  isArchived: boolean
  canSendMessage: boolean
}

export type TourGroupConversationListResponse = {
  conversations: TourGroupConversationSummaryResponse[]
  groupChatConversationId: string | null
}

export type TourGroupMessageAttachmentResponse = {
  attachmentId: string
  attachmentType: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export type TourGroupUploadedAttachmentResponse = {
  attachmentId: string
  attachmentType: string
  publicUrl: string
  storagePath: string
  originalFileName: string
  mimeType: string
  fileSize: number
  sortOrder: number
  createdAt: string
}

export type TourGroupMessageReactionResponse = {
  reactionType: string
  count: number
  reactedByCurrentUser: boolean
}

export type TourGroupMessageResponse = {
  messageId: string
  conversationId: string
  messageType: string
  senderUserId: string
  senderDisplayName: string
  senderAvatarUrl: string | null
  content: string
  replyToMessageId: string | null
  replyToPreview: string | null
  status: string
  createdAt: string
  updatedAt: string
  attachments: TourGroupMessageAttachmentResponse[]
  reactions: TourGroupMessageReactionResponse[]
  canEdit: boolean
  canDelete: boolean
  canRecall: boolean
  canReact: boolean
  isMine: boolean
}

export type TourGroupMessageListResponse = {
  messages: TourGroupMessageResponse[]
}

export type TourGroupMessageSearchResultResponse = {
  conversationId: string
  conversationTitle: string
  message: TourGroupMessageResponse
}

export type TourGroupMessageSearchResponse = {
  results: TourGroupMessageSearchResultResponse[]
}

export type TourGroupDetailsResponse = {
  group: TourGroupSummaryResponse
  memberships: TourGroupMembershipResponse[]
  membershipTravelers: TourGroupMembershipTravelerResponse[]
  planItems: GroupPlanItemResponse[]
  planOptions: GroupPlanOptionResponse[]
  selections: GroupPlanSelectionResponse[]
  selectionOrderLinks: GroupSelectionOrderLinkResponse[]
  selectionOrderProjections: GroupSelectionOrderProjectionResponse[]
  bookings: OrderResponse[]
}

export type TourGroupListResponse = {
  groups: TourGroupSummaryResponse[]
}

export type TourGroupPaySelectionResponse = {
  group: TourGroupDetailsResponse
  orders: OrderResponse[]
}
