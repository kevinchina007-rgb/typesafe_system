// 本文件定义订单支付请求，和后端 `PayOrderPlannerRequest` 保持同名镜像。

export type PayOrderPlannerRequest = {
  orderId: string
  paymentMethod: string
  paymentSucceeded: boolean
  travelerIds?: string[] | null
}

export const payOrderPlannerRequestFromJson = (json: string): PayOrderPlannerRequest =>
  JSON.parse(json) as PayOrderPlannerRequest

export const payOrderPlannerRequestToJson = (value: PayOrderPlannerRequest): string =>
  JSON.stringify(value)
