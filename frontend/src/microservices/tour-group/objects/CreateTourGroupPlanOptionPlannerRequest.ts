// This file defines `tour-group` module `CreateTourGroupPlanOptionPlannerRequest` as request payload data and provides JSON helpers.

export type CreateTourGroupPlanOptionPlannerRequest = {
  groupId: string
  organizerUserId: string
  planItemId: string
  resourceType: string
  resourceId: string
  resourceVariantCode: string | null
  resourceContext: string | null
  label: string
  description: string
  defaultQuantity: number
}
export const createTourGroupPlanOptionPlannerRequestFromJson = (json: string): CreateTourGroupPlanOptionPlannerRequest =>
  JSON.parse(json) as CreateTourGroupPlanOptionPlannerRequest

export const createTourGroupPlanOptionPlannerRequestToJson = (value: CreateTourGroupPlanOptionPlannerRequest): string =>
  JSON.stringify(value)
