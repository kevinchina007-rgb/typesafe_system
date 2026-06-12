// 本文件定义 tour-group 模块的 `GroupSelectionOrderProjectionResponse`，作为响应数据并提供 JSON 编解码。

export type GroupSelectionOrderProjectionResponse = {
  selectionId: string
  orderId: string
  orderStatus: string
  paymentStatus: string
  supplierReviewStatus: string
  refundStatus: string | null
  bookingSummaryLabel: string
}
export const groupSelectionOrderProjectionResponseFromJson = (json: string): GroupSelectionOrderProjectionResponse =>
  JSON.parse(json) as GroupSelectionOrderProjectionResponse

export const groupSelectionOrderProjectionResponseToJson = (value: GroupSelectionOrderProjectionResponse): string =>
  JSON.stringify(value)
