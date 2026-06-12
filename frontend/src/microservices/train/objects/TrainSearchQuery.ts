// 本文件定义 train 模块的 `TrainSearchQuery`，作为查询条件并提供 JSON 编解码。

export type TrainSearchQuery = {
  fromStation?: string
  toStation?: string
  date?: string
}
export const trainSearchQueryFromJson = (json: string): TrainSearchQuery =>
  JSON.parse(json) as TrainSearchQuery

export const trainSearchQueryToJson = (value: TrainSearchQuery): string =>
  JSON.stringify(value)
