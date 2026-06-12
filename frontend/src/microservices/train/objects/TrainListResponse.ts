// 本文件定义 train 模块的 `TrainListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { TrainResponse } from './TrainResponse'

export type TrainListResponse = {
  trains: TrainResponse[]
}
export const trainListResponseFromJson = (json: string): TrainListResponse =>
  JSON.parse(json) as TrainListResponse

export const trainListResponseToJson = (value: TrainListResponse): string =>
  JSON.stringify(value)
