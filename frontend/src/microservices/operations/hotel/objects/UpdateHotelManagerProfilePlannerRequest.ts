// 鏈枃浠跺畾涔?operations 妯″潡鐨?`UpdateHotelManagerProfilePlannerRequest`锛屼綔涓簆lanner 璇锋眰鍙傛暟骞舵彁渚?JSON 缂栬В鐮併€?

export type UpdateHotelManagerProfilePlannerRequest = {
  managerId: string
  displayName: string
  email: string
  hotelName: string
  hotelLocation: string
}

export const updateHotelManagerProfilePlannerRequestFromJson = (json: string): UpdateHotelManagerProfilePlannerRequest =>
  JSON.parse(json) as UpdateHotelManagerProfilePlannerRequest

export const updateHotelManagerProfilePlannerRequestToJson = (value: UpdateHotelManagerProfilePlannerRequest): string =>
  JSON.stringify(value)

