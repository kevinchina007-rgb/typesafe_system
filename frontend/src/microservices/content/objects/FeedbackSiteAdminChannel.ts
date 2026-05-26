export type FeedbackSiteAdminChannel = 'user' | 'manager'
export const feedbackSiteAdminChannelFromJson = (json: string): FeedbackSiteAdminChannel =>
  JSON.parse(json) as FeedbackSiteAdminChannel

export const feedbackSiteAdminChannelToJson = (value: FeedbackSiteAdminChannel): string =>
  JSON.stringify(value)
