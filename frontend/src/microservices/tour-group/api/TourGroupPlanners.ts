import type { OrderListResponse } from '@/microservices/order/objects/OrderListResponse'
import type { TourGroupChatSettingsResponse } from '@/microservices/tour-group/objects/TourGroupChatSettingsResponse'
import type { TourGroupConversationListResponse } from '@/microservices/tour-group/objects/TourGroupConversationListResponse'
import type { TourGroupConversationSummaryResponse } from '@/microservices/tour-group/objects/TourGroupConversationSummaryResponse'
import type { TourGroupDetailsResponse } from '@/microservices/tour-group/objects/TourGroupDetailsResponse'
import type { TourGroupListResponse } from '@/microservices/tour-group/objects/TourGroupListResponse'
import type { TourGroupMessageListResponse } from '@/microservices/tour-group/objects/TourGroupMessageListResponse'
import type { TourGroupMessageSearchResponse } from '@/microservices/tour-group/objects/TourGroupMessageSearchResponse'
import type { TourGroupPaySelectionResponse } from '@/microservices/tour-group/objects/TourGroupPaySelectionResponse'
import type { TourGroupUploadedAttachmentResponse } from '@/microservices/tour-group/objects/TourGroupUploadedAttachmentResponse'
import { createQueryString, createSingleFileFormData, executeApiRequest, executeJsonApiRequest, executeMultipartApiRequest } from '@/microservices/common/api/ApiTransport'

function normalizeTourGroupDetails(response: TourGroupDetailsResponse): TourGroupDetailsResponse {
  return {
    ...response,
    planItems: response.planItems ?? [],
    planOptions: response.planOptions ?? [],
    selections: response.selections ?? [],
    selectionOrderLinks: response.selectionOrderLinks ?? [],
    selectionOrderProjections: response.selectionOrderProjections ?? [],
    bookings: response.bookings ?? [],
  }
}

export const createTourGroup = (payload: {
    organizerUserId: string
    title: string
    description: string
    destination: string
    startDate: string
    endDate: string
    capacity: number
  }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/CreateTourGroupPlanner', 'POST', payload).then(normalizeTourGroupDetails)

export const listTourGroups = (): Promise<TourGroupListResponse> =>
    executeJsonApiRequest('/ListTourGroupsPlanner', 'POST', {})

export const getTourGroup = (groupId: string): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/GetTourGroupDetailsPlanner', 'POST', { groupId }).then(normalizeTourGroupDetails)

export const joinTourGroup = (groupId: string, payload: { userId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/JoinTourGroupPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const addTourGroupMembershipTraveler = (groupId: string, payload: { userId: string; travelerId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/AddMembershipTravelerPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const createTourGroupPlanItem = (
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
    executeJsonApiRequest(`/tour-groups/${groupId}/plan-items`, 'POST', payload)

export const createTourGroupPlanOption = (
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
    executeJsonApiRequest(`/plan-items/${planItemId}/options${createQueryString({ groupId })}`, 'POST', payload)

export const createTourGroupSelection = (
    planItemId: string,
    groupId: string,
    payload: { userId: string; optionId: string; quantity: number; travelerIds: string[] },
  ): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/plan-items/${planItemId}/selections${createQueryString({ groupId })}`, 'POST', payload)

export const submitTourGroupSelection = (selectionId: string, payload: { userId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/submit`, 'POST', payload)

export const confirmTourGroupSelection = (selectionId: string, payload: { organizerUserId: string; reviewNote?: string | null }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/confirm`, 'POST', payload)

export const rejectTourGroupSelection = (selectionId: string, payload: { organizerUserId: string; reviewNote: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/reject`, 'POST', payload)

export const payTourGroupSelection = (selectionId: string, payload: { userId: string; paymentMethod: string }): Promise<TourGroupPaySelectionResponse> =>
    executeJsonApiRequest(`/selections/${selectionId}/pay`, 'POST', payload)

export const batchPayTourGroupSelections = (payload: { userId: string; selectionIds: string[]; paymentMethod: string }): Promise<TourGroupPaySelectionResponse> =>
    executeJsonApiRequest('/selections/batch-pay', 'POST', payload)

export const batchConfirmTourGroupSelections = (payload: { organizerUserId: string; selectionIds: string[]; reviewNote?: string | null }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest('/selections/batch-confirm', 'POST', payload)

export const batchRejectTourGroupSelections = (payload: { organizerUserId: string; selectionIds: string[]; reviewNote: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest('/selections/batch-reject', 'POST', payload)

export const listTourGroupBookings = (groupId: string): Promise<OrderListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/bookings`)

export const getTourGroupChatSettings = (groupId: string): Promise<TourGroupChatSettingsResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat-settings`)

export const updateTourGroupChatSettings = (groupId: string, payload: { allowMemberDirectChat: boolean }): Promise<TourGroupChatSettingsResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/chat-settings`, 'PATCH', payload)

export const listTourGroupChatMessages = (groupId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/messages`)

export const sendTourGroupChatMessage = (groupId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/chat/messages`, 'POST', payload)

export const listTourGroupDirectConversations = (groupId: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/direct-conversations`)

export const listTourGroupConversations = (groupId: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/conversations`)

export const searchTourGroupConversations = (groupId: string, q: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/conversations/search${createQueryString({ q })}`)

export const searchTourGroupMessages = (groupId: string, q: string): Promise<TourGroupMessageSearchResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/search${createQueryString({ q })}`)

export const getOrCreateTourGroupDirectConversation = (groupId: string, payload: { targetUserId: string }): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/direct-conversations`, 'POST', payload)

export const listDirectConversationMessages = (conversationId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/direct-conversations/${conversationId}/messages`)

export const listConversationMessages = (conversationId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/conversations/${conversationId}/messages`)

export const markConversationRead = (conversationId: string): Promise<TourGroupConversationSummaryResponse> =>
    executeApiRequest(`/conversations/${conversationId}/read`, { method: 'POST' })

export const uploadConversationAttachment = (groupId: string, conversationId: string, attachmentFile: File): Promise<TourGroupUploadedAttachmentResponse> =>
    executeMultipartApiRequest(
      `/conversations/${conversationId}/attachments${createQueryString({ groupId })}`,
      'POST',
      createSingleFileFormData('attachment', attachmentFile),
    )

export const sendConversationMessage = (
    conversationId: string,
    payload: {
      messageType?: string
      content: string
      replyToMessageId?: string | null
      attachments?: TourGroupUploadedAttachmentResponse[]
    },
  ): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/conversations/${conversationId}/messages`, 'POST', payload)

export const sendDirectConversationMessage = (conversationId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/messages`, 'POST', payload)

export const editConversationMessage = (messageId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/messages/${messageId}`, 'PATCH', payload)

export const deleteConversationMessage = (messageId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/delete`, { method: 'POST' })

export const recallConversationMessage = (messageId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/recall`, { method: 'POST' })

export const addConversationReaction = (messageId: string, reactionType: string): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/messages/${messageId}/reactions`, 'POST', { reactionType })

export const removeConversationReaction = (messageId: string, reactionType: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/reactions/${encodeURIComponent(reactionType)}`, { method: 'DELETE' })

export const updateDirectConversationMuteState = (conversationId: string, muted: boolean): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/mute`, 'PATCH', { muted })

export const updateDirectConversationArchiveState = (conversationId: string, archived: boolean): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/archive`, 'PATCH', { archived })
