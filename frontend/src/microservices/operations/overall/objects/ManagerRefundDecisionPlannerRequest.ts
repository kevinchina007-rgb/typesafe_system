// 本文件定义 `ApproveManagerRefundPlanner` 和 `RejectManagerRefundPlanner` 的请求对象。

export type ManagerRefundDecisionPlannerRequest = {
  managerId: string
  managerType: string
  orderId: string
}

export const managerRefundDecisionPlannerRequestFromJson = (json: string): ManagerRefundDecisionPlannerRequest =>
  JSON.parse(json) as ManagerRefundDecisionPlannerRequest

export const managerRefundDecisionPlannerRequestToJson = (value: ManagerRefundDecisionPlannerRequest): string =>
  JSON.stringify(value)
