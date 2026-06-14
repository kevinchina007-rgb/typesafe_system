// This file defines `tour-group` module `RemoveTourGroupMessageReactionPlannerRequest` as request payload data and provides JSON helpers.

export type RemoveTourGroupMessageReactionPlannerRequest = {
  messageId: string
  sessionId: string
  reactionType: string
}
export const removeTourGroupMessageReactionPlannerRequestFromJson = (json: string): RemoveTourGroupMessageReactionPlannerRequest =>
  JSON.parse(json) as RemoveTourGroupMessageReactionPlannerRequest

export const removeTourGroupMessageReactionPlannerRequestToJson = (value: RemoveTourGroupMessageReactionPlannerRequest): string =>
  JSON.stringify(value)
