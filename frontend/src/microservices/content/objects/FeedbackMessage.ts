import type { FeedbackMessageResponse } from './FeedbackMessageResponse'

export type FeedbackMessage = FeedbackMessageResponse
export const feedbackMessageFromJson = (json: string): FeedbackMessage =>
  JSON.parse(json) as FeedbackMessage

export const feedbackMessageToJson = (value: FeedbackMessage): string =>
  JSON.stringify(value)
