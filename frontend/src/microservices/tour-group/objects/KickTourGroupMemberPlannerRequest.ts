// This file defines `tour-group` module `KickTourGroupMemberPlannerRequest` as request payload data and provides JSON helpers.

export type KickTourGroupMemberPlannerRequest = {
  groupId: string
  organizerUserId: string
  targetUserId: string
}
export const kickTourGroupMemberPlannerRequestFromJson = (json: string): KickTourGroupMemberPlannerRequest =>
  JSON.parse(json) as KickTourGroupMemberPlannerRequest

export const kickTourGroupMemberPlannerRequestToJson = (value: KickTourGroupMemberPlannerRequest): string =>
  JSON.stringify(value)
