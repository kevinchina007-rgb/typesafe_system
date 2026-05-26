export type FeedbackThreadKind = 'ServiceReview' | 'ManagerEscalation'
export const feedbackThreadKindFromJson = (json: string): FeedbackThreadKind =>
  JSON.parse(json) as FeedbackThreadKind

export const feedbackThreadKindToJson = (value: FeedbackThreadKind): string =>
  JSON.stringify(value)
