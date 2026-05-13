import type { SupplierReviewDecisionResponse } from '@/microservices/order/objects/SupplierReviewDecisionResponse'

export type ManagerTaskResponse = {
  taskId?: string
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
