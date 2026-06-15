// 本文件定义订单退款申请请求，和后端 `RequestRefundPlannerRequest` 保持同名镜像。

export type RequestRefundPlannerRequest = {
  orderId: string
  refundReason: string
}

export const requestRefundPlannerRequestFromJson = (json: string): RequestRefundPlannerRequest =>
  JSON.parse(json) as RequestRefundPlannerRequest

export const requestRefundPlannerRequestToJson = (value: RequestRefundPlannerRequest): string =>
  JSON.stringify(value)
