// This file defines `tour-group` module `CreateTourGroupSelectionPlannerRequest` as request payload data and provides JSON helpers.

export type CreateTourGroupSelectionPlannerRequest = {
  groupId: string
  userId: string
  planItemId: string
  optionId: string
  quantity: number
  travelerIds: string[]
}
export const createTourGroupSelectionPlannerRequestFromJson = (json: string): CreateTourGroupSelectionPlannerRequest =>
  JSON.parse(json) as CreateTourGroupSelectionPlannerRequest

export const createTourGroupSelectionPlannerRequestToJson = (value: CreateTourGroupSelectionPlannerRequest): string =>
  JSON.stringify(value)
