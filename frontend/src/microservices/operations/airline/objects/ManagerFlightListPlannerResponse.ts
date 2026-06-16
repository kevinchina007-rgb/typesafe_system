// 本文件定义航空管理员航班列表响应结构。

import type { ManagerFlightPlannerResponse } from './ManagerFlightPlannerResponse'

export type ManagerFlightListPlannerResponse = {
  flights: ManagerFlightPlannerResponse[]
}

export const managerFlightListPlannerResponseFromJson = (json: string): ManagerFlightListPlannerResponse =>
  JSON.parse(json) as ManagerFlightListPlannerResponse

export const managerFlightListPlannerResponseToJson = (value: ManagerFlightListPlannerResponse): string =>
  JSON.stringify(value)
