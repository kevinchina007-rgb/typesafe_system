import type { TourGroupConversationListResponse } from '@/microservices/tour-group/objects/TourGroupConversationListResponse'
import type { TourGroupConversationSummaryResponse } from '@/microservices/tour-group/objects/TourGroupConversationSummaryResponse'
import type { TourGroupMessageListResponse } from '@/microservices/tour-group/objects/TourGroupMessageListResponse'
import type { TourGroupMessageSearchResponse } from '@/microservices/tour-group/objects/TourGroupMessageSearchResponse'
import type { TourGroupUploadedAttachmentResponse } from '@/microservices/tour-group/objects/TourGroupUploadedAttachmentResponse'
import { executeApiRequest, executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import { createChatQueryString } from './TourGroupPlannerSupportShared'

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

