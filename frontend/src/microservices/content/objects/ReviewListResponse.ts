// 本文件定义 content 模块的 `ReviewListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { ReviewResponse } from './ReviewResponse'

export type ReviewListResponse = {
  reviews: ReviewResponse[]
}
export const reviewListResponseFromJson = (json: string): ReviewListResponse =>
  JSON.parse(json) as ReviewListResponse

export const reviewListResponseToJson = (value: ReviewListResponse): string =>
  JSON.stringify(value)
