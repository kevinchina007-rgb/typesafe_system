// This file defines `tour-group` module `GetTourGroupDetailsPlannerRequest` as request payload data and provides JSON helpers.

export type GetTourGroupDetailsPlannerRequest = {
  groupId: string
}
export const getTourGroupDetailsPlannerRequestFromJson = (json: string): GetTourGroupDetailsPlannerRequest =>
  JSON.parse(json) as GetTourGroupDetailsPlannerRequest

export const getTourGroupDetailsPlannerRequestToJson = (value: GetTourGroupDetailsPlannerRequest): string =>
  JSON.stringify(value)
