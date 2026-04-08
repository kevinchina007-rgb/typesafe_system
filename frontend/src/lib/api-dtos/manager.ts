import type { SupplierReviewDecisionResponse } from './orders'

export type ManagerTaskResponse = {
  orderId: string
  orderItemId: string
  buyerUserId: string
  taskType: string
  supplierReviewStatus: string
  summaryLabel: string
  detailLabel: string
  requestedAt: string
  reviewDecision: SupplierReviewDecisionResponse | null
  reviewedBy: string | null
  reviewedAt: string | null
  reviewNote: string | null
}

export type ManagerTaskListResponse = {
  tasks: ManagerTaskResponse[]
}

export type ManagerBatchDecisionResponse = {
  processedCount: number
  orderItemIds: string[]
  action: string
}

export type ManagerRefundTaskResponse = {
  orderId: string
  buyerUserId: string
  taskType: string
  summaryLabel: string
  refundId: string
  refundReason: string
  refundAmount: string
  refundCurrency: string
  requestedAt: string
}

export type ManagerRefundTaskListResponse = {
  tasks: ManagerRefundTaskResponse[]
}
