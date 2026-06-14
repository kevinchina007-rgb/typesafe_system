// This file defines `tour-group` module `SubmitTourGroupSelectionPlannerRequest` as request payload data and provides JSON helpers.

export type SubmitTourGroupSelectionPlannerRequest = {
  userId: string
  selectionId: string
}
export const submitTourGroupSelectionPlannerRequestFromJson = (json: string): SubmitTourGroupSelectionPlannerRequest =>
  JSON.parse(json) as SubmitTourGroupSelectionPlannerRequest

export const submitTourGroupSelectionPlannerRequestToJson = (value: SubmitTourGroupSelectionPlannerRequest): string =>
  JSON.stringify(value)
