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
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

const userSessionStorageKey = 'flypig.userSessionId'

function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

function createChatQueryString(queryEntries: Record<string, string | number | boolean | null | undefined> = {}): string {
  return createQueryString({ sessionId: readUserSessionId(), ...queryEntries })
}

function readFileAsBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      const base64 = result.includes(',') ? result.split(',').slice(1).join(',') : result
      resolve(base64)
    }
    reader.onerror = () => reject(reader.error ?? new Error('Failed to read file'))
    reader.readAsDataURL(file)
  })
}

function normalizeTourGroupDetails(response: TourGroupDetailsResponse): TourGroupDetailsResponse {
  return {
    ...response,
    planItems: response.planItems ?? [],
    planOptions: response.planOptions ?? [],
    selections: response.selections ?? [],
    selectionOrderLinks: response.selectionOrderLinks ?? [],
    selectionOrderProjections: response.selectionOrderProjections ?? [],
    blacklists: response.blacklists ?? [],
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
    coverImageUrl?: string | null
    tags?: string[]
  }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/CreateTourGroupPlanner', 'POST', payload).then(normalizeTourGroupDetails)

export const listTourGroups = (): Promise<TourGroupListResponse> =>
    executeJsonApiRequest('/ListTourGroupsPlanner', 'POST', {})

export const getTourGroup = (groupId: string): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/GetTourGroupDetailsPlanner', 'POST', { groupId }).then(normalizeTourGroupDetails)

export const joinTourGroup = (groupId: string, payload: { userId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/JoinTourGroupPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const leaveTourGroup = (groupId: string, payload: { userId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/LeaveTourGroupPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const addTourGroupMembershipTraveler = (groupId: string, payload: { userId: string; travelerId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/AddMembershipTravelerPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const removeTourGroupMembershipTraveler = (groupId: string, payload: { userId: string; travelerId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/RemoveMembershipTravelerPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const kickTourGroupMember = (groupId: string, payload: { organizerUserId: string; targetUserId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/KickTourGroupMemberPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const blacklistTourGroupMember = (groupId: string, payload: { organizerUserId: string; targetUserId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/BlacklistTourGroupMemberPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

export const transferTourGroupLeader = (groupId: string, payload: { organizerUserId: string; targetUserId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/TransferTourGroupLeaderPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

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
    executeJsonApiRequest<TourGroupDetailsResponse>('/CreateTourGroupPlanItemPlanner', 'POST', { groupId, ...payload }).then(normalizeTourGroupDetails)

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
    executeJsonApiRequest<TourGroupDetailsResponse>('/CreateTourGroupPlanOptionPlanner', 'POST', { groupId, planItemId, ...payload }).then(normalizeTourGroupDetails)

export const createTourGroupSelection = (
    planItemId: string,
    groupId: string,
    payload: { userId: string; optionId: string; quantity: number; travelerIds: string[] },
  ): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/CreateTourGroupSelectionPlanner', 'POST', { groupId, planItemId, ...payload }).then(normalizeTourGroupDetails)

export const submitTourGroupSelection = (selectionId: string, payload: { userId: string }): Promise<TourGroupDetailsResponse> =>
    executeJsonApiRequest<TourGroupDetailsResponse>('/SubmitTourGroupSelectionPlanner', 'POST', { selectionId, ...payload }).then(normalizeTourGroupDetails)

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
    executeApiRequest(`/tour-groups/${groupId}/chat-settings${createChatQueryString()}`)

export const updateTourGroupChatSettings = (groupId: string, payload: { allowMemberDirectChat: boolean }): Promise<TourGroupChatSettingsResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/chat-settings${createChatQueryString()}`, 'PATCH', payload)

export const listTourGroupChatMessages = (groupId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/messages${createChatQueryString()}`)

export const sendTourGroupChatMessage = (groupId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/chat/messages${createChatQueryString()}`, 'POST', payload)

export const listTourGroupDirectConversations = (groupId: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/direct-conversations${createChatQueryString()}`)

export const listTourGroupConversations = (groupId: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/conversations${createChatQueryString()}`)

export const searchTourGroupConversations = (groupId: string, q: string): Promise<TourGroupConversationListResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/conversations/search${createChatQueryString({ q })}`)

export const searchTourGroupMessages = (groupId: string, q: string): Promise<TourGroupMessageSearchResponse> =>
    executeApiRequest(`/tour-groups/${groupId}/chat/search${createChatQueryString({ q })}`)

export const getOrCreateTourGroupDirectConversation = (groupId: string, payload: { targetUserId: string }): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/tour-groups/${groupId}/direct-conversations${createChatQueryString()}`, 'POST', payload)

export const listDirectConversationMessages = (conversationId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/direct-conversations/${conversationId}/messages${createChatQueryString()}`)

export const listConversationMessages = (conversationId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/conversations/${conversationId}/messages${createChatQueryString()}`)

export const markConversationRead = (conversationId: string): Promise<TourGroupConversationSummaryResponse> =>
    executeApiRequest(`/conversations/${conversationId}/read${createChatQueryString()}`, { method: 'POST' })

export const uploadConversationAttachment = (groupId: string, conversationId: string, attachmentFile: File): Promise<TourGroupUploadedAttachmentResponse> =>
    readFileAsBase64(attachmentFile).then(base64Content =>
      executeJsonApiRequest<TourGroupUploadedAttachmentResponse>(
        `/conversations/${conversationId}/attachments${createChatQueryString({ groupId })}`,
        'POST',
        {
          fileName: attachmentFile.name,
          mimeType: attachmentFile.type || 'application/octet-stream',
          base64Content,
        },
      ),
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
    executeJsonApiRequest(`/conversations/${conversationId}/messages${createChatQueryString()}`, 'POST', payload)

export const sendDirectConversationMessage = (conversationId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/messages${createChatQueryString()}`, 'POST', payload)

export const editConversationMessage = (messageId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/messages/${messageId}${createChatQueryString()}`, 'PATCH', payload)

export const deleteConversationMessage = (messageId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/delete${createChatQueryString()}`, { method: 'POST' })

export const recallConversationMessage = (messageId: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/recall${createChatQueryString()}`, { method: 'POST' })

export const addConversationReaction = (messageId: string, reactionType: string): Promise<TourGroupMessageListResponse> =>
    executeJsonApiRequest(`/messages/${messageId}/reactions${createChatQueryString()}`, 'POST', { reactionType })

export const removeConversationReaction = (messageId: string, reactionType: string): Promise<TourGroupMessageListResponse> =>
    executeApiRequest(`/messages/${messageId}/reactions/${encodeURIComponent(reactionType)}${createChatQueryString()}`, { method: 'DELETE' })

export const updateDirectConversationMuteState = (conversationId: string, muted: boolean): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/mute${createChatQueryString()}`, 'PATCH', { muted })

export const updateDirectConversationArchiveState = (conversationId: string, archived: boolean): Promise<TourGroupConversationSummaryResponse> =>
    executeJsonApiRequest(`/direct-conversations/${conversationId}/archive${createChatQueryString()}`, 'PATCH', { archived })
