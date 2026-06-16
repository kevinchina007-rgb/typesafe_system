// 本文件定义 ListManagerFlightOrdersPlanner 的航空订单查询请求。

export type ManagerFlightOrdersPlannerRequest = {
  managerId: string
  flightId: string
}

export const managerFlightOrdersPlannerRequestFromJson = (json: string): ManagerFlightOrdersPlannerRequest =>
  JSON.parse(json) as ManagerFlightOrdersPlannerRequest

export const managerFlightOrdersPlannerRequestToJson = (value: ManagerFlightOrdersPlannerRequest): string =>
  JSON.stringify(value)
