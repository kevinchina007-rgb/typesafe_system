// This file defines `tour-group` module `LeaveTourGroupPlannerRequest` as request payload data and provides JSON helpers.

export type LeaveTourGroupPlannerRequest = {
  groupId: string
  userId: string
}
export const leaveTourGroupPlannerRequestFromJson = (json: string): LeaveTourGroupPlannerRequest =>
  JSON.parse(json) as LeaveTourGroupPlannerRequest

export const leaveTourGroupPlannerRequestToJson = (value: LeaveTourGroupPlannerRequest): string =>
  JSON.stringify(value)
