import type { FeedbackThreadResponse } from './FeedbackThreadResponse'

export type FeedbackThread = FeedbackThreadResponse
export const feedbackThreadFromJson = (json: string): FeedbackThread =>
  JSON.parse(json) as FeedbackThread

export const feedbackThreadToJson = (value: FeedbackThread): string =>
  JSON.stringify(value)
