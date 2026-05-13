export type RefundResponse = {
  refundId: string
  refundAmount: string
  refundCurrency: string
  refundReason: string
  refundStatus: string
  requestedAt: string
  approvedAt: string | null
  settledAt: string | null
}
