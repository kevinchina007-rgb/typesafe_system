// This file defines `tour-group` module `AddConversationReactionPlannerRequest` as request payload data and provides JSON helpers.

export type AddConversationReactionPlannerRequest = {
  reactionType: string
}
export const addConversationReactionPlannerRequestFromJson = (json: string): AddConversationReactionPlannerRequest =>
  JSON.parse(json) as AddConversationReactionPlannerRequest

export const addConversationReactionPlannerRequestToJson = (value: AddConversationReactionPlannerRequest): string =>
  JSON.stringify(value)
