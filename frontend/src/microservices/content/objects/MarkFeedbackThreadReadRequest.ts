import type { FeedbackAudience } from './FeedbackAudience'

export type MarkFeedbackThreadReadRequest = {
  audience: FeedbackAudience
}
export const markFeedbackThreadReadRequestFromJson = (json: string): MarkFeedbackThreadReadRequest =>
  JSON.parse(json) as MarkFeedbackThreadReadRequest

export const markFeedbackThreadReadRequestToJson = (value: MarkFeedbackThreadReadRequest): string =>
  JSON.stringify(value)
