// 本文件定义 content 模块的 `FeedbackManagerType`，作为传输数据并提供 JSON 编解码。

export type FeedbackManagerType = 'Airline' | 'Hotel' | 'Train' | 'Attraction' | 'SiteAdmin'
export const feedbackManagerTypeFromJson = (json: string): FeedbackManagerType =>
  JSON.parse(json) as FeedbackManagerType

export const feedbackManagerTypeToJson = (value: FeedbackManagerType): string =>
  JSON.stringify(value)
