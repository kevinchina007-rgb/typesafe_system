// 本文件定义 ToggleManagerFlightStatusPlanner 的航班状态切换请求。

export type ToggleManagerFlightStatusPlannerRequest = {
  managerId: string
  flightId: string
}

export const toggleManagerFlightStatusPlannerRequestFromJson = (json: string): ToggleManagerFlightStatusPlannerRequest =>
  JSON.parse(json) as ToggleManagerFlightStatusPlannerRequest

export const toggleManagerFlightStatusPlannerRequestToJson = (value: ToggleManagerFlightStatusPlannerRequest): string =>
  JSON.stringify(value)
