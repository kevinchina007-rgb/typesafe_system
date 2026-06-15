// 本文件定义订单支付查询请求，和后端 `FindOrderPaymentRequest` 保持同名语义镜像。

export type FindOrderPaymentPlannerRequest = {
  orderId: string
  paymentId: string
}

export const findOrderPaymentPlannerRequestFromJson = (json: string): FindOrderPaymentPlannerRequest =>
  JSON.parse(json) as FindOrderPaymentPlannerRequest

export const findOrderPaymentPlannerRequestToJson = (value: FindOrderPaymentPlannerRequest): string =>
  JSON.stringify(value)
