// 本文件定义 train 模块的 `TrainRefundPolicyPlannerResponse`，用于描述退票规则并提供 JSON 编解码。

export type TrainRefundPolicyPlannerResponse = {
  startOffsetMinutesBeforeDeparture: number
  endOffsetMinutesBeforeDeparture: number
  refundType: string
  refundRate: string
}

export const trainRefundPolicyPlannerResponseFromJson = (json: string): TrainRefundPolicyPlannerResponse =>
  JSON.parse(json) as TrainRefundPolicyPlannerResponse

export const trainRefundPolicyPlannerResponseToJson = (value: TrainRefundPolicyPlannerResponse): string =>
  JSON.stringify(value)
