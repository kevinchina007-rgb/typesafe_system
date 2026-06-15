// BlogPostListResponse：博客域博客文章列表返回对象。

import type { BlogPostSummaryResponse } from './BlogPostSummaryResponse'

export type BlogPostListResponse = {
  posts: BlogPostSummaryResponse[]
}
export const blogPostListResponseFromJson = (json: string): BlogPostListResponse =>
  JSON.parse(json) as BlogPostListResponse

export const blogPostListResponseToJson = (value: BlogPostListResponse): string =>
  JSON.stringify(value)
