// This file defines `tour-group` module `MarkConversationReadPlannerRequest` as request payload data and provides JSON helpers.

export type MarkConversationReadPlannerRequest = {
}
export const markConversationReadPlannerRequestFromJson = (json: string): MarkConversationReadPlannerRequest =>
  JSON.parse(json) as MarkConversationReadPlannerRequest

export const markConversationReadPlannerRequestToJson = (value: MarkConversationReadPlannerRequest): string =>
  JSON.stringify(value)
