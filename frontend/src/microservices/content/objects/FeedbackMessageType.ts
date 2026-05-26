export type FeedbackMessageType = 'text' | 'orderCancellationRequest' | 'system'
export const feedbackMessageTypeFromJson = (json: string): FeedbackMessageType =>
  JSON.parse(json) as FeedbackMessageType

export const feedbackMessageTypeToJson = (value: FeedbackMessageType): string =>
  JSON.stringify(value)
