// This file defines `tour-group` module `CreateTourGroupPlannerRequest` as request payload data and provides JSON helpers.

export type CreateTourGroupPlannerRequest = {
  organizerUserId: string
  title: string
  description: string
  destination: string
  startDate: string
  endDate: string
  capacity: number
  coverImageUrl: string | null
  tags: string[]
}
export const createTourGroupPlannerRequestFromJson = (json: string): CreateTourGroupPlannerRequest =>
  JSON.parse(json) as CreateTourGroupPlannerRequest

export const createTourGroupPlannerRequestToJson = (value: CreateTourGroupPlannerRequest): string =>
  JSON.stringify(value)
