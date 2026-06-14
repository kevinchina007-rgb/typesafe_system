// 本文件定义 train 模块的 `TrainSuggestionsPlannerRequest`，用于提交火车搜索建议查询并提供 JSON 编解码。

export type TrainSuggestionsPlannerRequest = {
  q: string
}
export const trainSuggestionsPlannerRequestFromJson = (json: string): TrainSuggestionsPlannerRequest =>
  JSON.parse(json) as TrainSuggestionsPlannerRequest

export const trainSuggestionsPlannerRequestToJson = (value: TrainSuggestionsPlannerRequest): string =>
  JSON.stringify(value)
