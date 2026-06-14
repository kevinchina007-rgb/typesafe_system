// This file defines `tour-group` module `BlacklistTourGroupMemberPlannerRequest` as request payload data and provides JSON helpers.

export type BlacklistTourGroupMemberPlannerRequest = {
  groupId: string
  organizerUserId: string
  targetUserId: string
}
export const blacklistTourGroupMemberPlannerRequestFromJson = (json: string): BlacklistTourGroupMemberPlannerRequest =>
  JSON.parse(json) as BlacklistTourGroupMemberPlannerRequest

export const blacklistTourGroupMemberPlannerRequestToJson = (value: BlacklistTourGroupMemberPlannerRequest): string =>
  JSON.stringify(value)
