// 鏈枃浠跺畾涔?operations 妯″潡鐨?`UpdateAirlineManagerProfilePlannerRequest`锛屼綔涓簆lanner 璇锋眰鍙傛暟骞舵彁渚?JSON 缂栬В鐮併€?

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

