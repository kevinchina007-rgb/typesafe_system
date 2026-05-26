export type FeedbackAudience = 'User' | 'Manager' | 'SiteAdmin'
export const feedbackAudienceFromJson = (json: string): FeedbackAudience =>
  JSON.parse(json) as FeedbackAudience

export const feedbackAudienceToJson = (value: FeedbackAudience): string =>
  JSON.stringify(value)
