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
