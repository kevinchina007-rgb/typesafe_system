// This file defines `tour-group` module `DeleteTourGroupMessagePlannerRequest` as request payload data and provides JSON helpers.

export type DeleteTourGroupMessagePlannerRequest = {
  messageId: string
  sessionId: string
}
export const deleteTourGroupMessagePlannerRequestFromJson = (json: string): DeleteTourGroupMessagePlannerRequest =>
  JSON.parse(json) as DeleteTourGroupMessagePlannerRequest

export const deleteTourGroupMessagePlannerRequestToJson = (value: DeleteTourGroupMessagePlannerRequest): string =>
  JSON.stringify(value)
