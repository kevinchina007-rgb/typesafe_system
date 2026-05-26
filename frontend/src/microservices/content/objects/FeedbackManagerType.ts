export type FeedbackManagerType = 'Airline' | 'Hotel' | 'Train' | 'Attraction' | 'SiteAdmin'
export const feedbackManagerTypeFromJson = (json: string): FeedbackManagerType =>
  JSON.parse(json) as FeedbackManagerType

export const feedbackManagerTypeToJson = (value: FeedbackManagerType): string =>
  JSON.stringify(value)
