// 本文件定义 operations 模块的 `ManagerRefundTaskResponse`，作为响应数据并提供 JSON 编解码。

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
