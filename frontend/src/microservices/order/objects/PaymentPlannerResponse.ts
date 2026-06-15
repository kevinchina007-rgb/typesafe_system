// 本文件定义订单支付响应，和后端 `PaymentPlannerResponse` 保持同名镜像。

export type PaymentPlannerResponse = {
  paymentId: string
  paymentAmount: string
  paymentCurrency: string
  paymentMethod: string
  paymentStatus: string
  authorizedAt: string
  capturedAt: string | null
}

export const paymentPlannerResponseFromJson = (json: string): PaymentPlannerResponse =>
  JSON.parse(json) as PaymentPlannerResponse

export const paymentPlannerResponseToJson = (value: PaymentPlannerResponse): string =>
  JSON.stringify(value)
