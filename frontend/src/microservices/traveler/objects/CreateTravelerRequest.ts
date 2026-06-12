// 本文件定义 traveler 模块的 `CreateTravelerRequest`，作为请求参数并提供 JSON 编解码。

import type { TravelerProfileInput } from './TravelerProfileInput'

export type CreateTravelerRequest = TravelerProfileInput
export const createTravelerRequestFromJson = (json: string): CreateTravelerRequest =>
  JSON.parse(json) as CreateTravelerRequest

export const createTravelerRequestToJson = (value: CreateTravelerRequest): string =>
  JSON.stringify(value)
