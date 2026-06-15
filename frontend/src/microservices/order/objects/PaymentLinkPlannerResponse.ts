// 本文件定义支付链接响应，和后端 `PaymentLinkPlannerResponse` 保持同名镜像。

export type PaymentLinkPlannerResponse = {
  paymentUrl: string
  expiresAt: string
}

export const paymentLinkPlannerResponseFromJson = (json: string): PaymentLinkPlannerResponse =>
  JSON.parse(json) as PaymentLinkPlannerResponse

export const paymentLinkPlannerResponseToJson = (value: PaymentLinkPlannerResponse): string =>
  JSON.stringify(value)
