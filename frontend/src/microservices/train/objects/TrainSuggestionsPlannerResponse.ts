// 本文件定义 train 模块的 `TrainSuggestionsPlannerResponse`，用于表示单条火车搜索建议并提供 JSON 编解码。

export type TrainSuggestionsPlannerResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}
export const trainSuggestionsPlannerResponseFromJson = (json: string): TrainSuggestionsPlannerResponse =>
  JSON.parse(json) as TrainSuggestionsPlannerResponse

export const trainSuggestionsPlannerResponseToJson = (value: TrainSuggestionsPlannerResponse): string =>
  JSON.stringify(value)
