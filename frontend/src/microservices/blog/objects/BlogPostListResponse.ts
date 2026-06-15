// 本文件定义 content 模块的 `BlogPostListResponse`，作为列表响应数据并提供 JSON 编解码。

import type { BlogPostSummaryResponse } from './BlogPostSummaryResponse'

export type BlogPostListResponse = {
  posts: BlogPostSummaryResponse[]
}
export const blogPostListResponseFromJson = (json: string): BlogPostListResponse =>
  JSON.parse(json) as BlogPostListResponse

export const blogPostListResponseToJson = (value: BlogPostListResponse): string =>
  JSON.stringify(value)
