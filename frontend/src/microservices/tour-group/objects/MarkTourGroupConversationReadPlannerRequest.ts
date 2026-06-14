// This file defines `tour-group` module `MarkTourGroupConversationReadPlannerRequest` as request payload data and provides JSON helpers.

export type MarkTourGroupConversationReadPlannerRequest = {
  conversationId: string
  sessionId: string
}
export const markTourGroupConversationReadPlannerRequestFromJson = (json: string): MarkTourGroupConversationReadPlannerRequest =>
  JSON.parse(json) as MarkTourGroupConversationReadPlannerRequest

export const markTourGroupConversationReadPlannerRequestToJson = (value: MarkTourGroupConversationReadPlannerRequest): string =>
  JSON.stringify(value)
