export type UpdateAirlineManagerProfilePlannerRequest = {
  managerId: string
  displayName: string
  airlineName: string
  airlineCode: string
  logoAssetPath?: string | null
}

export const updateAirlineManagerProfilePlannerRequestFromJson = (json: string): UpdateAirlineManagerProfilePlannerRequest =>
  JSON.parse(json) as UpdateAirlineManagerProfilePlannerRequest

export const updateAirlineManagerProfilePlannerRequestToJson = (value: UpdateAirlineManagerProfilePlannerRequest): string =>
  JSON.stringify(value)
