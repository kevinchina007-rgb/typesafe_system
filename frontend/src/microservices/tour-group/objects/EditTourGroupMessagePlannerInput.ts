// This file defines `tour-group` module `EditTourGroupMessagePlannerInput` as request payload data and provides JSON helpers.

export type EditTourGroupMessagePlannerInput = {
  messageId: string
  sessionId: string
  content: string
}
export const editTourGroupMessagePlannerInputFromJson = (json: string): EditTourGroupMessagePlannerInput =>
  JSON.parse(json) as EditTourGroupMessagePlannerInput

export const editTourGroupMessagePlannerInputToJson = (value: EditTourGroupMessagePlannerInput): string =>
  JSON.stringify(value)
