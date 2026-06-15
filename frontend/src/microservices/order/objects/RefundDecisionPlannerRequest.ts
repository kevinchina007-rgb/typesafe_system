// 本文件定义订单退款决策请求，和后端 `RefundDecisionPlannerRequest` 保持同名镜像。

export type RefundDecisionPlannerRequest = {
  orderId: string
  refundId: string
}

export const refundDecisionPlannerRequestFromJson = (json: string): RefundDecisionPlannerRequest =>
  JSON.parse(json) as RefundDecisionPlannerRequest

export const refundDecisionPlannerRequestToJson = (value: RefundDecisionPlannerRequest): string =>
  JSON.stringify(value)
