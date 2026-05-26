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
export const refundResponseFromJson = (json: string): RefundResponse =>
  JSON.parse(json) as RefundResponse

export const refundResponseToJson = (value: RefundResponse): string =>
  JSON.stringify(value)
