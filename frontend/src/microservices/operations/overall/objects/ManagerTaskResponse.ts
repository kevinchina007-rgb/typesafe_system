// 本文件定义 `ListManagerTasksPlanner` 返回的单条任务数据。

import type { SupplierReviewDecisionResponse } from './SupplierReviewDecisionResponse'

export type ManagerTaskResponse = {
  taskId: string | null
  taskType: string
  orderItemId: string
  orderId: string
  orderItemKind: string
  detailLabel: string
  supplierReviewStatus: string
  supplierReviewDecision: SupplierReviewDecisionResponse | null
  summaryLabel: string
  bookedAmount: string
  bookedCurrency: string
  requestedAt: string
  createdAt: string
  reviewedAt: string | null
  reviewedBy: string | null
  reviewNote: string | null
}

export const managerTaskResponseFromJson = (json: string): ManagerTaskResponse =>
  JSON.parse(json) as ManagerTaskResponse

export const managerTaskResponseToJson = (value: ManagerTaskResponse): string =>
  JSON.stringify(value)
