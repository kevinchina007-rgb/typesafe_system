// 本文件定义 train 模块的 `TrainRefundPolicyResponse`，作为响应数据并提供 JSON 编解码。

export type TrainRefundPolicyResponse = {
  startOffsetMinutesBeforeDeparture: number
  endOffsetMinutesBeforeDeparture: number
  refundType: string
  refundRate: string
}
export const trainRefundPolicyResponseFromJson = (json: string): TrainRefundPolicyResponse =>
  JSON.parse(json) as TrainRefundPolicyResponse

export const trainRefundPolicyResponseToJson = (value: TrainRefundPolicyResponse): string =>
  JSON.stringify(value)
