// 本文件定义 train 模块的 `TrainSuggestionListPlannerResponse`，用于表示火车建议结果列表并提供 JSON 编解码。

import type { TrainSuggestionsPlannerResponse } from './TrainSuggestionsPlannerResponse'

export type TrainSuggestionListPlannerResponse = {
  suggestions: TrainSuggestionsPlannerResponse[]
}
export const trainSuggestionListPlannerResponseFromJson = (json: string): TrainSuggestionListPlannerResponse =>
  JSON.parse(json) as TrainSuggestionListPlannerResponse

export const trainSuggestionListPlannerResponseToJson = (value: TrainSuggestionListPlannerResponse): string =>
  JSON.stringify(value)
