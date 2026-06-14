// This file defines `tour-group` module `EditTourGroupMessagePlannerRequest` as request payload data and provides JSON helpers.

export type EditTourGroupMessagePlannerRequest = {
  content: string
}
export const editTourGroupMessagePlannerRequestFromJson = (json: string): EditTourGroupMessagePlannerRequest =>
  JSON.parse(json) as EditTourGroupMessagePlannerRequest

export const editTourGroupMessagePlannerRequestToJson = (value: EditTourGroupMessagePlannerRequest): string =>
  JSON.stringify(value)
