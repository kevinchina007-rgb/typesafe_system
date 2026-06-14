// This file defines `tour-group` module `AddMembershipTravelerPlannerRequest` as request payload data and provides JSON helpers.

export type AddMembershipTravelerPlannerRequest = {
  groupId: string
  userId: string
  travelerId: string
}
export const addMembershipTravelerPlannerRequestFromJson = (json: string): AddMembershipTravelerPlannerRequest =>
  JSON.parse(json) as AddMembershipTravelerPlannerRequest

export const addMembershipTravelerPlannerRequestToJson = (value: AddMembershipTravelerPlannerRequest): string =>
  JSON.stringify(value)
