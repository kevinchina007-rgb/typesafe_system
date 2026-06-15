// 本文件定义订单退款响应，和后端 `RefundPlannerResponse` 保持同名镜像。

export type RefundPlannerResponse = {
  refundId: string
  refundAmount: string
  refundCurrency: string
  refundReason: string
  refundStatus: string
  requestedAt: string
  approvedAt: string | null
  settledAt: string | null
}

export const refundPlannerResponseFromJson = (json: string): RefundPlannerResponse =>
  JSON.parse(json) as RefundPlannerResponse

export const refundPlannerResponseToJson = (value: RefundPlannerResponse): string =>
  JSON.stringify(value)
