// 本文件定义 traveler 模块的 `UpdateTravelerRequest`，作为请求参数并提供 JSON 编解码。

import type { TravelerProfileInput } from './TravelerProfileInput'

export type UpdateTravelerRequest = TravelerProfileInput
export const updateTravelerRequestFromJson = (json: string): UpdateTravelerRequest =>
  JSON.parse(json) as UpdateTravelerRequest

export const updateTravelerRequestToJson = (value: UpdateTravelerRequest): string =>
  JSON.stringify(value)
