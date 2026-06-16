export type RegisterRailwayManagerPlannerRequest = {
  operatorCode: string
  email: string
  displayName: string
  password: string
}

export const registerRailwayManagerPlannerRequestFromJson = (json: string): RegisterRailwayManagerPlannerRequest =>
  JSON.parse(json) as RegisterRailwayManagerPlannerRequest

export const registerRailwayManagerPlannerRequestToJson = (value: RegisterRailwayManagerPlannerRequest): string =>
  JSON.stringify(value)

