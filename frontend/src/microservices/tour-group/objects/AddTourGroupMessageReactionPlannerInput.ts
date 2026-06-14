// This file defines `tour-group` module `AddTourGroupMessageReactionPlannerInput` as request payload data and provides JSON helpers.

export type AddTourGroupMessageReactionPlannerInput = {
  messageId: string
  sessionId: string
  reactionType: string
}
export const addTourGroupMessageReactionPlannerInputFromJson = (json: string): AddTourGroupMessageReactionPlannerInput =>
  JSON.parse(json) as AddTourGroupMessageReactionPlannerInput

export const addTourGroupMessageReactionPlannerInputToJson = (value: AddTourGroupMessageReactionPlannerInput): string =>
  JSON.stringify(value)
