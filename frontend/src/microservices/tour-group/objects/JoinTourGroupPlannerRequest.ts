// This file defines `tour-group` module `JoinTourGroupPlannerRequest` as request payload data and provides JSON helpers.

export type JoinTourGroupPlannerRequest = {
  groupId: string
  userId: string
}
export const joinTourGroupPlannerRequestFromJson = (json: string): JoinTourGroupPlannerRequest =>
  JSON.parse(json) as JoinTourGroupPlannerRequest

export const joinTourGroupPlannerRequestToJson = (value: JoinTourGroupPlannerRequest): string =>
  JSON.stringify(value)
