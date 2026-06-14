// This file defines `tour-group` module `CreateTourGroupPlanItemPlannerRequest` as request payload data and provides JSON helpers.

export type CreateTourGroupPlanItemPlannerRequest = {
  groupId: string
  organizerUserId: string
  itemType: string
  title: string
  description: string
  scheduledAt: string
  endsAt: string | null
  sequenceNo: number
}
export const createTourGroupPlanItemPlannerRequestFromJson = (json: string): CreateTourGroupPlanItemPlannerRequest =>
  JSON.parse(json) as CreateTourGroupPlanItemPlannerRequest

export const createTourGroupPlanItemPlannerRequestToJson = (value: CreateTourGroupPlanItemPlannerRequest): string =>
  JSON.stringify(value)
