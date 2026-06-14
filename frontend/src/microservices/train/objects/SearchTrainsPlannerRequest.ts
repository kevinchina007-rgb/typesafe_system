// 本文件定义 train 模块的 `SearchTrainsPlannerRequest`，用于按出发地、到达地和日期搜索火车并提供 JSON 编解码。

export type SearchTrainsPlannerRequest = {
  fromStation?: string
  toStation?: string
  date?: string
}
export const searchTrainsPlannerRequestFromJson = (json: string): SearchTrainsPlannerRequest =>
  JSON.parse(json) as SearchTrainsPlannerRequest

export const searchTrainsPlannerRequestToJson = (value: SearchTrainsPlannerRequest): string =>
  JSON.stringify(value)
