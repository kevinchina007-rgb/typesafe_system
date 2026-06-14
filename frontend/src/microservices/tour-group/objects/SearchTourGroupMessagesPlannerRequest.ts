// This file defines `tour-group` module `SearchTourGroupMessagesPlannerRequest` as request payload data and provides JSON helpers.

export type SearchTourGroupMessagesPlannerRequest = {
  groupId: string
  sessionId: string
  query: string
}
export const searchTourGroupMessagesPlannerRequestFromJson = (json: string): SearchTourGroupMessagesPlannerRequest =>
  JSON.parse(json) as SearchTourGroupMessagesPlannerRequest

export const searchTourGroupMessagesPlannerRequestToJson = (value: SearchTourGroupMessagesPlannerRequest): string =>
  JSON.stringify(value)
