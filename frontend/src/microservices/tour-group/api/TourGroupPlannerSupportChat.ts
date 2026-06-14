import type { TourGroupMessageListResponse } from '@/microservices/tour-group/objects/TourGroupMessageListResponse'
import { executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import { createChatQueryString } from './TourGroupPlannerSupportShared'

export const listTourGroupChatMessages = (groupId: string): Promise<TourGroupMessageListResponse> =>
  executeApiRequest(`/tour-groups/${groupId}/chat/messages${createChatQueryString()}`)

export const sendTourGroupChatMessage = (groupId: string, payload: { content: string }): Promise<TourGroupMessageListResponse> =>
  executeJsonApiRequest(`/tour-groups/${groupId}/chat/messages${createChatQueryString()}`, 'POST', payload)
