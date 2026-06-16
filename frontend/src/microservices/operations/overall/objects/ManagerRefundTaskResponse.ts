// 鏈枃浠跺畾涔?operations 妯″潡鐨?`ManagerRefundTaskResponse`锛屼綔涓哄搷搴旀暟鎹苟鎻愪緵 JSON 缂栬В鐮併€?

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

