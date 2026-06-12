// 本文件定义 operations 模块的 `UpdateHotelManagerProfilePlannerRequest`，作为planner 请求参数并提供 JSON 编解码。

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
