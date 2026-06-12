// 本文件定义 content 模块的 `BlogPostResponse`，作为响应数据并提供 JSON 编解码。

import type { BlogCommentResponse } from './BlogCommentResponse'
import type { BlogPostSummaryResponse } from './BlogPostSummaryResponse'

export type BlogPostResponse = {
  post: BlogPostSummaryResponse
  content: string
  comments: BlogCommentResponse[]
}
export const blogPostResponseFromJson = (json: string): BlogPostResponse =>
  JSON.parse(json) as BlogPostResponse

export const blogPostResponseToJson = (value: BlogPostResponse): string =>
  JSON.stringify(value)
