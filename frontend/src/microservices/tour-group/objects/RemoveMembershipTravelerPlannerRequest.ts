// This file defines `tour-group` module `RemoveMembershipTravelerPlannerRequest` as request payload data and provides JSON helpers.

export type RemoveMembershipTravelerPlannerRequest = {
  groupId: string
  userId: string
  travelerId: string
}
export const removeMembershipTravelerPlannerRequestFromJson = (json: string): RemoveMembershipTravelerPlannerRequest =>
  JSON.parse(json) as RemoveMembershipTravelerPlannerRequest

export const removeMembershipTravelerPlannerRequestToJson = (value: RemoveMembershipTravelerPlannerRequest): string =>
  JSON.stringify(value)
