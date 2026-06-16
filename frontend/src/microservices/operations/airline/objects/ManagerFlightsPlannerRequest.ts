// 本文件定义 ListManagerFlightsPlanner 的航空管理查询请求。

export type ManagerFlightsPlannerRequest = {
  managerId: string
  managerType: string
  departureAirports?: string[] | null
  arrivalAirports?: string[] | null
  departureDate?: string | null
  timeRange?: string | null
  sortDirection?: string | null
}

export const managerFlightsPlannerRequestFromJson = (json: string): ManagerFlightsPlannerRequest =>
  JSON.parse(json) as ManagerFlightsPlannerRequest

export const managerFlightsPlannerRequestToJson = (value: ManagerFlightsPlannerRequest): string =>
  JSON.stringify(value)
