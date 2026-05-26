export type FeedbackSenderRole = 'User' | 'Manager' | 'SiteAdmin' | 'System'
export const feedbackSenderRoleFromJson = (json: string): FeedbackSenderRole =>
  JSON.parse(json) as FeedbackSenderRole

export const feedbackSenderRoleToJson = (value: FeedbackSenderRole): string =>
  JSON.stringify(value)
