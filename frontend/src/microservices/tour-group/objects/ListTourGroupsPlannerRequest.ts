// This file defines `tour-group` module `ListTourGroupsPlannerRequest` as request payload data and provides JSON helpers.

export type ListTourGroupsPlannerRequest = {
}
export const listTourGroupsPlannerRequestFromJson = (json: string): ListTourGroupsPlannerRequest =>
  JSON.parse(json) as ListTourGroupsPlannerRequest

export const listTourGroupsPlannerRequestToJson = (value: ListTourGroupsPlannerRequest): string =>
  JSON.stringify(value)
