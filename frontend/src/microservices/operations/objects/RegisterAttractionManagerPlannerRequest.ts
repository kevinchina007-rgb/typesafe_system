export type RegisterAttractionManagerPlannerRequest = {
  email: string
  displayName: string
  password: string
}

export const registerAttractionManagerPlannerRequestFromJson = (json: string): RegisterAttractionManagerPlannerRequest =>
  JSON.parse(json) as RegisterAttractionManagerPlannerRequest

export const registerAttractionManagerPlannerRequestToJson = (value: RegisterAttractionManagerPlannerRequest): string =>
  JSON.stringify(value)
