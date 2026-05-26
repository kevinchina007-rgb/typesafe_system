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
export const managerRefundTaskResponseFromJson = (json: string): ManagerRefundTaskResponse =>
  JSON.parse(json) as ManagerRefundTaskResponse

export const managerRefundTaskResponseToJson = (value: ManagerRefundTaskResponse): string =>
  JSON.stringify(value)
