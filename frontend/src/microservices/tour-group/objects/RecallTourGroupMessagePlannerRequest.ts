// This file defines `tour-group` module `RecallTourGroupMessagePlannerRequest` as request payload data and provides JSON helpers.

export type RecallTourGroupMessagePlannerRequest = {
  messageId: string
  sessionId: string
}
export const recallTourGroupMessagePlannerRequestFromJson = (json: string): RecallTourGroupMessagePlannerRequest =>
  JSON.parse(json) as RecallTourGroupMessagePlannerRequest

export const recallTourGroupMessagePlannerRequestToJson = (value: RecallTourGroupMessagePlannerRequest): string =>
  JSON.stringify(value)
