import type { FeedbackThreadResponse } from './FeedbackThreadResponse'

export type FeedbackThreadListResponse = {
  threads: FeedbackThreadResponse[]
}
export const feedbackThreadListResponseFromJson = (json: string): FeedbackThreadListResponse =>
  JSON.parse(json) as FeedbackThreadListResponse

export const feedbackThreadListResponseToJson = (value: FeedbackThreadListResponse): string =>
  JSON.stringify(value)
