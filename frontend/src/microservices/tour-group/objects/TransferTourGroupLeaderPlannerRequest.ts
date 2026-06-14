// This file defines `tour-group` module `TransferTourGroupLeaderPlannerRequest` as request payload data and provides JSON helpers.

export type TransferTourGroupLeaderPlannerRequest = {
  groupId: string
  organizerUserId: string
  targetUserId: string
}
export const transferTourGroupLeaderPlannerRequestFromJson = (json: string): TransferTourGroupLeaderPlannerRequest =>
  JSON.parse(json) as TransferTourGroupLeaderPlannerRequest

export const transferTourGroupLeaderPlannerRequestToJson = (value: TransferTourGroupLeaderPlannerRequest): string =>
  JSON.stringify(value)
