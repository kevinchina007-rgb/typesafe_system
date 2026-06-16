// 本文件定义 hotel 子域自己的管理员会话返回体。
// 它对应 RegisterHotelManagerPlanner / UpdateHotelManagerProfilePlanner 的返回数据，不再借用 overall 子域。
export type HotelManagerSessionPlannerResponse = {
  managerId: string
  managerType: string
  email: string
  displayName: string
  status: string
  scopeId: string
  logoAssetPath?: string | null
  createdAt: string
}

export const hotelManagerSessionPlannerResponseFromJson = (json: string): HotelManagerSessionPlannerResponse =>
  JSON.parse(json) as HotelManagerSessionPlannerResponse

export const hotelManagerSessionPlannerResponseToJson = (value: HotelManagerSessionPlannerResponse): string =>
  JSON.stringify(value)
