// 本文件定义 `BatchConfirmManagerTasksPlanner` 和 `BatchRejectManagerTasksPlanner` 的请求对象。

export type ManagerBatchDecisionPlannerRequest = {
  managerId: string
  managerType: string
  orderItemIds: string[]
  reason?: string | null
  note?: string | null
}

export const managerBatchDecisionPlannerRequestFromJson = (json: string): ManagerBatchDecisionPlannerRequest =>
  JSON.parse(json) as ManagerBatchDecisionPlannerRequest

export const managerBatchDecisionPlannerRequestToJson = (value: ManagerBatchDecisionPlannerRequest): string =>
  JSON.stringify(value)
