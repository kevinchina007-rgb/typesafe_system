// 本文件定义 train 模块的 `TrainListPlannerResponse`，用于表示火车列表结果并提供 JSON 编解码。

import type { TrainPlannerResponse } from './TrainPlannerResponse'

export type TrainListPlannerResponse = {
  trains: TrainPlannerResponse[]
}
export const trainListPlannerResponseFromJson = (json: string): TrainListPlannerResponse =>
  JSON.parse(json) as TrainListPlannerResponse

export const trainListPlannerResponseToJson = (value: TrainListPlannerResponse): string =>
  JSON.stringify(value)
