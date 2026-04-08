import type {
  OrderListResponse,
  TourGroupChatSettingsResponse,
  TourGroupConversationListResponse,
  TourGroupConversationSummaryResponse,
  TourGroupDetailsResponse,
  TourGroupListResponse,
  TourGroupMessageListResponse,
  TourGroupMessageSearchResponse,
  TourGroupPaySelectionResponse,
  TourGroupUploadedAttachmentResponse,
} from '../api-dtos'
import { createQueryString, createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '../api-transport'

export const tourGroupApiClient = {
  createTourGroup: (payload: {
    organizerUserId: string
    title: string
    description: string
    destination: string
    startDate: string
    endDate: string
    capacity: number
  }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest('/tour-groups', 'POST', payload),

  listTourGroups: (): Promise<TourGroupListResponse> =>
    executeApiRequest('/tour-groups'),

  getTourGroup: (groupId: string): Promise<TourGroupDetailsResponse> =>
    executeApiRequest(`/tour-groups/${groupId}`),

  joinTourGroup: (groupId: string, payload: { userId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/memberships`, 'POST', payload),

  addTourGroupMembershipTraveler: (groupId: string, payload: { userId: string; travelerId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/membership-travelers`, 'POST', payload),

  createTourGroupPlanItem: (
    groupId: string,
    payload: {
      organizerUserId: string
      itemType: string
      title: string
      description: string
      scheduledAt: string
      endsAt?: string | null
      sequenceNo: number
    },
  ): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/plan-items`, 'POST', payload),

  createTourGroupPlanOption: (
    planItemId: string,
    groupId: string,
    payload: {
      organizerUserId: string
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/plan-items/${planItemId}/options${createQueryString({ groupId })}`, 'POST', payload),

  createTourGroupSelection: (
    planItemId: string,
    groupId: string,
    payload: { userId: string; optionId: string; quantity: number; travelerIds: string[] },
  ): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/plan-items/${planItemId}/selections${createQueryString({ groupId })}`, 'POST', payload),

  submitTourGroupSelection: (selectionId: string, payload: { userId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/submit`, 'POST', payload),

  confirmTourGroupSelection: (selectionId: string, payload: { organizerUserId: string; reviewNote?: string | null }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/confirm`, 'POST', payload),

  rejectTourGroupSelection: (selectionId: string, payload: { organizerUserId: string; reviewNote: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/reject`, 'POST', payload),

  payTourGroupSelection: (selectionId: string, payload: { userId: string; paymentMethod: string }): Promise<TourGroupPaySelectionResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/pay`, 'POST', payload),

  batchPayTourGroupSelections: (payload: { userId: string; selectionIds: string[]; paymentMethod: string }): Promise<TourGroupPaySelectionResponse> =>
    executeJsonApiRequest('/selections/batch-pay', 'POST', payload),

  batchConfirmTourGroupSelections: (payload: { organizerUserId: string; selectionIds: string[]; reviewNote?: string | null }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest('/selections/batch-confirm', 'POST', payload),

  batchRejectTourGroupSelections: (payload: { organizerUserId: string; selectionIds: string[]; reviewNote: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest('/selections/batch-reject', 'POST', payload),

  listTourGroupBookings: (groupId: string): Promise<OrderListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/bookings`),

  getTourGroupChatSettings: (groupId: string): Promise<TourGroupChatSettingsResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat-settings`),

  updateTourGroupChatSettings: (groupId: string, payload: { allowMemberDirectChat: boolean }): Promise<TourGroupChatSettingsResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/chat-settings`, 'PATCH', payload),

  listTourGroupChatMessages: (groupId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/messages`),

  sendTourGroupChatMessage: (groupId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/chat/messages`, 'POST', payload),

  listTourGroupDirectConversations: (groupId: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/direct-conversations`),

  listTourGroupConversations: (groupId: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/conversations`),

  searchTourGroupConversations: (groupId: string, q: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/conversations/search${createQueryString({ q })}`),

  searchTourGroupMessages: (groupId: string, q: string): Promise<TourGroupMessageSearchResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/search${createQueryString({ q })}`),

  getOrCreateTourGroupDirectConversation: (groupId: string, payload: { targetUserId: string }): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/direct-conversations`, 'POST', payload),

  listDirectConversationMessages: (conversationId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/direct-conversations/${conversationId}/messages`),

  listConversationMessages: (conversationId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/conversations/${conversationId}/messages`),

  markConversationRead: (conversationId: string): Promise<TourGroupConversationSummaryResponse> =>
    executeApiRequest(`/conversations/${conversationId}/read`, { method: 'POST' }),

  uploadConversationAttachment: (groupId: string, conversationId: string, attachmentFile: File): Promise<TourGroupUploadedAttachmentResponse> =>
    executeMultipartApiRequest(
      `/conversations/${conversationId}/attachments${createQueryString({ groupId })}`,
      'POST',
      createSingleFileFormData('attachment', attachmentFile),
    ),

  sendConversationMessage: (
    conversationId: string,
    payload: {
      messageType?: string
      content: string
      replyToMessageId?: string | null
      attachments?: TourGroupUploadedAttachmentResponse[]
    },
  ): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/conversations/${conversationId}/messages`, 'POST', payload),

  sendDirectConversationMessage: (conversationId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/messages`, 'POST', payload),

  editConversationMessage: (messageId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/messages/${messageId}`, 'PATCH', payload),

  deleteConversationMessage: (messageId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/delete`, { method: 'POST' }),

  recallConversationMessage: (messageId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/recall`, { method: 'POST' }),

  addConversationReaction: (messageId: string, reactionType: string): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/messages/${messageId}/reactions`, 'POST', { reactionType }),

  removeConversationReaction: (messageId: string, reactionType: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/reactions/${encodeURIComponent(reactionType)}`, { method: 'DELETE' }),

  updateDirectConversationMuteState: (conversationId: string, muted: boolean): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/mute`, 'PATCH', { muted }),

  updateDirectConversationArchiveState: (conversationId: string, archived: boolean): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/archive`, 'PATCH', { archived }),
}
