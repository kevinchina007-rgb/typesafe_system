// 本文件定义 `ConfirmManagerBookingItemPlanner` 和 `RejectManagerBookingItemPlanner` 的请求对象。

export type ManagerDecisionPlannerRequest = {
  managerId: string
  managerType: string
  orderItemId: string
  reason?: string | null
  note?: string | null
}

export const managerDecisionPlannerRequestFromJson = (json: string): ManagerDecisionPlannerRequest =>
  JSON.parse(json) as ManagerDecisionPlannerRequest

export const managerDecisionPlannerRequestToJson = (value: ManagerDecisionPlannerRequest): string =>
  JSON.stringify(value)
